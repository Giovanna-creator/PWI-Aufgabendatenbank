<template>
  <v-dialog
    :model-value="modelValue"
    max-width="960"
    scrollable
    @update:model-value="(v) => $emit('update:modelValue', v)"
  >
    <v-card class="preview-dialog">
      <v-card-title class="preview-title">
        <v-icon icon="mdi-eye" size="22" class="preview-title-icon" />
        Vorschau – Inhalte nach Template
        <v-spacer />
        <v-btn
          icon="mdi-close"
          variant="text"
          size="small"
          @click="$emit('update:modelValue', false)"
        />
      </v-card-title>
      <v-divider />
      <v-card-text class="preview-body">
        <template v-for="(group, gIdx) in previewGroups" :key="gIdx">
          <div
            v-if="group.type === 'split'"
            class="preview-split-row"
          >
            <div
              v-for="c in group.contents"
              :key="c.id ?? gIdx + '-' + group.contents.indexOf(c)"
              class="preview-block"
            >
              <div class="preview-block-purpose">{{ c.purpose }}</div>
              <img
                v-if="isImage(c)"
                :src="blobSrc(c)"
                class="preview-img"
                @error.once="() => markImgError(c)"
              />
              <div
                v-else-if="hasBlob(c)"
                class="preview-blob"
              >
                <v-icon
                  icon="mdi-file-pdf-box"
                  size="32"
                  color="#f40"
                />
                <a
                  :href="blobSrc(c)"
                  target="_blank"
                  class="preview-blob-link"
                >
                  <v-icon
                    icon="mdi-download"
                    size="16"
                  />
                  Öffnen / Herunterladen
                </a>
              </div>
              <div
                v-else
                class="preview-block-text"
              >{{ previewText(c) }}</div>
            </div>
          </div>
          <div
            v-else
            :key="group.contents[0]?.id ?? 'g-' + gIdx"
            class="preview-block"
          >
            <div class="preview-block-purpose">{{ group.contents[0]?.purpose }}</div>
            <img
              v-if="isImage(group.contents[0])"
              :src="blobSrc(group.contents[0])"
              class="preview-img"
              @error.once="() => markImgError(group.contents[0])"
            />
            <div
              v-else-if="hasBlob(group.contents[0])"
              class="preview-blob"
            >
              <v-icon
                icon="mdi-file-pdf-box"
                size="32"
                color="#f40"
              />
              <a
                :href="blobSrc(group.contents[0])"
                target="_blank"
                class="preview-blob-link"
              >
                <v-icon
                  icon="mdi-download"
                  size="16"
                />
                Öffnen / Herunterladen
              </a>
            </div>
            <div
              v-else
              class="preview-block-text"
            >{{ previewText(group.contents[0]) }}</div>
          </div>
        </template>
        <p
          v-if="previewGroups.length === 0"
          class="preview-empty"
        >Keine Inhalte vorhanden</p>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Content } from '@/lib/types'
import { useExerciseStore } from '@/stores/exerciseStore'
import { applyTemplateOrder } from '../../representation/applyTemplateOrder'

const props = defineProps<{
  modelValue: boolean
  templateXml: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const store = useExerciseStore()
const imgErrors = ref(new Set<string>())

function previewText(c: { jsonContent?: { text?: string } } | undefined): string {
  if (!c) return ''
  const text = c.jsonContent?.text ?? ''
  return text.length > 120 ? text.slice(0, 120) + '…' : text
}

function hasBlob(c: { blobContent?: string; blobMimeType?: string }): boolean {
  return !!(c.blobMimeType || c.blobContent)
}

function isImage(c: { id?: string; blobContent?: string; blobMimeType?: string; contentType?: string }): boolean {
  if (!hasBlob(c)) return false
  const mime = c.blobMimeType || c.contentType || ''
  return mime.startsWith('image/') && !imgErrors.value.has(c.id ?? '')
}

function blobSrc(c: { id?: string }): string {
  if (!c.id) return ''
  return store.getBlobUrl(c.id)
}

function markImgError(c: { id?: string }) {
  if (c.id) {
    imgErrors.value = new Set([...imgErrors.value, c.id])
  }
}

const previewGroups = computed(() => {
  const item = store.selectedInnerItem
  if (!item) return []
  const template = props.templateXml || store.templateById(item.representationTemplate)
  return applyTemplateOrder(item.contents ?? [], template)
})
</script>

<style scoped lang="scss">
.preview-dialog {
  background-color: #2d2d2d !important;
  color: #d4d4d4;
  border-radius: 16px !important;
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  padding: 14px 20px !important;
  background: #252526;
  border-bottom: 1px solid #3c3c3c;
  flex-shrink: 0;
}

.preview-title-icon {
  color: #007fd4;
}

.preview-body {
  padding: 24px !important;
}

.preview-split-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 20px;
}

.preview-block {
  margin-bottom: 20px;
}

.preview-split-row .preview-block {
  margin-bottom: 0;
}

.preview-block-purpose {
  font-size: 10px;
  font-weight: 600;
  color: #969696;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 6px;
}

.preview-block-text {
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  color: #cccccc;
}

.preview-img {
  max-width: 100%;
  height: auto;
  display: block;
  border-radius: 4px;
}

.preview-blob {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #1e1e1e;
  border-radius: 12px;
}

.preview-blob-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #007fd4;
  text-decoration: none;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
  transition: background 0.15s;
}

.preview-blob-link:hover {
  background: rgba(0, 127, 212, 0.1);
}

.preview-empty {
  color: #999;
  text-align: center;
  padding: 60px 0;
}
</style>
