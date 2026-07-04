<template>
  <v-list-item
    :active="store.selectedItem?.id === element.id"
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
        <span
          v-if="position"
          class="position-number"
        >{{ position }}.</span>
        <v-icon
          v-else
          size="8"
          class="bullet-icon"
        >
          mdi-circle
        </v-icon>
        <v-icon
          size="18"
          class="type-icon"
        >
          {{ isSubItem ? 'mdi-file-document-outline' : 'mdi-file-document' }}
        </v-icon>
      </div>
    </template>
    <v-list-item-title class="tree-node-title">
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

  <AdbDeleteDialog
    v-model:visible="showDeleteDialog"
    @confirm="onDeleteConfirmed"
  />

  <AdbConfirmDialog
    v-model:visible="showConvertDialog"
    title="In Kollektion umwandeln"
    :message="convertMessage"
    confirm-label="Fortfahren"
    @confirm="onConvertConfirmed"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import AdbDeleteDialog from '@/feature/aufgabendatenbank/editor/components/AdbDeleteDialog.vue'
import AdbConfirmDialog from './AdbConfirmDialog.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { getInnerItem, type TreeItem } from '@/lib/types'

const store = useExerciseStore()

/** Whether a drag is currently hovering this item. */
const isDragOver = ref(false)
const showDeleteDialog = ref(false)
const showConvertDialog = ref(false)
const variantCount = ref(0)

/** Warnung vor dem Umwandeln: Varianten (horizontal) werden danach nicht mehr genutzt. */
const convertMessage = computed(() => {
  const n = variantCount.value
  const hat = n === 1 ? 'besitzt 1 Variante' : `besitzt ${n} Varianten`
  const werden = n === 1 ? 'wird diese Variante' : 'werden diese Varianten'
  return `Diese Aufgabe ${hat}.\n\nNach der Umwandlung in eine Kollektion ${werden} nicht mehr verwendet.\n\nMöchten Sie fortfahren?`
})

const props = defineProps<{
  element: TreeItem
  title: string
  position?: number | null
  isSubItem?: boolean
}>()

const onDragOver = () => {
  isDragOver.value = true
}

const onDragLeave = () => {
  isDragOver.value = false
}

/** Select the item in the store. */
const onClick = () => {
  store.selectItem(props.element)
}

/**
 * Convert the underlying item into a Collection.
 * Hat die Aufgabe Varianten, erst warnen (die horizontale Varianten-Beziehung
 * geht dabei verloren) und auf Bestätigung warten.
 */
const onMakeCollection = async () => {
  const item = getInnerItem(props.element)
  const count = await store.getVariantCount(item.id)
  if (count > 0) {
    variantCount.value = count
    showConvertDialog.value = true
  } else {
    store.makeItemACollection(item)
  }
}

const onConvertConfirmed = () => {
  store.makeItemACollection(getInnerItem(props.element))
}

/** Show confirmation dialog before deleting. */
const onDelete = () => {
  showDeleteDialog.value = true
}

const onDeleteConfirmed = () => {
  const item = getInnerItem(props.element)
  store.deleteItem(item)
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

.bullet-icon {
  margin-left: 2px;
  width: 8px;
  min-width: 8px;
  margin-right: 10px;
  color: #888888;
}

.position-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 16px;
  margin-left: -4px;
  margin-right: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #007fd4;
  line-height: 1;
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