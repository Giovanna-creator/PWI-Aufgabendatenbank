<template>
  <v-card class="adb-structure-card">
    <v-card-title class="adb-structure-title">
      <span>Struktur</span>
    </v-card-title>
    <v-card-text class="adb-structure-text pa-0">
      <v-list
        density="compact"
        nav
      >
        <AdbTreeItem
          :items="items"
          :is-draggable="true"
          @update-items="updateRootItems"
        />
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { inject, computed, type Ref } from 'vue'
import AdbTreeItem from './components/AdbTreeItem.vue'
import type { Item, Collection, CollectionItem } from '@/lib/types'

const adb = inject<{ 
  rootItems: Ref<(Item | Collection)[]>,
  updateRootItems: (newItems: (Item | Collection)[]) => void,
  getInnerItem: (element: Item | Collection | CollectionItem) => Item
}>('adb')

const items = computed(() => {
  if (!adb?.rootItems?.value) return []
  return adb.rootItems.value.filter(item => {
    const inner = adb.getInnerItem(item)
    // Hide items that have a rootItemId (Implementation B children OR items in collections)
    if (inner.rootItemId) return false
    
    return true
  })
})

const updateRootItems = (newItems: (Item | Collection)[]) => {
  adb?.updateRootItems(newItems)
}
</script>

<style lang="scss" scoped>
.adb-structure-card {
  height: 100%;
  width: 100%;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
}

.adb-structure-title {
  font-weight: 700;
  font-size: 1rem; /* text-md */
  border-bottom: 1px solid #e5e7eb; /* border-b */
  padding-bottom: 0.5rem; /* pb-2 */
}

.adb-structure-text {
  padding: 1rem; /* p-4 */
  font-size: 1rem; /* text-base */
  color: #374151; /* text-gray-700 */
}
</style>
