<template>
  <component
    :is="isDraggable ? Draggable : 'div'"
    v-bind="dragOptions"
    v-model="internalItems"
    @change="onChange"
    item-key="id"
    class="pl-4"
  >
    <template #item="{ element }">
      <div class="tree-node">
        <!-- Implementation A: Collection -->
        <v-list-group v-if="isCollection(element)" :value="element.id">
          <template v-slot:activator="{ props }">
            <v-list-item v-bind="props" prepend-icon="mdi-folder" @click="selectItem(element)">
              <v-list-item-title>
                {{ getTitle(element) }}
              </v-list-item-title>
              <template v-slot:append>
                <v-icon v-if="element.order" size="small" color="primary" class="mr-2">mdi-format-list-numbered</v-icon>
                <v-btn icon="mdi-plus" variant="text" size="small" @click.stop="addChild(element)"></v-btn>
              </template>
            </v-list-item>
          </template>

          <!-- Nested items in collection -->
          <AdbTreeItem
            v-if="element.items"
            :items="element.items"
            :is-draggable="true"
            @update-items="(newItems) => updateCollectionChildren(element, newItems)"
          />
        </v-list-group>

        <!-- Implementation B or Simple Item -->
        <div v-else>
          <v-list-item
            :prepend-icon="element.item ? 'mdi-file-document-outline' : 'mdi-file-document'"
            @click="selectItem(element)"
          >
            <v-list-item-title>
              <span v-if="element.position" class="mr-2 text-primary font-weight-bold">{{ element.position }}.</span>
              {{ getTitle(element) }}
            </v-list-item-title>
            <template v-slot:append v-if="!element.collectionId">
              <v-btn icon="mdi-plus" variant="text" size="small" @click.stop="addChild(element)"></v-btn>
            </template>
          </v-list-item>

          <!-- Nested items via rootItem (Implementation B) -->
          <AdbTreeItem
            v-if="hasChildren(element)"
            :items="getChildren(element)"
            :is-draggable="true"
            @update-items="(newItems) => updateItemChildren(element, newItems)"
          />
        </div>
      </div>
    </template>
  </component>
</template>

<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'
import draggable from 'vuedraggable'

const Draggable = draggable

import type { Item, Collection, CollectionItem } from '@/lib/types'

const props = defineProps<{
  items: (Item | Collection | CollectionItem)[]
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
  ghostClass: 'ghost'
}))

const adb = inject<{
  rootItems: Ref<(Item | Collection)[]>,
  selectItem: (item: Item | Collection | CollectionItem) => void,
  addItemToCollection: (collection: Collection) => void,
  createItem: (rootItemId?: string | null) => Item
}>('adb')

const selectItem = (item: Item | Collection | CollectionItem) => {
  if (adb && adb.selectItem) {
    adb.selectItem(item)
  }
}

const onChange = (evt: any) => {
  console.log('Tree change', evt)
}

const isCollection = (element: any): element is Collection => {
  return (element as Collection).parent !== undefined && (element as Collection).items !== undefined
}

const getTitle = (element: Item | Collection | CollectionItem) => {
  if ('parent' in element && element.parent) {
    return element.parent.contents?.[0]?.jsonContent?.text || 'Collection ' + element.id
  }
  if ('item' in element && element.item) {
    return element.item.contents?.[0]?.jsonContent?.text || 'Item ' + element.id
  }
  const item = element as Item
  return item.contents?.[0]?.jsonContent?.text || 'Exercise ' + item.id
}

const addChild = (element: Item | Collection | CollectionItem) => {
  if (isCollection(element)) {
    adb?.addItemToCollection(element)
  } else {
    // Implementation B: Root Item
    let itemId: string | undefined
    if ('item' in element) {
        itemId = element.item.id
    } else {
        itemId = (element as Item).id
    }
    
    if (itemId) {
        const newItem = adb?.createItem(itemId)
        if (adb && adb.rootItems) {
            // New item is already added to rootItems by createItem if rootItemId is null
            // but for implementation B we need it in rootItems with rootItemId
            if (newItem && !adb.rootItems.value.includes(newItem)) {
                adb.rootItems.value.push(newItem)
            }
        }
    }
  }
}

const hasChildren = (element: Item | Collection | CollectionItem) => {
  if (!adb?.rootItems?.value) return false
  const itemId = 'item' in element ? element.item.id : (element as Item).id
  return adb.rootItems.value.some((item: any) => item.rootItemId === itemId)
}

const getChildren = (element: Item | Collection | CollectionItem) => {
  if (!adb?.rootItems?.value) return []
  const itemId = 'item' in element ? element.item.id : (element as Item).id
  return adb.rootItems.value.filter((item: any) => item.rootItemId === itemId)
}

const updateCollectionChildren = (element: Collection, newItems: CollectionItem[]) => {
  element.items = newItems
  if (element.order) {
    element.items.forEach((child: CollectionItem, index: number) => {
      child.position = index + 1
    })
  }
}

const updateItemChildren = (element: Item | Collection | CollectionItem, newItems: Item[]) => {
  // Logic to handle reordering of items with same rootItemId in the flat rootItems list
  console.log('Update item children', element, newItems)
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>

