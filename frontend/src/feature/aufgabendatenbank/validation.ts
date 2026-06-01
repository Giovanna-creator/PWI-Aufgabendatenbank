import type { Item, CollectionItem } from '@/lib/types'
import { isCollection as checkIsCollection } from '@/lib/types'

export interface ValidationIssue {
  message: string
}

/**
 * Walk every item reachable from a collection's items array.
 */
function* walkCollectionItems(items: CollectionItem[]): Generator<Item> {
  for (const ci of items) {
    yield ci.item
    if (checkIsCollection(ci.item)) {
      yield* walkCollectionItems(ci.item.items)
    }
  }
}

/**
 * Collect every item nested inside collections starting from rootItems.
 */
function* walkAllItems(rootItems: Item[]): Generator<Item> {
  for (const item of rootItems) {
    yield item
    if (checkIsCollection(item)) {
      yield* walkCollectionItems(item.items)
    }
  }
}

/**
 * Validate tree data against design rules.
 * Returns a list of issues (empty = no problems).
 */
export function validateTreeData(rootItems: Item[]): ValidationIssue[] {
  const issues: ValidationIssue[] = []
  const allItems = [...walkAllItems(rootItems)]

  // 1. Only collections can have children
  for (const item of allItems) {
    if (!checkIsCollection(item)) {
      const items = (item as any).items
      if (Array.isArray(items) && items.length > 0) {
        const title = item.contents?.[0]?.jsonContent?.text ?? item.id
        issues.push({
          message: `"${title}" ist kein Collection-Item, hat aber ${items.length} Kind-Elemente.`
        })
      }
    }
  }

  // 2. No duplicate items (same ID in rootItems AND inside a collection)
  const rootIds = new Set(rootItems.map((i) => i.id))
  for (const item of allItems) {
    if (rootIds.has(item.id)) {
      // Check if this item is also nested inside any collection
      for (const ri of rootItems) {
        if (checkIsCollection(ri)) {
          for (const nested of walkCollectionItems(ri.items)) {
            if (nested.id === item.id && ri.id !== item.id) {
              const title = item.contents?.[0]?.jsonContent?.text ?? item.id
              issues.push({
                message: `"${title}" (${item.id}) ist sowohl in rootItems als auch in einer Kollektion enthalten.`
              })
            }
          }
        }
      }
    }
  }

  // 3. Dangling rootItemId references
  const allIds = new Set(allItems.map((i) => i.id))
  for (const item of allItems) {
    if (item.rootItemId && !allIds.has(item.rootItemId)) {
      const title = item.contents?.[0]?.jsonContent?.text ?? item.id
      issues.push({
        message: `"${title}" verweist auf rootItemId "${item.rootItemId}", das nicht existiert.`
      })
    }
  }

  return issues
}
