# AGENTS.md — Claude Code Mobile

> **AI assistant: this file is the project's memory.** Read all of it before doing anything, follow it, and keep it current. Chatbots on this project run on small credit budgets and can be cut off at any moment. The next chatbot only knows what you leave here and in Git.

## 1. Start of every session

1. Read this whole file. Before touching the Android app, also read `docs/SPEC.md` (the full spec, architecture, and verified Claude Code CLI facts).
2. Run `git status` and `git log --oneline -10` to see the latest saved work. If Git is broken, use **Recovery** in §3.
3. Resume from §6 **In progress** first, then the first unchecked milestone in §7. Don't redo finished work.

## 2. Owner's standing rules

- The owner builds with small credit budgets and moves to a new v0 account when credits run out, so any session can end mid-task.
- **Save after every milestone, automatically — don't ask first.** The owner asked for this so the project can run on auto without losing work.
- Work in small, complete milestones: one feature, page, or fix at a time. **Build in stages and briefly explain each stage** when you finish it.
- Spend credits on building: keep replies short, don't re-read files you've already read, and don't refactor working code without a reason.
- Make routine choices yourself with sensible defaults, and record the important ones in §11 **Decisions**.
- Never commit secrets. `.env*` files are gitignored; record only env var **names** in §8.

## 3. Save protocol

Run after every milestone, and before ending any turn that changed files.

1. Update this file: §5 **Current status**, §6 **In progress**, §7 **Milestones** (tick finished items), and add a row to the top of §10 **Milestone log**.
2. Commit and push to GitHub `main` (one line, safe to re-run):

   ```bash
   git remote add github https://github.com/krushnahon99-netizen/claude-code-mobile.git 2>/dev/null; git add -A && (git diff --cached --quiet || git commit -m "milestone: <what now works>") && git push github HEAD:main
   ```

   - Push to the `github` remote, and **never change or remove `origin`**: in v0, `origin` can be v0's internal Git (`git.v0.app`), which v0 uses to save versions.
   - Keep `HEAD:main`. After an import, v0 edits on its own working branch, but the next account imports `main`, so `main` must always have the latest work.
   - If your tool asks for approval before pushing to `main`, request it with the push command. v0's own Git sync also keeps every milestone on the working branch.
   - Low on credits with work half done? Save anyway with `wip: <what's half done>` and describe it in §6.
   - Push rejected: run `git pull --rebase github main`, then push again. Never force-push.
   - Authentication error: ask the owner to connect GitHub (v0 settings, top right → Git), then retry.

**Recovery.** If `git status` says "not a git repository" (for example after a sandbox reset), this reconnects to the GitHub history and keeps your current files:

```bash
git init -b main
git remote add github https://github.com/krushnahon99-netizen/claude-code-mobile.git
git fetch github main && git reset github/main
```

## 4. Project goal

- **Name:** Claude Code Mobile. **Repo:** https://github.com/krushnahon99-netizen/claude-code-mobile (private; handoff branch `main`).
- **What:** an Android app (Kotlin + Jetpack Compose) that runs the real Claude Code CLI inside a hidden Ubuntu arm64 environment (proot) and shows it through a native chat UI. The user never sees a terminal. Everything runs on-device, with no server.
- **MVP:**
  - First-run setup with a progress bar: Ubuntu, Node.js, then `npm install -g @anthropic-ai/claude-code`.
  - Streaming chat with collapsible tool cards and diffs.
  - Approve / Deny for edits and commands.
  - Project picker and file browser.
  - Login with an Anthropic account, an API key, or a custom base URL and token, with encrypted secrets.
  - Foreground service and wake lock during tasks.
  - Clear errors.
- **Look:** modern, clean chat, dark by default, Claude-orange accent.
- **Full spec:** `docs/SPEC.md` (owner's words verbatim, architecture, CLI protocol).
- The root Next.js app is only a **status page** for the v0 preview: it renders §6, §7, and §10 of this file. The product lives in `android/`.

## 5. Current status

- Milestone 1 done: spec (`docs/SPEC.md`), milestone plan (§7), `README.md`, and the status page at `/`.
- No Android code yet. Next is milestone 2, the Android project skeleton plus the CI APK build.

## 6. In progress

- Milestone 2: the Android skeleton (`android/`) and CI workflow (`.github/workflows/android.yml`) are pushed, and the first **Android APK** CI run hasn't passed yet. Check it with `gh run list --repo krushnahon99-netizen/claude-code-mobile --limit 3`, fix any failure, then tick milestone 2.

## 7. Milestones

Keep this exact format, `N. [x] **Title** — details`: the status page parses it.

1. [x] **Plan and docs** — spec in `docs/SPEC.md`, this milestone plan, `README.md`, and the status page.
2. [ ] **Android project skeleton** — Gradle project in `android/`, Compose app shell (theme, navigation, placeholder screens), and a GitHub Actions workflow that builds a debug APK.
3. [ ] **Linux layer** — Termux proot shipped as `libproot.so` jniLibs, rootfs download with SHA-256 check and tar extraction, and the proot command builder and process runner.
4. [ ] **First-run setup** — setup screen with step progress: storage check, Ubuntu, base packages, Node.js, Claude Code, verify. Resumable steps and a Repair button.
5. [ ] **Claude Code bridge** — run `claude` with stream-json in and out, `StreamJsonParser` to typed events, the control protocol (initialize, `can_use_tool`, interrupt), and parser unit tests.
6. [ ] **Chat screen** — streaming message list with Markdown and code blocks, tool-call cards, a diff view for edits, and a composer with send and stop.
7. [ ] **Permission approvals** — Approve/Deny cards wired to `can_use_tool`, a notification when the app is in the background, and a permission-mode picker.
8. [ ] **Projects and files** — project picker (private projects folder, optional shared storage) and a simple file browser and viewer.
9. [ ] **Login and settings** — `claude auth login` in native UI, API key / base URL / token in EncryptedSharedPreferences, and model choice.
10. [ ] **Reliability** — foreground service and wake lock, plain-language errors, session resume, and handling for low storage and Android's phantom-process killer.

## 8. Tech stack and conventions

**Android app (`android/`, from milestone 2)**

- Kotlin 2.1, Android Gradle Plugin 8.7, Gradle 8.11, JDK 17. Package `com.claudecodemobile`.
- Jetpack Compose (BOM 2024.12.01), Material 3, navigation-compose, kotlinx-serialization-json, coroutines, Apache Commons Compress, androidx security-crypto.
- minSdk 26, compile/target SDK 35, `arm64-v8a` only.
- proot binaries are **not committed**. `android/scripts/fetch-proot.sh` downloads them in CI into `app/src/main/jniLibs/arm64-v8a/`, which is gitignored.
- The v0 sandbox has no JDK or Android SDK, so **CI is the compiler**. After pushing, check `gh run list --repo krushnahon99-netizen/claude-code-mobile --limit 3`, and read failures with `gh run view <id> --log-failed`.

**Status page (repo root)**

- Next.js 16.4 (App Router), React 19, TypeScript 5.7, pnpm.
- Tailwind CSS v4 (tokens in `app/globals.css`), shadcn/ui (`base-nova`), and `lucide-react` 1.x. Brand icons such as GitHub were removed from lucide 1.x.
- `next.config.mjs` sets `typescript.ignoreBuildErrors: true`, so run `pnpm exec tsc --noEmit` before saving web changes.

**Integrations and env vars:** none. The app needs no server-side env vars; users enter their own keys on the phone.

## 9. File map

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | This handoff file, the single source of truth for progress |
| `docs/SPEC.md` | Full spec: owner's request, architecture, Claude Code CLI protocol |
| `README.md` | Public overview of the project |
| `app/page.tsx` | Status page that renders progress from this file |
| `lib/project-status.ts` | Parses §6, §7, and §10 of this file for the status page |
| `components/status/` | Status page sections (header, milestones, architecture, activity) |
| `app/layout.tsx`, `app/globals.css` | Status page metadata, fonts, and dark theme tokens |
| `android/` | The Android app (added in milestone 2) |

## 10. Milestone log (newest first)

This file is loaded into every AI turn, so keep it short: keep about 15 rows here and move older ones to `docs/MILESTONES.md`. In **By**, write `Chatbot N`, using the last row's N if you're continuing the same chat, or N + 1 if you're a new chat.

| # | Date | Milestone | By |
| --- | --- | --- | --- |
| 2 | 2026-10-29 | Plan and docs: spec in `docs/SPEC.md`, 10-milestone plan, README, status page | Chatbot 2 (v0) |
| 1 | 2026-10-29 | Created the GitHub repo and the AGENTS.md handoff and save system | Chatbot 1 (v0) |

## 11. Decisions

- 2026-10-29: `AGENTS.md` is the single handoff file. v0 and many AI coding tools load it automatically.
- 2026-10-29: `main` is the handoff branch, and every milestone is pushed there.
- 2026-10-29: The repo is private. Make it public in GitHub settings if it needs to be shared.
- 2026-10-29: The Android app lives in `android/`. The root Next.js app stays as a status page, so the v0 preview shows progress.
- 2026-10-29: proot is Termux's aarch64 build (plus libtalloc), fetched in CI and shipped as jniLibs. That's the only way it can execute on Android 10+.
- 2026-10-29: The rootfs is Ubuntu Base 24.04 LTS arm64, verified against Ubuntu's `SHA256SUMS`. Node.js comes from the official nodejs.org tarball (latest v24 LTS), because Ubuntu's apt build is v18.
- 2026-10-29: Approve/Deny uses the Agent SDK's stdio control protocol (`--permission-prompt-tool stdio`), not a separate MCP server.
- 2026-10-29: Account login runs `claude auth login` (the code is pasted through stdin), and the CLI keeps those credentials in the app-private rootfs. API keys, base URL, and token go in EncryptedSharedPreferences.
- 2026-10-29: Projects default to the app-private folder `/root/projects` inside the rootfs, which has full Linux file semantics. Shared storage access is optional and comes later.

## 12. For the owner

**Moving to a new v0 account**

1. In the new account, start a new chat, click **+** in the prompt box, choose **Import from GitHub**, then pick `claude-code-mobile` with base branch `main`.
2. Send: **"Read AGENTS.md and continue the project. After every milestone, commit and push to GitHub main without asking me."**
3. Optional: add that sentence as a v0 Rule (settings → Rules) so every chat in the account follows it.

**Getting the app (from milestone 2):** on GitHub, open **Actions**, then the latest **Android APK** run, and download the `claude-code-mobile-debug` artifact. Unzip it and install the APK on your phone (allow installs from unknown sources).
