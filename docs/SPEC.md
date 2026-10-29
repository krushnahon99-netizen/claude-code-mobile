# Claude Code Mobile — Spec

The owner's request is kept word for word in §1. Everything in §4 was checked against the official Claude Code docs (code.claude.com/docs) and the Agent SDK source (github.com/anthropics/claude-agent-sdk-python) on 2026-10-29. Re-check §4 if a Claude Code release breaks something.

## 1. Owner's request (verbatim, 2026-10-29)

> Build an Android app (Kotlin + Jetpack Compose) that runs Claude Code inside a hidden Linux environment and shows it through a native chat UI instead of a terminal.
>
> GOAL — Do what Termux does (install and run Claude Code in a Linux environment on the phone), but the user never sees a terminal. They see a clean chat interface.
>
> ARCHITECTURE
> 1. Linux layer: bundle proot (shipped as a native library so it can execute on Android 10+) and an Ubuntu arm64 rootfs. On first launch, download and extract the rootfs.
> 2. First-run setup screen with a progress bar. It should: install Node.js (v20+) inside Ubuntu; run `npm install -g @anthropic-ai/claude-code`; handle npm's allow-scripts postinstall warning, and verify with `claude --version`; NOT bundle Claude Code itself; always download it from npm.
> 3. Claude Code bridge: launch Claude Code in non-interactive mode (`claude -p` with `--output-format stream-json`, and `--input-format stream-json` for multi-turn) inside proot, read the JSON stream, and parse it into UI events. Check the official docs for exact flags.
> 4. Native UI: chat bubbles for user and assistant messages; collapsible cards for tool calls (read file, edit file, run command); diff view for file edits; Approve / Deny buttons for edits and commands (permission prompts); a project folder picker and a simple file browser.
> 5. Login/settings screen: sign in with an Anthropic account, or enter an API key, or set a custom base URL (`ANTHROPIC_BASE_URL`) and auth token. Store secrets in Android Keystore / EncryptedSharedPreferences, never in plain text.
>
> CONSTRAINTS — Everything runs on-device, with no separate server. Keep the process alive with a foreground service and a wakelock during tasks. Handle the ~1 GB storage need gracefully. Show clear errors (not raw terminal output).
>
> DELIVERABLES — Project structure, the proot setup/install code, the stream-json parser, the main chat screen, and the permission-approval flow. Build in stages and explain each step.

## 2. Product summary

- **Who:** developers who want to use Claude Code from an Android phone without touching a terminal.
- **Screens:** Setup (first run) → Login → Projects → Chat, with Files and Settings reachable from Chat.
- **Look and feel:** modern, clean chat. Dark by default, warm near-black background, Claude-orange accent (`#D97757`), monospace for code, commands, and paths.
- **Devices:** Android 8.0+ (minSdk 26), arm64-v8a only. About 1.2 GB of free storage and internet for the first-run setup.

## 3. Architecture

```
Compose UI (screens)  →  ViewModels  →  ClaudeSession (bridge)  →  LinuxEnvironment (proot)  →  Ubuntu rootfs  →  claude
```

### 3.1 Linux layer

- **proot binary:** Termux's aarch64 `proot` + `libtalloc` packages from `packages.termux.dev`. `android/scripts/fetch-proot.sh` downloads them in CI (they are not committed) and stages them as:
  - `jniLibs/arm64-v8a/libproot.so` (the `proot` executable)
  - `jniLibs/arm64-v8a/libproot-loader.so` (`libexec/proot/loader`)
  - `jniLibs/arm64-v8a/libtalloc.so` (DT_NEEDED in `libproot.so` patched from `libtalloc.so.2` with `patchelf`, because Android only extracts files named `lib*.so`)
- **Why jniLibs:** on Android 10+ an app may only execute files from its `nativeLibraryDir` (W^X). Set `packaging.jniLibs.useLegacyPackaging = true` so the files are extracted there. AGP then writes `android:extractNativeLibs="true"` into the merged manifest, so don't add it to the source manifest.
- **Host environment for proot:** `PROOT_TMP_DIR=<cacheDir>/proot-tmp`, `PROOT_LOADER=<nativeLibraryDir>/libproot-loader.so`, `LD_LIBRARY_PATH=<nativeLibraryDir>`.
- **Command shape:**
  `libproot.so --kill-on-exit --link2symlink --sysvipc -0 -r <rootfs> -b /dev -b /proc -b /sys -b <fake proc files> -w <cwd> /usr/bin/env -u LD_LIBRARY_PATH -u PROOT_LOADER -u PROOT_TMP_DIR /bin/bash -lc '<command>'`
  Secrets go in the process environment, never on the command line.
- **Fake /proc files:** Android blocks some `/proc` entries (for example `/proc/stat`, `/proc/loadavg`), so bind generated fakes over the ones that can't be read, like proot-distro does.
- **Rootfs:** Ubuntu Base 24.04 LTS arm64 from `https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/`. Read `SHA256SUMS`, pick the `ubuntu-base-*-base-arm64.tar.gz` entry, download it, and verify the hash. Extract in Kotlin with Apache Commons Compress: create symlinks with `Os.symlink`, and copy hard links instead of linking them (app storage rejects hard links).
- **After extraction:** write `/etc/resolv.conf` (public DNS) and `/etc/hosts`. Set `APT::Sandbox::User "root";` so apt works under fake root.
- **Paths:** rootfs at `filesDir/linux/rootfs`, step markers at `filesDir/linux/state/`, projects at `rootfs/root/projects/<name>` (app-private, full Linux file semantics).

### 3.2 First-run setup (each step is idempotent and writes a marker when done)

1. **Storage check:** require about 1.2 GB free (`StatFs`). Show a warning under 2 GB and block under 1.2 GB, with a plain explanation.
2. **Download Ubuntu** (~30 MB) with byte progress, then verify SHA-256.
3. **Extract Ubuntu** (~90 MB on disk), with file-count progress.
4. **Configure Linux:** DNS, hosts, apt sandbox, fake /proc.
5. **Base packages:** `apt-get update && apt-get install -y --no-install-recommends ca-certificates git ripgrep curl`.
6. **Node.js:** official `linux-arm64` tarball from `https://nodejs.org/dist/latest-v24.x/`, checked against `SHASUMS256.txt`, extracted to `/opt/node`, with symlinks in `/usr/local/bin`. (Ubuntu's apt `nodejs` is v18, which is too old.)
7. **Claude Code:** `npm install -g @anthropic-ai/claude-code`. Always downloaded from npm, never bundled.
8. **Postinstall fix:** if `claude --version` fails (placeholder message `Error: claude native binary not installed.`), run `node "$(npm root -g)/@anthropic-ai/claude-code/install.cjs"`. If that can't run, fall back to launching `cli-wrapper.cjs`.
9. **Verify:** `claude --version` prints something like `2.1.211 (Claude Code)`.

### 3.3 Claude Code bridge

Launch inside proot with cwd set to the project folder:

```
claude -p --input-format stream-json --output-format stream-json --verbose \
  --include-partial-messages --permission-prompt-tool stdio --permission-mode default \
  [--model <model>] [--resume <session-id>]
```

**stdin** (one JSON object per line):

| Purpose | JSON |
| --- | --- |
| Handshake, sent first | `{"type":"control_request","request_id":"<id>","request":{"subtype":"initialize","hooks":null}}` |
| User message | `{"type":"user","session_id":"","message":{"role":"user","content":"<text>"},"parent_tool_use_id":null}` |
| Stop the turn | `{"type":"control_request","request_id":"<id>","request":{"subtype":"interrupt"}}` |
| Approve | `{"type":"control_response","response":{"subtype":"success","request_id":"<id>","response":{"behavior":"allow","updatedInput":<original input>}}}` |
| Deny | `{"type":"control_response","response":{"subtype":"success","request_id":"<id>","response":{"behavior":"deny","message":"The user denied this action."}}}` |

**stdout** events (NDJSON). Skip non-JSON lines, and ignore unknown `type`/`subtype` values, because the protocol keeps growing.

| Event | Use |
| --- | --- |
| `system` / `init` | First event: `session_id`, `model`, `cwd`, `tools`, `permissionMode` |
| `stream_event` | Raw API stream events (needs `--include-partial-messages`). Text deltas: `event.type == "content_block_delta"` and `event.delta.type == "text_delta"` |
| `assistant` | `message.content[]` blocks: `text`, `thinking`, `tool_use{id,name,input}`. A non-null `parent_tool_use_id` means a subagent |
| `user` | `tool_result{tool_use_id, content, is_error}` blocks |
| `control_request` / `can_use_tool` | Permission prompt: `request_id`, `request.tool_name`, `request.input`, `request.permission_suggestions` → show Approve/Deny |
| `control_cancel_request` | `request_id`: the prompt was withdrawn, so dismiss its card |
| `control_response` | Answers to our own requests (initialize, interrupt) |
| `system` / `api_retry` | `attempt`, `max_retries`, `retry_delay_ms`, `error` category |
| `result` | End of a turn: `subtype`, `is_error`, `result`, `session_id`, `total_cost_usd`, `duration_ms`, `num_turns`, `permission_denials` |

**Stopping:** the `interrupt` control request ends the turn cleanly. SIGTERM exits with code 143 and leaves the turn unfinished.

### 3.4 Auth

- **Anthropic account:** run `claude auth login` (Claude subscription) or `claude auth login --console` (API billing) inside proot. It prints a URL. The user signs in in the browser, the browser shows a code, and `claude auth login` reads that code from stdin. The app opens the URL and sends the code from a native text field. The CLI keeps the credentials in `/root/.claude/` inside the app-private rootfs.
- **Status:** `claude auth status` prints JSON and exits 0 when logged in. `authMethod` is one of `none`, `claude.ai`, `oauth_token`, `api_key`, `api_key_helper`, `third_party`.
- **API key:** `ANTHROPIC_API_KEY`. In `-p` mode, a key that is present always wins over the account login.
- **Custom endpoint:** `ANTHROPIC_BASE_URL` + `ANTHROPIC_AUTH_TOKEN`.
- **Secret storage:** EncryptedSharedPreferences (AES-256-GCM with a Keystore `MasterKey`). Secrets are passed as process environment only: never as command-line arguments, never logged.
- **Don't use `--bare` with account login:** bare mode never reads OAuth credentials.

### 3.5 Native UI

- **Chat items:** user bubble, assistant bubble (Markdown with code blocks), thinking chip, collapsible tool card, permission card, retry/error banner, turn summary (cost and duration).
- **Tool cards:**
  - `Read`: the file path.
  - `Edit` / `Write` / `MultiEdit`: a diff built from the tool input (`old_string` → `new_string`, or the full `content`).
  - `Bash`: the command plus its output.
  - `Grep` / `Glob`: the pattern.
  - `WebFetch` / `WebSearch`: the URL or query.
  - `Task` / `Agent`: the subagent.
  - Anything else: generic JSON.
- **Permission card:** tool name, a human summary (command, file, or URL), the diff for edits, and Approve / Deny. "Always allow" sends `updatedPermissions` from `permission_suggestions` (later).

### 3.6 Android constraints

- **Keep alive:** foreground service (type `specialUse`) plus a partial wake lock while setup or a turn runs. The notification shows progress and "Claude needs your approval".
- **Phantom process killer (Android 12+):** it can kill child processes of backgrounded apps. The foreground service makes this less likely. Android 14+ has a developer option to disable child-process restrictions, so mention it in errors.
- **Storage budget:** Ubuntu ~100 MB, packages ~150 MB, Node.js ~200 MB, Claude Code ~250 MB, plus projects. About 1 GB total.
- **Errors:** map known stderr text to plain language (no internet, out of storage, signed out, bad key, rate limited, overloaded, killed by Android, setup incomplete). Keep the raw log behind a "Details" expander.

## 4. Verified Claude Code facts (2026-10-29)

- `--output-format stream-json` in print mode needs `--verbose`. `--include-partial-messages` needs `-p` and stream-json output.
- `-p --input-format stream-json` with no prompt argument reads user messages from stdin (docs example: `claude -p --input-format stream-json --output-format stream-json --verbose --replay-user-messages`).
- Agent SDK hosts handle permission prompts by passing `--permission-prompt-tool stdio`; the CLI then sends `control_request` frames with subtype `can_use_tool`. `--permission-prompts host` is the default (v2.1.259+).
- From v2.1.302, `claude -p` starts in Manual (`default`) mode when nothing sets a mode. Always pass `--permission-mode` explicitly.
- `--permission-mode` values: `default`, `acceptEdits`, `plan`, `auto`, `dontAsk`, `bypassPermissions` (`manual` is an alias for `default`).
- The npm package delivers a native binary as a per-platform optional dependency (`linux-arm64` for glibc Ubuntu), and its postinstall (`install.cjs`) copies it into place. npm 12+ blocks dependency lifecycle scripts by default, so run `install.cjs` manually when `claude --version` reports the placeholder error.
- `claude auth login` reads the pasted login code from stdin. `claude auth status` reports JSON and exit codes 0/1.
- `--resume <session-id>` continues a session. `--session-id <uuid>` sets one up front.

## 5. Risks to watch

- The Bun-compiled native `claude` binary under proot on Android kernels is untested. If it fails, try `PROOT_NO_SECCOMP=1`, then the `cli-wrapper.cjs` route.
- Devices with 16 KB memory pages.
- Termux's package index layout could change. `fetch-proot.sh` parses `Packages` instead of hard-coding versions.
