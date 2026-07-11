import type { Item, CollectionItem } from '@/lib/types'
import { isCollection as checkIsCollection } from '@/lib/types'

export interface ValidationIssue {
  message: string
}




function flattenCollectionItems(items: CollectionItem[] | undefined): Item[] {
  return (items ?? []).flatMap(ci =>
    checkIsCollection(ci.item)
      ? [ci.item, ...flattenCollectionItems(ci.item.items)]
      : [ci.item]
  )
}

function flattenAllItems(rootItems: Item[]): Item[] {
  return rootItems.flatMap(item =>
    checkIsCollection(item)
      ? [item, ...flattenCollectionItems(item.items)]
      : [item]
  )
}




function getItemTitle(item: Item): string {
  return item.contents?.[0]?.jsonContent?.text ?? item.id
}


function toIssue(message: string): ValidationIssue {
  return { message }
}

// Regel 1: Nur Kollektionen dürfen Kinder haben

export function checkOnlyCollectionsHaveChildren(items: Item[]): ValidationIssue[] {
  return items
    .filter(item => !checkIsCollection(item))
    .filter(item => item.items !== undefined && item.items.length > 0)
    .map(item => toIssue(
      `"${getItemTitle(item)}" ist kein Collection-Item, hat aber ${item.items!.length} Kind-Elemente.`
    ))
}

// Regel 2: Kein Item darf sowohl in rootItems als auch in einer Kollektion sein

export function checkNoDuplicateItems(rootItems: Item[]): ValidationIssue[] {
  const idsInsideCollections = new Set(
    rootItems
      .filter(checkIsCollection)
      .flatMap(ri => flattenCollectionItems(ri.items))
      .map(nested => nested.id)
  )

  return rootItems
    // Nur Root-Items, deren ID auch in einer Kollektion vorkommt
    .filter(item => idsInsideCollections.has(item.id))
    .map(item => toIssue(
      `"${getItemTitle(item)}" (${item.id}) ist sowohl in rootItems als auch in einer Kollektion enthalten.`
    ))
}

// Regel 3: Jede rootItemId-Referenz muss auflösbar sein

export function checkNoDanglingRootItemId(items: Item[]): ValidationIssue[] {
  const allIds = new Set(items.map(i => i.id))

  return items
    // Nur Items mit rootItemId, deren Referenz im Baum nicht existiert
    .filter((item): item is Item & { rootItemId: string } => !!item.rootItemId)
    .filter(item => !allIds.has(item.rootItemId))
    .map(item => toIssue(
      `"${getItemTitle(item)}" verweist auf rootItemId "${item.rootItemId}", das nicht existiert.`
    ))
}

// Zusammengesetzter Validator

export function validateTreeData(rootItems: Item[]): ValidationIssue[] {
  const allItems = flattenAllItems(rootItems)

  return [
    ...checkOnlyCollectionsHaveChildren(allItems),
    ...checkNoDanglingRootItemId(allItems)
  ]
}
