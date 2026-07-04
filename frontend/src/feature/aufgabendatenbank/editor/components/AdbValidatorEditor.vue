<template>
  <div v-if="store.selectedInnerItem" class="validator-section">
    <div class="section-header">
      <span class="section-title">Validatoren (Restriktionen)</span>
      <v-btn
        icon="mdi-plus"
        variant="text"
        size="x-small"
        class="add-btn"
        @click="showCreateDialog = true"
      />
    </div>

    <div class="validators-list">
      <div
        v-for="v in linkedValidators"
        :key="v.validatorId"
        class="validator-chip"
      >
        <div class="validator-info">
          <span class="validator-desc">{{ v.description }}</span>
          <span class="validator-rule">{{ v.validator }}</span>
        </div>
        <v-btn
          icon="mdi-close"
          variant="text"
          size="x-small"
          class="remove-btn"
          @click="store.unlinkValidatorFromSelectedItem(v.validatorId)"
        />
      </div>
      <div v-if="linkedValidators.length === 0" class="empty-hint">
        Keine Validatoren zugewiesen
      </div>
    </div>

    <div v-if="unlinkedValidators.length > 0" class="available-section">
      <span class="available-label">Verfügbare Validatoren</span>
      <div class="available-list">
        <div
          v-for="v in unlinkedValidators"
          :key="v.validatorId"
          class="available-chip"
          @click="store.linkValidatorToSelectedItem(v.validatorId)"
        >
          {{ v.description }}
        </div>
      </div>
    </div>

    <v-dialog
      v-model="showCreateDialog"
      max-width="500"
      persistent
    >
      <v-card class="dialog-card">
        <v-card-title class="dialog-title">Neuen Validator erstellen</v-card-title>
        <v-card-text class="dialog-text">
          <v-text-field
            v-model="newDescription"
            label="Beschreibung"
            placeholder="z. B. muss INNER JOIN enthalten"
            variant="outlined"
            density="compact"
            hide-details
            class="mb-3"
          />
          <v-textarea
            v-model="newRule"
            label="Regeltext"
            placeholder="z. B. CHECK(sql_query CONTAINS &quot;INNER JOIN&quot;)"
            variant="outlined"
            density="compact"
            hide-details
            rows="3"
          />
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-btn
            variant="text"
            class="cancel-btn"
            @click="cancelCreate"
          >
            Abbrechen
          </v-btn>
          <v-btn
            variant="text"
            class="save-btn"
            :disabled="!newDescription.trim() || !newRule.trim()"
            @click="confirmCreate"
          >
            Erstellen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useExerciseStore } from '@/stores/exerciseStore'

const store = useExerciseStore()
const showCreateDialog = ref(false)
const newDescription = ref('')
const newRule = ref('')

const linkedValidators = computed(() => {
  const inner = store.selectedInnerItem
  if (!inner) return []
  return store.allValidators.filter((v) => inner.validators.includes(v.validatorId))
})

const unlinkedValidators = computed(() => {
  const inner = store.selectedInnerItem
  if (!inner) return []
  return store.allValidators.filter((v) => !inner.validators.includes(v.validatorId))
})

async function confirmCreate() {
  const desc = newDescription.value.trim()
  const rule = newRule.value.trim()
  if (!desc || !rule) return
  const dto = await store.createValidator(desc, rule)
  if (dto) {
    await store.linkValidatorToSelectedItem(dto.validatorId)
  }
  cancelCreate()
}

function cancelCreate() {
  showCreateDialog.value = false
  newDescription.value = ''
  newRule.value = ''
}
</script>

<style scoped>
.validator-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #333;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #969696;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.add-btn {
  color: #007fd4 !important;
}

.validators-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.validator-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #2a2a2a;
  border: 1px solid #444;
  border-radius: 4px;
  padding: 8px 10px;
  gap: 8px;
}

.validator-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.validator-desc {
  font-size: 13px;
  color: #cccccc;
  font-weight: 500;
}

.validator-rule {
  font-size: 11px;
  color: #888;
  font-family: monospace;
}

.remove-btn {
  color: #c04040 !important;
  flex-shrink: 0;
}

.empty-hint {
  font-size: 12px;
  color: #666;
  font-style: italic;
  padding: 4px 0;
}

.available-section {
  margin-top: 8px;
}

.available-label {
  font-size: 11px;
  color: #777;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  display: block;
  margin-bottom: 6px;
}

.available-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.available-chip {
  font-size: 12px;
  color: #aaa;
  background-color: #252525;
  border: 1px dashed #555;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  transition: all 0.15s;
}

.available-chip:hover {
  color: #007fd4;
  border-color: #007fd4;
  background-color: #0d2b45;
}

.dialog-card {
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.dialog-title {
  color: #cccccc;
  font-size: 16px;
  font-weight: 600;
}

.dialog-text {
  padding-top: 16px !important;
}

.dialog-actions {
  padding: 8px 16px 16px !important;
}

.cancel-btn {
  color: #969696 !important;
}

.save-btn {
  color: #007fd4 !important;
}
</style>
