import { getPurposesFromXml } from './templateXml'

export function parseOrderXml(xml: string): string[] {
  return getPurposesFromXml(xml)
}
