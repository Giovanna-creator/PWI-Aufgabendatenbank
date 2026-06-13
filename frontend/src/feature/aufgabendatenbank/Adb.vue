<template>
  <div class="adb-container">
    <AdbToolbar />
    <div
      ref="containerRef"
      class="adb-content"
    >
      <div
        class="adb-sidebar"
        :style="{ width: sidebarWidth + 'px' }"
      >
        <AdbStructure />
      </div>
      <div
        class="adb-resizer"
        @mousedown="startResizing"
      />
      <div class="adb-editor">
        <AdbEditor />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useSidebarResizer } from './composables/useSidebarResizer'
import { useExerciseStore } from '@/stores/exerciseStore'
import AdbStructure from './structure/AdbStructure.vue'
import AdbEditor from './editor/AdbEditor.vue'
import AdbToolbar from './toolbar/AdbToolbar.vue'

const store = useExerciseStore()
const { sidebarWidth, containerRef, startResizing } = useSidebarResizer()

onMounted(() => {
  store.loadTree()
})
</script>

<style lang="scss" scoped>
.adb-container {
  height: calc(100vh - 64px); /* Subtract header height */
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #1e1e1e;
}

.adb-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.adb-sidebar {
  height: 100%;
  flex-shrink: 0;
  overflow: hidden;
}

.adb-resizer {
  width: 4px;
  cursor: col-resize;
  background-color: transparent;
  transition: background-color 0.2s;
  z-index: 10;
  margin: 0 -2px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 2px;
    right: 1px;
    background-color: transparent;
    transition: background-color 0.2s;
  }

  &:hover::after, &:active::after {
    background-color: #007fd4;
  }
}

.adb-editor {
  flex: 1;
  height: 100%;
  overflow: hidden;
  min-width: 0;
}
</style>
