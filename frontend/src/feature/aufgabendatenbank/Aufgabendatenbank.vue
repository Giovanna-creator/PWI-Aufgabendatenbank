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
import { useAdbActions } from './composables/useAdbActions'
import AufgabendatenbankStructure from './structure/AufgabendatenbankStructure.vue'
import AufgabendatenbankEditor from './editor/AufgabendatenbankEditor.vue'
import AufgabendatenbankToolbar from './toolbar/AufgabendatenbankToolbar.vue'

// --- Local Data State ---
const rootItems = ref<(Item | Collection)[]>([])
const selectedItem = ref<Item | Collection | CollectionItem | null>(null)

const {
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
} = useAdbActions(rootItems, selectedItem)

provide('adb', {
  rootItems,
  selectedItem,
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
