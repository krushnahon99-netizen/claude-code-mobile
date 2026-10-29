import { Circle, CircleCheck, CircleDot } from 'lucide-react'
import type { Milestone, MilestoneState } from '@/lib/project-status'
import { cn } from '@/lib/utils'
import { InlineMarkdown } from './inline-markdown'

const STATE_ICON = { done: CircleCheck, current: CircleDot, todo: Circle } as const

const STATE_LABEL: Record<MilestoneState, string> = {
  done: 'Done',
  current: 'Up next',
  todo: 'Planned',
}

export function MilestoneList({ milestones }: { milestones: Milestone[] }) {
  return (
    <section aria-labelledby="milestones-heading" className="flex flex-col gap-2">
      <h2
        id="milestones-heading"
        className="text-sm font-medium uppercase tracking-wider text-muted-foreground"
      >
        Milestones
      </h2>
      <ol className="flex flex-col">
        {milestones.map((milestone) => {
          const Icon = STATE_ICON[milestone.state]
          const isTodo = milestone.state === 'todo'

          return (
            <li key={milestone.number} className="flex gap-4 border-b py-4 last:border-b-0">
              <Icon
                aria-hidden="true"
                className={cn(
                  'mt-0.5 size-5 shrink-0',
                  isTodo ? 'text-muted-foreground/50' : 'text-primary',
                )}
              />
              <div className="flex min-w-0 flex-col gap-1">
                <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
                  <span className="font-mono text-xs text-muted-foreground">
                    M{milestone.number}
                  </span>
                  <h3 className={cn('font-medium', isTodo && 'text-muted-foreground')}>
                    {milestone.title}
                  </h3>
                  {milestone.state === 'current' ? (
                    <span className="rounded-full bg-primary/15 px-2 py-0.5 text-xs font-medium text-primary">
                      {STATE_LABEL.current}
                    </span>
                  ) : (
                    <span className="sr-only">{STATE_LABEL[milestone.state]}</span>
                  )}
                </div>
                <p className="text-sm leading-relaxed text-muted-foreground">
                  <InlineMarkdown text={milestone.description} />
                </p>
              </div>
            </li>
          )
        })}
      </ol>
    </section>
  )
}
