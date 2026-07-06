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
  OrderTogglePayload,
  AuthorDTO,
  LicenseDTO,
  ItemTypeDTO,
  ContentTypeDTO,
  ValidatorDTO,
  CreateValidatorPayload,
  SearchParams
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

const seedValidators: ValidatorDTO[] = [
  {
    validatorId: 'val-must-inner-join',
    description: 'muss INNER JOIN enthalten',
    validator: 'CHECK(sql_query CONTAINS "INNER JOIN")'
  },
  {
    validatorId: 'val-order-by',
    description: 'muss ORDER BY enthalten',
    validator: 'CHECK(sql_query CONTAINS "ORDER BY")'
  },
  {
    validatorId: 'val-no-subqueries',
    description: 'keine verschachtelten Subqueries',
    validator: 'CHECK(NOT CONTAINS "SELECT ... FROM (SELECT ...)")'
  }
]

export class DevAdbApiService implements ApiAdapter {
  async getRootItems(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true')
    return dummyData.rootItems.map(toDTO)
  }

  async searchItems(params: SearchParams): Promise<ItemDTO[]> {
    log('GET', '/api/items', params)

    const allItems: Item[] = []
    const collect = (items: Item[]) => {
      for (const item of items) {
        allItems.push(item)
        if (item.items) {
          for (const ci of item.items) {
            allItems.push(ci.item)
          }
        }
      }
    }
    collect(dummyData.rootItems)

    const q = (s: string | undefined) => s?.toLowerCase() ?? ''

    const result = allItems.filter((item) => {
      if (params.search) {
        const text = q(params.search)
        const match = item.contents.some((c) => {
          const jsonStr = JSON.stringify(c.jsonContent ?? {})
          return q(jsonStr).includes(text)
        })
        if (!match) return false
      }
      if (params.authorId) {
        const author = q(params.authorId)
        if (!q(item.author).includes(author) && !q(item.authorId ?? '').includes(author)) {
          return false
        }
      }
      if (params.itemTypeId) {
        if (q(item.itemTypeId ?? '') !== q(params.itemTypeId) &&
            q(item.item_type) !== q(params.itemTypeId)) {
          return false
        }
      }
      if (params.tag) {
        const tagText = q(params.tag)
        const found = item.tags.some((t) => q(t).includes(tagText))
        if (!found) return false
      }
      return true
    })

    return result.map(toDTO)
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

  async updateItem(itemId: string, payload: CreateItemPayload): Promise<ItemDTO> {
    log('PUT', `/api/items/${itemId}`, payload)
    const item = findItem(itemId, dummyData.rootItems)
    const base = item ? toDTO(item) : null
    return {
      itemId,
      authorId: payload.authorId,
      authorDescriptor: base?.authorDescriptor ?? 'author',
      licenseId: payload.licenseId,
      licenseName: base?.licenseName ?? null,
      itemTypeId: payload.itemTypeId,
      itemTypeName: base?.itemTypeName ?? 'exercise',
      itemTemplateId: payload.itemTemplateId ?? null,
      rootItemId: payload.rootItemId ?? null,
      tagIds: payload.tagIds ?? [],
      validatorIds: payload.validatorIds ?? [],
      modifierIds: payload.modifierIds ?? [],
      isCollection: base?.isCollection ?? false,
      collectionId: base?.collectionId ?? null,
      contents: base?.contents ?? [],
      createdAt: base?.createdAt ?? nowISO(),
      updatedAt: nowISO(),
      items: base?.items,
      order: base?.order
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

  async uploadBlob(contentId: string, file: File): Promise<void> {
    log('POST', `/api/contents/${contentId}/blob`, {
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type
    })
  }

  getBlobUrl(contentId: string): string {
    return `/api/contents/${contentId}/blob`
  }

  async loadFullTree(): Promise<ItemDTO[]> {
    log('GET', '/api/items?root=true (loadFullTree)')
    return dummyData.rootItems.map(toDTO)
  }

  // ── Referenzdaten (gleiche Seeds wie database/init/init.sql) ───────────────

  async getAuthors(): Promise<AuthorDTO[]> {
    log('GET', '/api/authors')
    return [
      { id: 'd0000000-0000-0000-0000-000000000001', descriptor: 'Prof. Dr. Markus Siepermann', mail: 'markus.siepermann@mni.thm.de' },
      { id: 'd0000000-0000-0000-0000-000000000002', descriptor: 'Johannes Kunz', mail: 'johannes.kunz@mni.thm.de' },
      { id: 'd0000000-0000-0000-0000-000000000003', descriptor: 'Joelle Kamwa Mokam', mail: 'joelle.kamwa@mni.thm.de' }
    ]
  }

  async getLicenses(): Promise<LicenseDTO[]> {
    log('GET', '/api/licenses')
    return [
      { id: 'b0000000-0000-0000-0000-000000000001', name: 'CC-BY-4.0' },
      { id: 'b0000000-0000-0000-0000-000000000002', name: 'CC-BY-SA-4.0' },
      { id: 'b0000000-0000-0000-0000-000000000003', name: 'MIT' },
      { id: 'b0000000-0000-0000-0000-000000000004', name: 'Internal-THM' }
    ]
  }

  async getItemTypes(): Promise<ItemTypeDTO[]> {
    log('GET', '/api/item-types')
    return [
      { id: 'e0000000-0000-0000-0000-000000000001', name: 'SQL-Abfrage', description: 'Aufgaben, die das Schreiben einer SQL-Abfrage erfordern' },
      { id: 'e0000000-0000-0000-0000-000000000002', name: 'Modellierung', description: 'Erstellung eines Datenmodells' },
      { id: 'e0000000-0000-0000-0000-000000000003', name: 'Multiple-Choice', description: 'Auswahl der richtigen Antwort(en) aus mehreren Optionen' },
      { id: 'e0000000-0000-0000-0000-000000000004', name: 'Freitext', description: 'Offene Textantwort' }
    ]
  }

  async getContentTypes(): Promise<ContentTypeDTO[]> {
    log('GET', '/api/content-types')
    return [
      { id: 'a0000000-0000-0000-0000-000000000001', name: 'text/plain', description: 'Einfacher Text' },
      { id: 'a0000000-0000-0000-0000-000000000002', name: 'text/markdown', description: 'Markdown-formatierter Text' },
      { id: 'a0000000-0000-0000-0000-000000000003', name: 'application/json', description: 'Strukturierter JSON-Inhalt' },
      { id: 'a0000000-0000-0000-0000-000000000004', name: 'image/png', description: 'PNG-Bilder' },
      { id: 'a0000000-0000-0000-0000-000000000005', name: 'image/jpeg', description: 'JPEG-Bilder' },
      { id: 'a0000000-0000-0000-0000-000000000006', name: 'application/pdf', description: 'PDF-Dokumente' }
    ]
  }

  async createAuthor(payload: { descriptor: string; mail: string | null }): Promise<AuthorDTO> {
    log('POST', '/api/authors', payload)
    return { id: crypto.randomUUID(), descriptor: payload.descriptor, mail: payload.mail }
  }

  async createLicense(payload: { name: string }): Promise<LicenseDTO> {
    log('POST', '/api/licenses', payload)
    return { id: crypto.randomUUID(), name: payload.name }
  }

  async createItemType(payload: { name: string; description: string | null }): Promise<ItemTypeDTO> {
    log('POST', '/api/item-types', payload)
    return { id: crypto.randomUUID(), name: payload.name, description: payload.description }
  }

  async createContentType(payload: { name: string; description: string | null }): Promise<ContentTypeDTO> {
    log('POST', '/api/content-types', payload)
    return { id: crypto.randomUUID(), name: payload.name, description: payload.description }
  }

  async getItemsByRootId(rootItemId: string): Promise<ItemDTO[]> {
    log('GET', `/api/items?rootItemId=${rootItemId}`)
    return dummyData.rootItems
      .flatMap(item => item.items ?? [])
      .filter(ci => ci.item.rootItemId === rootItemId)
      .map(ci => toDTO(ci.item))
  }

  // ── Validators ───────────────────────────────────────────────────────

  private _validators: ValidatorDTO[] = [...seedValidators]

  async getValidators(): Promise<ValidatorDTO[]> {
    log('GET', '/api/validators')
    return [...this._validators]
  }

  async createValidator(payload: CreateValidatorPayload): Promise<ValidatorDTO> {
    log('POST', '/api/validators', payload)
    const v: ValidatorDTO = {
      validatorId: uid('val'),
      description: payload.description,
      validator: payload.validator
    }
    this._validators.push(v)
    return v
  }

  async updateValidator(id: string, payload: CreateValidatorPayload): Promise<ValidatorDTO> {
    log('PUT', `/api/validators/${id}`, payload)
    const idx = this._validators.findIndex(v => v.validatorId === id)
    if (idx === -1) throw new Error('Validator nicht gefunden')
    this._validators[idx] = { ...this._validators[idx], ...payload }
    return this._validators[idx]
  }

  async deleteValidator(id: string): Promise<void> {
    log('DELETE', `/api/validators/${id}`)
    this._validators = this._validators.filter(v => v.validatorId !== id)
  }

  async getValidatorsForItem(itemId: string): Promise<ValidatorDTO[]> {
    log('GET', `/api/items/${itemId}/validators`)
    const item = findItem(itemId, dummyData.rootItems)
    if (!item) return []
    return (item.validators ?? [])
      .map((vid: string) => this._validators.find(v => v.validatorId === vid))
      .filter(Boolean) as ValidatorDTO[]
  }

  async addValidatorToItem(itemId: string, validatorId: string): Promise<ItemDTO> {
    log('POST', `/api/items/${itemId}/validators/${validatorId}`)
    const item = findItem(itemId, dummyData.rootItems)
    if (!item) throw new Error('Item nicht gefunden')
    if (!item.validators) item.validators = []
    if (!item.validators.includes(validatorId)) {
      item.validators.push(validatorId)
    }
    return toDTO(item)
  }

  async removeValidatorFromItem(itemId: string, validatorId: string): Promise<void> {
    log('DELETE', `/api/items/${itemId}/validators/${validatorId}`)
    const item = findItem(itemId, dummyData.rootItems)
    if (!item) return
    if (item.validators) {
      item.validators = item.validators.filter((v: string) => v !== validatorId)
    }
  }
}

export default new DevAdbApiService()
