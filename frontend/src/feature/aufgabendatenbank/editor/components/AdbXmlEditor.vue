<template>
  <div class="xml-editor-wrapper">
    <Codemirror
      :model-value="modelValue"
      :style="{ minHeight: '120px' }"
      :extensions="extensions"
      :indent-with-tab="true"
      :tab-size="2"
      :autofocus="true"
      class="xml-codemirror"
      @change="onChange"
    />
  </div>
</template>

<script setup lang="ts">
import { Codemirror } from 'vue-codemirror'
import { xml } from '@codemirror/lang-xml'
import { oneDark } from '@codemirror/theme-one-dark'
import { basicSetup, EditorView } from 'codemirror'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const extensions = [
  basicSetup,
  xml(),
  oneDark,
  EditorView.lineWrapping,
]

function onChange(value: string) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.xml-editor-wrapper {
  min-height: 120px;
}

.xml-codemirror {
  border: 1px solid var(--adb-border);
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.15s;
}

.xml-codemirior:focus-within {
  border-color: var(--adb-accent);
}

.xml-codemirror {
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
}
</style>
