import axios, { type AxiosInstance } from 'axios'
import type {
  ApiAdapter,
  ItemDTO,
  CollectionItemDTO,
  CreateItemPayload,
  CreateCollectionPayload,
  UpdateCollectionPayload
} from './api-adapter.types'

/**
 * Real HTTP implementation of {@link ApiAdapter}.
 *
 * Sends actual HTTP requests to the Spring Boot backend using axios.
 * Paths follow `frontend/frontend-backend-communication.md`.
 *
 * ## Usage
 * ```ts
 * import adbApi from '@/feature/aufgabendatenbank/adbApi.service'
 * const items = await adbApi.getRootItems()
 * ```
 *
 * ## Switching to dev mode
 * To use the dev/logging adapter instead, change the import in `main.ts`.
 *
 * @see DevAdbApiService
 */
export class AdbApiService implements ApiAdapter {
  private readonly http: AxiosInstance

  /**
   * @param baseURL - Backend base URL. Defaults to `/api` so it works with
   * the Vite proxy configuration out of the box.
   */
  constructor(baseURL = '/api') {
    this.http = axios.create({
      baseURL,
      headers: { 'Content-Type': 'application/json' }
    })
  }

  /**
   * **GET /api/items?root=true**
   *
   * Loads all root-level items (items without a parent collection).
   * Each item includes its `contents` inline. Response does NOT include
   * nested children — those are loaded on demand via {@link getCollectionItems}.
   */
  async getRootItems(): Promise<ItemDTO[]> {
    const { data } = await this.http.get<ItemDTO[]>('/items', { params: { root: true } })
    return data
  }

  /**
   * **GET /api/collections/{id}/items**
   *
   * Loads the direct children of a collection. Each child includes its
   * `position` and the full nested `item` DTO.
   */
  async getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]> {
    const { data } = await this.http.get<CollectionItemDTO[]>(`/collections/${collectionId}/items`)
    return data
  }

  /**
   * **POST /api/items**
   *
   * Creates a new item. The backend assigns the `id` and returns
   * the persisted DTO.
   */
  async createItem(payload: CreateItemPayload): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>('/items', payload)
    return data
  }

  /**
   * **POST /api/collections**
   *
   * Creates a root collection: the backend creates an Item with
   * `item_type='collection'` plus the corresponding Collection record.
   * This is NOT for converting existing items — use {@link convertItemToCollection}.
   */
  async createCollection(payload: CreateCollectionPayload): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>('/collections', payload)
    return data
  }

  /**
   * **POST /api/items/{id}/collection**
   *
   * Converts an existing exercise item into a collection.
   * The backend flips `item_type` to 'collection' and creates
   * the corresponding Collection record.
   */
  async convertItemToCollection(itemId: string): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>(`/items/${itemId}/collection`)
    return data
  }

  /**
   * **POST /api/collection/{id}/items**
   *
   * Adds an existing item to a collection by creating a CollectionItem record.
   * If the target collection is ordered, the backend auto-assigns a position.
   */
  async addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO> {
    const { data } = await this.http.post<CollectionItemDTO>(`/collection/${collectionId}/items`, { itemId })
    return data
  }

  /**
   * **DELETE /api/collections/{id}/items/{itemId}**
   *
   * Removes an item from a collection — breaks the relationship only.
   * The item itself is NOT deleted; its `rootItemId` becomes `null`
   * (making it a root item).
   */
  async removeItemFromCollection(collectionId: string, itemId: string): Promise<void> {
    await this.http.delete(`/collections/${collectionId}/items/${itemId}`)
  }

  /**
   * **DELETE /api/items/{id}**
   *
   * Deletes an item with cascade:
   * - Item_Content records
   * - Collection record (if item is a collection)
   * - CollectionItem join records
   */
  async deleteItem(itemId: string): Promise<void> {
    await this.http.delete(`/items/${itemId}`)
  }

  /**
   * **PUT /api/collections/{id}**
   *
   * Updates collection metadata. Used to toggle `order` on or off.
   * When toggling on, the backend auto-assigns sequential positions.
   * When toggling off, positions are kept (UI hides them).
   */
  async updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO> {
    const { data } = await this.http.put<ItemDTO>(`/collections/${collectionId}`, payload)
    return data
  }

  /**
   * **PUT /api/collection/{id}/items/{itemId}**
   *
   * Updates the position of an item within an ordered collection
   * (triggered by drag-and-drop reorder).
   * The backend recalculates sibling positions to maintain a gapless sequence.
   */
  async updateCollectionItemPosition(collectionId: string, itemId: string, position: number): Promise<void> {
    await this.http.put(`/collection/${collectionId}/items/${itemId}`, { position })
  }

  /**
   * Loads all root items without nesting children.
   *
   * Progressive recursive loading is handled by the store —
   * see {@link useExerciseStore._loadChildrenRecursively}.
   * This method just returns the top level; children are fetched
   * on demand via {@link getCollectionItems}.
   */
  async loadFullTree(): Promise<ItemDTO[]> {
    return this.getRootItems()
  }
}

/** Singleton instance, pre-configured with the default `/api` base URL. */
export default new AdbApiService()
