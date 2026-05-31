<template>
  <v-list-group
    v-model="internalIsOpen"
    :value="element.id"
    :class="{ 'drop-target': isDragOver, 'tree-folder-group': true }"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDragLeave"
  >
    <template #activator="{ props: groupProps, isOpen }">
      <v-list-item
        v-bind="groupProps"
        :active="adb?.selectedItem.value?.id === element.id"
        class="tree-folder"
        density="compact"
        min-height="22"
        color="white"
        @click="onSelect"
      >
        <template #prepend>
          <div class="tree-node-icons">
            <v-icon
              size="16"
              :icon="isOpen ? 'mdi-chevron-down' : 'mdi-chevron-right'"
              class="expansion-icon"
            />
            <v-icon
              size="18"
              class="type-icon"
            >
              {{ element.order ? 'mdi-format-list-numbered' : (isSubItem ? 'mdi-folder-outline' : 'mdi-folder') }}
            </v-icon>
          </div>
        </template>
        <v-list-item-title class="tree-node-title">
          <span
            v-if="position"
            class="position-label"
          >{{ position }}.</span>
          {{ title }}
        </v-list-item-title>
        <template #append>
          <v-menu location="bottom end">
            <template #activator="{ props: menuProps }">
              <v-btn
                icon="mdi-dots-vertical"
                variant="text"
                size="x-small"
                v-bind="menuProps"
                class="action-btn"
                @click.stop
              />
            </template>
            <v-list density="compact">
              <v-list-item @click="adb?.addItemToCollection(element)">
                <template #prepend>
                  <v-icon size="small">
                    mdi-plus
                  </v-icon>
                </template>
                <v-list-item-title>Aufgabe hinzufügen</v-list-item-title>
              </v-list-item>
              <v-list-item @click="adb?.toggleCollectionOrder(element)">
                <template #prepend>
                  <v-icon size="small">
                    {{ element.order ? 'mdi-order-alphabetical-ascending' : 'mdi-format-list-numbered' }}
                  </v-icon>
                </template>
                <v-list-item-title>{{ element.order ? 'Reihenfolge entfernen' : 'Reihenfolge aktivieren' }}</v-list-item-title>
              </v-list-item>
              <v-divider />
              <v-list-item
                color="error"
                @click="adb?.deleteCollection(element)"
              >
                <template #prepend>
                  <v-icon
                    color="error"
                    size="small"
                  >
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
    <div class="folder-children">
      <slot />
    </div>
  </v-list-group>
</template>

<script setup lang="ts">
import { inject, ref, type Ref } from 'vue'
import type { Collection, Item, TreeItem } from '@/lib/types'

const internalIsOpen = ref(false)
const isDragOver = ref(false)
let expansionTimeout: any = null

const onDragOver = () => {
  isDragOver.value = true
  if (!internalIsOpen.value && !expansionTimeout) {
    expansionTimeout = setTimeout(() => {
      internalIsOpen.value = true
      emit('toggle-expand')
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
  selectedItem: Ref<TreeItem | null>,
  selectItem: (item: TreeItem) => void,
  addItemToCollection: (collection: Collection) => void,
  toggleCollectionOrder: (collection: Collection) => void,
  deleteCollection: (collection: Collection) => void,
  getInnerItem: (element: TreeItem) => Item
}>('adb')

const onSelect = () => {
  adb?.selectItem(props.element)
  emit('toggle-expand')
}
</script>

<style scoped lang="scss">
.tree-folder-group {
  :deep(.v-list-group__items) {
    --indent-padding: 12px !important;
  }
}

.tree-folder {
  padding-inline-start: 8px !important;
  margin-bottom: 0 !important;
  border-radius: 0 !important;
  transition: none !important;
  cursor: pointer;
  color: #cccccc !important;
  position: relative;
  z-index: 5;

  &:hover {
    background-color: #2a2d2e !important;

    .action-btn {
      opacity: 1;
    }
  }

  &.v-list-item--active {
    background-color: #37373d !important;
    color: #ffffff !important;

    &::before {
      opacity: 0 !important;
    }
  }
}

.tree-node-icons {
  display: flex;
  align-items: center;
  margin-right: 6px;
}

.expansion-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  color: #cccccc;
}

.type-icon {
  color: #cccccc;
}

.tree-node-title {
  font-size: 13px !important;
  line-height: 22px;
}

.position-label {
  color: #cccccc;
  font-weight: 600;
  margin-right: 4px;
}

.action-btn {
  opacity: 0;
  transition: opacity 0.1s;
}

.folder-children {
  position: relative;

  &::before {
    content: "";
    position: absolute;
    left: 15px;
    top: 0;
    bottom: 0;
    width: 1px;
    background-color: #5a5a5a;
    pointer-events: none;
    z-index: 10;
  }
}

:deep(.v-list-group__items) {
  position: relative;
  z-index: 0;
}

.drop-target {
  background-color: rgba(0, 127, 212, 0.1);
  box-shadow: inset 0 0 0 1px #007fd4;
}
</style>