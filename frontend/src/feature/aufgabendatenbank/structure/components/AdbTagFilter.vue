<template>
  <v-menu
    v-model="open"
    :close-on-content-click="false"
    location="bottom start"
    offset="4"
  >
    <template #activator="{ props }">
      <div
        class="tag-filter-field"
        v-bind="props"
      >
        <span class="tag-filter-caption">Filtern nach Tag</span>
        <div class="tag-filter-value-row">
          <span
            class="tag-filter-value"
            :class="{ placeholder: !store.tagFilter }"
          >
            {{ store.tagFilter ? '#' + store.tagPath(store.tagFilter) : 'Alle Tags' }}
          </span>
          <v-icon
            v-if="store.tagFilter"
            size="16"
            class="tag-filter-clear"
            @click.stop="select(null)"
          >
            mdi-close
          </v-icon>
          <v-icon
            size="18"
            class="tag-filter-arrow"
          >
            {{ open ? 'mdi-menu-up' : 'mdi-menu-down' }}
          </v-icon>
        </div>
      </div>
    </template>

    <div class="tag-filter-menu">
      <div
        class="tag-menu-all"
        :class="{ active: !store.tagFilter }"
        @click="select(null)"
      >
        Alle anzeigen
      </div>
      <div
        v-if="store.tagTree.length"
        class="tag-menu-divider"
      />
      <div
        v-if="!store.tagTree.length"
        class="tag-menu-empty"
      >
        Noch keine Tags
      </div>
      <AdbTagFilterNode
        v-for="node in store.tagTree"
        :key="node.id"
        :node="node"
        :depth="0"
        @select="select"
        @delete="onDelete"
      />
    </div>
  </v-menu>

  <AdbConfirmDialog
    v-model:visible="showDeleteDialog"
    title="Tag löschen"
    :message="deleteMessage"
    confirm-label="Löschen"
    @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import AdbTagFilterNode from './AdbTagFilterNode.vue'
import AdbConfirmDialog from './tree/AdbConfirmDialog.vue'
import type { TagNode } from '@/stores/exerciseStore'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()
const open = ref(false)

const showDeleteDialog = ref(false)
const tagToDelete = ref<TagNode | null>(null)

function select(id: string | null) {
  store.setTagFilter(id)
  open.value = false
}

function onDelete(node: TagNode) {
  tagToDelete.value = node
  showDeleteDialog.value = true
}

const deleteMessage = computed(() => {
  const n = tagToDelete.value
  if (!n) return ''
  const base = `Tag „${n.tag}" wird von allen Aufgaben entfernt.`
  const kids = n.children.length ? ' Untergeordnete Tags werden zu obersten Tags.' : ''
  return base + kids + '\n\nMöchten Sie fortfahren?'
})

function confirmDelete() {
  if (tagToDelete.value) store.deleteTag(tagToDelete.value.id)
  tagToDelete.value = null
}
</script>

<style scoped>
.tag-filter-field {
  margin-top: 8px;
  border: 1px solid #555;
  border-radius: 4px;
  padding: 4px 8px 5px;
  background-color: #3c3c3c;
  cursor: pointer;

  &:hover {
    border-color: #777;
  }
}

.tag-filter-caption {
  display: block;
  font-size: 10px;
  color: #969696;
  line-height: 12px;
}

.tag-filter-value-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-filter-value {
  flex: 1;
  font-size: 13px;
  color: #cccccc;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &.placeholder {
    color: #888;
  }
}

.tag-filter-clear {
  color: #888;

  &:hover {
    color: #ccc;
  }
}

.tag-filter-arrow {
  color: #969696;
}

.tag-filter-menu {
  background-color: #252526;
  border: 1px solid #454545;
  border-radius: 4px;
  padding: 4px 0;
  min-width: 220px;
  max-height: 340px;
  overflow-y: auto;
}

.tag-menu-all {
  padding: 4px 12px;
  font-size: 13px;
  color: #cccccc;
  cursor: pointer;

  &:hover {
    background-color: #2a2d2e;
  }

  &.active {
    color: #007fd4;
    font-weight: 600;
  }
}

.tag-menu-divider {
  height: 1px;
  background-color: #3a3a3a;
  margin: 4px 0;
}

.tag-menu-empty {
  padding: 6px 12px;
  font-size: 12px;
  color: #6a6a6a;
  font-style: italic;
}
</style>
