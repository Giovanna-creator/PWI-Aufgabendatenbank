export function getPurposesFromXml(xml: string): string[] {
  return [...xml.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
}

export function ensurePurposeInXml(xml: string, purpose: string): string {
  if (xml.includes(`<purpose>${escapeXml(purpose)}</purpose>`)) return xml
  return xml.replace('</layout>', `  <purpose>${escapeXml(purpose)}</purpose>\n</layout>`)
}

export function removePurposeFromXml(xml: string, purpose: string): string {
  const result = xml.replace(
    new RegExp(`\\s*<purpose>${escapeRegex(purpose)}</purpose>`, 'g'),
    ''
  )
  return result
}

export function escapeXml(s: string): string {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

export function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

export function buildXmlFromPurposes(purposes: string[]): string {
  if (purposes.length === 0) return '<layout>\n</layout>'
  const lines = purposes.map(p => `  <purpose>${escapeXml(p)}</purpose>`)
  return `<layout>\n${lines.join('\n')}\n</layout>`
}
