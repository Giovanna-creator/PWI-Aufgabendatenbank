import { defineStore } from 'pinia'
import { validateTreeData } from '@/feature/aufgabendatenbank/validation'
import { useNotificationStore } from '@/stores/useNotificationStore'
import {
  type Item,
  type Collection,
  type CollectionItem,
  type TreeItem,
  type Content,
  getInnerItem,
  isCollection as checkIsCollection,
  isCollectionItem
} from '@/lib/types'
import type {
  ApiAdapter,
  ItemDTO,
  ContentDTO,
  ContentSummaryDTO,
  CollectionItemDTO,
  ValidatorDTO
} from '@/feature/aufgabendatenbank/api-adapter.types'

// ── Seed-UUIDs (müssen mit database/init/init.sql übereinstimmen) ─────────────

const SEED_AUTHOR_ID = 'd0000000-0000-0000-0000-000000000001'
const SEED_LICENSE_ID = 'b0000000-0000-0000-0000-000000000001'
const SEED_ITEM_TYPE_ID = 'e0000000-0000-0000-0000-000000000001'
const SEED_CONTENT_TYPE_ID = 'a0000000-0000-0000-0000-000000000003'

// ── Adapter injection ─────────────────────────────────────────────────────────

let _adapter: ApiAdapter | null = null

/**
 * Inject the API adapter into the exercise store.
 * Must be called before `useExerciseStore()` is invoked.
 */
export function setApiAdapter(adapter: ApiAdapter): void {
  _adapter = adapter
}

// ── DTO → Store type mappers ──────────────────────────────────────────────────

function toContent(dto: ContentSummaryDTO): Content {
  return {
    id: dto.itemContentId,
    license: null,
    contentType: dto.itemContentTypeName,
    author: '',
    tags: [],
    purpose: '',
    jsonContent: {} as Record<string, any>,
    blobContent: dto.hasBlobContent ? '(binary)' : ''
  }
}

function toFullContent(dto: ContentDTO): Content {
  let jsonContent: Record<string, any> = {}
  if (dto.jsonSerializedContent) {
    try {
      jsonContent = JSON.parse(dto.jsonSerializedContent)
    } catch {
      jsonContent = { text: dto.jsonSerializedContent }
    }
  }
  return {
    id: dto.itemContentId,
    license: dto.licenseName ?? null,
    contentType: dto.itemContentTypeName,
    author: dto.authorDescriptor,
    tags: dto.tagIds,
    purpose: dto.purpose ?? '',
    jsonContent,
    blobContent: dto.hasBlobContent ? '(binary)' : ''
  }
}

function toItem(dto: ItemDTO): Item {
  return {
    id: dto.itemId,
    item_type: dto.isCollection ? 'collection' : 'exercise',
    author: dto.authorDescriptor,
    representationTemplate: dto.itemTemplateId ?? null,
    license: dto.licenseName ?? null,
    tags: dto.tagIds ?? [],
    validators: dto.validatorIds ?? [],
    modifiers: dto.modifierIds ?? [],
    rootItemId: dto.rootItemId ?? null,
    contents: (dto.contents ?? []).map(toContent),
    // Eine Kollektion hat IMMER ein items-Array (zunächst leer, bis die
    // Kinder via _loadChildrenRecursively nachgeladen werden). Sonst
    // crasht z. B. die Validierung mit undefined.flatMap(...).
    items: dto.items?.map(toCollectionItem) ?? (dto.isCollection ? [] : undefined),
    order: dto.order,
    collectionId: dto.collectionId ?? null
  }
}

function toCollectionItem(dto: CollectionItemDTO): CollectionItem {
  return {
    id: dto.subItemId,
    collectionId: '',
    item: toItem(dto.item!),
    position: dto.position
  }
}

// ── State ─────────────────────────────────────────────────────────────────────

interface ExerciseState {
  rootItems: Item[]
  selectedItem: TreeItem | null
  variants: Item[]
  loading: boolean
  loadingContent: boolean
  error: string | null
  loadingChildrenIds: string[]
  allValidators: ValidatorDTO[]
}

// ── Store ─────────────────────────────────────────────────────────────────────

export const useExerciseStore = defineStore('exercise', {
  state: (): ExerciseState => ({
    rootItems: [],
    selectedItem: null,
    variants: [],
    loading: false,
    loadingContent: false,
    error: null,
    loadingChildrenIds: [],
    allValidators: []
  }),

  getters: {
    selectedInnerItem: (state): Item | null => {
      if (!state.selectedItem) return null
      return getInnerItem(state.selectedItem)
    },

    isCollectionSelected(): boolean {
      const inner = this.selectedInnerItem
      return inner ? checkIsCollection(inner) : false
    },

    selectedCollection(): Collection | null {
      const inner = this.selectedInnerItem
      return inner && checkIsCollection(inner) ? (inner as Collection) : null
    },

    isOrdered(): boolean {
      const coll = this.selectedCollection
      return coll ? coll.order === true : false
    }
  },

  actions: {
    // ── Notifications ─────────────────────────────────────────────────────────

    /** Push an API error string to the global notification store. */
    _notifyError(e: unknown) {
      const notifStore = useNotificationStore()
      const msg = (e as any)?.response?.data?.message || (e as any)?.response?.data || String(e)
      const status = (e as any)?.response?.status ? `[${(e as any).response.status}] ` : ''
      notifStore.push(status + JSON.stringify(msg), 'error', 12000)
    },

    // ── Initialisation (progressive loading) ──────────────────────────────────

    /**
     * Load the exercise tree progressively.
     *
     * Phase 1: `getRootItems()` — roots appear in the UI immediately.
     * Phase 2: `_loadChildrenRecursively()` — each collection's children
     * load in the background with a per-node spinner in the tree view.
     */
    async loadTree() {
      this.loading = true
      this.error = null
      try {
        const [dtos] = await Promise.all([
          _adapter!.getRootItems(),
          this.loadValidators()
        ])
        this.rootItems = dtos.map(toItem)
        // Fire background recursive loading — no await so UI shows roots now
        this._loadChildrenRecursively(this.rootItems)
      } catch (e) {
        this.error = String(e)
        this._notifyError(e)
      } finally {
        this.loading = false
      }
    },

    /**
     * Recursively load children for every collection in `items`.
     *
     * Each collection is flagged in `loadingChildrenIds` while its
     * children are being fetched. Sub-collections inside the loaded
     * children trigger their own fetch. All siblings at the same depth
     * load in parallel.
     */
    async _loadChildrenRecursively(items: Item[]) {
      const promises = items
        .filter((item) => item.item_type === 'collection' || checkIsCollection(item))
        .map(async (item) => {
          const collection = item as Collection
          // Kollektion-Endpunkte brauchen die item_collection_id, nicht die item_id.
          // Fehlt sie (z. B. optimistisch angelegt, Backend-Antwort noch unterwegs),
          // überspringen — der Reload liefert sie später.
          if (!collection.collectionId) return
          this.loadingChildrenIds = [...this.loadingChildrenIds, collection.id]
          try {
            const dtos = await _adapter!.getCollectionItems(collection.collectionId)
            collection.items = dtos.map(toCollectionItem)
            // Recurse into sub-collections
            await this._loadChildrenRecursively(collection.items.map((ci) => ci.item))
          } catch (e) {
            this._notifyError(e)
          } finally {
            this.loadingChildrenIds = this.loadingChildrenIds.filter((id) => id !== collection.id)
          }
        })
      await Promise.all(promises)
    },

    // ── Selection ─────────────────────────────────────────────────────────────

    selectItem(item: TreeItem) {
      this.selectedItem = item
      this.variants = []
      const inner = getInnerItem(item)
      this.loadItemContent(inner.id)
      if (!checkIsCollection(inner)) {
        const baseId = inner.rootItemId ?? inner.id
        this.loadVariants(baseId)
      }
    },

    async loadItemContent(itemId: string) {
      this.loadingContent = true
      try {
        const dtos = await _adapter!.getContents(itemId)
        if (dtos.length > 0) {
          const item = this._findItemById(itemId)
          if (item) {
            item.contents = dtos.map(toFullContent)
          }
        }
      } catch (e) {
        this._notifyError(e)
      } finally {
        this.loadingContent = false
      }
    },

    _findItemById(id: string, items?: Item[], visited?: Set<string>): Item | null {
      const searchItems = items ?? this.rootItems
      visited = visited ?? new Set()
      for (const item of searchItems) {
        if (visited.has(item.id)) continue
        visited.add(item.id)
        if (item.id === id) return item
        if (item.items) {
          for (const ci of item.items) {
            if (ci.item.id === id) return ci.item
            const found = this._findItemById(id, ci.item.items?.map((sci) => sci.item), visited)
            if (found) return found
          }
        }
      }
      return null
    },

    /**
     * Toggle a collection's `order` flag.
     *
     * Positions are KEPT in the data when order toggles off (the UI
     * simply hides them). When enabling, items without a position get
     * one assigned.
     */
    toggleCollectionOrder(collection: Collection) {
      collection.order = !collection.order
      if (collection.order) {
        collection.items.forEach((item, index) => {
          if (item.position == null) {
            item.position = index + 1
          }
        })
      }
      if (collection.collectionId) {
        _adapter?.toggleCollectionOrder(collection.collectionId, { order: collection.order })
          .catch((e) => this._notifyError(e))
      }
      this._syncOrderedCollectionItems(collection)
    },

    // ── Validator Actions ────────────────────────────────────────────────────

    async loadValidators() {
      try {
        this.allValidators = await _adapter!.getValidators()
      } catch (e) {
        this._notifyError(e)
      }
    },

    async createValidator(description: string, rule: string): Promise<ValidatorDTO | null> {
      try {
        const dto = await _adapter!.createValidator({ description, validator: rule })
        this.allValidators.push(dto)
        return dto
      } catch (e) {
        this._notifyError(e)
        return null
      }
    },

    async linkValidatorToSelectedItem(validatorId: string) {
      const inner = this.selectedInnerItem
      if (!inner) return
      if (inner.validators.includes(validatorId)) return
      inner.validators.push(validatorId)
      try {
        await _adapter!.addValidatorToItem(inner.id, validatorId)
      } catch (e) {
        inner.validators = inner.validators.filter((v: string) => v !== validatorId)
        this._notifyError(e)
      }
    },

    async unlinkValidatorFromSelectedItem(validatorId: string) {
      const inner = this.selectedInnerItem
      if (!inner) return
      inner.validators = inner.validators.filter((v: string) => v !== validatorId)
      try {
        await _adapter!.removeValidatorFromItem(inner.id, validatorId)
      } catch (e) {
        if (!inner.validators.includes(validatorId)) {
          inner.validators.push(validatorId)
        }
        this._notifyError(e)
      }
    },

    // ── Content Actions ───────────────────────────────────────────────────────

    addContentToSelectedItem() {
      const inner = this.selectedInnerItem
      if (!inner) return
      const now = Date.now().toString()
      const content: Content = {
        id: 'content-' + now,
        license: null,
        contentType: 'text',
        author: inner.author ?? 'author',
        tags: [],
        purpose: 'Neuer Inhalt',
        jsonContent: { text: '' },
        blobContent: ''
      }
      inner.contents.push(content)
      _adapter?.createContent(inner.id, {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).then((dto) => {
        if (dto) content.id = dto.itemContentId
      }).catch((e) => this._notifyError(e))
    },

    removeContentFromSelectedItem(index: number) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const removedId = inner.contents[index].id
      inner.contents.splice(index, 1)
      if (removedId) {
        _adapter?.deleteContent(removedId).catch((e) => this._notifyError(e))
      }
    },

    updateContentText(index: number, text: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      content.jsonContent.text = text
      _adapter?.updateContent(content.id ?? '', {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    updateContentPurpose(index: number, purpose: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      content.purpose = purpose
      _adapter?.updateContent(content.id ?? '', {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    async uploadBlob(index: number, file: File) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      const contentId = content.id
      if (!contentId) return
      try {
        await _adapter!.uploadBlob(contentId, file)
        content.blobMimeType = file.type
        content.blobContent = file.type.startsWith('image/') ? '(image)' : '(binary)'
      } catch (e) {
        this._notifyError(e)
      }
    },

    // ── Variants ────────────────────────────────────────────────────────────────

    async loadVariants(baseItemId: string) {
      this.variants = []
      try {
        const dtos = await _adapter!.getItemsByRootId(baseItemId)
        this.variants = dtos.map(toItem)
        await Promise.all(this.variants.map(async (v) => {
          const contentDtos = await _adapter!.getContents(v.id)
          if (contentDtos.length > 0) {
            v.contents = contentDtos.map(toFullContent)
          }
        }))
      } catch (e) {
        this._notifyError(e)
      }
    },

    async createVariant(baseItemId: string) {
      const variant = this._createItemData(baseItemId)
      this.variants.push(variant)
      try {
        const dto = await _adapter!.createItem({
          authorId: SEED_AUTHOR_ID,
          licenseId: SEED_LICENSE_ID,
          itemTypeId: SEED_ITEM_TYPE_ID,
          rootItemId: baseItemId
        })
        variant.id = dto.itemId
        if (variant.contents.length > 0) {
          const contentDto = await _adapter!.createContent(dto.itemId, {
            licenseId: SEED_LICENSE_ID,
            itemContentTypeId: SEED_CONTENT_TYPE_ID,
            authorId: SEED_AUTHOR_ID,
            purpose: variant.contents[0].purpose,
            jsonSerializedContent: JSON.stringify(variant.contents[0].jsonContent)
          })
          if (contentDto && variant.contents[0]) {
            variant.contents[0].id = contentDto.itemContentId
          }
        }
      } catch (e) {
        this._notifyError(e)
        this.variants = this.variants.filter((v) => v !== variant)
      }
    },

    updateVariantText(variantIndex: number, contentIndex: number, text: string) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      content.jsonContent.text = text
      _adapter?.updateContent(content.id ?? '', {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    updateVariantPurpose(variantIndex: number, contentIndex: number, purpose: string) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      content.purpose = purpose
      _adapter?.updateContent(content.id ?? '', {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    addVariantContent(variantIndex: number) {
      const variant = this.variants[variantIndex]
      if (!variant) return
      const now = Date.now().toString()
      const content: Content = {
        id: 'content-' + now,
        license: null,
        contentType: 'text',
        author: variant.author ?? 'author',
        tags: [],
        purpose: 'Neuer Inhalt',
        jsonContent: { text: '' },
        blobContent: ''
      }
      variant.contents.push(content)
      _adapter?.createContent(variant.id, {
        licenseId: SEED_LICENSE_ID,
        itemContentTypeId: SEED_CONTENT_TYPE_ID,
        authorId: SEED_AUTHOR_ID,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).then((dto) => {
        if (dto) content.id = dto.itemContentId
      }).catch((e) => this._notifyError(e))
    },

    removeVariantContent(variantIndex: number, contentIndex: number) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const removedId = variant.contents[contentIndex].id
      variant.contents.splice(contentIndex, 1)
      if (removedId) {
        _adapter?.deleteContent(removedId).catch((e) => this._notifyError(e))
      }
    },

    async uploadVariantBlob(variantIndex: number, contentIndex: number, file: File) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      const contentId = content.id
      if (!contentId) return
      try {
        await _adapter!.uploadBlob(contentId, file)
        content.blobMimeType = file.type
        content.blobContent = file.type.startsWith('image/') ? '(image)' : '(binary)'
      } catch (e) {
        this._notifyError(e)
      }
    },

    // ── Tree helpers ──

    /**
     * Remove an item from all collections and from rootItems.
     * @returns The ID of the parent collection the item was removed from, or null.
     */
    _detachItem(itemId: string, excludeId?: string): string | null {
      let parentId: string | null = null
      const removeFromCollections = (collections: Collection[]) => {
        collections.forEach((coll) => {
          const hadItem = coll.items.some((ci) => ci.item.id === itemId)
          if (hadItem) parentId = coll.id
          coll.items = coll.items.filter((ci) => ci.item.id !== itemId)
          const nested = coll.items.map((ci) => ci.item).filter(checkIsCollection)
          if (nested.length > 0) removeFromCollections(nested)
        })
      }
      removeFromCollections(this.rootItems.filter(checkIsCollection) as Collection[])
      this.rootItems = this.rootItems.filter((ri) => ri.id !== itemId || ri.id === excludeId)
      return parentId
    },

    /**
     * Walk the tree to find a collection by ID.
     */
    _findCollectionById(id: string, items?: Item[]): Collection | null {
      const searchItems = items ?? this.rootItems
      for (const item of searchItems) {
        if (item.id === id && checkIsCollection(item)) return item as Collection
        if (checkIsCollection(item)) {
          const coll = item as Collection
          const found = this._findCollectionById(id, coll.items.map((ci) => ci.item))
          if (found) return found
        }
      }
      return null
    },

    /** Build a minimal Item object with a unique ID and single Content block. */
    _createItemData(rootItemId: string | null = null): Item {
      const now = Date.now().toString()
      return {
        id: 'item-' + now,
        item_type: 'exercise',
        author: 'author',
        representationTemplate: null,
        license: null,
        tags: [],
        validators: [],
        modifiers: [],
        rootItemId,
        contents: [
          {
            id: 'content-' + now,
            license: null,
            contentType: 'text',
            author: 'author',
            tags: [],
            purpose: 'Neuer Inhalt',
            jsonContent: { text: '' },
            blobContent: ''
          }
        ]
      }
    },

    /**
     * After a mutation to an ordered collection, diff the current
     * positions against their expected sequential order and push
     * any changes to the API.
     *
     * Only fires when `collection.order === true`.
     */
    /**
     * Persist positions for an ordered collection.
     *
     * @param collection - The collection to sync.
     * @param force - When `true`, call the API for every item regardless
     * of whether its local position already matches. Used after DnD reorder
     * where positions are pre-set in `.map()` but still need to be persisted.
     */
    _syncOrderedCollectionItems(collection: Collection, force = false) {
      if (!collection.order || !_adapter || !collection.collectionId) return
      const collectionId = collection.collectionId
      collection.items.forEach((item, index) => {
        const expected = index + 1
        if (force || item.position !== expected) {
          _adapter!.updateCollectionItemPosition(collectionId, item.item.id, expected)
            .catch((e) => this._notifyError(e))
          item.position = expected
        }
      })
    },

    // ── CRUD Actions ──

    createItem(rootItemId: string | null = null, addToRoot = true, onCreated?: (realId: string) => void): Item {
      const item = this._createItemData(rootItemId)
      if (addToRoot) this.rootItems.push(item)
      _adapter?.createItem({
        authorId: SEED_AUTHOR_ID,
        licenseId: SEED_LICENSE_ID,
        itemTypeId: SEED_ITEM_TYPE_ID,
        rootItemId: item.rootItemId ?? null
      }).then((dto) => {
        if (dto) {
          item.id = dto.itemId
          onCreated?.(dto.itemId)
          if (item.contents.length > 0) {
            _adapter?.createContent(dto.itemId, {
              licenseId: SEED_LICENSE_ID,
              itemContentTypeId: SEED_CONTENT_TYPE_ID,
              authorId: SEED_AUTHOR_ID,
              purpose: item.contents[0].purpose,
              jsonSerializedContent: JSON.stringify(item.contents[0].jsonContent)
            }).then((contentDto) => {
              if (contentDto && item.contents[0]) {
                item.contents[0].id = contentDto.itemContentId
              }
            }).catch((e) => this._notifyError(e))
          }
        }
      }).catch((e) => this._notifyError(e))
      this.validate()
      return item
    },

    createCollection(): Collection {
      const now = Date.now().toString()
      const collection: Collection = {
        id: 'coll-' + now,
        item_type: 'collection',
        author: 'author',
        representationTemplate: null,
        license: null,
        tags: [],
        validators: [],
        modifiers: [],
        // Eine Kollektion ist im Backend eine Item ohne eigenen Content.
        contents: [],
        items: [],
        order: false
      }
      this.rootItems.push(collection)
      // Backend-Modell: erst eine Item anlegen, dann zur Kollektion machen
      // (POST /items → POST /items/{id}/collection). So taucht die Kollektion
      // beim Neuladen über GET /items?root=true wieder auf.
      _adapter?.createItem({
        authorId: SEED_AUTHOR_ID,
        licenseId: SEED_LICENSE_ID,
        itemTypeId: SEED_ITEM_TYPE_ID,
        rootItemId: null
      })
        .then((dto) => {
          if (!dto) return
          collection.id = dto.itemId
          return _adapter?.convertItemToCollection(dto.itemId)
        })
        .then((collDto) => {
          // item_collection_id merken — nötig für alle weiteren Collection-Calls
          if (collDto) collection.collectionId = collDto.collectionId ?? null
        })
        .catch((e) => this._notifyError(e))
      this.validate()
      return collection
    },

    addItemToCollection(collection: Collection): CollectionItem {
      const rootId = collection.rootItemId ?? null
      const item = this.createItem(rootId, false, (realId) => {
        if (collection.collectionId) {
          _adapter?.addItemToCollection(collection.collectionId, realId)
            .catch((e) => this._notifyError(e))
        }
      })
      const collectionItem: CollectionItem = {
        id: 'coll-item-' + Date.now().toString(),
        collectionId: collection.id,
        item,
        position: collection.order ? collection.items.length + 1 : null
      }
      collection.items.push(collectionItem)
      this._syncOrderedCollectionItems(collection)
      this.validate()
      return collectionItem
    },

    makeItemACollection(item: Item): Collection {
      if (checkIsCollection(item)) return item as Collection
      item.item_type = 'collection'
      item.items = []
      item.order = false
      _adapter?.convertItemToCollection(item.id)
        .then((dto) => {
          if (dto) item.collectionId = dto.collectionId ?? null
        })
        .catch((e) => this._notifyError(e))
      this.validate()
      return item as Collection
    },

    deleteItem(itemToDelete: Item) {
      const itemId = itemToDelete.id
      const parentId = this._detachItem(itemId)
      if (this.selectedItem && getInnerItem(this.selectedItem).id === itemId) {
        this.selectedItem = null
      }
      _adapter?.deleteItem(itemId).catch((e) => this._notifyError(e))
      if (parentId) {
        const parent = this._findCollectionById(parentId)
        if (parent) this._syncOrderedCollectionItems(parent)
      }
      this.validate()
    },

    deleteCollection(collectionToDelete: Collection) {
      const itemsToDelete = [...collectionToDelete.items]
      this.deleteItem(collectionToDelete)
      itemsToDelete.forEach((ci) => this.deleteItem(ci.item))
    },

    // ── DnD / Reorder Actions ──

    updateCollectionItems(collection: Collection, newItems: TreeItem[]) {
      collection.items = newItems.map((item, index) => {
        const inner = getInnerItem(item)
        if (inner.id !== collection.id) {
          const oldParentId = this._detachItem(inner.id, collection.id)
          if (oldParentId) {
            if (oldParentId !== collection.id) {
              // Cross-collection move: remove from source, add to target
              // (Collection-Calls über item_collection_id, nicht item_id)
              const oldParent = this._findCollectionById(oldParentId)
              if (oldParent?.collectionId) {
                _adapter?.removeItemFromCollection(oldParent.collectionId, inner.id)
                  .catch((e) => this._notifyError(e))
              }
              if (collection.collectionId) {
                _adapter?.addItemToCollection(collection.collectionId, inner.id)
                  .catch((e) => this._notifyError(e))
              }
              if (oldParent) this._syncOrderedCollectionItems(oldParent)
            }
            // oldParentId === collection.id → item stayed, skip API (reorder only)
          } else {
            // Root-to-collection move: add to target
            if (collection.collectionId) {
              _adapter?.addItemToCollection(collection.collectionId, inner.id)
                .catch((e) => this._notifyError(e))
            }
          }
        }
        inner.rootItemId = collection.rootItemId ?? null
        if (isCollectionItem(item)) {
          return { ...item, collectionId: collection.id, position: collection.order ? index + 1 : null } as CollectionItem
        }
        return {
          id: 'coll-item-' + Date.now().toString() + '-' + index,
          collectionId: collection.id,
          item: inner,
          position: collection.order ? index + 1 : null
        } as CollectionItem
      })
      this._syncOrderedCollectionItems(collection, true)
      this.validate()
    },

    updateRootItems(newItems: TreeItem[]) {
      const mapped = newItems.map((item) => {
        const inner = getInnerItem(item)
        const parentId = this._detachItem(inner.id)
        // Moved from a collection to root: notify backend
        if (parentId) {
          const oldParent = this._findCollectionById(parentId)
          if (oldParent?.collectionId) {
            _adapter?.removeItemFromCollection(oldParent.collectionId, inner.id)
              .catch((e) => this._notifyError(e))
          }
          if (oldParent) this._syncOrderedCollectionItems(oldParent)
        }
        return inner as Item
      })
      this.rootItems = mapped
      this.validate()
    },

    // ── Validation ──

    validate() {
      const issues = validateTreeData(this.rootItems)
      if (issues.length === 0) return
      const notifStore = useNotificationStore()
      for (const issue of issues) {
        notifStore.push(issue.message, 'error', 8000)
      }
    }
  }
})
