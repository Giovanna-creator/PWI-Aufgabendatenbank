<template>
  <div class="content-card">
    <div class="card-header">
      <div
        v-if="!editingPurpose"
        class="purpose-group"
        @click.stop="startEditPurpose"
      >
        <span class="purpose-badge">{{ content.purpose }}</span>
        <v-icon
          icon="mdi-pencil-outline"
          size="14"
          class="edit-hint"
        />
      </div>
      <div
        v-else
        class="field-wrapper"
      >
        <span class="field-label">Zweck</span>
        <input
          ref="purposeInput"
          :value="content.purpose"
          class="purpose-input"
          @input="onPurposeInput"
          @blur="editingPurpose = false"
          @keydown.enter="editingPurpose = false"
          @keydown.escape="editingPurpose = false"
        >
      </div>
      <div class="header-actions">
        <v-btn
          icon="mdi-close"
          variant="text"
          size="x-small"
          class="delete-btn"
          @click.stop="emit('delete')"
        />
      </div>
    </div>

    <div class="card-divider" />

    <div class="card-body">
      <div
        v-if="!editingText"
        class="content-text"
        @click="startEditText"
      >
        <span class="content-label">{{ displayText || 'Klicken zum Bearbeiten...' }}</span>
        <v-icon
          icon="mdi-pencil-outline"
          size="14"
          class="edit-hint"
        />
      </div>
      <div
        v-else
        class="field-wrapper"
      >
        <span class="field-label field-label--content">Inhalt</span>
        <textarea
          ref="textInput"
          :value="displayText"
          class="content-textarea"
          placeholder="Klicken zum Bearbeiten..."
          @input="onTextInput"
          @blur="editingText = false"
          @keydown.escape="editingText = false"
        />
      </div>

      <div class="content-meta-row">
        <v-select
          :model-value="content.contentTypeId"
          :items="store.contentTypes"
          item-title="name"
          item-value="id"
          label="Inhaltstyp"
          density="compact"
          variant="outlined"
          hide-details
          class="content-meta-select"
          @update:model-value="(v) => emit('update:meta', { contentTypeId: v })"
        />
        <v-select
          :model-value="content.licenseId"
          :items="store.licenses"
          item-title="name"
          item-value="id"
          label="Lizenz"
          density="compact"
          variant="outlined"
          hide-details
          class="content-meta-select"
          @update:model-value="(v) => emit('update:meta', { licenseId: v })"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import type { Content } from '@/lib/types'
import { useExerciseStore } from '@/stores/exerciseStore'

const props = defineProps<{
  content: Content
}>()

const emit = defineEmits<{
  'update:text': [value: string]
  'update:purpose': [value: string]
  'update:meta': [meta: { contentTypeId?: string; licenseId?: string }]
  delete: []
}>()

const store = useExerciseStore()

const editingText = ref(false)
const editingPurpose = ref(false)
const purposeInput = ref<HTMLInputElement | null>(null)
const textInput = ref<HTMLTextAreaElement | null>(null)

const displayText = computed(() => props.content.jsonContent?.text ?? '')

function onPurposeInput(e: Event) {
  emit('update:purpose', (e.target as HTMLInputElement).value)
}

function onTextInput(e: Event) {
  emit('update:text', (e.target as HTMLTextAreaElement).value)
}

function startEditPurpose() {
  editingPurpose.value = true
  nextTick(() => purposeInput.value?.focus())
}

function startEditText() {
  editingText.value = true
  nextTick(() => {
    const el = textInput.value
    if (el) {
      el.focus()
      el.setSelectionRange(el.value.length, el.value.length)
    }
  })
}
</script>

<style scoped lang="scss">
.content-card {
  background: #2d2d2d;
  border: 1px solid #3c3c3c;
  border-radius: 24px;
  padding: 12px 16px;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.purpose-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  margin: 0 -8px;
  border-radius: 8px;
  transition: background 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.04);
    outline: 1px dashed rgba(255, 255, 255, 0.12);
    outline-offset: -1px;
  }
}

.purpose-badge {
  display: inline-block;
  color: #cccccc;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
  user-select: none;
}

.field-wrapper {
  flex: 1;
  position: relative;
}

.field-label {
  position: absolute;
  top: 3px;
  left: 12px;
  font-size: 8px;
  line-height: 1;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  pointer-events: none;
}

.field-label--content {
  top: 5px;
}

.purpose-input {
  all: unset;
  display: block;
  width: 100%;
  padding: 11px 8px 2px 12px;
  color: #cccccc;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
  border-bottom: 1px solid #007fd4;
  outline: none;
  box-sizing: border-box;
}

.edit-hint {
  color: #555;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;
}

.purpose-group:hover .edit-hint,
.content-text:hover .edit-hint {
  opacity: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 24px;
}

.card-divider {
  height: 0;
  border: none;
  border-bottom: 1px solid #3c3c3c;
  margin: 6px -16px 8px;
}

.delete-btn {
  color: #666 !important;
  opacity: 0.7;
  transition:
    opacity 0.15s,
    color 0.15s,
    background 0.15s;

  &:hover {
    opacity: 1;
    color: #ff7a84 !important;
  }
}

.card-body {
  padding-left: 0;
}

.content-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.content-meta-select {
  flex: 1 1 140px;
  min-width: 120px;
}

.content-text {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 15px;
  line-height: 1.6;
  color: #cccccc;
  cursor: pointer;
  padding: 8px 10px;
  margin: 0 -10px;
  border-radius: 8px;
  word-wrap: break-word;
  transition: background 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.04);
    outline: 1px dashed rgba(255, 255, 255, 0.12);
    outline-offset: -1px;
  }
}

.content-label {
  flex: none;
}

.content-textarea {
  all: unset;
  display: block;
  width: 100%;
  box-sizing: border-box;
  font-size: 15px;
  line-height: 1.6;
  color: #cccccc;
  background: #1e1e1e;
  border: 1px solid #007fd4;
  border-radius: 8px;
  padding: 14px 12px 6px 12px;
  resize: vertical;
  min-height: 48px;
  font-family: inherit;

  &::placeholder {
    color: #666;
  }
}
</style>
