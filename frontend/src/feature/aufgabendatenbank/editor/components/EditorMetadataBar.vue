<template>
  <div>
    <div class="editor-header-row">
      <h3 class="text-h6 mb-2">
        {{ itemTypeLabel }}
      </h3>
      <div
        v-if="store.isCollectionSelected"
        class="order-toggle-area"
      >
        <span class="order-hint">Geordnete Liste</span>
        <div
          class="order-card"
          :class="{ visible: store.isOrdered }"
        >
          <v-btn
            icon="mdi-format-list-numbered"
            variant="text"
            size="small"
            :ripple="false"
            class="order-btn"
            :class="{ active: store.isOrdered }"
            @click="toggleOrder"
          />
        </div>
        <div class="delete-card">
          <v-btn
            variant="text"
            :ripple="false"
            class="delete-btn"
            @click="showDeleteDialog = true"
          >
            Löschen
          </v-btn>
        </div>
      </div>

      <AdbDeleteDialog
        v-model:visible="showDeleteDialog"
        @confirm="deleteSelectedItem"
      />
    </div>

    <div
      v-if="inner"
      class="meta-row"
    >
      <AdbRefSelect
        :model-value="inner.itemTypeId"
        :items="store.itemTypes"
        item-title="name"
        label="Typ"
        type="itemType"
        class="meta-select"
        @update:model-value="(v) => onMeta({ itemTypeId: v })"
      />
      <AdbRefSelect
        :model-value="inner.authorId"
        :items="store.authors"
        item-title="descriptor"
        label="Autor"
        type="author"
        class="meta-select"
        @update:model-value="(v) => onMeta({ authorId: v })"
      />
      <AdbRefSelect
        :model-value="inner.licenseId"
        :items="store.licenses"
        item-title="name"
        label="Lizenz"
        type="license"
        class="meta-select"
        @update:model-value="(v) => onMeta({ licenseId: v })"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import AdbRefSelect from '../../toolbar/AdbRefSelect.vue'
import AdbDeleteDialog from './AdbDeleteDialog.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()
const showDeleteDialog = ref(false)

const inner = computed(() => store.selectedInnerItem)

const itemTypeLabel = computed(() => {
  if (!store.selectedInnerItem) return ''
  const type = store.selectedInnerItem.item_type
  const label = type === 'collection' ? 'Kollektion' : 'Aufgabe'
  const firstText = store.selectedInnerItem?.contents?.[0]?.jsonContent?.text
  return firstText ? `${label}: ${firstText}` : label
})

function toggleOrder() {
  const coll = store.selectedCollection
  if (coll) {
    store.toggleCollectionOrder(coll)
  }
}

function deleteSelectedItem() {
  if (store.selectedInnerItem) {
    store.deleteItem(store.selectedInnerItem)
  }
}

function onMeta(meta: { authorId?: string; licenseId?: string; itemTypeId?: string }) {
  if (inner.value) store.updateItemMeta(inner.value, meta)
}
</script>

<style scoped lang="scss">
.editor-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.meta-select {
  flex: 1 1 240px;
  min-width: 220px;
}

.order-toggle-area {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-hint {
  font-size: 11px;
  color: var(--adb-text-tertiary);
  user-select: none;
  white-space: nowrap;
}

.order-card {
  transition: all 0.15s;
  border-radius: 4px;
  padding: 0;
  line-height: 0;

  &.visible {
    background-color: var(--adb-bg-active);
    box-shadow: 0 0 0 1px var(--adb-accent);
  }
}

.delete-card {
  border-radius: 4px;
  padding: 0 14px;
  line-height: 0;
  background-color: var(--adb-bg-active);
  box-shadow: 0 0 0 1px var(--adb-danger);
  display: flex;
  align-items: center;
  height: 32px;
  margin-left: 12px;
}

.delete-btn {
  color: var(--adb-danger) !important;
  font-size: 13px;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  transition: color 0.15s;
  margin: 0 !important;
  padding: 0 !important;
  min-width: 0 !important;
  height: 32px !important;
  --v-btn-height: 32px;
  background: transparent !important;

  :deep(.v-btn__overlay) {
    display: none;
  }

  :deep(.v-btn__content) {
    padding: 0;
  }

  &:hover {
    color: var(--adb-danger-hover) !important;
  }
}

.order-btn {
  color: var(--adb-icon) !important;
  transition: color 0.15s;
  margin: 0 !important;
  padding: 1rem !important;
  min-width: 0 !important;
  width: 22px;
  height: 22px;
  background: transparent !important;

  :deep(.v-btn__overlay) {
    display: none;
  }

  :deep(.v-btn__content) {
    padding: 0;
  }

  :deep(.v-icon) {
    font-size: 22px;
  }

  &:hover {
    color: var(--adb-text-tertiary) !important;
  }

  &.active {
    color: var(--adb-accent) !important;

    &:hover {
      color: var(--adb-accent-hover) !important;
    }
  }
}
</style>
