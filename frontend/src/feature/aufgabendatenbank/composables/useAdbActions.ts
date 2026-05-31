import { type Ref } from 'vue'
import {
  type Collection,
  type CollectionItem,
  getInnerItem,
  isCollection as checkIsCollection,
  isCollectionItem,
  type Item,
  type TreeItem
} from '@/lib/types'

export function useAdbActions(rootItems: Ref<Item[]>, selectedItem: Ref<TreeItem | null>) {
  // --- HELPERS ---
  const getItemId = (item: TreeItem): string => getInnerItem(item).id

  /**
   * Detaches an item from its current parent (either a collection or root items)
   */
  const detachItem = (itemId: string, excludeId?: string) => {
    // 1. Remove from all collections
    const recursiveRemove = (collections: Collection[]) => {
      collections.forEach(coll => {
        coll.items = coll.items.filter(ci => ci.item.id !== itemId)
        const nestedCollections = coll.items
          .map(ci => ci.item)
          .filter(checkIsCollection)
        if (nestedCollections.length > 0) recursiveRemove(nestedCollections)
      })
    }
    recursiveRemove(rootItems.value.filter(checkIsCollection))

    // 2. Remove from root items
    rootItems.value = rootItems.value.filter(ri => ri.id !== itemId || ri.id === excludeId)
  }

  // --- ACTIONS ---
  const selectItem = (item: TreeItem) => {
    selectedItem.value = item
  }

  const createItem = (rootItemId: string | null = null, addToRoot = true) => {
    const newItem: Item = {
      id: 'item-' + Date.now().toString(),
      item_type: 'exercise',
      author: 'author',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: rootItemId,
      contents: [{
        id: 'content-' + Date.now().toString(),
        license: null,
        contentType: 'text',
        author: 'author',
        tags: [],
        purpose: 'title',
        jsonContent: { text: `New Task` },
        blobContent: ''
      }]
    }
    if (addToRoot) rootItems.value.push(newItem)
    return newItem
  }

  const createCollection = () => {
    const newCollection: Collection = {
      id: 'coll-' + Date.now().toString(),
      item_type: 'collection',
      author: 'author',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: [{
        id: 'content-coll-' + Date.now().toString(),
        license: null,
        contentType: 'text',
        author: 'author',
        tags: [],
        purpose: 'title',
        jsonContent: { text: `New Collection` },
        blobContent: ''
      }],
      items: [],
      order: false
    }
    rootItems.value.push(newCollection)
    return newCollection
  }

  const addItemToCollection = (collection: Collection) => {
    const newItem = createItem(null, false)
    const collectionItem: CollectionItem = {
      id: 'coll-item-' + Date.now().toString(),
      collectionId: collection.id,
      item: newItem,
      position: collection.order ? collection.items.length + 1 : null
    }
    collection.items.push(collectionItem)
    return collectionItem
  }

  const toggleCollectionOrder = (collection: Collection) => {
    collection.order = !collection.order
    collection.items.forEach((item, index) => {
      item.position = collection.order ? index + 1 : null
    })
  }

  const makeItemACollection = (item: Item): Collection => {
    if (checkIsCollection(item)) return item
    
    item.item_type = 'collection'
    item.items = []
    item.order = false
    
    return item as Collection
  }

  const deleteItem = (itemToDelete: Item) => {
    const itemId = itemToDelete.id
    detachItem(itemId)

    // Remove subtasks (Implementation B)
    // We create a copy of the list to avoid issues with array modification during iteration
    const subTasks = [...rootItems.value.filter(ri => ri.rootItemId === itemId)]
    subTasks.forEach(deleteItem)

    if (selectedItem.value && getItemId(selectedItem.value) === itemId) {
      selectedItem.value = null
    }
  }

  const deleteCollection = (collectionToDelete: Collection) => {
    const itemsToDelete = [...collectionToDelete.items]
    deleteItem(collectionToDelete)
    itemsToDelete.forEach(ci => deleteItem(ci.item))
  }

  // --- UPDATES / DRAG-AND-DROP ---
  const updateCollectionItems = (collection: Collection, newItems: TreeItem[]) => {
    // 1. Map to CollectionItems and normalize
    collection.items = newItems.map((item, index) => {
      const inner = getInnerItem(item)
      if (inner.id !== collection.id) {
        detachItem(inner.id, collection.id)
      }
      inner.rootItemId = null // Implementation A items should not have rootItemId
      
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
  }

  const updateItemChildren = (parent: Item, children: TreeItem[]) => {
    const parentId = parent.id

    // 1. Clean up and normalize
    const normalizedChildren = children.map(child => {
      const item = getInnerItem(child)
      if (item.id !== parent.id) {
        detachItem(item.id, parent.id)
      }
      item.rootItemId = parentId
      return item as Item
    })

    // 2. Update global state
    const otherItems = rootItems.value.filter(item => item.rootItemId !== parentId || item.id === parent.id)
    rootItems.value = [...otherItems, ...normalizedChildren]
  }

  const updateRootItems = (newItems: TreeItem[]) => {
    const mapped = newItems.map(item => {
      const inner = getInnerItem(item)
      detachItem(inner.id)
      if (!checkIsCollection(inner)) inner.rootItemId = null
      return inner as Item
    })
    const mappedIds = new Set(mapped.map(i => i.id))
    const preserved = rootItems.value.filter(i => i.rootItemId && mappedIds.has(i.rootItemId))
    // Keep orphaned children whose parent moved to a collection
    const orphaned = rootItems.value.filter(i => i.rootItemId && !mappedIds.has(i.rootItemId))
    rootItems.value = [...mapped, ...preserved, ...orphaned]
  }

  return {
    selectItem,
    createItem,
    createCollection,
    addItemToCollection,
    toggleCollectionOrder,
    makeItemACollection,
    deleteItem,
    deleteCollection,
    updateCollectionItems,
    updateItemChildren,
    updateRootItems,
    getInnerItem,
    isCollectionItem,
    checkIsCollection
  }
}
