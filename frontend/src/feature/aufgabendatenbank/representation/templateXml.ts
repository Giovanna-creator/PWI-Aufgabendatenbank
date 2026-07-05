export function getPurposesFromXml(xml: string): string[] {
  return [...xml.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
}

export interface SplitGroup {
  purposes: string[]
  kind: 'standalone' | 'split'
}

export function getSplitsFromXml(xml: string): SplitGroup[] {
  const result: SplitGroup[] = []
  const re = /<split>([\s\S]*?)<\/split>|<purpose>(.*?)<\/purpose>/g
  let match
  while ((match = re.exec(xml)) !== null) {
    if (match[1] !== undefined) {
      const purposes = [...match[1].matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m2 => m2[1])
      result.push({ purposes, kind: 'split' })
    } else {
      result.push({ purposes: [match[2]], kind: 'standalone' })
    }
  }
  return result
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

export function splitPurposeInXml(xml: string, purpose: string, nextPurpose?: string): string {
  if (nextPurpose) {
    const result = xml.replace(
      new RegExp(`([ \\t]*)<purpose>${escapeRegex(purpose)}</purpose>\\s*<purpose>${escapeRegex(nextPurpose)}</purpose>`),
      (_, indent) =>
        `${indent}<split>\n${indent}  <purpose>${escapeXml(purpose)}</purpose>\n${indent}  <purpose>${escapeXml(nextPurpose)}</purpose>\n${indent}</split>`
    )
    if (result !== xml) return result
  }
  return xml.replace(
    new RegExp(`([ \\t]*)<purpose>${escapeRegex(purpose)}</purpose>`),
    (_, indent) =>
      `${indent}<split>\n${indent}  <purpose>${escapeXml(purpose)}</purpose>\n${indent}</split>`
  )
}

export function unsplitPurposeFromXml(xml: string, purpose: string): string {
  const escaped = escapeRegex(purpose)
  const result = xml.replace(
    new RegExp(`([ \\t]*)<split>([\\s\\S]*?)<purpose>${escaped}<\\/purpose>([\\s\\S]*?)<\\/split>`, 'g'),
    (_, indent, before, after) => {
      const leftPurposes = [...before.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
      const rightPurposes = [...after.matchAll(/<purpose>(.*?)<\/purpose>/g)].map(m => m[1])
      const all = [purpose, ...leftPurposes, ...rightPurposes]
      return all.map(p => `${indent}<purpose>${escapeXml(p)}</purpose>`).join('\n')
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

export function buildXmlFromSplits(splits: SplitGroup[]): string {
  if (splits.length === 0) return '<layout>\n</layout>'
  const parts = splits.map(group => {
    if (group.kind === 'standalone') {
      return `  <purpose>${escapeXml(group.purposes[0])}</purpose>`
    }
    if (group.purposes.length >= 2) {
      return `  <split>\n    <purpose>${escapeXml(group.purposes[0])}</purpose>\n    <purpose>${escapeXml(group.purposes[1])}</purpose>\n  </split>`
    }
    return `  <split>\n    <purpose>${escapeXml(group.purposes[0])}</purpose>\n  </split>`
  })
  return `<layout>\n${parts.join('\n')}\n</layout>`
}
