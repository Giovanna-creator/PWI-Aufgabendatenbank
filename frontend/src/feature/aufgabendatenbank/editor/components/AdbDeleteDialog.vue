<template>
  <v-dialog
    v-model="internalVisible"
    max-width="400"
    persistent
  >
    <v-card class="delete-dialog-card">
      <v-card-title class="delete-dialog-title">
        {{ title }}
      </v-card-title>
      <v-card-text class="delete-dialog-text">
        {{ message }}
      </v-card-text>
      <v-card-actions class="delete-dialog-actions">
        <v-spacer />
        <v-btn
          variant="text"
          class="cancel-btn"
          @click="onCancel"
        >
          Abbrechen
        </v-btn>
        <v-btn
          variant="flat"
          class="confirm-btn"
          @click="onConfirm"
        >
          Löschen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  message?: string
}>(), {
  title: 'Löschen bestätigen',
  message: 'Sind Sie sicher, dass Sie dieses Element löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.'
})

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const internalVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

function onConfirm() {
  emit('confirm')
  emit('update:visible', false)
}

function onCancel() {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.delete-dialog-card {
  background-color: var(--adb-bg-primary) !important;
  color: var(--adb-text-primary) !important;
  border: 1px solid var(--adb-border);
}

.delete-dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--adb-text-primary);
  padding: 20px 24px 8px;
}

.delete-dialog-text {
  font-size: 13px;
  color: var(--adb-text-tertiary);
  padding: 8px 24px 16px;
  line-height: 1.5;
}

.delete-dialog-actions {
  padding: 8px 16px 16px;
}

.cancel-btn {
  color: var(--adb-text-tertiary) !important;
  text-transform: none;
  font-weight: 500;

  &:hover {
    color: var(--adb-text-primary) !important;
  }
}

.confirm-btn {
  background-color: var(--adb-danger) !important;
  color: var(--adb-text-inverse) !important;
  text-transform: none;
  font-weight: 600;

  &:hover {
    background-color: var(--adb-danger-hover) !important;
  }
}
</style>
