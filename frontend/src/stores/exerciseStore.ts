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

    itemTitle(): string {
      const inner = this.selectedInnerItem
      return inner?.contents?.[0]?.jsonContent?.text ?? ''
    },

    isOrdered(): boolean {
      const coll = this.selectedCollection
      return coll ? coll.order === true : false
    }
  },

  actions: {
    selectItem(item: TreeItem) {
      this.selectedItem = item
    },

    setItemTitle(title: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents?.[0]) return
      inner.contents[0].jsonContent.text = title
    },

    toggleCollectionOrder(collection: Collection) {
      collection.order = !collection.order
      collection.items.forEach((item, index) => {
        item.position = collection.order ? index + 1 : null
      })
    },

    // ── helpers ──

    /**
     * Entfernt ein Item aus allen Kollektionen und aus rootItems.
     * excludeId: Dieses Item (meist der Parent) wird beim Filter übersprungen,
     * damit es nicht versehentlich aus rootItems gelöscht wird.
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
      this.rootItems = this.rootItems.filter((ri) => ri.id !== itemId || ri.id === excludeId)
    },

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

    createItem(rootItemId: string | null = null, addToRoot = true): Item {
      const item = this._createItemData(rootItemId)
      if (addToRoot) this.rootItems.push(item)
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

    makeItemACollection(item: Item): Collection {
      if (checkIsCollection(item)) return item as Collection
      item.item_type = 'collection'
      item.items = []
      item.order = false
      return item as Collection
    },

    deleteItem(itemToDelete: Item) {
      const itemId = itemToDelete.id
      this._detachItem(itemId)
      const subTasks = [...this.rootItems.filter((ri) => ri.rootItemId === itemId)]
      subTasks.forEach((st) => this.deleteItem(st))
      if (this.selectedItem && getInnerItem(this.selectedItem).id === itemId) {
        this.selectedItem = null
      }
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
          this._detachItem(inner.id, collection.id)
        }
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

    updateItemChildren(parent: Item, children: TreeItem[]) {
      const parentId = parent.id
      const normalized = children.map((child) => {
        const item = getInnerItem(child)
        if (item.id !== parent.id) {
          this._detachItem(item.id, parent.id)
        }
        item.rootItemId = parentId
        return item as Item
      })
      const otherItems = this.rootItems.filter(
        (item) => item.rootItemId !== parentId || item.id === parent.id
      )
      this.rootItems = [...otherItems, ...normalized]
    },

    updateRootItems(newItems: TreeItem[]) {
      const mapped = newItems.map((item) => {
        const inner = getInnerItem(item)
        this._detachItem(inner.id)
        if (!checkIsCollection(inner)) inner.rootItemId = null
        return inner as Item
      })
      const mappedIds = new Set(mapped.map((i) => i.id))
      const preserved = this.rootItems.filter((i) => i.rootItemId && mappedIds.has(i.rootItemId))
      const orphaned = this.rootItems.filter((i) => i.rootItemId && !mappedIds.has(i.rootItemId))
      this.rootItems = [...mapped, ...preserved, ...orphaned]
    }
  }
})
