<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="store.selectedItem">
        <h3 class="text-h6 mb-2">
          Edit: {{ itemTitle }}
        </h3>

        <v-text-field
          v-model="itemTitle"
          label="Titel / Aufgabe"
          variant="outlined"
          class="mb-4"
        />

        <v-switch
          v-if="isCollection"
          v-model="isOrdered"
          label="Kollektion sequenziell (geordnet) machen"
          color="primary"
        />

        <p class="text-caption text-grey">
          ID: {{ store.selectedItem.id }}
        </p>
      </div>
      <div v-else>
        Bitte wählen Sie eine Aufgabe oder Kollektion aus dem Struktur-Baum aus.
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()

const isCollection = computed(() => store.isCollectionSelected)

const itemTitle = computed({
  get: () => store.itemTitle,
  set: (val) => store.setItemTitle(val)
})

const isOrdered = computed({
  get: () => store.isOrdered,
  set: (val: boolean) => {
    const coll = store.selectedCollection
    if (coll && coll.order !== val) {
      store.toggleCollectionOrder(coll)
    }
  }
})
</script>

<style lang="scss" scoped>
.adb-editor-card {
  height: 100%;
  width: 100%;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.adb-editor-title {
  font-weight: 500;
  font-size: 0.75rem;
  line-height: 16px;
  text-transform: uppercase;
  color: #969696;
  border-bottom: 1px solid #333333;
  padding: 8px 16px !important;
  background-color: #252526;
  user-select: none;
}

.adb-editor-text {
  padding: 20px;
  font-size: 13px;
  color: #cccccc;
}

:deep(.v-text-field) {
  .v-field {
    background-color: #3c3c3c !important;
    color: #cccccc !important;
    border-radius: 2px;
  }
  .v-label {
    color: #969696 !important;
  }
}
</style>
