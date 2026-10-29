import type { LogEntry } from '@/lib/project-status'
import { InlineMarkdown } from './inline-markdown'

export function ActivityLog({ entries }: { entries: LogEntry[] }) {
  if (entries.length === 0) return null

  return (
    <section aria-labelledby="activity-heading" className="flex flex-col gap-4">
      <h2
        id="activity-heading"
        className="text-sm font-medium uppercase tracking-wider text-muted-foreground"
      >
        Recent activity
      </h2>
      <ol className="flex flex-col gap-3">
        {entries.map((entry) => (
          <li key={entry.number} className="flex flex-col gap-2 rounded-xl border bg-card p-4">
            <div className="flex flex-wrap items-center justify-between gap-2 font-mono text-xs text-muted-foreground">
              <time dateTime={entry.date}>{entry.date}</time>
              <span>{entry.by}</span>
            </div>
            <p className="text-sm leading-relaxed">
              <InlineMarkdown text={entry.milestone} />
            </p>
          </li>
        ))}
      </ol>
    </section>
  )
}
