export function InlineMarkdown({ text }: { text: string }) {
  const segments = text.split('`')

  return (
    <>
      {segments.map((segment, index) =>
        index % 2 === 1 ? (
          <code
            key={index}
            className="rounded bg-muted px-1 py-0.5 font-mono text-[0.85em] text-foreground"
          >
            {segment}
          </code>
        ) : (
          <span key={index}>{segment.replaceAll('**', '')}</span>
        ),
      )}
    </>
  )
}
