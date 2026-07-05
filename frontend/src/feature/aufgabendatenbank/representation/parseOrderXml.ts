export function parseOrderXml(xml: string): string[] {
  const matches = [...xml.matchAll(/<purpose>(.*?)<\/purpose>/g)]
  return matches.map(m => m[1])
}
