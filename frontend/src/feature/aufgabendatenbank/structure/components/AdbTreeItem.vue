<template>
  <Draggable
    v-bind="dragOptions"
    v-model="internalItems"
    item-key="id"
    class="tree-draggable"
    :disabled="!isDraggable"
  >
    <template #item="{ element }">
      <div class="tree-node">
        <AdbTreeFolder
          v-if="isCollection(element)"
          :element="getCollection(element)"
          :title="getTitle(element)"
          :position="getPosition(element)"
          :is-sub-item="isSubItem(element)"
        >
          <!-- Items inside the collection -->
          <AdbTreeItem
            :items="getCollection(element).items"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateCollectionChildren(getCollection(element), newItems)"
          />
        </AdbTreeFolder>

        <div v-else>
          <AdbTreeFile
            :element="getInnerItem(element)"
            :title="getTitle(element)"
            :position="getPosition(element)"
            :is-sub-item="isSubItem(element)"
          />
        </div>
      </div>
    </template>
  </Draggable>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import draggable from 'vuedraggable'
import AdbTreeFile from './tree/AdbTreeFile.vue'
import AdbTreeFolder from './tree/AdbTreeFolder.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { type Collection, type TreeItem } from '@/lib/types'
import { getInnerItem, isCollectionItem, isCollection as checkIsCollection } from '@/lib/types'

const Draggable = draggable
const store = useExerciseStore()

const props = defineProps<{
  items: TreeItem[]
  isDraggable?: boolean
}>()

const emit = defineEmits(['update-items'])

/** Two-way binding bridge for vuedraggable. */
const internalItems = computed({
  get: () => props.items,
  set: (val) => emit('update-items', val)
})

/** Options passed to the vuedraggable component. */
const dragOptions = computed(() => ({
  animation: 200,
  group: 'tree',
  disabled: false,
  ghostClass: 'ghost',
  itemKey: 'id'
}))

/** Cast an unknown element to TreeItem. */
const toItem = (element: unknown): TreeItem => element as TreeItem

/** Type guard: true when the element's inner item is a Collection. */
const isCollection = (element: unknown): element is Collection => {
  return checkIsCollection(getInnerItem(toItem(element)))
}

/** Get the Collection from an element (assumes it is one). */
const getCollection = (element: unknown): Collection => {
  return getInnerItem(toItem(element)) as Collection
}

/** Derive the display title from the element's first Content block. */
const getTitle = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  const first = inner?.contents?.[0]?.jsonContent?.text
  if (first != null && first !== '') return first
  return inner?.item_type === 'collection' ? 'New Collection' : 'New Task'
}

/** Get the position of a CollectionItem, or null for root-level items. */
const getPosition = (element: unknown): number | null | undefined => {
  const e = toItem(element)
  return isCollectionItem(e) ? e.position : null
}

/** True when the element lives inside a collection (i.e. is nested). */
const isSubItem = (element: unknown): boolean => {
  const e = toItem(element)
  return isCollectionItem(e) && !!e.collectionId
}

/** Forward DnD-updated children to the store for a Collection. */
const updateCollectionChildren = (element: Collection, newItems: TreeItem[]) => {
  store.updateCollectionItems(element, newItems)
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: var(--adb-bg-hover);
}

.tree-draggable {
  padding-left: 24px;
  min-height: 4px;
}

.nested-drop-zone {
  min-height: 4px;
}
</style>
