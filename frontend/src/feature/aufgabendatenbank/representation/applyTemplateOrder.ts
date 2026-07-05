import type { Content } from '@/lib/types'
import { getSplitsFromXml, getPurposesFromXml } from './templateXml'

export interface ContentGroup {
  type: 'standalone' | 'split'
  contents: Content[]
}

export function applyTemplateOrder(
  contents: Content[],
  template: string | null
): ContentGroup[] {
  if (!template) return contents.map(c => ({ type: 'standalone' as const, contents: [c] }))
  const groups: ContentGroup[] = []
  const splits = getSplitsFromXml(template)
  for (const group of splits) {
    const matched = group.purposes
      .map(p => contents.find(c => c.purpose === p))
      .filter((c): c is Content => c != null)
    if (matched.length === 0) continue
    groups.push({
      type: group.kind,
      contents: matched
    })
  }
  const flatOrder = getPurposesFromXml(template)
  const remaining = contents.filter(c => !flatOrder.includes(c.purpose))
  for (const c of remaining) groups.push({ type: 'standalone', contents: [c] })
  return groups
}

export function flattenContentGroups(groups: ContentGroup[]): Content[] {
  return groups.flatMap(g => g.contents)
}
