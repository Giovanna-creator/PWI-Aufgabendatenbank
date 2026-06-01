<template>
  <div
    class="content-card"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
  >
    <div class="card-header">
      <span
        v-if="!editingPurpose"
        class="purpose-badge"
        @click.stop="startEditPurpose"
      >
        {{ content.purpose }}
      </span>
      <input
        v-else
        ref="purposeInput"
        :value="content.purpose"
        class="purpose-input"
        @input="onPurposeInput"
        @blur="editingPurpose = false"
        @keydown.enter="editingPurpose = false"
        @keydown.escape="editingPurpose = false"
      />
      <v-btn
        v-if="hovered && !hideDelete"
        icon="mdi-close"
        variant="text"
        size="x-small"
        class="delete-btn"
        @click.stop="emit('delete')"
      />
    </div>

    <div class="card-divider" />

    <div class="card-body">
      <p
        v-if="!editingText"
        class="content-text"
        @click="startEditText"
      >
        {{ displayText || 'Klicken zum Bearbeiten...' }}
      </p>
      <textarea
        v-else
        ref="textInput"
        :value="displayText"
        class="content-textarea"
        @input="onTextInput"
        @blur="editingText = false"
        @keydown.escape="editingText = false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import type { Content } from '@/lib/types'

const props = defineProps<{
  content: Content
  hideDelete?: boolean
}>()

const emit = defineEmits<{
  'update:text': [value: string]
  'update:purpose': [value: string]
  delete: []
}>()

const hovered = ref(false)
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
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 10px;
  transition: border-color 0.15s;

  &:hover {
    border-color: #505050;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.purpose-badge {
  display: inline-block;
  padding: 4px 0;
  color: #969696;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: color 0.15s;
  user-select: none;

  &:hover {
    color: #cccccc;
  }
}

.purpose-input {
  all: unset;
  display: inline-block;
  padding: 4px 0;
  color: #cccccc;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.02em;
  border-bottom: 1px solid #007fd4;
  outline: none;
  min-width: 60px;
}

.card-divider {
  height: 0;
  border: none;
  border-bottom: 1px solid #3c3c3c;
  margin: 6px -16px 8px;
}

.delete-btn {
  flex-shrink: 0;
  color: #555 !important;
  transition: color 0.15s;

  &:hover {
    color: #e06c75 !important;
  }

  :deep(.v-icon) {
    font-size: 14px !important;
  }
}

.card-body {
  padding-left: 0;
}

.content-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #cccccc;
  cursor: pointer;
  padding: 4px 0;
  border-radius: 6px;
  transition: color 0.15s;
  word-wrap: break-word;

  &:hover {
    color: #ffffff;
  }
}

.content-textarea {
  all: unset;
  display: block;
  width: 100%;
  box-sizing: border-box;
  font-size: 13px;
  line-height: 1.6;
  color: #cccccc;
  background: #1e1e1e;
  border: 1px solid #007fd4;
  border-radius: 8px;
  padding: 8px 10px;
  resize: vertical;
  min-height: 40px;
  font-family: inherit;

  &::placeholder {
    color: #666;
  }
}
</style>
