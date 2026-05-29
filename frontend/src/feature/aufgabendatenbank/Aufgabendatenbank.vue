<template>
  <div class="adb-container">
    <!-- Top bar for actions and options -->
    <AufgabendatenbankToolbar />

    <!-- Main content area -->
    <div class="adb-content">
      <!-- Sidebar for item structure -->
      <div class="adb-sidebar">
        <AufgabendatenbankStructure />
      </div>

      <!-- Main editor area -->
      <div class="adb-editor">
        <AufgabendatenbankEditor />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'
import type { Item, Collection, CollectionItem } from '@/lib/types'
import AufgabendatenbankStructure from './structure/AufgabendatenbankStructure.vue'
import AufgabendatenbankEditor from './editor/AufgabendatenbankEditor.vue'
import AufgabendatenbankToolbar from './toolbar/AufgabendatenbankToolbar.vue'

// --- Local Data State ---
const rootItems = ref<(Item | Collection)[]>([])
const selectedItem = ref<Item | Collection | null>(null)

const selectItem = (item: Item | Collection) => {
  selectedItem.value = item
}

const createItem = (rootItemId: string | null = null) => {
  const newItem: Item = {
    id: 'item-' + Date.now().toString(),
    item_type: 'exercise',
    author: 'author',
    representationTemplate: null,
    license: null,
    tags: [],
    validators: [],
    modifiers: [],
    rootItem: null,
    rootItemId: rootItemId,
    contents: [
      {
        id: 'content-' + Date.now().toString(),
        license: null,
        contentType: 'text',
        author: 'author',
        tags: [],
        purpose: 'title',
        jsonContent: { text: `New Task` },
        blobContent: ''
      }
    ]
  }
  if (!rootItemId) {
    rootItems.value.push(newItem)
  }
  return newItem
}

const createCollection = () => {
  const parentItem: Item = {
    id: 'item-coll-' + Date.now().toString(),
    item_type: 'collection_parent',
    author: 'author',
    representationTemplate: null,
    license: null,
    tags: [],
    validators: [],
    modifiers: [],
    rootItem: null,
    contents: [
      {
        id: 'content-coll-' + Date.now().toString(),
        license: null,
        contentType: 'text',
        author: 'author',
        tags: [],
        purpose: 'title',
        jsonContent: { text: `New Collection` },
        blobContent: ''
      }
    ]
  }

  const newCollection: Collection = {
    id: 'coll-' + Date.now().toString(),
    parent: parentItem,
    items: [],
    order: false
  }

  rootItems.value.push(newCollection)
  return newCollection
}

const addItemToCollection = (collection: Collection) => {
  const newItem = createItem()
  const collectionItem: CollectionItem = {
    id: 'coll-item-' + Date.now().toString(),
    collectionId: collection.id,
    item: newItem,
    position: collection.order ? collection.items.length + 1 : null
  }
  collection.items.push(collectionItem)
}

provide('adb', {
  rootItems,
  selectedItem,
  selectItem,
  createItem,
  createCollection,
  addItemToCollection
} as const)

/*
 * Feature logic for Aufgabendatenbank goes here.
 *
 * PURPOSE:
 * This view is the central hub for the creation and management of
 * exercises (Items) and collections. It provides the core interface
 * for structuring, organizing, and editing tasks and task-sequences
 * throughout the platform.
 */
</script>

<style lang="scss" scoped>
.adb-container {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.adb-content {
  display: flex;
}

.adb-sidebar {
  width: 33.333333%;
  height: 100%;
}

.adb-editor {
  width: 66.666667%;
  height: 100%;
}
</style>
