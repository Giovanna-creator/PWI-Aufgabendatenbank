<template>
  <v-list-item
    :active="adb?.selectedItem.value?.id === element.id"
    :class="{ 'drop-target': isDragOver, 'tree-file': true }"
    density="compact"
    min-height="22"
    color="white"
    @click="onClick"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDragLeave"
  >
    <template #prepend>
      <div class="tree-node-icons">
        <v-icon
          v-if="hasChildren"
          size="16"
          :icon="isOpen ? 'mdi-chevron-down' : 'mdi-chevron-right'"
          class="expansion-icon"
        />
        <div
          v-else
          class="expansion-spacer"
        />
        <v-icon
          size="18"
          class="type-icon"
        >
          {{ hasChildren ? (isSubItem ? 'mdi-folder-outline' : 'mdi-folder') : (isSubItem ? 'mdi-file-document-outline' : 'mdi-file-document') }}
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
          <v-list-item @click="onMakeCollection">
            <template #prepend>
              <v-icon size="small">
                mdi-folder-plus
              </v-icon>
            </template>
            <v-list-item-title>In Kollektion umwandeln</v-list-item-title>
          </v-list-item>
          <v-divider />
          <v-list-item
            color="error"
            @click="onDelete"
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

<script setup lang="ts">
import { inject, ref, type Ref } from 'vue'
import type { Item, Collection, TreeItem } from '@/lib/types'

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
  element: TreeItem
  title: string
  position?: number | null
  isSubItem?: boolean
  hasChildren?: boolean
  isOpen?: boolean
}>()

const emit = defineEmits(['toggle-expand'])

const adb = inject<{
  selectedItem: Ref<TreeItem | null>,
  selectItem: (item: TreeItem) => void,
  createItem: (rootItemId?: string | null) => Item,
  makeItemACollection: (item: Item) => Collection,
  deleteItem: (item: Item) => void,
  getInnerItem: (element: TreeItem) => Item
}>('adb')

const onClick = () => {
  adb?.selectItem(props.element)
  if (props.hasChildren) {
    emit('toggle-expand')
  }
}

const onMakeCollection = () => {
  const item = adb?.getInnerItem(props.element)
  if (item) adb?.makeItemACollection(item)
}

const onDelete = () => {
  const item = adb?.getInnerItem(props.element)
  if (item) adb?.deleteItem(item)
}
</script>

<style scoped lang="scss">
.tree-file {
  padding-inline-start: 8px !important;
  margin-bottom: 0 !important;
  border-radius: 0 !important;
  transition: none !important;
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

.expansion-spacer {
  width: 20px;
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

.drop-target {
  background-color: rgba(0, 127, 212, 0.1);
  box-shadow: inset 0 0 0 1px #007fd4;
}
</style>