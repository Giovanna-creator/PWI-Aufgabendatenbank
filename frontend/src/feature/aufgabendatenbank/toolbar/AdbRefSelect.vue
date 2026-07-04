<template>
  <div class="ref-row">
    <v-select
      :model-value="modelValue"
      :items="items"
      :item-title="itemTitle"
      item-value="id"
      :label="label"
      variant="outlined"
      density="compact"
      hide-details
      class="ref-select"
      @update:model-value="onSelect"
    />
    <v-btn
      variant="tonal"
      color="primary"
      prepend-icon="mdi-plus"
      class="ref-new-btn"
      @click="dialog = true"
    >
      Neu
    </v-btn>

    <v-dialog
      v-model="dialog"
      max-width="420"
    >
      <v-card class="ref-card">
        <v-card-title class="ref-title">
          {{ titleText }}
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="primary"
            label="Name"
            variant="outlined"
            density="compact"
            hide-details
            autofocus
            class="mb-3"
            @keyup.enter="confirm"
          />
          <v-text-field
            v-if="secondaryLabel"
            v-model="secondary"
            :label="secondaryLabel"
            variant="outlined"
            density="compact"
            hide-details
            @keyup.enter="confirm"
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
            :disabled="!primary.trim()"
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
import { ref, computed } from 'vue'
import { useExerciseStore } from '@/stores/exerciseStore'

type RefType = 'author' | 'license' | 'itemType' | 'contentType'

const props = defineProps<{
  modelValue: string | null | undefined
  label: string
  items: unknown[]
  itemTitle: string
  type: RefType
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string | undefined] }>()

const store = useExerciseStore()
const dialog = ref(false)
const primary = ref('')
const secondary = ref('')

// v-select kann null liefern (z. B. beim Leeren) -> auf undefined normalisieren.
function onSelect(v: unknown) {
  emit('update:modelValue', (v as string | null) ?? undefined)
}

const titleText = computed(
  () =>
    ({
      author: 'Neuer Autor',
      license: 'Neue Lizenz',
      itemType: 'Neuer Typ',
      contentType: 'Neuer Inhaltstyp'
    })[props.type]
)

// Lizenz hat kein zweites Feld (Schema: nur der Name).
const secondaryLabel = computed(() => {
  if (props.type === 'author') return 'Mail'
  if (props.type === 'license') return ''
  return 'Beschreibung'
})

function close() {
  dialog.value = false
  primary.value = ''
  secondary.value = ''
}

async function confirm() {
  if (!primary.value.trim()) return
  const created = await store.createReference(props.type, primary.value, secondary.value)
  // Bei Erfolg auswählen und schliessen; sonst offen lassen (Fehler kommt als Notification)
  if (created) {
    emit('update:modelValue', created.id)
    close()
  }
}
</script>

<style scoped>
.ref-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ref-select {
  flex: 1 1 auto;
}

.ref-new-btn {
  flex: 0 0 auto;
}

.ref-card {
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.ref-title {
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
