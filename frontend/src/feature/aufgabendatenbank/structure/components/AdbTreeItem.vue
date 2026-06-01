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
          @toggle-expand="toggleExpand(getId(element))"
        >
          <!-- Items inside the collection -->
          <AdbTreeItem
            :items="getCollection(element).items"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateCollectionChildren(getCollection(element), newItems)"
          />

          <!-- Items linked via rootItemId, shown beneath the same collection node -->
          <AdbTreeItem
            v-if="isExpanded(getId(element))"
            :items="getChildren(element)"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateItemChildren(element, newItems)"
          />
        </AdbTreeFolder>

        <!-- Non-collection items: either a plain exercise or one with rootItemId children -->
        <div v-else>
          <AdbTreeFile
            :element="getInnerItem(element)"
            :title="getTitle(element)"
            :position="getPosition(element)"
            :is-sub-item="isSubItem(element)"
            :has-children="hasChildren(element)"
            :is-open="isExpanded(getId(element))"
            @toggle-expand="toggleExpand(getId(element))"
          />

          <!-- Items linked via rootItemId -->
          <AdbTreeItem
            v-if="isExpanded(getId(element))"
            :items="getChildren(element)"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateItemChildren(element, newItems)"
          />
        </div>
      </div>
    </template>
  </Draggable>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
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

/** Tracks which tree node IDs are expanded. */
const expandedItems = ref<Record<string, boolean>>({})

/** Toggle the expanded state of a node by its ID. */
const toggleExpand = (id: string) => {
  expandedItems.value[id] = !expandedItems.value[id]
}

/** Whether a given node ID is currently expanded. */
const isExpanded = (id: string) => !!expandedItems.value[id]

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

/** Extract the ID from a tree element. */
const getId = (element: unknown): string => {
  return toItem(element).id
}

/** Derive the display title from the element's first Content block. */
const getTitle = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.contents?.[0]?.jsonContent?.text || (inner?.item_type === 'collection' ? 'New Collection' : 'New Task')
}

/** Get the position of a CollectionItem, or null for root-level items. */
const getPosition = (element: unknown): number | null | undefined => {
  const e = toItem(element)
  return isCollectionItem(e) ? e.position : null
}

/** True when the element lives inside a collection or has a rootItemId (i.e. is nested). */
const isSubItem = (element: unknown): boolean => {
  const e = toItem(element)
  const inner = getInnerItem(e)
  if (inner.rootItemId) return true
  if (isCollectionItem(e)) return !!e.collectionId
  return false
}

/** True when the element has children linked via rootItemId. */
const hasChildren = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.id ? store.rootItems.some((ri) => ri.rootItemId === inner.id) : false
}

/** Get children whose rootItemId matches the element's ID. */
const getChildren = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.id ? store.rootItems.filter((ri) => ri.rootItemId === inner.id) : []
}

/** Forward DnD-updated children to the store for a Collection. */
const updateCollectionChildren = (element: Collection, newItems: TreeItem[]) => {
  store.updateCollectionItems(element, newItems)
}

/** Forward DnD-updated children to the store for a rootItemId-based parent. */
const updateItemChildren = (element: unknown, newItems: TreeItem[]) => {
  store.updateItemChildren(getInnerItem(toItem(element)), newItems)
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: #2a2d2e;
}

.tree-draggable {
  padding-left: 12px;
}

.nested-drop-zone {
  min-height: 4px;
}
</style>
