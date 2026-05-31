<template>
  <v-list-item
    :active="adb?.selectedItem.value?.id === element.id"
    @click="adb?.selectItem(element); emit('toggle-expand')"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    :class="{ 'drop-target': isDragOver }"
  >
    <template #prepend>
      <div class="d-flex align-center">
        <v-icon
          v-if="hasChildren"
          :icon="isOpen ? 'mdi-chevron-down' : 'mdi-chevron-right'"
          class="mr-1"
        />
        <v-icon :class="{ 'ml-9': !hasChildren }">
          {{ isSubItem ? 'mdi-file-document-outline' : 'mdi-file-document' }}
        </v-icon>
      </div>
    </template>
    <v-list-item-title>
      <span
        v-if="position"
        class="mr-2 text-primary font-weight-bold"
      >{{ position }}.</span>
      {{ title }}
    </v-list-item-title>
    <template #append>
      <v-menu location="bottom end">
        <template #activator="{ props: menuProps }">
          <v-btn
            icon="mdi-dots-vertical"
            variant="text"
            size="small"
            v-bind="menuProps"
            @click.stop
          />
        </template>
        <v-list density="compact">
          <v-list-item @click="onMakeCollection">
            <template #prepend>
              <v-icon>mdi-folder-plus</v-icon>
            </template>
            <v-list-item-title>In Kollektion umwandeln</v-list-item-title>
          </v-list-item>
          <v-divider />
          <v-list-item
            color="error"
            @click="onDelete"
          >
            <template #prepend>
              <v-icon color="error">
                mdi-delete
              </v-icon>
            </template>
            <v-list-item-title class="text-error">
              Löschen
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import { inject, ref, type Ref } from 'vue'
import type { Item, Collection, CollectionItem } from '@/lib/types'

const isDragOver = ref(false)
let expansionTimeout: any = null

const onDragOver = () => {
  isDragOver.value = true
  if (!props.isOpen && !expansionTimeout) {
    expansionTimeout = setTimeout(() => {
      emit('toggle-expand')
      expansionTimeout = null
    }, 800)
  }
}

const onDragLeave = () => {
  isDragOver.value = false
  if (expansionTimeout) {
    clearTimeout(expansionTimeout)
    expansionTimeout = null
  }
}

const props = defineProps<{
  element: Item | CollectionItem
  title: string
  position?: number | null
  isSubItem?: boolean
  hasChildren?: boolean
  isOpen?: boolean
}>()

const emit = defineEmits(['toggle-expand'])

const adb = inject<{
  selectedItem: Ref<Item | Collection | CollectionItem | null>,
  selectItem: (item: Item | Collection | CollectionItem) => void,
  createItem: (rootItemId?: string | null) => Item,
  makeItemACollection: (item: Item) => Collection,
  deleteItem: (item: Item) => void,
  getInnerItem: (element: Item | Collection | CollectionItem) => Item
}>('adb')

const onMakeCollection = () => {
  const item = adb?.getInnerItem(props.element)
  if (item) adb?.makeItemACollection(item)
}

const onDelete = () => {
  const item = adb?.getInnerItem(props.element)
  if (item) adb?.deleteItem(item)
}
</script>

<style scoped>
.drop-target {
  background-color: rgba(25, 118, 210, 0.05);
}
</style>