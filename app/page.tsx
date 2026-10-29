import { ActivityLog } from '@/components/status/activity-log'
import { ArchitectureList } from '@/components/status/architecture-list'
import { MilestoneList } from '@/components/status/milestone-list'
import { StatusHeader } from '@/components/status/status-header'
import { getProjectStatus } from '@/lib/project-status'

export const dynamic = 'force-static'

export default async function Page() {
  const status = await getProjectStatus()
  const done = status.milestones.filter((milestone) => milestone.state === 'done').length

  return (
    <main className="mx-auto flex min-h-dvh w-full max-w-2xl flex-col gap-12 px-5 py-10 sm:py-16">
      <StatusHeader done={done} total={status.milestones.length} inProgress={status.inProgress} />
      <MilestoneList milestones={status.milestones} />
      <ArchitectureList />
      <ActivityLog entries={status.log.slice(0, 5)} />
      <footer className="border-t pt-6 text-sm leading-relaxed text-muted-foreground">
        This page is generated from <code className="font-mono text-foreground">AGENTS.md</code>.
        The app itself lives in <code className="font-mono text-foreground">android/</code>.
      </footer>
    </main>
  )
}
