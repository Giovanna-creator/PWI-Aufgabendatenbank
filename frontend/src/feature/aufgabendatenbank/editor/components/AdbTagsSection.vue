<template>
  <div class="tags-section">
    <div class="tags-header">
      <span class="tags-label">Tags</span>
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
            persistent-hint
            hint="Mit / Unterebenen anlegen (z. B. DBS/Normalisierung). Keine Leerzeichen."
            autofocus
            class="mb-4"
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
  const raw = name.value.trim()
  if (!raw) return
  const created = await store.createTagPath(raw, parentId.value, description.value)
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

.tags-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.tags-label {
  font-size: 0.75rem;
  color: var(--adb-text-secondary);
}

.tags-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.tags-empty {
  color: var(--adb-text-tertiary);
  font-size: 0.85rem;
}

.tags-tree {
  max-height: 220px;
  overflow-y: auto;
  border: 1px solid var(--adb-border);
  border-radius: 4px;
  padding: 4px 2px;
  margin-bottom: 10px;
  background-color: var(--adb-bg-chip);
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
  background-color: var(--adb-bg-primary) !important;
  color: var(--adb-text-primary) !important;
}

.tag-title {
  color: var(--adb-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.cancel-btn {
  color: var(--adb-text-secondary) !important;
}

.confirm-btn {
  color: var(--adb-accent) !important;
}
</style>
