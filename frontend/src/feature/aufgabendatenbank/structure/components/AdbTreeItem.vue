<template>
  <component
    :is="isDraggable ? 'draggable' : 'div'"
    v-bind="dragOptions"
    v-model="internalItems"
    @change="onChange"
    item-key="id"
    class="pl-4"
  >
    <template #item="{ element, index }">
      <!-- Collection (Parent Item) -->
      <v-list-group v-if="element.item_type === 'collection' || element.parent" :value="element.id">
        <template v-slot:activator="{ props }">
          <v-list-item v-bind="props" prepend-icon="mdi-folder" @click="selectItem(element)">
            <v-list-item-title>
              {{ element.parent?.contents[0]?.jsonContent?.text || element.contents?.[0]?.jsonContent?.text || 'Collection ' + element.id }}
            </v-list-item-title>
            <template v-slot:append v-if="element.order">
              <v-icon size="small" color="primary">mdi-format-list-numbered</v-icon>
            </template>
          </v-list-item>
        </template>

        <!-- Nested items -->
        <AdbTreeItem
          v-if="element.items"
          :items="element.items"
          :is-draggable="true"
          @update-items="(newItems) => updateChildren(element, newItems)"
        />
      </v-list-group>

      <!-- Standard Item (Leaf) -->
      <v-list-item
        v-else
        prepend-icon="mdi-file-document-outline"
        @click="selectItem(element)"
      >
        <v-list-item-title>
          <span v-if="isOrdered(element)" class="mr-2 text-primary font-weight-bold">{{ index + 1 }}.</span>
          {{ element.item?.contents?.[0]?.jsonContent?.text || element.contents?.[0]?.jsonContent?.text || 'Item ' + element.id }}
        </v-list-item-title>
      </v-list-item>
    </template>
  </component>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import draggable from 'vuedraggable'

const props = defineProps<{
  items: any[]
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

const adb = inject<any>('adb')

const selectItem = (item: any) => {
  if (adb && adb.selectItem) {
    adb.selectItem(item)
  }
}

const onChange = (evt: any) => {
  // Handle order changes if needed
  console.log('Tree change', evt)
}

const updateChildren = (element: any, newItems: any[]) => {
  element.items = newItems
}

const isOrdered = (element: any) => {
  // If element is inside a collection we check if the collection is ordered
  // This is a naive check for the UI visualization
  return element.position !== undefined && element.position !== null
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>

