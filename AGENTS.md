# AGENTS.md — Claude Code Mobile

> **AI assistant: this file is the project's memory.** Read all of it before doing anything, follow it, and keep it current. Chatbots on this project run on small credit budgets and can be cut off at any moment. The next chatbot only knows what you leave here and in Git.

## 1. Start of every session

1. Read this whole file.
2. Run `git status` and `git log --oneline -10` to see the latest saved work. If Git is broken, use **Recovery** in §3.
3. Resume from §6 **In progress** first, then the first unchecked item in §7 **Next steps**. Don't redo finished work.
4. If §4 **Project goal** still says "not defined yet", ask the owner one short question to define it before building features.

## 2. Owner's standing rules

- The owner builds with small credit budgets and moves to a new v0 account when credits run out, so any session can end mid-task.
- **Save after every milestone, automatically — don't ask first.** The owner asked for this so the project can run on auto without losing work.
- Work in small, complete milestones: one feature, page, or fix at a time.
- Spend credits on building: keep replies short, don't re-read files you've already read, and don't refactor working code without a reason.
- Make routine choices yourself with sensible defaults, and record the important ones in §11 **Decisions**.
- Never commit secrets. `.env*` files are gitignored; record only env var **names** in §8.

## 3. Save protocol

Run after every milestone, and before ending any turn that changed files.

1. Update this file: §5 **Current status**, §6 **In progress**, §7 **Next steps** (tick finished items), and add a row to the top of §10 **Milestone log**.
2. Commit and push to GitHub `main` (one line, safe to re-run):

   ```bash
   git remote add github https://github.com/krushnahon99-netizen/claude-code-mobile.git 2>/dev/null; git add -A && (git diff --cached --quiet || git commit -m "milestone: <what now works>") && git push github HEAD:main
   ```

   - Push to the `github` remote, and **never change or remove `origin`**: in v0, `origin` can be v0's internal Git (`git.v0.app`), which v0 uses to save versions.
   - Keep `HEAD:main`. After an import, v0 edits on its own working branch, but the next account imports `main`, so `main` must always have the latest work.
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

- **Name:** Claude Code Mobile
- **Repo:** https://github.com/krushnahon99-netizen/claude-code-mobile (private; handoff branch `main`)
- **Spec: not defined yet.** The owner has only given the name. Unconfirmed guess: a mobile-first web app that brings a Claude Code-style AI coding assistant to the phone.
- When the owner describes the app, replace the line above with the confirmed spec: who it's for, MVP features, screens, integrations, and look and feel.

## 5. Current status

- Blank v0 starter: Next.js 16 with shadcn/ui. `app/page.tsx` still shows the default v0 placeholder, and the metadata title is still "v0 App".
- The GitHub repo and this handoff system are set up.
- No features, database, auth, or integrations yet.

## 6. In progress

- Nothing.

## 7. Next steps

1. [ ] Get the owner's description of Claude Code Mobile and write it into §4.
2. [ ] Split the spec into milestones and list them here.
3. [ ] Build the first milestone. Include setting the `app/layout.tsx` metadata to "Claude Code Mobile".

## 8. Tech stack and conventions

- Next.js 16.4 (App Router), React 19, TypeScript 5.7. Package manager: pnpm (`pnpm dev`, `pnpm build`).
- Tailwind CSS v4 through `@tailwindcss/postcss`. There's no `tailwind.config`; theme tokens live in `app/globals.css`. Also uses `tw-animate-css`.
- shadcn/ui with style `base-nova` (built on `@base-ui/react`), base color neutral, CSS variables. Add components with `pnpm dlx shadcn@latest add <name>`.
- Icons: `lucide-react`. Analytics: `@vercel/analytics`, rendered in production only.
- Path alias `@/*` points to the project root. The `cn()` helper is in `lib/utils.ts`.
- `next.config.mjs` sets `typescript.ignoreBuildErrors: true`, so builds don't catch type errors. Run `pnpm exec tsc --noEmit` before saving a large milestone.
- **Integrations and env vars:** none yet. When you add one, list the integration and its env var names here. They don't carry over to a new v0 account: the owner must reconnect the integration or copy the values from the old account's Vars. Save every database schema change as a file in the repo (for example `scripts/001-init.sql`) so a fresh database can be rebuilt.

## 9. File map

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | This handoff file, the single source of truth |
| `app/layout.tsx` | Root layout: metadata, viewport, analytics |
| `app/page.tsx` | Home page (still the v0 placeholder) |
| `app/globals.css` | Tailwind v4 setup and light/dark design tokens |
| `components/ui/` | shadcn/ui components (only `button.tsx` so far) |
| `lib/utils.ts` | `cn()` class-name helper |
| `public/` | App icons and placeholder images |

## 10. Milestone log (newest first)

This file is loaded into every AI turn, so keep it short: keep about 15 rows here and move older ones to `docs/MILESTONES.md`. In **By**, write `Chatbot N`, using the last row's N if you're continuing the same chat, or N + 1 if you're a new chat.

| # | Date | Milestone | By |
| --- | --- | --- | --- |
| 1 | 2026-10-29 | Created the GitHub repo and the AGENTS.md handoff and save system | Chatbot 1 (v0) |

## 11. Decisions

- 2026-10-29: `AGENTS.md` is the single handoff file. v0 and many AI coding tools load it automatically.
- 2026-10-29: `main` is the handoff branch, and every milestone is pushed there.
- 2026-10-29: The repo is private. Make it public in GitHub settings if it needs to be shared.

## 12. For the owner: moving to a new v0 account

1. In the new account, start a new chat, click **+** in the prompt box, choose **Import from GitHub**, then pick `claude-code-mobile` with base branch `main`.
2. Send: **"Read AGENTS.md and continue the project. After every milestone, commit and push to GitHub main without asking me."**
3. Optional: add that sentence as a v0 Rule (settings → Rules) so every chat in the account follows it.
4. If the app uses a database or another integration (see §8), reconnect it in the new account.
