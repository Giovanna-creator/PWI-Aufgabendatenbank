<template>
  <v-dialog
    v-model="internalVisible"
    max-width="420"
    persistent
  >
    <v-card class="confirm-dialog-card">
      <v-card-title class="confirm-dialog-title">
        {{ title }}
      </v-card-title>
      <v-card-text class="confirm-dialog-text">
        {{ message }}
      </v-card-text>
      <v-card-actions class="confirm-dialog-actions">
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
          {{ confirmLabel }}
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
  confirmLabel?: string
}>(), {
  title: 'Bestätigen',
  message: 'Möchten Sie fortfahren?',
  confirmLabel: 'Fortfahren'
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
.confirm-dialog-card {
  background-color: var(--adb-bg-primary) !important;
  color: var(--adb-text-primary) !important;
  border: 1px solid var(--adb-border);
}

.confirm-dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--adb-text-primary);
  padding: 20px 24px 8px;
}

.confirm-dialog-text {
  font-size: 13px;
  color: var(--adb-text-tertiary);
  padding: 8px 24px 16px;
  line-height: 1.5;
  white-space: pre-line;
}

.confirm-dialog-actions {
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
  background-color: var(--adb-accent) !important;
  color: var(--adb-text-inverse) !important;
  text-transform: none;
  font-weight: 600;

  &:hover {
    background-color: var(--adb-accent-hover) !important;
  }
}
</style>
