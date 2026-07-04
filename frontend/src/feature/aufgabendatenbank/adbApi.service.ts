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
  AuthorDTO,
  LicenseDTO,
  ItemTypeDTO,
  ContentTypeDTO
} from './api-adapter.types'

export class AdbApiService implements ApiAdapter {
  private readonly http: AxiosInstance

  constructor(baseURL = '/api') {
    this.http = axios.create({
      baseURL,
      headers: { 'Content-Type': 'application/json' }
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

  async updateItem(itemId: string, payload: CreateItemPayload): Promise<ItemDTO> {
    const { data } = await this.http.put<ItemDTO>(`/items/${itemId}`, payload)
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

  async loadFullTree(): Promise<ItemDTO[]> {
    return this.getRootItems()
  }

  async getAuthors(): Promise<AuthorDTO[]> {
    const { data } = await this.http.get<AuthorDTO[]>('/authors')
    return data
  }

  async getLicenses(): Promise<LicenseDTO[]> {
    const { data } = await this.http.get<LicenseDTO[]>('/licenses')
    return data
  }

  async getItemTypes(): Promise<ItemTypeDTO[]> {
    const { data } = await this.http.get<ItemTypeDTO[]>('/item-types')
    return data
  }

  async getContentTypes(): Promise<ContentTypeDTO[]> {
    const { data } = await this.http.get<ContentTypeDTO[]>('/content-types')
    return data
  }

  async createAuthor(payload: { descriptor: string; mail: string | null }): Promise<AuthorDTO> {
    const { data } = await this.http.post<AuthorDTO>('/authors', payload)
    return data
  }

  async createLicense(payload: { name: string }): Promise<LicenseDTO> {
    const { data } = await this.http.post<LicenseDTO>('/licenses', payload)
    return data
  }

  async createItemType(payload: { name: string; description: string | null }): Promise<ItemTypeDTO> {
    const { data } = await this.http.post<ItemTypeDTO>('/item-types', payload)
    return data
  }

  async createContentType(payload: { name: string; description: string | null }): Promise<ContentTypeDTO> {
    const { data } = await this.http.post<ContentTypeDTO>('/content-types', payload)
    return data
  }
}

export default new AdbApiService()
