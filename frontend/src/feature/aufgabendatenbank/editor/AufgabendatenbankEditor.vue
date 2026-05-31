<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="selectedItem">
        <h3 class="text-h6 mb-2">
          Edit: {{ itemTitle }}
        </h3>

        <v-text-field
          v-model="itemTitle"
          label="Titel / Aufgabe"
          variant="outlined"
          class="mb-4"
        />

        <v-switch
          v-if="isCollection"
          v-model="isOrdered"
          label="Kollektion sequenziell (geordnet) machen"
          color="primary"
        />

        <p class="text-caption text-grey">
          ID: {{ selectedItem.id }}
        </p>
      </div>
      <div v-else>
        Bitte wählen Sie eine Aufgabe oder Kollektion aus dem Struktur-Baum aus.
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { inject, computed, type Ref } from 'vue'
import { isCollection as checkIsCollection, getInnerItem, type Collection, type TreeItem } from '@/lib/types'

const adb = inject<{ 
  selectedItem: Ref<TreeItem | null>,
  toggleCollectionOrder: (collection: Collection) => void
}>('adb')

const selectedItem = computed<TreeItem | null>(() => adb?.selectedItem?.value || null)

const isCollection = computed(() => {
  return selectedItem.value ? checkIsCollection(getInnerItem(selectedItem.value)) : false
})

const itemTitle = computed({
  get: () => {
    const item = selectedItem.value
    if (!item) return ''
    const innerItem = getInnerItem(item)
    return innerItem.contents?.[0]?.jsonContent?.text || ''
  },
  set: (val) => {
    const item = selectedItem.value
    if (!item) return
    const innerItem = getInnerItem(item)
    if (innerItem.contents && innerItem.contents[0]) {
      innerItem.contents[0].jsonContent.text = val
    }
  }
})

const isOrdered = computed({
  get: () => {
    const item = selectedItem.value
    if (!item) return false
    const innerItem = getInnerItem(item)
    return checkIsCollection(innerItem) && innerItem.order === true
  },
  set: (val: boolean) => {
    const item = selectedItem.value
    if (!item) return
    const innerItem = getInnerItem(item)
    if (checkIsCollection(innerItem)) {
      if (innerItem.order !== val) {
        adb?.toggleCollectionOrder(innerItem)
      }
    }
  }
})
</script>

<style lang="scss" scoped>
.adb-editor-card {
  height: 100%;
  width: 100%;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.adb-editor-title {
  font-weight: 500;
  font-size: 0.75rem;
  line-height: 16px;
  text-transform: uppercase;
  color: #969696;
  border-bottom: 1px solid #333333;
  padding: 8px 16px !important;
  background-color: #252526;
  user-select: none;
}

.adb-editor-text {
  padding: 20px;
  font-size: 13px;
  color: #cccccc;
}

:deep(.v-text-field) {
  .v-field {
    background-color: #3c3c3c !important;
    color: #cccccc !important;
    border-radius: 2px;
  }
  .v-label {
    color: #969696 !important;
  }
}
</style>
