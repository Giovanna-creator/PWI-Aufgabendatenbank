export interface ContentDTO {
  itemContentId: string
  itemContentTypeId: string
  itemContentTypeName: string
  authorId: string
  authorDescriptor: string
  licenseId: string | null
  licenseName: string | null
  purpose: string | null
  jsonSerializedContent: string | null
  hasBlobContent: boolean
  tagIds: string[]
  createdAt: string
  updatedAt: string
}

export interface CollectionItemDTO {
  subItemId: string
  position: number | null
  item?: ItemDTO
}

export interface ItemDTO {
  itemId: string
  authorId: string
  authorDescriptor: string
  licenseId: string | null
  licenseName: string | null
  itemTypeId: string
  itemTypeName: string
  itemTemplateId: string | null
  rootItemId: string | null
  tagIds: string[]
  validatorIds: string[]
  modifierIds: string[]
  isCollection: boolean
  // item_collection_id, falls dieses Item eine Kollektion ist (sonst null).
  // Wird für alle /api/collections/{id}/... Aufrufe benötigt.
  collectionId?: string | null
  contents: ContentSummaryDTO[]
  createdAt: string
  updatedAt: string
  items?: CollectionItemDTO[]
  order?: boolean
}

export interface ContentSummaryDTO {
  itemContentId: string
  itemContentTypeName: string
  hasJsonContent: boolean
  hasBlobContent: boolean
}

// ── Referenzdaten (Lookup-Listen für Dropdowns) ──────────────────────────────

export interface AuthorDTO {
  id: string
  descriptor: string
  mail: string | null
}

export interface LicenseDTO {
  id: string
  name: string
}

export interface ItemTypeDTO {
  id: string
  name: string
  description: string | null
}

export interface ContentTypeDTO {
  id: string
  name: string
  description: string | null
}

export interface CreateItemPayload {
  authorId: string
  licenseId: string
  itemTypeId: string
  itemTemplateId?: string | null
  rootItemId?: string | null
  tagIds?: string[]
  validatorIds?: string[]
  modifierIds?: string[]
}

export interface CreateCollectionPayload {
  parentItemId?: string | null
  order?: boolean
  subItems?: { subitemId: string; position?: number | null }[]
}

export interface CreateContentPayload {
  licenseId: string
  itemContentTypeId: string
  authorId: string
  purpose?: string
  jsonSerializedContent?: string | null
  tagIds?: string[]
}

export interface UpdateCollectionPayload {
  order: boolean
}

export interface OrderTogglePayload {
  order: boolean
}

export interface ApiAdapter {
  getRootItems(): Promise<ItemDTO[]>

  getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]>

  createItem(payload: CreateItemPayload): Promise<ItemDTO>

  updateItem(itemId: string, payload: CreateItemPayload): Promise<ItemDTO>

  createCollection(payload: CreateCollectionPayload): Promise<ItemDTO>

  convertItemToCollection(itemId: string): Promise<ItemDTO>

  addItemToCollection(collectionId: string, itemId: string): Promise<CollectionItemDTO>

  removeItemFromCollection(collectionId: string, itemId: string): Promise<void>

  deleteItem(itemId: string): Promise<void>

  updateCollection(collectionId: string, payload: UpdateCollectionPayload): Promise<ItemDTO>

  toggleCollectionOrder(collectionId: string, payload: OrderTogglePayload): Promise<ItemDTO>

  updateCollectionItemPosition(collectionId: string, itemId: string, position: number): Promise<void>

  getContents(itemId: string): Promise<ContentDTO[]>

  createContent(itemId: string, payload: CreateContentPayload): Promise<ContentDTO>

  updateContent(contentId: string, payload: CreateContentPayload): Promise<ContentDTO>

  deleteContent(contentId: string): Promise<void>

  loadFullTree(): Promise<ItemDTO[]>

  // Referenzdaten für Dropdowns
  getAuthors(): Promise<AuthorDTO[]>
  getLicenses(): Promise<LicenseDTO[]>
  getItemTypes(): Promise<ItemTypeDTO[]>
  getContentTypes(): Promise<ContentTypeDTO[]>
}
