<template>
  <v-card class="adb-structure-card">
    <v-card-title class="adb-structure-title">
      <span>Struktur</span>
    </v-card-title>
    <v-card-text class="adb-structure-text pa-0">
      <v-list density="compact" nav>
        <AdbTreeItem
          :items="adb?.rootItems"
          :is-draggable="true"
          @update-items="updateRootItems"
        />
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import AdbTreeItem from './components/AdbTreeItem.vue'

const adb = inject<any>('adb')

const updateRootItems = (newItems: any[]) => {
  if (adb && adb.rootItems) {
    adb.rootItems.value = newItems
  }
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
