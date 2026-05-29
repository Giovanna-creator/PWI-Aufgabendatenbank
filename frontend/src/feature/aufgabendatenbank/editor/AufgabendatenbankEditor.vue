<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="selectedItem">
        <h3 class="text-h6 mb-2">Edit: {{ itemTitle }}</h3>

        <v-text-field
          v-model="itemTitle"
          label="Titel / Aufgabe"
          variant="outlined"
          class="mb-4"
        ></v-text-field>

        <v-switch
          v-if="selectedItem.item_type === 'collection' || selectedItem.parent"
          v-model="isOrdered"
          label="Kollektion sequenziell (geordnet) machen"
          color="primary"
          @change="toggleOrder"
        ></v-switch>

        <p class="text-caption text-grey">ID: {{ selectedItem.id }}</p>
      </div>
      <div v-else>
        Bitte wählen Sie eine Aufgabe oder Kollektion aus dem Struktur-Baum aus.
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue'

const adb = inject<any>('adb')
const selectedItem = computed(() => adb?.selectedItem?.value)

const itemTitle = computed({
  get: () => {
    const item = selectedItem.value
    if (!item) return ''
    if (item.parent) return item.parent.contents[0]?.jsonContent?.text || ''
    if (item.item) return item.item.contents[0]?.jsonContent?.text || ''
    return item.contents?.[0]?.jsonContent?.text || ''
  },
  set: (val) => {
    const item = selectedItem.value
    if (!item) return
    if (item.parent && item.parent.contents[0]) {
      item.parent.contents[0].jsonContent.text = val
    } else if (item.item && item.item.contents[0]) {
      item.item.contents[0].jsonContent.text = val
    } else if (item.contents && item.contents[0]) {
      item.contents[0].jsonContent.text = val
    }
  }
})

const isOrdered = computed({
  get: () => {
    return selectedItem.value?.order === true
  },
  set: (val: boolean) => {
    if (selectedItem.value) {
      selectedItem.value.order = val
      updatePositions(selectedItem.value)
    }
  }
})

const toggleOrder = () => {
  // Is handled in setter
}

const updatePositions = (collection: any) => {
  if (collection && collection.items) {
    collection.items.forEach((child: any, index: number) => {
      child.position = collection.order ? index + 1 : null
    })
  }
}
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
