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

export interface ValidatorDTO {
  validatorId: string
  description: string
  validator: string
}

export interface CreateValidatorPayload {
  description: string
  validator: string
}

export interface ApiAdapter {
  getRootItems(): Promise<ItemDTO[]>

  getCollectionItems(collectionId: string): Promise<CollectionItemDTO[]>

  createItem(payload: CreateItemPayload): Promise<ItemDTO>

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

  uploadBlob(contentId: string, file: File): Promise<void>

  getBlobUrl(contentId: string): string

  loadFullTree(): Promise<ItemDTO[]>

  getItemsByRootId(rootItemId: string): Promise<ItemDTO[]>

  uploadBlob(contentId: string, file: File): Promise<void>

  // ── Validators ───────────────────────────────────────────────────────

  getValidators(): Promise<ValidatorDTO[]>

  createValidator(payload: CreateValidatorPayload): Promise<ValidatorDTO>

  updateValidator(id: string, payload: CreateValidatorPayload): Promise<ValidatorDTO>

  deleteValidator(id: string): Promise<void>

  getValidatorsForItem(itemId: string): Promise<ValidatorDTO[]>

  addValidatorToItem(itemId: string, validatorId: string): Promise<ItemDTO>

  removeValidatorFromItem(itemId: string, validatorId: string): Promise<void>
}
