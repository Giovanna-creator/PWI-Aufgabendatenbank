<template>
  <div class="tag-node-wrapper">
    <div
      class="tag-node"
      :style="{ paddingLeft: (depth * 14 + 6) + 'px' }"
    >
      <v-icon
        v-if="node.children.length"
        size="18"
        class="chevron"
        @click.stop="expanded = !expanded"
      >
        {{ expanded ? 'mdi-chevron-down' : 'mdi-chevron-right' }}
      </v-icon>
      <span
        v-else
        class="chevron-spacer"
      />
      <span
        class="tag-node-label"
        :class="{ active: store.tagFilter === node.id }"
        @click="emit('select', node.id)"
      >
        {{ node.tag }}
      </span>
      <v-icon
        size="15"
        class="tag-delete"
        @click.stop="emit('delete', node)"
      >
        mdi-delete-outline
      </v-icon>
    </div>

    <template v-if="expanded">
      <AdbTagFilterNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        @select="(id) => emit('select', id)"
        @delete="(n) => emit('delete', n)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TagNode } from '@/stores/exerciseStore'
import { useExerciseStore } from '@/stores/exerciseStore'

defineProps<{ node: TagNode; depth: number }>()
const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'delete', node: TagNode): void
}>()

const store = useExerciseStore()
const expanded = ref(false)
</script>

<style scoped>
.tag-node {
  display: flex;
  align-items: center;
  min-height: 28px;
  color: var(--adb-text-primary);
  font-size: 13px;

  &:hover {
    background-color: var(--adb-bg-hover);
  }
}

.chevron {
  color: var(--adb-text-tertiary);
  cursor: pointer;
  width: 18px;
  min-width: 18px;

  &:hover {
    color: var(--adb-text-primary);
  }
}

.chevron-spacer {
  display: inline-block;
  width: 18px;
  min-width: 18px;
}

.tag-node-label {
  flex: 1;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 3px;
  user-select: none;

  &:hover {
    color: var(--adb-text-inverse);
  }

  &.active {
    color: var(--adb-accent);
    font-weight: 600;
  }
}

.tag-delete {
  color: var(--adb-text-tertiary);
  opacity: 0;
  cursor: pointer;
  margin-right: 6px;
  transition: opacity 0.1s;

  &:hover {
    color: var(--adb-danger-hover);
  }
}

.tag-node:hover .tag-delete {
  opacity: 1;
}
</style>
