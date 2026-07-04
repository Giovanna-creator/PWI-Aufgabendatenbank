<template>
  <v-dialog
    :model-value="store.createDialogOpen"
    max-width="520"
    @update:model-value="(v) => { if (!v) store.closeCreateDialog() }"
  >
    <v-card class="create-card">
      <v-card-title class="create-title">
        {{ store.createDialogTarget ? 'Neue Aufgabe in Sammlung' : 'Neue Aufgabe' }}
      </v-card-title>
      <v-card-text>
        <AdbRefSelect
          v-model="itemTypeId"
          :items="store.itemTypes"
          item-title="name"
          label="Typ"
          type="itemType"
          class="mb-3"
        />
        <AdbRefSelect
          v-model="authorId"
          :items="store.authors"
          item-title="descriptor"
          label="Autor"
          type="author"
          class="mb-3"
        />
        <AdbRefSelect
          v-model="licenseId"
          :items="store.licenses"
          item-title="name"
          label="Lizenz"
          type="license"
          class="mb-3"
        />
        <v-textarea
          v-model="text"
          label="Aufgabenstellung"
          variant="outlined"
          density="compact"
          hide-details
          rows="3"
        />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          class="cancel-btn"
          @click="store.closeCreateDialog()"
        >
          Abbrechen
        </v-btn>
        <v-btn
          variant="text"
          class="confirm-btn"
          @click="confirm"
        >
          Erstellen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import AdbRefSelect from './AdbRefSelect.vue'

const store = useExerciseStore()

const itemTypeId = ref<string | undefined>(undefined)
const authorId = ref<string | undefined>(undefined)
const licenseId = ref<string | undefined>(undefined)
const text = ref('')

// Beim Öffnen mit Default-Werten vorbelegen (Formular ist nicht strikt:
// Felder dürfen leer bleiben, dann greifen die Defaults).
watch(
  () => store.createDialogOpen,
  (open) => {
    if (open) {
      itemTypeId.value = store.defaultItemTypeId
      authorId.value = store.defaultAuthorId
      licenseId.value = store.defaultLicenseId
      text.value = ''
    }
  }
)

function confirm() {
  store.createItemFromForm(
    {
      itemTypeId: itemTypeId.value,
      authorId: authorId.value,
      licenseId: licenseId.value,
      text: text.value
    },
    store.createDialogTarget
  )
  store.closeCreateDialog()
}
</script>

<style scoped>
.create-card {
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.create-title {
  color: #cccccc;
  font-size: 16px;
  font-weight: 600;
}

.cancel-btn {
  color: #969696 !important;
}

.confirm-btn {
  color: #007fd4 !important;
}
</style>
