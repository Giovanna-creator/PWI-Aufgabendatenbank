import type { Item, CollectionItem } from '@/lib/types'
import { isCollection as checkIsCollection } from '@/lib/types'

export interface ValidationIssue {
  message: string
}

// ── Tree flattening ──────────────────────────────────────────────────────────

/** Recursively flatten a collection's items[] into a single-level array. */
function flattenCollectionItems(items: CollectionItem[] | undefined): Item[] {
  return (items ?? []).flatMap(ci =>
    // Drill into nested collections to collect their items too
    checkIsCollection(ci.item)
      ? [ci.item, ...flattenCollectionItems(ci.item.items)]
      : [ci.item]
  )
}

/** Flatten rootItems + all nested collection items into one flat array. */
function flattenAllItems(rootItems: Item[]): Item[] {
  return rootItems.flatMap(item =>
    // Unpack collection contents alongside the collection node itself
    checkIsCollection(item)
      ? [item, ...flattenCollectionItems(item.items)]
      : [item]
  )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Best-effort human-readable label: first content text, falling back to ID. */
function getItemTitle(item: Item): string {
  return item.contents?.[0]?.jsonContent?.text ?? item.id
}

/** Wrap a message string into a ValidationIssue. */
function toIssue(message: string): ValidationIssue {
  return { message }
}

// ── Rule 1: Only collections may have children ───────────────────────────────

export function checkOnlyCollectionsHaveChildren(items: Item[]): ValidationIssue[] {
  return items
    // Keep only non-collection items (exercises etc.)
    .filter(item => !checkIsCollection(item))
    // Of those, keep only the ones that unexpectedly have children
    .filter(item => item.items !== undefined && item.items.length > 0)
    // Convert each violation into a human-readable issue
    .map(item => toIssue(
      `"${getItemTitle(item)}" ist kein Collection-Item, hat aber ${item.items!.length} Kind-Elemente.`
    ))
}

// ── Rule 2: No item may appear both in rootItems and inside a collection ─────

export function checkNoDuplicateItems(rootItems: Item[]): ValidationIssue[] {
  // Build a set of every item ID that lives nested inside any collection
  const idsInsideCollections = new Set(
    rootItems
      .filter(checkIsCollection)
      .flatMap(ri => flattenCollectionItems(ri.items))
      .map(nested => nested.id)
  )

  return rootItems
    // Keep only root-level items whose ID also appears inside a collection
    .filter(item => idsInsideCollections.has(item.id))
    .map(item => toIssue(
      `"${getItemTitle(item)}" (${item.id}) ist sowohl in rootItems als auch in einer Kollektion enthalten.`
    ))
}

// ── Rule 3: Every rootItemId reference must resolve ─────────────────────────

export function checkNoDanglingRootItemId(items: Item[]): ValidationIssue[] {
  // Index all known item IDs for O(1) lookup
  const allIds = new Set(items.map(i => i.id))

  return items
    // Narrow type: keep only items that actually have a rootItemId set
    .filter((item): item is Item & { rootItemId: string } => !!item.rootItemId)
    // Of those, keep only items whose rootItemId points to nothing in the tree
    .filter(item => !allIds.has(item.rootItemId))
    .map(item => toIssue(
      `"${getItemTitle(item)}" verweist auf rootItemId "${item.rootItemId}", das nicht existiert.`
    ))
}

// ── Composed validator ───────────────────────────────────────────────────────

export function validateTreeData(rootItems: Item[]): ValidationIssue[] {
  // Pre-compute a flat view so each check doesn't have to re-traverse
  const allItems = flattenAllItems(rootItems)

  return [
    ...checkOnlyCollectionsHaveChildren(allItems),
    ...checkNoDanglingRootItemId(allItems)
  ]
}
