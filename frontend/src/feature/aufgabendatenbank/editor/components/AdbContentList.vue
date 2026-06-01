<template>
  <div class="content-list">
    <AdbContentEditor
      v-for="(content, index) in contents"
      :key="content.id ?? index"
      :content="content"
      :hide-delete="contents.length <= 1"
      @update:text="(val) => store.updateContentText(index, val)"
      @update:purpose="(val) => store.updateContentPurpose(index, val)"
      @delete="store.removeContentFromSelectedItem(index)"
    />

    <button
      type="button"
      class="add-content-btn"
      @click="store.addContentToSelectedItem()"
    >
      <v-icon icon="mdi-plus" size="18" />
      <span>Inhalt hinzufügen</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AdbContentEditor from './AdbContentEditor.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()

const contents = computed(() => store.selectedInnerItem?.contents ?? [])
</script>

<style scoped lang="scss">
.content-list {
  margin-bottom: 8px;
}

.add-content-btn {
  all: unset;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  box-sizing: border-box;
  padding: 14px 0;
  border: 1px dashed #555;
  border-radius: 12px;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, background 0.15s;

  &:hover {
    border-color: #888;
    color: #cccccc;
    background: rgba(255, 255, 255, 0.03);
  }
}
</style>
