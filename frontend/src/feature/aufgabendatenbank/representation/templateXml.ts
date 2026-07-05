export function getPurposesFromXml(xml: string): string[] {
  return [...xml.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
}

export function getSplitsFromXml(xml: string): string[][] {
  const splits: string[][] = []
  const splitMatches = xml.matchAll(/<split>([\s\S]*?)<\/split>/g)
  for (const m of splitMatches) {
    const purposes = [...m[1].matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m2 => m2[1])
    splits.push(purposes)
  }
  const without = xml.replace(/<split>[\s\S]*?<\/split>/g, '')
  const standalone = [...without.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
  for (const p of standalone) splits.push([p])
  return splits
}

export function ensurePurposeInXml(xml: string, purpose: string): string {
  if (xml.includes(`<purpose>${escapeXml(purpose)}</purpose>`)) return xml
  return xml.replace('</layout>', `  <purpose>${escapeXml(purpose)}</purpose>\n</layout>`)
}

export function removePurposeFromXml(xml: string, purpose: string): string {
  const escaped = escapeRegex(purpose)
  let result = xml.replace(
    new RegExp(`\\s*<purpose>${escaped}</purpose>`, 'g'), ''
  )
  result = result.replace(/<split>\s*<\/split>/g, '')
  result = result.replace(/<split>\s*<\/split>/g, '')
  return result
}

export function splitPurposeInXml(xml: string, purpose: string): string {
  const escaped = escapeRegex(purpose)
  if (xml.includes(`<split>`)) {
    const replaced = xml.replace(
      new RegExp(`<purpose>${escaped}</purpose>`),
      `<split>\n    <purpose>${escapeXml(purpose)}</purpose>\n  </split>`
    )
    return replaced
  }
  return xml.replace(
    new RegExp(`<purpose>${escaped}</purpose>`),
    `<split>\n    <purpose>${escapeXml(purpose)}</purpose>\n  </split>`
  )
}

export function unsplitPurposeFromXml(xml: string, purpose: string): string {
  const escaped = escapeRegex(purpose)
  const result = xml.replace(
    new RegExp(`<split>([\\s\\S]*?)<purpose>${escaped}<\\/purpose>([\\s\\S]*?)<\\/split>`, 'g'),
    (_, before, after) => {
      const leftPurposes = [...before.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
      const rightPurposes = [...after.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
      const remaining = [...leftPurposes, ...rightPurposes]
      if (remaining.length === 0) return ''
      return remaining.map(p => `  <purpose>${escapeXml(p)}</purpose>`).join('\n')
    }
  )
  return result.replace(/<split>\s*<\/split>/g, '')
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

export function buildXmlFromSplits(splits: string[][]): string {
  if (splits.length === 0) return '<layout>\n</layout>'
  const parts = splits.map(group => {
    if (group.length <= 1) {
      return `  <purpose>${escapeXml(group[0])}</purpose>`
    }
    return `  <split>\n    <purpose>${escapeXml(group[0])}</purpose>\n    <purpose>${escapeXml(group[1])}</purpose>\n  </split>`
  })
  return `<layout>\n${parts.join('\n')}\n</layout>`
}
