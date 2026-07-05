import type { Content } from '@/lib/types'
import { parseOrderXml } from './parseOrderXml'

export function applyTemplateOrder(
  contents: Content[],
  template: string | null
): Content[] {
  if (!template) return contents
  const order = parseOrderXml(template)
  const ordered = order
    .map(p => contents.find(c => c.purpose === p))
    .filter((c): c is Content => c != null)
  const remaining = contents.filter(c => !order.includes(c.purpose))
  return [...ordered, ...remaining]
}
