/**
 * API adapter contract and DTOs for the Aufgabendatenbank feature.
 *
 * ## Architecture
 * The store depends on the {@link ApiAdapter} interface (Dependency Inversion).
 * Two implementations exist:
 * - {@link AdbApiService} — real HTTP calls via axios (production)
 * - {@link DevAdbApiService} — logs requests, returns dummy data (development)
 *
 * Switching between them is a one-line change in `main.ts` (Pinia plugin).
 *
 * ## Endpoint reference
 * All paths follow `frontend/frontend-backend-communication.md`.
 * Note the inconsistent pluralisation ("collection" vs "collections") — this
 * matches the backend routes as defined in that spec.
 */

// ── API DTOs ──────────────────────────────────────────────────────────────────

/**
 * Content block DTO returned by the API.
 * Each item has zero or more content blocks, linked by `purpose`.
 */
export interface ContentDTO {
  id?: string
  license: string | null
  contentType: string
  author: string
  purpose: string
  jsonContent: Record<string, unknown>
  blobContent: string
}

/**
 * Collection sub-item DTO returned by the API.
 * Represents an item nested inside a collection, with an optional `position`.
 * When the parent collection has `order: true`, `position` is a positive integer
 * indicating the item's place in the sequence. When `order: false`, `position`
 * is `null` (but may still contain stale values — the UI hides them per new spec).
 */
export interface CollectionItemDTO {
  id: string
  collectionId: string
  item: ItemDTO
  position: number | null
}

/**
 * Item DTO returned by the API.
 *
 * If `item_type === 'collection'`, the response includes `items` (children)
 * and `order` (whether the collection is ordered). Non-collection items omit
 * both fields.
 *
 * **GET /api/items/root?=true** returns items at the root level.
 * The `isCollection` detection is done via `item_type === 'collection'`.
 */
export interface ItemDTO {
  id: string
  item_type: string
  author: string
  representationTemplate: string | null
  license: string | null
  rootItemId?: string | null
  contents: ContentDTO[]
  items?: CollectionItemDTO[]
  order?: boolean
}

// ── Request payloads ──────────────────────────────────────────────────────────

/**
 * Payload for **POST /api/items**.
 *
 * Creates a new exercise or collection item. The backend assigns `id`.
 */
export interface CreateItemPayload {
  item_type: string
  author: string
  rootItemId: string | null
  contents: CreateContentPayload[]
}

/**
 * Payload for **POST /api/collections**.
 *
 * Creates a new root collection — the backend first creates an `Item` (with
 * `item_type='collection'`), then creates the corresponding `Collection` record
 * with the given `order` flag.
 *
 * This endpoint does NOT convert an existing item into a collection; use
 * **POST /api/items/{id}/collection** for that.
 */
export interface CreateCollectionPayload {
  item_type: 'collection'
  author: string
  contents: CreateContentPayload[]
  order: boolean
}

/**
 * Content block payload for create/update operations.
 */
export interface CreateContentPayload {
  license: string | null
  contentType: string
  author: string
  purpose: string
  jsonContent: Record<string, unknown>
  blobContent: string
}

/**
 * Payload for **PUT /api/collections/{id}**.
 *
 * Used to update collection-level metadata (currently only `order`).
 */
export interface UpdateCollectionPayload {
  order: boolean
}

// ── Adapter Interface ─────────────────────────────────────────────────────────

/**
 * Abstract API adapter for the Aufgabendatenbank feature.
 *
 * The store depends on this interface. Either implementation
 * (HTTP or dev/logging) can be swapped in via the Pinia plugin in `main.ts`.
 */
export interface ApiAdapter {
/**
 * **GET /api/items?root=true**
 *
 * Loads all root-level items. The backend returns items whose
 * `rootItemId` is `null`. Each response includes inline `contents`.
 * Collection items also carry `order` (but `items` is NOT populated here —
 * children are loaded on demand via {@link getCollectionItems}).
 */
  getRootItems(): Promise<ItemDTO[]>

  /**
   * **GET /api/collections/{id}/items**
   *
   * Loads all direct children of a collection.
   * Each child carries its `position` and nested `item` data.
   * The parent's `order` flag determines whether positions are meaningful.
   */
  getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]>

  /**
   * **POST /api/items**
   *
   * Creates a new item (exercise or collection).
   * The backend assigns the `id` and returns the persisted DTO.
   */
  createItem(payload: CreateItemPayload): Promise<ItemDTO>

  /**
   * **POST /api/collections**
   *
   * Creates a root collection: an Item (item_type='collection') plus
   * a Collection record. Not for converting existing items.
   */
  createCollection(payload: CreateCollectionPayload): Promise<ItemDTO>

  /**
   * **POST /api/items/{id}/collection**
   *
   * Converts an existing exercise item into a collection.
   * The backend flips `item_type` to 'collection' and creates
   * the corresponding Collection record.
   */
  convertItemToCollection(itemId: string): Promise<ItemDTO>

  /**
   * **POST /api/collection/{id}/items**
   *
   * Adds an existing item to a collection.
   * Creates a `CollectionItem` record linking the item to the collection.
   * If the target collection is ordered, the backend auto-assigns a position.
   * Returns the created CollectionItem.
   */
  addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO>

  /**
   * **DELETE /api/collections/{id}/items/{itemId}**
   *
   * Removes an item from a collection — this breaks the relationship only.
   * The item itself is NOT deleted; its `rootItemId` is set to `null`
   * (making it a root item).
   * If the collection was ordered, the backend recalculates sibling positions.
   */
  removeItemFromCollection(collectionId: string, itemId: string): Promise<void>

  /**
   * **DELETE /api/items/{id}**
   *
   * Deletes an item with cascade:
   * - Associated `Item_Content` records
   * - `Collection` record (if the item is a collection)
   * - `CollectionItem` join records (if the item belongs to collections)
   */
  deleteItem(itemId: string): Promise<void>

  /**
   * **PUT /api/collections/{id}**
   *
   * Updates collection-level metadata.
   * Primarily used to set `order: false`.
   * When order toggles off, positions are kept in the database but the UI
   * should not display them (per new spec, positions are preserved).
   */
  updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO>

  /**
   * **PUT /api/collection/{id}/items/{itemId}**
   *
   * Updates the position of an item within an ordered collection
   * (e.g. after drag-and-drop reorder).
   * The backend recalculates sibling positions to maintain a gapless sequence.
   */
  updateCollectionItemPosition(collectionId: string, itemId: string, position: number): Promise<void>

  /**
   * Load all root items. Children are NOT included — the store loads
   * them progressively via {@link getCollectionItems} with per-node
   * loading state, so the UI can show spinners.
   *
   * @returns Root-level items only (collections have empty `items`).
   */
  loadFullTree(): Promise<ItemDTO[]>
}
