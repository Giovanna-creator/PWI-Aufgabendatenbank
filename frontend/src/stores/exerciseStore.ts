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
  CollectionItemDTO
} from '@/feature/aufgabendatenbank/api-adapter.types'

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

function toContent(dto: ContentDTO): Content {
  return {
    id: dto.id,
    license: dto.license,
    contentType: dto.contentType,
    author: dto.author,
    tags: [],
    purpose: dto.purpose,
    jsonContent: dto.jsonContent as Record<string, any>,
    blobContent: dto.blobContent
  }
}

function toItem(dto: ItemDTO): Item {
  return {
    id: dto.id,
    item_type: dto.item_type,
    author: dto.author,
    representationTemplate: dto.representationTemplate ?? null,
    license: dto.license ?? null,
    tags: [],
    validators: [],
    modifiers: [],
    rootItemId: dto.rootItemId ?? null,
    contents: (dto.contents ?? []).map(toContent),
    items: dto.items?.map(toCollectionItem),
    order: dto.order
  }
}

function toCollectionItem(dto: CollectionItemDTO): CollectionItem {
  return {
    id: dto.id,
    collectionId: dto.collectionId,
    item: toItem(dto.item),
    position: dto.position
  }
}

// ── State ─────────────────────────────────────────────────────────────────────

interface ExerciseState {
  rootItems: Item[]
  selectedItem: TreeItem | null
  loading: boolean
  error: string | null
  loadingChildrenIds: string[]
}

// ── Store ─────────────────────────────────────────────────────────────────────

export const useExerciseStore = defineStore('exercise', {
  state: (): ExerciseState => ({
    rootItems: [],
    selectedItem: null,
    loading: false,
    error: null,
    loadingChildrenIds: []
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
      notifStore.push(String(e), 'error', 8000)
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
        const dtos = await _adapter!.getRootItems()
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
          this.loadingChildrenIds = [...this.loadingChildrenIds, collection.id]
          try {
            const dtos = await _adapter!.getCollectionItems(collection.id)
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
      _adapter?.updateCollection(collection.id, { order: collection.order })
        .catch((e) => this._notifyError(e))
      this._syncOrderedCollectionItems(collection)
    },

    // ── Content Actions ───────────────────────────────────────────────────────

    addContentToSelectedItem() {
      const inner = this.selectedInnerItem
      if (!inner) return
      const now = Date.now().toString()
      inner.contents.push({
        id: 'content-' + now,
        license: null,
        contentType: 'text',
        author: inner.author ?? 'author',
        tags: [],
        purpose: 'Neuer Inhalt',
        jsonContent: { text: '' },
        blobContent: ''
      })
    },

    removeContentFromSelectedItem(index: number) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      inner.contents.splice(index, 1)
    },

    updateContentText(index: number, text: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      inner.contents[index].jsonContent.text = text
    },

    updateContentPurpose(index: number, purpose: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      inner.contents[index].purpose = purpose
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
      if (!collection.order || !_adapter) return
      collection.items.forEach((item, index) => {
        const expected = index + 1
        if (force || item.position !== expected) {
          _adapter!.updateCollectionItemPosition(collection.id, item.id, expected)
            .catch((e) => this._notifyError(e))
          item.position = expected
        }
      })
    },

    // ── CRUD Actions ──

    createItem(rootItemId: string | null = null, addToRoot = true): Item {
      const item = this._createItemData(rootItemId)
      if (addToRoot) this.rootItems.push(item)
      _adapter?.createItem({
        item_type: item.item_type,
        author: item.author,
        rootItemId: item.rootItemId ?? null,
        contents: item.contents.map((c) => ({
          license: c.license,
          contentType: c.contentType,
          author: c.author,
          purpose: c.purpose,
          jsonContent: c.jsonContent as Record<string, unknown>,
          blobContent: c.blobContent
        }))
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
        contents: [
          {
            id: 'content-coll-' + now,
            license: null,
            contentType: 'text',
            author: 'author',
            tags: [],
            purpose: 'Neuer Inhalt',
            jsonContent: { text: '' },
            blobContent: ''
          }
        ],
        items: [],
        order: false
      }
      this.rootItems.push(collection)
      _adapter?.createCollection({
        item_type: 'collection',
        author: collection.author,
        contents: collection.contents.map((c) => ({
          license: c.license,
          contentType: c.contentType,
          author: c.author,
          purpose: c.purpose,
          jsonContent: c.jsonContent as Record<string, unknown>,
          blobContent: c.blobContent
        })),
        order: false
      }).catch((e) => this._notifyError(e))
      this.validate()
      return collection
    },

    addItemToCollection(collection: Collection): CollectionItem {
      const rootId = collection.rootItemId ?? collection.id
      const item = this.createItem(rootId, false)
      const collectionItem: CollectionItem = {
        id: 'coll-item-' + Date.now().toString(),
        collectionId: collection.id,
        item,
        position: collection.order ? collection.items.length + 1 : null
      }
      collection.items.push(collectionItem)
      _adapter?.addItemToCollection(collection.id, item.id)
        .catch((e) => this._notifyError(e))
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
              _adapter?.removeItemFromCollection(oldParentId, inner.id)
                .catch((e) => this._notifyError(e))
              _adapter?.addItemToCollection(collection.id, inner.id)
                .catch((e) => this._notifyError(e))
              const oldParent = this._findCollectionById(oldParentId)
              if (oldParent) this._syncOrderedCollectionItems(oldParent)
            }
            // oldParentId === collection.id → item stayed, skip API (reorder only)
          } else {
            // Root-to-collection move: add to target
            _adapter?.addItemToCollection(collection.id, inner.id)
              .catch((e) => this._notifyError(e))
          }
        }
        inner.rootItemId = collection.rootItemId ?? collection.id
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
          _adapter?.removeItemFromCollection(parentId, inner.id)
            .catch((e) => this._notifyError(e))
          const oldParent = this._findCollectionById(parentId)
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
