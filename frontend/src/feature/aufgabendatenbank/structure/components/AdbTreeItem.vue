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
        <!-- Implementation A: Collection -->
        <AdbTreeFolder
          v-if="isCollection(element)"
          :element="getCollection(element)"
          :title="getTitle(element)"
          :position="getPosition(element)"
          :is-sub-item="isSubItem(element)"
          @toggle-expand="toggleExpand(getId(element))"
        >
          <AdbTreeItem
            :items="getCollection(element).items"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateCollectionChildren(getCollection(element), newItems)"
          />

          <!-- Nested items via rootItem (Implementation B) -->
          <AdbTreeItem
            v-if="isExpanded(getId(element))"
            :items="getChildren(element)"
            :is-draggable="true"
            class="nested-drop-zone"
            @update-items="(newItems) => updateItemChildren(element, newItems)"
          />
        </AdbTreeFolder>

        <!-- Implementation B or Simple Item -->
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

          <!-- Nested items via rootItem (Implementation B) -->
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

const internalItems = computed({
  get: () => props.items,
  set: (val) => emit('update-items', val)
})

const dragOptions = computed(() => ({
  animation: 200,
  group: 'tree',
  disabled: false,
  ghostClass: 'ghost',
  itemKey: 'id'
}))

const expandedItems = ref<Record<string, boolean>>({})

const toggleExpand = (id: string) => {
  expandedItems.value[id] = !expandedItems.value[id]
}

const isExpanded = (id: string) => !!expandedItems.value[id]

const toItem = (element: unknown): TreeItem => element as TreeItem

const isCollection = (element: unknown): element is Collection => {
  return checkIsCollection(getInnerItem(toItem(element)))
}

const getCollection = (element: unknown): Collection => {
  return getInnerItem(toItem(element)) as Collection
}

const getId = (element: unknown): string => {
  return toItem(element).id
}

const getTitle = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.contents?.[0]?.jsonContent?.text || (inner?.item_type === 'collection' ? 'New Collection' : 'New Task')
}

const getPosition = (element: unknown): number | null | undefined => {
  const e = toItem(element)
  return isCollectionItem(e) ? e.position : null
}

const isSubItem = (element: unknown): boolean => {
  const e = toItem(element)
  const inner = getInnerItem(e)
  if (inner.rootItemId) return true
  if (isCollectionItem(e)) return !!e.collectionId
  return false
}

const hasChildren = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.id ? store.rootItems.some((ri) => ri.rootItemId === inner.id) : false
}

const getChildren = (element: unknown) => {
  const inner = getInnerItem(toItem(element))
  return inner?.id ? store.rootItems.filter((ri) => ri.rootItemId === inner.id) : []
}

const updateCollectionChildren = (element: Collection, newItems: TreeItem[]) => {
  store.updateCollectionItems(element, newItems)
}

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
