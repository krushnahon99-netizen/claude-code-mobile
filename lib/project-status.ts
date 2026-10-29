import { readFile } from 'node:fs/promises'
import path from 'node:path'

export type MilestoneState = 'done' | 'current' | 'todo'

export interface Milestone {
  number: number
  title: string
  description: string
  state: MilestoneState
}

export interface LogEntry {
  number: string
  date: string
  milestone: string
  by: string
}

export interface ProjectStatus {
  milestones: Milestone[]
  log: LogEntry[]
  inProgress: string[]
}

const MILESTONE_LINE = /^\d+\.\s+\[( |x|X)\]\s+\*\*(.+?)\*\*\s*(?:[—–-]\s*)?(.*)$/

function capitalize(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1)
}

function getSection(markdown: string, numberPrefix: string): string {
  const start = markdown.indexOf(`\n## ${numberPrefix} `)
  if (start === -1) return ''
  const rest = markdown.slice(start + 1)
  const next = rest.indexOf('\n## ')
  return next === -1 ? rest : rest.slice(0, next)
}

function parseMilestones(section: string): Milestone[] {
  const parsed = section
    .split('\n')
    .map((line) => MILESTONE_LINE.exec(line.trim()))
    .filter((match): match is RegExpExecArray => match !== null)
    .map((match, index) => ({
      number: index + 1,
      done: match[1].toLowerCase() === 'x',
      title: match[2].trim(),
      description: capitalize(match[3].trim()),
    }))

  const currentIndex = parsed.findIndex((milestone) => !milestone.done)

  return parsed.map(({ done, ...milestone }, index) => ({
    ...milestone,
    state: done ? 'done' : index === currentIndex ? 'current' : 'todo',
  }))
}

function parseLog(section: string): LogEntry[] {
  return section
    .split('\n')
    .filter((line) => line.trim().startsWith('|'))
    .map((line) =>
      line
        .split('|')
        .slice(1, -1)
        .map((cell) => cell.trim()),
    )
    .filter((cells) => cells.length >= 4 && /^\d+$/.test(cells[0]))
    .map(([number, date, milestone, by]) => ({ number, date, milestone, by }))
}

function parseInProgress(section: string): string[] {
  return section
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('- '))
    .map((line) => line.slice(2).trim())
    .filter((item) => !/^nothing\.?$/i.test(item))
}

export async function getProjectStatus(): Promise<ProjectStatus> {
  const markdown = await readFile(path.join(process.cwd(), 'AGENTS.md'), 'utf8')

  return {
    milestones: parseMilestones(getSection(markdown, '7.')),
    log: parseLog(getSection(markdown, '10.')),
    inProgress: parseInProgress(getSection(markdown, '6.')),
  }
}
