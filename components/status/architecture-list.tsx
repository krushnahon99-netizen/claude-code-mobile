import { Box, Cable, Download, KeyRound, MessageSquare, ShieldCheck } from 'lucide-react'
import { InlineMarkdown } from './inline-markdown'

const LAYERS = [
  {
    icon: Box,
    title: 'Linux layer',
    body: 'proot ships in the APK as `libproot.so`, so it can run on Android 10+. It boots an Ubuntu arm64 rootfs downloaded on first launch.',
  },
  {
    icon: Download,
    title: 'First-run setup',
    body: 'A progress screen installs Node.js, runs `npm install -g @anthropic-ai/claude-code`, and checks `claude --version`.',
  },
  {
    icon: Cable,
    title: 'Claude Code bridge',
    body: 'Runs `claude -p` with stream-json input and output inside proot, and turns every JSON line into a chat event.',
  },
  {
    icon: MessageSquare,
    title: 'Native chat UI',
    body: 'Chat bubbles, collapsible tool cards, diffs for file edits, a project picker, and a file browser.',
  },
  {
    icon: ShieldCheck,
    title: 'Permission prompts',
    body: 'Every edit or command Claude wants to run gets Approve and Deny buttons, answered over the CLI control protocol.',
  },
  {
    icon: KeyRound,
    title: 'Login and settings',
    body: 'Anthropic account, API key, or custom base URL and token. Secrets are encrypted with the Android Keystore.',
  },
]

export function ArchitectureList() {
  return (
    <section aria-labelledby="architecture-heading" className="flex flex-col gap-4">
      <h2
        id="architecture-heading"
        className="text-sm font-medium uppercase tracking-wider text-muted-foreground"
      >
        How it works
      </h2>
      <ul className="grid gap-3 sm:grid-cols-2">
        {LAYERS.map(({ icon: Icon, title, body }) => (
          <li key={title} className="flex flex-col gap-2 rounded-xl border bg-card p-4">
            <div className="flex items-center gap-2">
              <Icon className="size-4 text-primary" aria-hidden="true" />
              <h3 className="font-medium">{title}</h3>
            </div>
            <p className="text-sm leading-relaxed text-muted-foreground">
              <InlineMarkdown text={body} />
            </p>
          </li>
        ))}
      </ul>
    </section>
  )
}
