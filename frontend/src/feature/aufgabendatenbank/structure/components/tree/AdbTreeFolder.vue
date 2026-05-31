<template>
  <v-list-group 
    :value="element.id" 
    v-model="internalIsOpen"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    :class="{ 'drop-target': isDragOver }"
  >
    <template #activator="{ props: groupProps, isOpen }">
      <v-list-item
        v-bind="groupProps"
        :active="adb?.selectedItem.value?.id === element.id"
        @click="onSelect(isOpen)"
      >
        <template #prepend>
          <div class="d-flex align-center">
            <v-icon
              :icon="isOpen ? 'mdi-chevron-down' : 'mdi-chevron-right'"
              class="mr-1"
            />
            <v-icon :class="{ 'ml-9': isSubItem && !element.order }">
              {{ element.order ? 'mdi-format-list-numbered' : (isSubItem ? 'mdi-folder-outline' : 'mdi-folder') }}
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
              <v-list-item @click="adb?.addItemToCollection(element)">
                <template #prepend>
                  <v-icon>mdi-plus</v-icon>
                </template>
                <v-list-item-title>Aufgabe hinzufügen</v-list-item-title>
              </v-list-item>
              <v-list-item @click="adb?.toggleCollectionOrder(element)">
                <template #prepend>
                  <v-icon>{{ element.order ? 'mdi-order-alphabetical-ascending' : 'mdi-format-list-numbered' }}</v-icon>
                </template>
                <v-list-item-title>{{ element.order ? 'Reihenfolge entfernen' : 'Reihenfolge aktivieren' }}</v-list-item-title>
              </v-list-item>
              <v-divider />
              <v-list-item
                color="error"
                @click="adb?.deleteCollection(element)"
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
    <slot />
  </v-list-group>
</template>

<script setup lang="ts">
import { inject, ref, type Ref } from 'vue'
import type { Item, Collection, CollectionItem } from '@/lib/types'

const internalIsOpen = ref(false)
const isDragOver = ref(false)
let expansionTimeout: any = null

const onDragOver = () => {
  isDragOver.value = true
  if (!internalIsOpen.value && !expansionTimeout) {
    expansionTimeout = setTimeout(() => {
      internalIsOpen.value = true
      expansionTimeout = null
    }, 800) // 800ms delay to auto-expand
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
  element: Collection
  title: string
  position?: number | null
  isSubItem?: boolean
}>()

const emit = defineEmits(['toggle-expand'])

const adb = inject<{
  selectedItem: Ref<Item | Collection | CollectionItem | null>,
  selectItem: (item: Item | Collection | CollectionItem) => void,
  addItemToCollection: (collection: Collection) => void,
  toggleCollectionOrder: (collection: Collection) => void,
  deleteCollection: (collection: Collection) => void,
  getInnerItem: (element: Item | Collection | CollectionItem) => Item
}>('adb')

const onSelect = (isOpen: boolean) => {
  adb?.selectItem(props.element)
  if (!isOpen !== internalIsOpen.value) {
    emit('toggle-expand')
  }
}
</script>

<style scoped>
.drop-target {
  background-color: rgba(25, 118, 210, 0.05);
}
</style>