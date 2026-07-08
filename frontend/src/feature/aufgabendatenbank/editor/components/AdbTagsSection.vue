<template>
  <div class="tags-section">
    <div class="tags-label">Tags</div>

    <div class="tags-chips">
      <v-chip
        v-for="tagId in item.tags"
        :key="tagId"
        size="small"
        closable
        class="tag-chip"
        @click:close="store.removeTagFromItem(item, tagId)"
      >
        #{{ store.tagPath(tagId) }}
      </v-chip>
      <span
        v-if="!item.tags.length"
        class="tags-empty"
      >Keine Tags</span>
    </div>

    <div
      v-if="store.tagTree.length"
      class="tags-tree"
    >
      <AdbTagCheckNode
        v-for="node in store.tagTree"
        :key="node.id"
        :node="node"
        :depth="0"
        :assigned="item.tags"
        @toggle="onToggle"
      />
    </div>

    <div class="tags-actions">
      <v-btn
        variant="tonal"
        color="primary"
        prepend-icon="mdi-plus"
        size="small"
        class="tags-new-btn"
        @click="dialog = true"
      >
        Neuer Tag
      </v-btn>
    </div>

    <v-dialog
      v-model="dialog"
      max-width="440"
    >
      <v-card class="tag-card">
        <v-card-title class="tag-title">
          Neuer Tag
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="name"
            label="Name"
            variant="outlined"
            density="compact"
            hide-details
            autofocus
            class="mb-3"
            @keyup.enter="confirm"
          />
          <v-select
            v-model="parentId"
            :items="store.tagOptions"
            item-title="path"
            item-value="id"
            label="Eltern-Tag (optional)"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            class="mb-3"
          />
          <v-text-field
            v-model="description"
            label="Beschreibung (optional)"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            class="cancel-btn"
            @click="close"
          >
            Abbrechen
          </v-btn>
          <v-btn
            variant="text"
            class="confirm-btn"
            :disabled="!name.trim()"
            @click="confirm"
          >
            Erstellen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Item } from '@/lib/types'
import AdbTagCheckNode from './AdbTagCheckNode.vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const props = defineProps<{ item: Item }>()
const store = useExerciseStore()

const dialog = ref(false)
const name = ref('')
const parentId = ref<string | null>(null)
const description = ref('')

/** Häkchen an/aus -> Tag zuweisen bzw. entfernen (Exklusivität regelt der Store). */
function onToggle(tagId: string, value: boolean) {
  if (value) store.assignTagToItem(props.item, tagId)
  else store.removeTagFromItem(props.item, tagId)
}

function close() {
  dialog.value = false
  name.value = ''
  parentId.value = null
  description.value = ''
}

async function confirm() {
  if (!name.value.trim()) return
  const created = await store.createTag(name.value, parentId.value, description.value)
  if (created) {
    store.assignTagToItem(props.item, created.id)
    close()
  }
}
</script>

<style scoped>
.tags-section {
  margin-bottom: 20px;
}

.tags-label {
  font-size: 0.75rem;
  color: #969696;
  margin-bottom: 6px;
}

.tags-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.tags-empty {
  color: #6a6a6a;
  font-size: 0.85rem;
}

.tags-tree {
  max-height: 220px;
  overflow-y: auto;
  border: 1px solid #3a3a3a;
  border-radius: 4px;
  padding: 4px 2px;
  margin-bottom: 10px;
  background-color: #232323;
}

.tags-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tags-new-btn {
  flex: 0 0 auto;
}

.tag-card {
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.tag-title {
  color: #cccccc;
  font-size: 15px;
  font-weight: 600;
}

.cancel-btn {
  color: #969696 !important;
}

.confirm-btn {
  color: #007fd4 !important;
}
</style>
