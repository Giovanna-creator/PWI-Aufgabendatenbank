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
import { computed, inject, ref, type Ref } from 'vue'
import draggable from 'vuedraggable'
import AdbTreeFile from './tree/AdbTreeFile.vue'
import AdbTreeFolder from './tree/AdbTreeFolder.vue'
import { type Item, type Collection, type CollectionItem, type TreeItem } from '@/lib/types'

const Draggable = draggable

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

// Expansion state for Implementation B items
const expandedItems = ref<Record<string, boolean>>({})

const toggleExpand = (id: string) => {
  expandedItems.value[id] = !expandedItems.value[id]
}

const isExpanded = (id: string) => !!expandedItems.value[id]

const adb = inject<{
  rootItems: Ref<Item[]>
  selectItem: (item: TreeItem) => void
  addItemToCollection: (collection: Collection) => void
  createItem: (rootItemId?: string | null) => Item
  makeItemACollection: (item: Item) => Collection
  deleteItem: (item: Item) => void
  deleteCollection: (collection: Collection) => void
  updateCollectionItems: (collection: Collection, newItems: TreeItem[]) => void
  updateItemChildren: (parent: Item, children: TreeItem[]) => void
  updateRootItems: (newItems: TreeItem[]) => void
  getInnerItem: (element: TreeItem) => Item
  isCollectionItem: (object: any) => object is CollectionItem
  checkIsCollection: (object: any) => object is Collection
}>('adb')

const asItem = (element: unknown): TreeItem => element as TreeItem

const isCollection = (element: unknown): element is Collection => {
  return adb?.checkIsCollection(adb.getInnerItem(asItem(element))) ?? false
}

const getCollection = (element: unknown): Collection => {
  return adb?.getInnerItem(asItem(element)) as Collection
}

const getInnerItem = (element: unknown): Item => {
  return adb?.getInnerItem(asItem(element)) as Item
}

const getId = (element: unknown): string => {
  return asItem(element).id
}

const getTitle = (element: unknown) => {
  const item = getInnerItem(element)
  return item?.contents?.[0]?.jsonContent?.text || (item?.item_type === 'collection' ? 'New Collection' : 'New Task')
}

const getPosition = (element: unknown): number | null | undefined => {
  const e = asItem(element)
  if ('position' in e) {
    return (e as CollectionItem).position
  }
  return null
}

const isSubItem = (element: unknown): boolean => {
  const e = asItem(element)
  const inner = getInnerItem(e)
  if (inner.rootItemId) return true
  if ('collectionId' in e) {
    return !!(e as CollectionItem).collectionId
  }
  return false
}

const hasChildren = (element: unknown) => {
  if (!adb?.rootItems?.value) return false
  const item = getInnerItem(element)
  if (!item?.id) return false
  return adb.rootItems.value.some((ri) => ri.rootItemId === item.id)
}

const getChildren = (element: unknown) => {
  if (!adb?.rootItems?.value) return []
  const item = getInnerItem(element)
  if (!item?.id) return []
  return adb.rootItems.value.filter((ri) => ri.rootItemId === item.id)
}

const updateCollectionChildren = (element: Collection, newItems: TreeItem[]) => {
  adb?.updateCollectionItems(element, newItems)
}

const updateItemChildren = (element: unknown, newItems: TreeItem[]) => {
  const parentItem = getInnerItem(element)
  adb?.updateItemChildren(parentItem, newItems)
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
