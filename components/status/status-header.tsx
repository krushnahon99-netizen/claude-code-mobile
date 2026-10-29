import { ExternalLink, FileText, Smartphone } from 'lucide-react'
import { InlineMarkdown } from './inline-markdown'

const REPO_URL = 'https://github.com/krushnahon99-netizen/claude-code-mobile'

interface StatusHeaderProps {
  done: number
  total: number
  inProgress: string[]
}

export function StatusHeader({ done, total, inProgress }: StatusHeaderProps) {
  const percent = total === 0 ? 0 : Math.round((done / total) * 100)

  return (
    <header className="flex flex-col gap-6">
      <p className="flex items-center gap-2 text-sm text-muted-foreground">
        <Smartphone className="size-4 text-primary" aria-hidden="true" />
        Android app · Kotlin + Jetpack Compose
      </p>

      <div className="flex flex-col gap-3">
        <h1 className="text-balance text-4xl font-semibold tracking-tight sm:text-5xl">
          Claude Code Mobile
        </h1>
        <p className="text-pretty text-lg leading-relaxed text-muted-foreground">
          The real Claude Code, running in a hidden Ubuntu environment on your phone, through a
          native chat app instead of a terminal.
        </p>
      </div>

      <div className="flex flex-col gap-3 rounded-xl border bg-card p-5">
        <div className="flex items-baseline justify-between gap-4">
          <h2 className="font-medium">Build progress</h2>
          <p className="font-mono text-sm text-muted-foreground">
            {done} / {total} milestones
          </p>
        </div>
        <div
          role="progressbar"
          aria-label="Milestones completed"
          aria-valuemin={0}
          aria-valuemax={total}
          aria-valuenow={done}
          className="h-2 overflow-hidden rounded-full bg-muted"
        >
          <div className="h-full rounded-full bg-primary" style={{ width: `${percent}%` }} />
        </div>
        {inProgress.length > 0 && (
          <ul className="flex flex-col gap-1 text-sm text-muted-foreground">
            {inProgress.map((item) => (
              <li key={item}>
                <span className="text-primary">In progress: </span>
                <InlineMarkdown text={item} />
              </li>
            ))}
          </ul>
        )}
      </div>

      <nav aria-label="Project links" className="flex flex-wrap gap-x-6 gap-y-3 text-sm">
        <a
          href={`${REPO_URL}/blob/main/docs/SPEC.md`}
          className="flex items-center gap-2 text-foreground underline-offset-4 hover:underline"
        >
          <FileText className="size-4 text-muted-foreground" aria-hidden="true" />
          Read the spec
        </a>
        <a
          href={REPO_URL}
          className="flex items-center gap-2 text-foreground underline-offset-4 hover:underline"
        >
          <ExternalLink className="size-4 text-muted-foreground" aria-hidden="true" />
          GitHub repository
        </a>
      </nav>
    </header>
  )
}
