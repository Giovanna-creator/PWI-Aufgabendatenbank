<template>
  <div class="adb-container">
    <!-- Top bar for actions and options -->
    <AufgabendatenbankToolbar />

    <!-- Main content area -->
    <div
      ref="containerRef"
      class="adb-content"
    >
      <!-- Sidebar for item structure -->
      <div 
        class="adb-sidebar" 
        :style="{ width: sidebarWidth + 'px' }"
      >
        <AufgabendatenbankStructure />
      </div>

      <!-- Resizer handle -->
      <div 
        class="adb-resizer" 
        @mousedown="startResizing"
      />

      <!-- Main editor area -->
      <div class="adb-editor">
        <AufgabendatenbankEditor />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, provide, onUnmounted } from 'vue'
import type { Item, TreeItem } from '@/lib/types'
import { useAdbActions } from './composables/useAdbActions'
import { dummyData } from './dummy-data'
import AufgabendatenbankStructure from './structure/AufgabendatenbankStructure.vue'
import AufgabendatenbankEditor from './editor/AufgabendatenbankEditor.vue'
import AufgabendatenbankToolbar from './toolbar/AufgabendatenbankToolbar.vue'

// --- Resizing Logic ---
const sidebarWidth = ref(300)
const isResizing = ref(false)
const containerRef = ref<HTMLElement | null>(null)

const startResizing = () => {
  isResizing.value = true
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', stopResizing)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

const handleMouseMove = (event: MouseEvent) => {
  if (!isResizing.value || !containerRef.value) return
  
  const containerRect = containerRef.value.getBoundingClientRect()
  const newWidth = event.clientX - containerRect.left
  
  // Constraints
  if (newWidth > 150 && newWidth < containerRect.width - 200) {
    sidebarWidth.value = newWidth
  }
}

const stopResizing = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', stopResizing)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

onUnmounted(() => {
  stopResizing()
})

// --- Local Data State ---
const rootItems = ref<Item[]>(dummyData.rootItems)
const selectedItem = ref<TreeItem | null>(null)

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
  height: calc(100vh - 64px); /* Subtract header height */
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #1e1e1e;
}

.adb-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.adb-sidebar {
  height: 100%;
  flex-shrink: 0;
  overflow: hidden;
}

.adb-resizer {
  width: 4px;
  cursor: col-resize;
  background-color: transparent;
  transition: background-color 0.2s;
  z-index: 10;
  margin: 0 -2px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 2px;
    right: 1px;
    background-color: transparent;
    transition: background-color 0.2s;
  }

  &:hover::after, &:active::after {
    background-color: #007fd4;
  }
}

.adb-editor {
  flex: 1;
  height: 100%;
  overflow: hidden;
  min-width: 0;
}
</style>
