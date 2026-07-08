<template>
  <div class="tag-check-wrapper">
    <div
      class="tag-check-node"
      :style="{ paddingLeft: (depth * 16 + 2) + 'px' }"
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
      <div
        class="tag-check-label"
        @click="emit('toggle', node.id, !checked)"
      >
        <v-icon
          size="18"
          class="checkbox"
          :class="{ checked }"
        >
          {{ checked ? 'mdi-checkbox-marked' : 'mdi-checkbox-blank-outline' }}
        </v-icon>
        <span class="tag-check-name">{{ node.tag }}</span>
      </div>
    </div>

    <template v-if="expanded">
      <AdbTagCheckNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :assigned="assigned"
        @toggle="(id, val) => emit('toggle', id, val)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { TagNode } from '@/stores/exerciseStore'

const props = defineProps<{ node: TagNode; depth: number; assigned: string[] }>()
const emit = defineEmits<{ (e: 'toggle', id: string, value: boolean): void }>()

const expanded = ref(true)
const checked = computed(() => props.assigned.includes(props.node.id))
</script>

<style scoped>
.tag-check-node {
  display: flex;
  align-items: center;
  min-height: 26px;
  font-size: 13px;
}

.chevron {
  color: #888;
  cursor: pointer;
  width: 18px;
  min-width: 18px;

  &:hover {
    color: #ccc;
  }
}

.chevron-spacer {
  display: inline-block;
  width: 18px;
  min-width: 18px;
}

.tag-check-label {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  cursor: pointer;
  padding: 1px 4px;
  border-radius: 3px;
  user-select: none;

  &:hover {
    background-color: #2a2d2e;
  }
}

.checkbox {
  color: #7a7a7a;

  &.checked {
    color: #007fd4;
  }
}

.tag-check-name {
  color: #cccccc;
}
</style>
