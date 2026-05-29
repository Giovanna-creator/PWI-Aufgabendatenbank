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

const createItem = () => {
  const newItem: Item = {
    id: 'item-' + Date.now().toString(),
    item_type: 'todo',
    author: 'author',
    representationTemplate: 'todo',
    license: 'todo',
    tags: 'todo',
    validators: 'todo',
    modifiers: 'todo',
    rootItem: null,
    contents: [
      {
        id: 'content-' + Date.now().toString(),
        license: 'todo',
        contentType: 'todo',
        author: 'todo',
        tags: 'todo',
        purpose: 'title',
        jsonContent: { text: `New Task ${rootItems.value.length + 1}` },
        blobContent: ''
      }
    ]
  }
  rootItems.value.push(newItem)
}

const createCollection = () => {
  const parentItem: Item = {
    id: 'item-coll-' + Date.now().toString(),
    item_type: 'collection',
    author: 'author',
    representationTemplate: 'todo',
    license: 'todo',
    tags: 'todo',
    validators: 'todo',
    modifiers: 'todo',
    rootItem: null,
    contents: [
      {
        id: 'content-coll-' + Date.now().toString(),
        license: 'todo',
        contentType: 'todo',
        author: 'todo',
        tags: 'todo',
        purpose: 'title',
        jsonContent: { text: `New Collection ${rootItems.value.length + 1}` },
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
}

provide('adb', {
  rootItems,
  selectedItem,
  selectItem,
  createItem,
  createCollection
})

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
