import axios, { type AxiosInstance } from 'axios'
import type {
  ApiAdapter,
  ItemDTO,
  ContentDTO,
  CollectionItemDTO,
  CreateItemPayload,
  CreateContentPayload,
  CreateCollectionPayload,
  UpdateCollectionPayload,
  OrderTogglePayload,
  ValidatorDTO,
  CreateValidatorPayload
} from './api-adapter.types'

export class AdbApiService implements ApiAdapter {
  private readonly http: AxiosInstance

  constructor(baseURL = '/api') {
    this.http = axios.create({
      baseURL
    })
  }

  async getRootItems(): Promise<ItemDTO[]> {
    const { data } = await this.http.get<ItemDTO[]>('/items', { params: { root: true } })
    return data
  }

  async getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]> {
    const { data } = await this.http.get<CollectionItemDTO[]>(`/collections/${collectionId}/items`)
    return data
  }

  async createItem(payload: CreateItemPayload): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>('/items', payload)
    return data
  }

  async createCollection(payload: CreateCollectionPayload): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>('/collections', payload)
    return data
  }

  async convertItemToCollection(itemId: string): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>(`/items/${itemId}/collection`)
    return data
  }

  async addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO> {
    const { data } = await this.http.post<CollectionItemDTO>(`/collections/${collectionId}/items`, { itemId })
    return data
  }

  async removeItemFromCollection(collectionId: string, itemId: string): Promise<void> {
    await this.http.delete(`/collections/${collectionId}/items/${itemId}`)
  }

  async deleteItem(itemId: string): Promise<void> {
    await this.http.delete(`/items/${itemId}`)
  }

  async updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO> {
    const { data } = await this.http.put<ItemDTO>(`/collections/${collectionId}`, payload)
    return data
  }

  async toggleCollectionOrder(collectionId: string, payload: OrderTogglePayload): Promise<ItemDTO> {
    const { data } = await this.http.put<ItemDTO>(`/collections/${collectionId}/order`, payload)
    return data
  }

  async updateCollectionItemPosition(collectionId: string, itemId: string, position: number): Promise<void> {
    await this.http.put(`/collections/${collectionId}/items/${itemId}/position`, { position })
  }

  async getContents(itemId: string): Promise<ContentDTO[]> {
    const { data } = await this.http.get<ContentDTO[]>(`/contents/by-item/${itemId}`)
    return data
  }

  async createContent(itemId: string, payload: CreateContentPayload): Promise<ContentDTO> {
    const { data } = await this.http.post<ContentDTO>(`/contents/by-item/${itemId}`, payload)
    return data
  }

  async updateContent(contentId: string, payload: CreateContentPayload): Promise<ContentDTO> {
    const { data } = await this.http.put<ContentDTO>(`/contents/${contentId}`, payload)
    return data
  }

  async deleteContent(contentId: string): Promise<void> {
    await this.http.delete(`/contents/${contentId}`)
  }

  async uploadBlob(contentId: string, file: File): Promise<void> {
    const formData = new FormData()
    formData.append('file', file)
    await this.http.post(`/contents/${contentId}/blob`, formData)
  }

  getBlobUrl(contentId: string): string {
    return `/api/contents/${contentId}/blob`
  }

  async loadFullTree(): Promise<ItemDTO[]> {
    return this.getRootItems()
  }

  async getItemsByRootId(rootItemId: string): Promise<ItemDTO[]> {
    const { data } = await this.http.get<ItemDTO[]>('/items', { params: { rootItemId } })
    return data
  }

  async uploadBlob(contentId: string, file: File): Promise<void> {
    const formData = new FormData()
    formData.append('file', file)
    await this.http.post(`/contents/${contentId}/blob`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  // ── Validators ───────────────────────────────────────────────────────

  async getValidators(): Promise<ValidatorDTO[]> {
    const { data } = await this.http.get<ValidatorDTO[]>('/validators')
    return data
  }

  async createValidator(payload: CreateValidatorPayload): Promise<ValidatorDTO> {
    const { data } = await this.http.post<ValidatorDTO>('/validators', payload)
    return data
  }

  async updateValidator(id: string, payload: CreateValidatorPayload): Promise<ValidatorDTO> {
    const { data } = await this.http.put<ValidatorDTO>(`/validators/${id}`, payload)
    return data
  }

  async deleteValidator(id: string): Promise<void> {
    await this.http.delete(`/validators/${id}`)
  }

  async getValidatorsForItem(itemId: string): Promise<ValidatorDTO[]> {
    const { data } = await this.http.get<ValidatorDTO[]>(`/items/${itemId}/validators`)
    return data
  }

  async addValidatorToItem(itemId: string, validatorId: string): Promise<ItemDTO> {
    const { data } = await this.http.post<ItemDTO>(`/items/${itemId}/validators/${validatorId}`)
    return data
  }

  async removeValidatorFromItem(itemId: string, validatorId: string): Promise<void> {
    await this.http.delete(`/items/${itemId}/validators/${validatorId}`)
  }
}

export default new AdbApiService()
