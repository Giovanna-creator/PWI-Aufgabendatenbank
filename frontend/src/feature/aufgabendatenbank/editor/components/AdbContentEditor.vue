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
      <!-- Text content (jsonContent.text) -->
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
        <AdbRefSelect
          :model-value="content.contentTypeId"
          :items="store.contentTypes"
          item-title="name"
          label="Inhaltstyp"
          type="contentType"
          class="content-meta-select"
          @update:model-value="(v) => emit('update:meta', { contentTypeId: v })"
        />
        <AdbRefSelect
          :model-value="content.licenseId"
          :items="store.licenses"
          item-title="name"
          label="Lizenz"
          type="license"
          class="content-meta-select"
          @update:model-value="(v) => emit('update:meta', { licenseId: v })"
        />
      </div>

      <!-- Blob section -->
      <div class="blob-section">
        <!-- Image preview -->
        <div
          v-if="isImageDisplayable"
          class="blob-preview"
        >
          <img
            :src="blobSrc"
            alt="Vorschau"
            class="preview-img"
            @error="onImgError"
          >
          <div class="blob-actions">
            <a
              :href="blobSrc"
              download
              class="blob-action-link"
            >
              <v-icon
                icon="mdi-download"
                size="16"
              />
              Herunterladen
            </a>
          </div>
        </div>

        <!-- PDF / unknown binary download link -->
        <div
          v-if="hasBlob && !isImageDisplayable"
          class="blob-download"
        >
          <v-icon
            icon="mdi-file-pdf-box"
            size="32"
            color="#f40"
          />
          <span class="blob-filename">PDF-Datei</span>
          <a
            :href="blobSrc"
            target="_blank"
            class="blob-action-link"
          >
            <v-icon
              icon="mdi-download"
              size="16"
            />
            Öffnen / Herunterladen
          </a>
        </div>

        <!-- Upload button (when no blob) -->
        <div
          v-if="!hasBlob"
          class="blob-upload"
        >
          <input
            ref="fileInput"
            type="file"
            accept="image/png,image/jpeg,application/pdf"
            class="file-input-hidden"
            @change="onFileSelected"
          >
          <button
            type="button"
            class="upload-btn"
            @click="triggerFilePicker"
          >
            <v-icon
              icon="mdi-upload"
              size="18"
            />
            <span>Datei hochladen (PNG, JPEG, PDF)</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import type { Content } from '@/lib/types'
import { useExerciseStore } from '@/stores/exerciseStore'
import AdbRefSelect from '../../toolbar/AdbRefSelect.vue'

const props = defineProps<{
  content: Content
  index: number
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
const fileInput = ref<HTMLInputElement | null>(null)
const imageError = ref(false)

const displayText = computed(() => props.content.jsonContent?.text ?? '')

const hasBlob = computed(() => {
  return !!(props.content.blobMimeType || props.content.blobContent)
})

const detectedMimeType = computed(() => {
  return props.content.blobMimeType || props.content.contentType || ''
})

const isImage = computed(() => {
  return detectedMimeType.value.startsWith('image/')
})

const isImageDisplayable = computed(() => {
  return hasBlob.value && isImage.value && !imageError.value
})

const blobSrc = computed(() => {
  if (!props.content.id) return ''
  return store.getBlobUrl(props.content.id)
})

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

function triggerFilePicker() {
  fileInput.value?.click()
}

function onFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  store.uploadBlob(props.index, file)
  input.value = ''
}

function onImgError() {
  imageError.value = true
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
  flex: 1 1 240px;
  min-width: 220px;
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

/* ── Blob section ── */

.blob-section {
  margin-top: 8px;
}

.blob-preview {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 10px;
  background: #1e1e1e;
  border-radius: 12px;
}

.preview-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  object-fit: contain;
}

.blob-actions {
  display: flex;
  gap: 8px;
}

.blob-action-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #007fd4;
  text-decoration: none;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
  transition: background 0.15s;

  &:hover {
    background: rgba(0, 127, 212, 0.1);
  }
}

.blob-download {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #1e1e1e;
  border-radius: 12px;
}

.blob-filename {
  font-size: 13px;
  color: #ccc;
  flex: 1;
}

.blob-upload {
  margin-top: 4px;
}

.file-input-hidden {
  display: none;
}

.upload-btn {
  all: unset;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  border: 1px dashed #555;
  transition: all 0.15s;

  &:hover {
    color: #ccc;
    border-color: #888;
    background: rgba(255, 255, 255, 0.03);
  }
}
</style>
