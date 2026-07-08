<template>
  <div class="adb-filters">
    <v-text-field
      v-model="localSearch"
      density="compact"
      variant="outlined"
      placeholder="Suche..."
      prepend-inner-icon="mdi-magnify"
      hide-details
      clearable
      class="adb-filter-search"
      @update:model-value="onSearchChange"
    />

    <v-select
      v-model="localAuthorId"
      :items="authorItems"
      item-title="title"
      item-value="value"
      density="compact"
      variant="outlined"
      label="Autor"
      hide-details
      clearable
      class="adb-filter-select"
      @update:model-value="store.setFilterAuthorId($event ?? null)"
    />

    <v-select
      v-model="localItemTypeId"
      :items="typeItems"
      item-title="title"
      item-value="value"
      density="compact"
      variant="outlined"
      label="Typ"
      hide-details
      clearable
      class="adb-filter-select"
      @update:model-value="store.setFilterItemTypeId($event ?? null)"
    />

    <div class="adb-filter-tag-tree">
      <AdbTagFilter />
    </div>

    <v-btn
      v-if="store.hasActiveFilters || store.tagFilter"
      icon="mdi-close"
      variant="text"
      density="compact"
      size="small"
      title="Filter zurücksetzen"
      class="adb-filter-clear"
      @click="clearAll"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AdbTagFilter from '../structure/components/AdbTagFilter.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()

const localSearch = ref(store.searchQuery)
const localAuthorId = ref<string | null>(store.filterAuthorId)
const localItemTypeId = ref<string | null>(store.filterItemTypeId)

const authorItems = computed(() =>
  store.authors.map((a) => ({
    title: a.descriptor,
    value: a.id
  }))
)

const typeItems = computed(() =>
  store.itemTypes.map((t) => ({
    title: t.name,
    value: t.id
  }))
)

let lastSearch = ''
function onSearchChange(val: unknown) {
  const v = typeof val === 'string' ? val : ''
  localSearch.value = v
  if (v !== lastSearch) {
    lastSearch = v
    store.setSearchQuery(v)
  }
}

function clearAll() {
  localSearch.value = ''
  localAuthorId.value = null
  localItemTypeId.value = null
  lastSearch = ''
  store.clearFilters()
  store.setTagFilter(null)
}
</script>

<style lang="scss" scoped>
.adb-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background-color: #1e1e1e;
  border-bottom: 1px solid #333333;
  flex-wrap: wrap;
}

.adb-filter-search {
  max-width: 220px;
  min-width: 140px;
}

.adb-filter-select {
  max-width: 180px;
  min-width: 120px;
}

.adb-filter-tag-tree {
  min-width: 180px;
  max-width: 260px;
}

.adb-filter-tag-tree :deep(.tag-filter-field) {
  margin-top: 0;
}

.adb-filter-clear {
  flex-shrink: 0;
}

:deep(.v-field) {
  background-color: #2d2d2d !important;
  color: #cccccc !important;
}

:deep(.v-field__input) {
  color: #cccccc !important;
  font-size: 0.8125rem;
}

:deep(.v-field--variant-outlined .v-field__outline) {
  color: #555555 !important;
}
</style>
