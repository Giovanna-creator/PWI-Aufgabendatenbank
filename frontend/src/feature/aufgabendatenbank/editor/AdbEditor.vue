<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="store.selectedItem">
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
          </div>
        </div>

        <AdbContentList />

        <p class="text-caption text-grey">
          ID: {{ store.selectedItem.id }}
        </p>
      </div>
      <div v-else>
        Bitte wählen Sie eine Aufgabe oder Kollektion aus dem Struktur-Baum aus.
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AdbContentList from './components/AdbContentList.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()

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
</script>

<style lang="scss" scoped>
.adb-editor-card {
  height: 100%;
  width: 100%;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.adb-editor-title {
  font-weight: 500;
  font-size: 0.75rem;
  line-height: 16px;
  text-transform: uppercase;
  color: #969696;
  border-bottom: 1px solid #333333;
  padding: 8px 16px !important;
  background-color: #252526;
  user-select: none;
}

.adb-editor-text {
  padding: 28px 36px;
  font-size: 13px;
  color: #cccccc;
}

.editor-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.order-toggle-area {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-hint {
  font-size: 11px;
  color: #777;
  user-select: none;
  white-space: nowrap;
}

.order-card {
  transition: all 0.15s;
  border-radius: 4px;
  padding: 0;
  line-height: 0;

  &.visible {
    background-color: #0d2b45;
    box-shadow: 0 0 0 1px #007fd4;
  }
}

.order-btn {
  color: #666 !important;
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
    color: #999 !important;
  }

  &.active {
    color: #007fd4 !important;

    &:hover {
      color: #1a9aff !important;
    }
  }
}

:deep(.v-text-field) {
  .v-field {
    background-color: #3c3c3c !important;
    color: #cccccc !important;
    border-radius: 2px;
  }
  .v-label {
    color: #969696 !important;
  }
}
</style>
