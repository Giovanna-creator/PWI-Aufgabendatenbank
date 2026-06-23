import { dummyData } from './dummy-data'
import type {
  ApiAdapter,
  ItemDTO,
  ContentDTO,
  ContentSummaryDTO,
  CollectionItemDTO,
  CreateItemPayload,
  CreateContentPayload,
  CreateCollectionPayload,
  UpdateCollectionPayload,
  OrderTogglePayload
} from './api-adapter.types'
import type { Item, Content, CollectionItem } from '@/lib/types'

function log(method: string, path: string, body?: unknown): void {
  const parts = [`[ADB Dev] ${method} ${path}`]
  if (body !== undefined) {
    parts.push('\u2192', JSON.stringify(body, null, 2))
  }
  console.log(...parts)
}

function uid(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`
}

const PLACEHOLDER_UUID = '00000000-0000-0000-0000-000000000000'

function nowISO(): string {
  return new Date().toISOString()
}

function toContentSummaryDTO(c: Content): ContentSummaryDTO {
  return {
    itemContentId: c.id ?? uid('content'),
    itemContentTypeName: c.contentType,
    hasJsonContent: c.jsonContent != null,
    hasBlobContent: !!c.blobContent
  }
}

function toContentDTO(c: Content): ContentDTO {
  return {
    itemContentId: c.id ?? uid('content'),
    itemContentTypeId: PLACEHOLDER_UUID,
    itemContentTypeName: c.contentType,
    authorId: PLACEHOLDER_UUID,
    authorDescriptor: c.author,
    licenseId: null,
    licenseName: c.license,
    purpose: c.purpose ?? null,
    jsonSerializedContent: c.jsonContent ? JSON.stringify(c.jsonContent) : null,
    hasBlobContent: !!c.blobContent,
    tagIds: [],
    createdAt: nowISO(),
    updatedAt: nowISO()
  }
}

function toDTO(item: Item): ItemDTO {
  return {
    itemId: item.id,
    authorId: PLACEHOLDER_UUID,
    authorDescriptor: item.author,
    licenseId: null,
    licenseName: item.license ?? null,
    itemTypeId: item.item_type === 'collection' ? 'collection-type-id' : 'exercise-type-id',
    itemTypeName: item.item_type === 'collection' ? 'collection' : 'exercise',
    itemTemplateId: item.representationTemplate,
    rootItemId: item.rootItemId ?? null,
    tagIds: item.tags ?? [],
    validatorIds: item.validators ?? [],
    modifierIds: item.modifiers ?? [],
    isCollection: item.item_type === 'collection' || !!item.items,
    // Im Dummy-Modus ist die "collection id" identisch zur item id.
    collectionId: item.item_type === 'collection' || !!item.items ? item.id : null,
    contents: (item.contents ?? []).map(toContentSummaryDTO),
    createdAt: nowISO(),
    updatedAt: nowISO(),
    items: item.items?.map(toCollectionItemDTO),
    order: item.order
  }
}

function toCollectionItemDTO(ci: CollectionItem): CollectionItemDTO {
  return {
    subItemId: ci.id,
    position: ci.position,
    item: ci.item ? toDTO(ci.item) : undefined
  }
}

function findItem(id: string, items: Item[]): Item | undefined {
  for (const item of items) {
    if (item.id === id) return item
    if (item.items) {
      const found = findItem(id, item.items.map((ci) => ci.item))
      if (found) return found
    }
  }
  return undefined
}

export class DevAdbApiService implements ApiAdapter {
  async getRootItems(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true')
    return dummyData.rootItems.map(toDTO)
  }

  async getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]> {
    log('GET', `/api/collections/${collectionId}/items`)
    const item = findItem(collectionId, dummyData.rootItems)
    if (item?.items) {
      return item.items.map(toCollectionItemDTO)
    }
    return []
  }

  async createItem(payload: CreateItemPayload): Promise<ItemDTO> {
    log('POST', '/api/items', payload)
    const id = uid('item')
    return {
      itemId: id,
      authorId: payload.authorId,
      authorDescriptor: 'author',
      licenseId: payload.licenseId,
      licenseName: null,
      itemTypeId: payload.itemTypeId,
      itemTypeName: 'exercise',
      itemTemplateId: payload.itemTemplateId ?? null,
      rootItemId: payload.rootItemId ?? null,
      tagIds: payload.tagIds ?? [],
      validatorIds: payload.validatorIds ?? [],
      modifierIds: payload.modifierIds ?? [],
      isCollection: false,
      contents: [],
      createdAt: nowISO(),
      updatedAt: nowISO()
    }
  }

  async createCollection(payload: CreateCollectionPayload): Promise<ItemDTO> {
    log('POST', '/api/collections', payload)
    const id = uid('coll')
    return {
      itemId: id,
      authorId: PLACEHOLDER_UUID,
      authorDescriptor: 'author',
      licenseId: null,
      licenseName: null,
      itemTypeId: 'collection-type-id',
      itemTypeName: 'collection',
      itemTemplateId: null,
      rootItemId: null,
      tagIds: [],
      validatorIds: [],
      modifierIds: [],
      isCollection: true,
      contents: [],
      createdAt: nowISO(),
      updatedAt: nowISO(),
      items: [],
      order: payload.order ?? false
    }
  }

  async convertItemToCollection(itemId: string): Promise<ItemDTO> {
    log('POST', `/api/items/${itemId}/collection`)
    const item = findItem(itemId, dummyData.rootItems)
    if (item) {
      const dto = toDTO(item)
      dto.isCollection = true
      dto.itemTypeName = 'collection'
      dto.collectionId = itemId
      dto.items = []
      dto.order = false
      return dto
    }
    return {
      itemId,
      authorId: PLACEHOLDER_UUID,
      authorDescriptor: 'author',
      licenseId: null,
      licenseName: null,
      itemTypeId: 'collection-type-id',
      itemTypeName: 'collection',
      itemTemplateId: null,
      rootItemId: null,
      tagIds: [],
      validatorIds: [],
      modifierIds: [],
      isCollection: true,
      collectionId: itemId,
      contents: [],
      createdAt: nowISO(),
      updatedAt: nowISO(),
      items: [],
      order: false
    }
  }

  async addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO> {
    log('POST', `/api/collections/${collectionId}/items`, { itemId })
    return {
      subItemId: uid('coll-item'),
      position: null,
      item: {
        itemId,
        authorId: PLACEHOLDER_UUID,
        authorDescriptor: 'author',
        licenseId: null,
        licenseName: null,
        itemTypeId: 'exercise-type-id',
        itemTypeName: 'exercise',
        itemTemplateId: null,
        rootItemId: collectionId,
        tagIds: [],
        validatorIds: [],
        modifierIds: [],
        isCollection: false,
        contents: [],
        createdAt: nowISO(),
        updatedAt: nowISO()
      }
    }
  }

  async removeItemFromCollection(_collectionId: string, _itemId: string): Promise<void> {
    log('DELETE', `/api/collections/${_collectionId}/items/${_itemId}`)
  }

  async deleteItem(itemId: string): Promise<void> {
    log('DELETE', `/api/items/${itemId}`)
  }

  async updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO> {
    log('PUT', `/api/collections/${collectionId}`, payload)
    const item = findItem(collectionId, dummyData.rootItems)
    if (item) {
      const dto = toDTO(item)
      dto.order = payload.order
      return dto
    }
    return {
      itemId: collectionId,
      authorId: PLACEHOLDER_UUID,
      authorDescriptor: 'author',
      licenseId: null,
      licenseName: null,
      itemTypeId: 'collection-type-id',
      itemTypeName: 'collection',
      itemTemplateId: null,
      rootItemId: null,
      tagIds: [],
      validatorIds: [],
      modifierIds: [],
      isCollection: true,
      contents: [],
      createdAt: nowISO(),
      updatedAt: nowISO(),
      items: [],
      order: payload.order
    }
  }

  async toggleCollectionOrder(collectionId: string, payload: OrderTogglePayload): Promise<ItemDTO> {
    log('PUT', `/api/collections/${collectionId}/order`, payload)
    return this.updateCollection(collectionId, payload)
  }

  async updateCollectionItemPosition(_collectionId: string, _itemId: string, _position: number): Promise<void> {
    log('PUT', `/api/collections/${_collectionId}/items/${_itemId}`, { position: _position })
  }

  async getContents(itemId: string): Promise<ContentDTO[]> {
    log('GET', `/api/items/${itemId}/contents`)
    const item = findItem(itemId, dummyData.rootItems)
    if (item?.contents) {
      return item.contents.map(toContentDTO)
    }
    return []
  }

  async createContent(_itemId: string, payload: CreateContentPayload): Promise<ContentDTO> {
    log('POST', `/api/items/${_itemId}/contents`, payload)
    return {
      itemContentId: uid('content'),
      itemContentTypeId: payload.itemContentTypeId,
      itemContentTypeName: 'text',
      authorId: payload.authorId,
      authorDescriptor: 'author',
      licenseId: payload.licenseId,
      licenseName: null,
      purpose: payload.purpose ?? null,
      jsonSerializedContent: payload.jsonSerializedContent ?? null,
      hasBlobContent: false,
      tagIds: payload.tagIds ?? [],
      createdAt: nowISO(),
      updatedAt: nowISO()
    }
  }

  async updateContent(contentId: string, payload: CreateContentPayload): Promise<ContentDTO> {
    log('PUT', `/api/contents/${contentId}`, payload)
    return {
      itemContentId: contentId,
      itemContentTypeId: payload.itemContentTypeId,
      itemContentTypeName: 'text',
      authorId: payload.authorId,
      authorDescriptor: 'author',
      licenseId: payload.licenseId,
      licenseName: null,
      purpose: payload.purpose ?? null,
      jsonSerializedContent: payload.jsonSerializedContent ?? null,
      hasBlobContent: false,
      tagIds: payload.tagIds ?? [],
      createdAt: nowISO(),
      updatedAt: nowISO()
    }
  }

  async deleteContent(contentId: string): Promise<void> {
    log('DELETE', `/api/contents/${contentId}`)
  }

  async loadFullTree(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true (loadFullTree)')
    return dummyData.rootItems.map(toDTO)
  }

  async getItemsByRootId(rootItemId: string): Promise<ItemDTO[]> {
    log('GET', `/api/items?rootItemId=${rootItemId}`)
    return dummyData.rootItems
      .flatMap(item => item.items ?? [])
      .filter(ci => ci.item.rootItemId === rootItemId)
      .map(ci => toDTO(ci.item))
  }
}

export default new DevAdbApiService()
