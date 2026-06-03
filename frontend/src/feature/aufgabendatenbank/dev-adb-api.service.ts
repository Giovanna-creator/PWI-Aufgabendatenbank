/**
 * Dev/logging implementation of {@link ApiAdapter}.
 *
 * Instead of making real HTTP calls, this adapter:
 * 1. Logs each request (method, path, body) to the browser console
 * 2. Returns data from the seed `dummy-data.ts` for read operations
 * 3. Returns plausible auto-generated responses for mutations
 *
 * Used in development (`npm run dev:dummy`). Switch to the real
 * {@link AdbApiService} via `main.ts`.
 *
 * ## Console output format
 * ```
 * [ADB Dev] GET /api/items?root=true
 * [ADB Dev] POST /api/items → { item_type: "exercise", ... }
 * ```
 *
 * Mutations also log a second line with the generated ID.
 */
import { dummyData } from './dummy-data'
import type {
  ApiAdapter,
  ItemDTO,
  CollectionItemDTO,
  CreateItemPayload,
  CreateCollectionPayload,
  UpdateCollectionPayload
} from './api-adapter.types'

function log(method: string, path: string, body?: unknown): void {
  const parts = [`[ADB Dev] ${method} ${path}`]
  if (body !== undefined) {
    parts.push('→', JSON.stringify(body, null, 2))
  }
  console.log(...parts)
}

/**
 * Deep-clone an ItemDTO to prevent accidental mutation of the seed data.
 */
function cloneItem(item: ItemDTO): ItemDTO {
  return JSON.parse(JSON.stringify(item))
}

/**
 * Generate a unique-ish ID based on a prefix and the current timestamp.
 */
function uid(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`
}

/**
 * Recursively search a tree of ItemDTOs for an item by ID.
 * The dummy-data tree may have sub-collections nested arbitrarily deep.
 */
function findItemInTree(id: string, items: ItemDTO[]): ItemDTO | undefined {
  for (const item of items) {
    if (item.id === id) return item
    if (item.items) {
      const found = findItemInTree(id, item.items.map((ci) => ci.item))
      if (found) return found
    }
  }
  return undefined
}

export class DevAdbApiService implements ApiAdapter {
  /**
   * **GET /api/items?root=true**
   *
   * Returns deep-cloned root items (children NOT populated — they
   * are loaded on demand via {@link getCollectionItems}).
   */
  async getRootItems(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true')
    return dummyData.rootItems.map(cloneItem)
  }

  /**
   * **GET /api/collections/{id}/items**
   *
   * Finds the collection by ID in seed data and returns its children.
   */
  async getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]> {
    log('GET', `/api/collections/${collectionId}/items`)
    const roots = dummyData.rootItems as unknown as ItemDTO[]
    const collection = findItemInTree(collectionId, roots)
    if (collection && collection.items) {
      return JSON.parse(JSON.stringify(collection.items))
    }
    return []
  }

  /**
   * **POST /api/items**
   *
   * Logs the payload and returns a mock ItemDTO with a generated ID.
   */
  async createItem(payload: CreateItemPayload): Promise<ItemDTO> {
    log('POST', '/api/items', payload)
    const id = uid('item')
    const item: ItemDTO = {
      id,
      item_type: payload.item_type,
      author: payload.author,
      representationTemplate: null,
      license: null,
      rootItemId: payload.rootItemId,
      contents: payload.contents.map((c) => ({
        id: uid('content'),
        ...c
      }))
    }
    console.log(`  ← ${id}`)
    return item
  }

  /**
   * **POST /api/collections**
   *
   * Logs the payload and returns a mock Collection ItemDTO.
   */
  async createCollection(payload: CreateCollectionPayload): Promise<ItemDTO> {
    log('POST', '/api/collections', payload)
    const id = uid('coll')
    const collection: ItemDTO = {
      id,
      item_type: 'collection',
      author: payload.author,
      representationTemplate: null,
      license: null,
      rootItemId: null,
      contents: payload.contents.map((c) => ({
        id: uid('content'),
        ...c
      })),
      items: [],
      order: payload.order
    }
    console.log(`  ← ${id}`)
    return collection
  }

  /**
   * **POST /api/items/{id}/collection**
   *
   * Logs the request. Finds the item in seed data and returns it with
   * `item_type` changed to `collection` and empty children.
   */
  async convertItemToCollection(itemId: string): Promise<ItemDTO> {
    log('POST', `/api/items/${itemId}/collection`)
    const roots = dummyData.rootItems as unknown as ItemDTO[]
    const item = findItemInTree(itemId, roots)
    if (item) {
      const cloned = JSON.parse(JSON.stringify(item)) as ItemDTO
      cloned.item_type = 'collection'
      cloned.items = []
      cloned.order = false
      return cloned
    }
    return {
      id: itemId,
      item_type: 'collection',
      author: 'author',
      representationTemplate: null,
      license: null,
      rootItemId: null,
      contents: [],
      items: [],
      order: false
    }
  }

  /**
   * **POST /api/collection/{id}/items**
   *
   * Logs the request and returns a mock CollectionItemDTO.
   */
  async addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO> {
    log('POST', `/api/collection/${collectionId}/items`, { itemId })
    const collItem: CollectionItemDTO = {
      id: uid('coll-item'),
      collectionId,
      item: {
        id: itemId,
        item_type: 'exercise',
        author: 'author',
        representationTemplate: null,
        license: null,
        rootItemId: collectionId,
        contents: []
      },
      position: null
    }
    console.log(`  ← ${collItem.id}`)
    return collItem
  }

  /**
   * **DELETE /api/collections/{id}/items/{itemId}**
   *
   * Logs the request only; no actual mutation in dev mode.
   */
  async removeItemFromCollection(collectionId: string, itemId: string): Promise<void> {
    log('DELETE', `/api/collections/${collectionId}/items/${itemId}`)
  }

  /**
   * **DELETE /api/items/{id}**
   *
   * Logs the request only; no actual deletion in dev mode.
   */
  async deleteItem(itemId: string): Promise<void> {
    log('DELETE', `/api/items/${itemId}`)
  }

  /**
   * **PUT /api/collections/{id}**
   *
   * Logs the request. Finds the collection in seed data, applies the update,
   * and returns the updated clone.
   */
  async updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO> {
    log('PUT', `/api/collections/${collectionId}`, payload)
    const roots = dummyData.rootItems as unknown as ItemDTO[]
    const collection = findItemInTree(collectionId, roots)
    if (collection && collection.item_type === 'collection') {
      const cloned = JSON.parse(JSON.stringify(collection)) as ItemDTO & { items: CollectionItemDTO[]; order: boolean }
      cloned.order = payload.order
      return cloned
    }
    return {
      id: collectionId,
      item_type: 'collection',
      author: 'author',
      representationTemplate: null,
      license: null,
      rootItemId: null,
      contents: [],
      items: [],
      order: payload.order
    }
  }

  /**
   * **PUT /api/collection/{id}/items/{itemId}**
   *
   * Logs the request only; position changes are not persisted in dev mode.
   */
  async updateCollectionItemPosition(collectionId: string, itemId: string, position: number): Promise<void> {
    log('PUT', `/api/collection/${collectionId}/items/${itemId}`, { position })
  }

  /**
   * Returns root items only (fully populated from seed data for
   * realistic development). The store drives progressive loading
   * of children via {@link getCollectionItems}.
   */
  async loadFullTree(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true (loadFullTree)')
    return dummyData.rootItems.map(cloneItem)
  }
}

/** Singleton instance for development use. */
export default new DevAdbApiService()
