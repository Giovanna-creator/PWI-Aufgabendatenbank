<template>
  <div class="adb-structure-container">
    <div class="adb-structure-header">
      <span class="text-uppercase text-caption font-weight-bold">Struktur</span>
    </div>
    <div class="adb-structure-content">
      <v-list
        density="compact"
        class="tree-root"
      >
        <AdbTreeItem
          :items="items"
          :is-draggable="true"
          @update-items="updateRootItems"
        />
      </v-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AdbTreeItem from './components/AdbTreeItem.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()

const items = computed(() =>
  store.rootItems.filter((item) => !item.rootItemId)
)

const updateRootItems = (newItems: typeof store.rootItems) => {
  store.updateRootItems(newItems)
}
</script>

<style lang="scss" scoped>
.adb-structure-container {
  height: 100%;
  width: 100%;
  background-color: #252526;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #333333;
  color: #cccccc;
}

.adb-structure-header {
  padding: 8px 16px;
  font-size: 0.75rem;
  line-height: 16px;
  color: #969696;
  user-select: none;
  background-color: #252526;
  border-bottom: 1px solid #333333;
}

.adb-structure-content {
  flex: 1;
  overflow-y: auto;
}

.tree-root {
  background: transparent !important;
  padding: 0 !important;
  user-select: none;
}

:deep(.v-list) {
  background: transparent !important;
}
</style>
