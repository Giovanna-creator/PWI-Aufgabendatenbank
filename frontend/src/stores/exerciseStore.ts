import { defineStore } from 'pinia'
import { dummyData } from '@/feature/aufgabendatenbank/dummy-data'
import {
  type Item,
  type Collection,
  type CollectionItem,
  type TreeItem,
  getInnerItem,
  isCollection as checkIsCollection,
  isCollectionItem
} from '@/lib/types'

interface State {
  rootItems: Item[]
  selectedItem: TreeItem | null
}

export const useExerciseStore = defineStore('exercise', {
  state: (): State => ({
    rootItems: [...dummyData.rootItems],
    selectedItem: null
  }),

  getters: {
    /** The raw Item in the selected tree node (unwraps CollectionItem if needed). */
    selectedInnerItem: (state): Item | null => {
      if (!state.selectedItem) return null
      return getInnerItem(state.selectedItem)
    },

    /** Whether the selected tree node is a Collection. */
    isCollectionSelected(): boolean {
      const inner = this.selectedInnerItem
      return inner ? checkIsCollection(inner) : false
    },

    /** The selected Collection, or null if none or not a Collection. */
    selectedCollection(): Collection | null {
      const inner = this.selectedInnerItem
      return inner && checkIsCollection(inner) ? (inner as Collection) : null
    },

    /** The display title from the first Content block of the selected item. */
    itemTitle(): string {
      const inner = this.selectedInnerItem
      return inner?.contents?.[0]?.jsonContent?.text ?? ''
    },

    /** Whether the selected collection is ordered (children have sequential positions). */
    isOrdered(): boolean {
      const coll = this.selectedCollection
      return coll ? coll.order === true : false
    }
  },

  actions: {
    /** Select a tree node as the active/editor target. */
    selectItem(item: TreeItem) {
      this.selectedItem = item
    },

    /** Update the first content block's title text on the selected item. */
    setItemTitle(title: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents?.[0]) return
      inner.contents[0].jsonContent.text = title
    },

    /** Toggle a collection's `order` flag and reassign/clear positions on its children. */
    toggleCollectionOrder(collection: Collection) {
      collection.order = !collection.order
      collection.items.forEach((item, index) => {
        item.position = collection.order ? index + 1 : null
      })
    },

    // ── helpers ──

    /**
     * Remove an item from all collections and from rootItems.
     * @param excludeId - If set, this ID is not removed from rootItems (to preserve the parent).
     */
    _detachItem(itemId: string, excludeId?: string) {
      const removeFromCollections = (collections: Collection[]) => {
        collections.forEach((coll) => {
          coll.items = coll.items.filter((ci) => ci.item.id !== itemId)
          const nested = coll.items.map((ci) => ci.item).filter(checkIsCollection)
          if (nested.length > 0) removeFromCollections(nested)
        })
      }
      removeFromCollections(this.rootItems.filter(checkIsCollection))
      // Keep excludeId around (the parent) so moving inside the same parent doesn't remove it
      this.rootItems = this.rootItems.filter((ri) => ri.id !== itemId || ri.id === excludeId)
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
            purpose: 'title',
            jsonContent: { text: 'New Task' },
            blobContent: ''
          }
        ]
      }
    },

    // ── CRUD Actions ──

    /** Create a new exercise item and optionally append it to rootItems. */
    createItem(rootItemId: string | null = null, addToRoot = true): Item {
      const item = this._createItemData(rootItemId)
      if (addToRoot) this.rootItems.push(item)
      return item
    },

    /** Create a new empty collection and push it to rootItems. */
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
            purpose: 'title',
            jsonContent: { text: 'New Collection' },
            blobContent: ''
          }
        ],
        items: [],
        order: false
      }
      this.rootItems.push(collection)
      return collection
    },

    /** Create a new exercise item and add it to a collection, assigning position if ordered. */
    addItemToCollection(collection: Collection): CollectionItem {
      const item = this.createItem(null, false)
      const collectionItem: CollectionItem = {
        id: 'coll-item-' + Date.now().toString(),
        collectionId: collection.id,
        item,
        position: collection.order ? collection.items.length + 1 : null
      }
      collection.items.push(collectionItem)
      return collectionItem
    },

    /** Convert a plain Item into a Collection by setting item_type and adding children arrays. */
    makeItemACollection(item: Item): Collection {
      if (checkIsCollection(item)) return item as Collection
      item.item_type = 'collection'
      item.items = []
      item.order = false
      return item as Collection
    },

    /**
     * Delete an item and all its children (linked via rootItemId).
     * Also removes it from any collection it belongs to.
     */
    deleteItem(itemToDelete: Item) {
      const itemId = itemToDelete.id
      this._detachItem(itemId)
      // Snapshot before deleting, since deleteItem mutates rootItems
      const subTasks = [...this.rootItems.filter((ri) => ri.rootItemId === itemId)]
      subTasks.forEach((st) => this.deleteItem(st))
      if (this.selectedItem && getInnerItem(this.selectedItem).id === itemId) {
        this.selectedItem = null
      }
    },

    deleteCollection(collectionToDelete: Collection) {
      // Snapshot because deleteItem will mutate collectionToDelete.items
      const itemsToDelete = [...collectionToDelete.items]
      this.deleteItem(collectionToDelete)
      itemsToDelete.forEach((ci) => this.deleteItem(ci.item))
    },

    // ── DnD / Reorder Actions ──

    /**
     * Replace a collection's children with a reordered/dragged set.
     * Detaches items that moved into this collection from elsewhere.
     */
    updateCollectionItems(collection: Collection, newItems: TreeItem[]) {
      collection.items = newItems.map((item, index) => {
        const inner = getInnerItem(item)
        // Items dragged in from elsewhere need to leave their old parent first
        if (inner.id !== collection.id) {
          this._detachItem(inner.id, collection.id)
        }
        // Once inside a collection, rootItemId referencing is no longer needed
        inner.rootItemId = null
        if (isCollectionItem(item)) {
          return { ...item, collectionId: collection.id, position: collection.order ? index + 1 : null }
        }
        return {
          id: 'coll-item-' + Date.now().toString() + '-' + index,
          collectionId: collection.id,
          item: inner,
          position: collection.order ? index + 1 : null
        }
      })
    },

    /**
     * Replace the children (rootItemId-based) of a parent item.
     * Detaches moved items and preserves unrelated rootItems.
     */
    updateItemChildren(parent: Item, children: TreeItem[]) {
      const parentId = parent.id
      const normalized = children.map((child) => {
        const item = getInnerItem(child)
        // If item was inside a collection, detach it before linking as rootItemId child
        if (item.id !== parent.id) {
          this._detachItem(item.id, parent.id)
        }
        item.rootItemId = parentId
        return item as Item
      })
      // Replace only the children of this parent, keep everything else untouched
      const otherItems = this.rootItems.filter(
        (item) => item.rootItemId !== parentId || item.id === parent.id
      )
      this.rootItems = [...otherItems, ...normalized]
    },

    /**
     * Replace rootItems after a top-level DnD reorder.
     * Preserves child items (rootItemId-based) whose parent is still in the new list.
     */
    updateRootItems(newItems: TreeItem[]) {
      const mapped = newItems.map((item) => {
        const inner = getInnerItem(item)
        this._detachItem(inner.id)
        if (!checkIsCollection(inner)) inner.rootItemId = null
        return inner as Item
      })
      const mappedIds = new Set(mapped.map((i) => i.id))
      // Children whose parent is still in the new list stay attached
      const preserved = this.rootItems.filter((i) => i.rootItemId && mappedIds.has(i.rootItemId))
      // Orphans (parent removed from tree) are kept at the end so they don't disappear
      const orphaned = this.rootItems.filter((i) => i.rootItemId && !mappedIds.has(i.rootItemId))
      this.rootItems = [...mapped, ...preserved, ...orphaned]
    }
  }
})
