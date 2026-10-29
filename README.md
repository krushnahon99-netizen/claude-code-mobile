# Claude Code Mobile

Run the real Claude Code on your Android phone. The app runs it inside a hidden Ubuntu environment and shows it in a clean native chat instead of a terminal.

> **Status:** early development. The live checklist is §7 of [`AGENTS.md`](AGENTS.md). The status page in this repo's web preview shows the same list.

## How it works

1. **Linux layer.** proot ships inside the APK as a native library (`libproot.so`), so it can run on Android 10+. On first launch the app downloads an Ubuntu 24.04 arm64 rootfs and extracts it.
2. **First-run setup.** A progress screen installs Node.js, runs `npm install -g @anthropic-ai/claude-code`, fixes the postinstall step if npm blocked it, and checks `claude --version`. Claude Code is always downloaded from npm, never bundled.
3. **Claude Code bridge.** The app runs `claude -p` with `--input-format stream-json` and `--output-format stream-json` inside proot, then parses each JSON line into a chat event.
4. **Native UI.** The chat has bubbles, collapsible tool cards (read, edit, run command), and diffs for file edits. Every edit or command gets Approve and Deny buttons. There's also a project picker and a file browser.
5. **Login and settings.** Sign in with an Anthropic account, use an API key, or set a custom `ANTHROPIC_BASE_URL` and auth token. Secrets are encrypted with the Android Keystore.

Everything runs on the phone. There's no separate server.

## Repository layout

| Path | What's there |
| --- | --- |
| `android/` | The Android app, Kotlin + Jetpack Compose |
| `.github/workflows/android.yml` | CI that builds the debug APK |
| `docs/SPEC.md` | Full spec, architecture, and the Claude Code stream-json protocol |
| `AGENTS.md` | Project memory and progress for the AI assistants building this |
| `app/`, `components/`, `lib/` | A small Next.js status page for the v0 preview |

## Getting the APK

GitHub Actions builds a debug APK whenever `android/` changes on `main`. On GitHub, open **Actions**, then the latest **Android APK** run, and download the `claude-code-mobile-debug` artifact. Unzip it and install `app-debug.apk`. Every build is signed with the same debug key, so updates install over each other.

To build locally, open `android/` in Android Studio, or run `./gradlew assembleDebug` in `android/` with JDK 17 and the Android SDK.

## Requirements

- Android 8.0 or newer on a 64-bit ARM phone.
- About 1.2 GB of free storage, and internet for the first-run setup.
- A Claude subscription, an Anthropic API key, or a compatible API endpoint.

Not affiliated with Anthropic. Claude Code is downloaded from npm on first run and is subject to Anthropic's terms.
