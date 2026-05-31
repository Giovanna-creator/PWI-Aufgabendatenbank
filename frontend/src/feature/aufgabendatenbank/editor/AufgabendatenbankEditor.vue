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
import { isCollection as checkIsCollection, getInnerItem, type Item, type Collection, type CollectionItem } from '@/lib/types'

const adb = inject<{ 
  selectedItem: Ref<Item | Collection | CollectionItem | null>,
  toggleCollectionOrder: (collection: Collection) => void
}>('adb')

const selectedItem = computed<Item | Collection | CollectionItem | null>(() => adb?.selectedItem?.value || null)

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
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
}

.adb-editor-title {
  font-weight: 700;
  font-size: 1rem; /* text-md */
  border-bottom: 1px solid #e5e7eb; /* border-b */
  padding-bottom: 0.5rem; /* pb-2 */
}

.adb-editor-text {
  padding: 1rem; /* p-4 */
  font-size: 1rem; /* text-base */
  color: #374151; /* text-gray-700 */
}
</style>
