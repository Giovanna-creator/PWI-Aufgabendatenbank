/**
 * @deprecated Use types from `@/feature/aufgabendatenbank/api-adapter.types` instead.
 * These old types will be removed in a future cleanup.
 */
export interface Content {
  id?: string
  license: string | null
  contentType: string
  author: string
  tags: string[]

  purpose: string
  jsonContent: Record<string, any>
  blobContent: string
  blobMimeType?: string
}

export interface Item {
  id: string
  item_type: string
  author: string
  representationTemplate: string | null
  license: string | null
  tags: string[]
  validators: any[]
  modifiers: any[]
  rootItemId?: string | null
  contents: Content[]
  items?: CollectionItem[]
  order?: boolean
  // Backend item_collection_id (nur bei Kollektionen gesetzt). Wird für
  // alle /api/collections/{id}/... Aufrufe verwendet — NICHT die item_id (id).
  collectionId?: string | null
}

export type Collection = Item & {
  item_type: 'collection'
  items: CollectionItem[]
  order: boolean
}

export type TreeItem = Item | CollectionItem

export interface CollectionItem {
  id: string
  collectionId: string
  item: Item
  position: number | null
}

export function getInnerItem(element: TreeItem): Item {
  if (isCollectionItem(element)) {
    return element.item
  }
  return element
}

export function isCollection(object: any): object is Collection {
  return object && typeof object.id === 'string' && object.item_type === 'collection'
}

export function isCollectionItem(object: any): object is CollectionItem {
  return object && typeof object.id === 'string' && 'collectionId' in object && 'item' in object
}
