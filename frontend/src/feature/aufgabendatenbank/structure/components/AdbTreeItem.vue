<template>
  <Draggable
    v-bind="dragOptions"
    v-model="internalItems"
    item-key="id"
    class="pl-4"
    :disabled="!isDraggable"
    @change="onChange"
  >
    <template #item="{ element }">
      <div class="tree-node">
        <!-- Implementation A: Collection -->
        <AdbTreeFolder
          v-if="isCollection(element)"
          :element="getCollection(element)"
          :title="getTitle(element)"
          :position="'position' in element ? element.position : null"
          :is-sub-item="!!element.rootItemId || ('collectionId' in element && !!element.collectionId)"
          @toggle-expand="toggleExpand(element.id)"
        >
          <AdbTreeItem
            :items="getCollection(element).items"
            :is-draggable="true"
            :parent-item="getCollection(element)"
            class="nested-drop-zone"
            @update-items="(newItems) => updateCollectionChildren(getCollection(element), newItems)"
          />

          <!-- Nested items via rootItem (Implementation B) -->
          <AdbTreeItem
            v-if="isExpanded(element.id)"
            :items="getChildren(element)"
            :is-draggable="true"
            :parent-item="getInnerItem(element)"
            class="nested-drop-zone"
            @update-items="(newItems) => updateItemChildren(element, newItems)"
          />
        </AdbTreeFolder>

        <!-- Implementation B or Simple Item -->
        <div v-else>
          <AdbTreeFile
            :element="element"
            :title="getTitle(element)"
            :position="element.position"
            :is-sub-item="!!element.collectionId || !!element.rootItemId"
            :has-children="hasChildren(element)"
            :is-open="isExpanded(element.id)"
            @toggle-expand="toggleExpand(element.id)"
          />

          <!-- Nested items via rootItem (Implementation B) -->
          <AdbTreeItem
            v-if="isExpanded(element.id)"
            :items="getChildren(element)"
            :is-draggable="true"
            :parent-item="getInnerItem(element)"
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
import { type Item, type Collection, type CollectionItem } from '@/lib/types'

const Draggable = draggable

const props = defineProps<{
  items: (Item | Collection | CollectionItem)[]
  isDraggable?: boolean
  parentItem?: Item | null
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
  rootItems: Ref<(Item | Collection)[]>,
  selectItem: (item: Item | Collection | CollectionItem) => void,
  addItemToCollection: (collection: Collection) => void,
  createItem: (rootItemId?: string | null) => Item,
  makeItemACollection: (item: Item) => Collection,
  deleteItem: (item: Item) => void,
  deleteCollection: (collection: Collection) => void,
  updateCollectionItems: (collection: Collection, newItems: (CollectionItem | Item | Collection)[]) => void,
  updateItemChildren: (parent: Item, children: (Item | Collection | CollectionItem)[]) => void,
  updateRootItems: (newItems: (Item | Collection | CollectionItem)[]) => void,
  getInnerItem: (element: Item | Collection | CollectionItem) => Item,
  isCollectionItem: (object: any) => object is CollectionItem,
  checkIsCollection: (object: any) => object is Collection
}>('adb')

const onChange = () => {
  // Logic is now mostly handled by the setters of internalItems and specific update methods
  // But we can add extra logic here if needed for cross-container moves
}

const isCollection = (element: Item | Collection | CollectionItem): element is Collection => {
  return adb?.checkIsCollection(adb.getInnerItem(element)) ?? false
}

const getCollection = (element: Item | Collection | CollectionItem): Collection => {
  return adb?.getInnerItem(element) as Collection
}

const getInnerItem = (element: Item | Collection | CollectionItem): Item => {
  return adb?.getInnerItem(element) as Item
}

const getTitle = (element: Item | Collection | CollectionItem) => {
  const item = adb?.getInnerItem(element)
  return item?.contents?.[0]?.jsonContent?.text || (item?.item_type === 'collection' ? 'New Collection' : 'New Task')
}

const hasChildren = (element: Item | Collection | CollectionItem) => {
  if (!adb?.rootItems?.value) return false
  const item = getInnerItem(element)
  if (!item?.id) return false
  return adb.rootItems.value.some((ri) => ri.rootItemId === item.id)
}

const getChildren = (element: Item | Collection | CollectionItem) => {
  if (!adb?.rootItems?.value) return []
  const item = getInnerItem(element)
  if (!item?.id) return []
  return adb.rootItems.value.filter((ri) => ri.rootItemId === item.id)
}


const updateCollectionChildren = (element: Collection, newItems: (Item | Collection | CollectionItem)[]) => {
  // If we receive a list that contains both CollectionItems and raw Items, 
  // the adb action will handle wrapping/normalization.
  adb?.updateCollectionItems(element, newItems)
}

const updateItemChildren = (element: Item | Collection | CollectionItem, newItems: (Item | Collection | CollectionItem)[]) => {
  const parentItem = getInnerItem(element)
  adb?.updateItemChildren(parentItem, newItems)
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}

.nested-drop-zone {
  min-height: 10px;
}
</style>

