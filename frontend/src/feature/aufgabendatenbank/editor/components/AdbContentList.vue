<template>
  <div class="content-list">
    <template v-for="(group, gIdx) in orderedGroups" :key="gIdx">
      <div
        v-if="group.type === 'split'"
        class="split-row"
      >
        <AdbContentEditor
          v-for="(content, cIdx) in group.contents"
          :key="content.id ?? gIdx + '-' + cIdx"
          :content="content"
          :index="realIndex(content)"
          class="split-cell"
          @update:text="(val) => store.updateContentText(realIndex(content), val)"
          @update:purpose="(val) => store.updateContentPurpose(realIndex(content), val)"
          @update:meta="(m) => store.updateContentMeta(realIndex(content), m)"
          @delete="store.removeContentFromSelectedItem(realIndex(content))"
        />
      </div>
      <AdbContentEditor
        v-else
        :key="contentIdKey(group.contents[0], gIdx)"
        :content="group.contents[0]"
        :index="realIndex(group.contents[0])"
        @update:text="(val) => store.updateContentText(realIndex(group.contents[0]), val)"
        @update:purpose="(val) => store.updateContentPurpose(realIndex(group.contents[0]), val)"
        @update:meta="(m) => store.updateContentMeta(realIndex(group.contents[0]), m)"
        @delete="store.removeContentFromSelectedItem(realIndex(group.contents[0]))"
      />
    </template>

    <button
      type="button"
      class="add-content-btn"
      @click="store.addContentToSelectedItem()"
    >
      <span class="btn-inner">
        <v-icon
          icon="mdi-plus"
          size="18"
        />
        <span>Inhalt hinzufügen</span>
      </span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Content } from '@/lib/types'
import AdbContentEditor from './AdbContentEditor.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { applyTemplateOrder, flattenContentGroups } from '../../representation/applyTemplateOrder'

const store = useExerciseStore()

const rawContents = computed(() => store.selectedInnerItem?.contents ?? [])

const orderedGroups = computed(() => {
  const item = store.selectedInnerItem
  if (!item) return []
  const template = store.templateById(item.representationTemplate)
  return applyTemplateOrder(rawContents.value, template)
})

function contentIdKey(content: Content, fallback: number): string {
  return content.id ?? 'c-' + fallback
}

function realIndex(content: Content): number {
  const item = store.selectedInnerItem
  if (!item) return -1
  return item.contents.indexOf(content)
}
</script>

<style scoped lang="scss">
.content-list {
  margin-bottom: 8px;
}

.split-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 10px;
}

.split-cell {
  min-width: 0;
}

.add-content-btn {
  all: unset;
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
  min-height: 48px;
  padding: 0 16px;
  border-radius: 24px;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  transition:
    color 0.15s,
    background-color 0.15s;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 24px;
    background: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' width='100%25' height='100%25'%3e%3crect width='100%25' height='100%25' fill='none' stroke='%23555' stroke-width='2.5' stroke-dasharray='14%2c 8' stroke-linejoin='round' rx='24'/%3e%3c/svg%3e") 0 0 / 100% 100% no-repeat;
    pointer-events: none;
    z-index: 0;
  }

  &:hover {
    color: #cccccc;
  }

  &:hover::before {
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' width='100%25' height='100%25'%3e%3crect width='100%25' height='100%25' fill='none' stroke='%23888' stroke-width='2.5' stroke-dasharray='14%2c 8' stroke-linejoin='round' rx='24'/%3e%3c/svg%3e");
  }
}

.btn-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  z-index: 1;
}

.order-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: 24px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s;

  background-color: transparent;
  border: 1px solid #444;

  &:hover {
    background-color: rgba(255, 255, 255, 0.03);
  }

  &.active {
    background-color: #252526;
    border-color: #007fd4;
  }
}

.order-icon {
  color: #666;
  transition: color 0.15s;

  &.active {
    color: #007fd4;
  }
}

.order-text {
  font-size: 13px;
  color: #aaa;
  transition: color 0.15s;

  .order-card.active & {
    color: #ccc;
  }
}
</style>
