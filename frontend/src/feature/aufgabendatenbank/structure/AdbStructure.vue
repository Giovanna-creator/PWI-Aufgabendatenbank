<template>
  <div class="adb-structure-container">
    <div class="adb-structure-header">
      <span class="text-uppercase text-caption font-weight-bold">Struktur</span>
      <span
        v-if="store.hasActiveFilters || store.tagFilter"
        class="text-caption text-disabled"
      >
        (gefiltert)
      </span>
    </div>
    <div class="adb-structure-content">
      <!-- When filters are active, show flat result list -->
      <template v-if="store.hasActiveFilters">
        <div
          v-if="store.filtering"
          class="adb-filter-status"
        >
          <v-progress-circular
            indeterminate
            size="16"
            width="2"
            class="mr-2"
          />
          Suche...
        </div>
        <v-list
          v-else-if="filteredItems.length > 0"
          density="compact"
          class="tree-root"
        >
          <v-list-item
            v-for="item in filteredItems"
            :key="item.itemId"
            :title="itemTitle(item)"
            :subtitle="item.authorDescriptor"
            density="compact"
            class="adb-filter-result-item"
            :active="store.selectedItem ? getInnerItem(store.selectedItem).id === item.itemId : false"
            @click="selectFilteredItem(item)"
          >
            <template #prepend>
              <v-icon
                :icon="item.isCollection ? 'mdi-folder' : 'mdi-file-document-outline'"
                size="small"
              />
            </template>
          </v-list-item>
        </v-list>
        <div
          v-else
          class="adb-filter-status"
        >
          Keine Ergebnisse
        </div>
      </template>

      <!-- Normal tree view -->
      <v-list
        v-else
        density="compact"
        class="tree-root"
      >
        <AdbTreeItem
          :items="items"
          :is-draggable="!store.tagFilter"
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
import { getInnerItem } from '@/lib/types'
import type { ItemDTO } from '@/feature/aufgabendatenbank/api-adapter.types'

const store = useExerciseStore()

/** Root-level items visible in the tree (respecting the active tag filter). */
const items = computed(() => store.visibleRootItems)

const filteredItems = computed(() => store.filteredItems ?? [])

function itemTitle(dto: ItemDTO): string {
  const name = dto.itemTypeName || (dto.isCollection ? 'Sammlung' : 'Aufgabe')
  return name
}

function selectFilteredItem(dto: ItemDTO) {
  // Convert DTO to store Item for selection
  const item = {
    id: dto.itemId,
    item_type: dto.isCollection ? 'collection' : 'exercise',
    author: dto.authorDescriptor,
    representationTemplate: dto.itemTemplateId ?? null,
    license: dto.licenseName ?? null,
    tags: dto.tagIds ?? [],
    validators: dto.validatorIds ?? [],
    modifiers: dto.modifierIds ?? [],
    rootItemId: dto.rootItemId ?? null,
    contents: [],
    items: dto.isCollection ? [] : undefined,
    order: dto.order,
    collectionId: dto.collectionId ?? null,
    authorId: dto.authorId ?? null,
    licenseId: dto.licenseId ?? null,
    itemTypeId: dto.itemTypeId ?? null,
    itemTypeName: dto.itemTypeName ?? null
  }
  store.selectItem(item)
}

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

.tag-filter {
  margin-top: 8px;
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

/* Suchergebnis-Liste (flache Ansicht bei aktiven Filtern) */
.adb-filter-status {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  font-size: 13px;
  color: #969696;
}

.adb-filter-result-item {
  color: #cccccc !important;

  &:hover {
    background-color: #2a2d2e !important;
  }

  &.v-list-item--active {
    background-color: #37373d !important;
  }
}

.adb-filter-result-item :deep(.v-list-item-title) {
  color: #cccccc !important;
  font-size: 13px;
}

.adb-filter-result-item :deep(.v-list-item-subtitle) {
  color: #8a8a8a !important;
  font-size: 11px;
  opacity: 1;
}

.adb-filter-result-item :deep(.v-icon) {
  color: #b0b0b0 !important;
}
</style>
