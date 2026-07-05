<template>
  <div class="xml-editor-wrapper">
    <pre
      class="xml-highlighted-pre"
      aria-hidden="true"
    ><code v-html="highlightedXml" /></pre>
    <textarea
      ref="textareaRef"
      class="xml-textarea"
      :value="modelValue"
      spellcheck="false"
      @input="onInput"
      @keydown="onKeydown"
      @scroll="syncScroll"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

const highlightedXml = computed(() => {
  const escaped = escapeHtml(props.modelValue)
  return escaped.replace(
    /(&lt;)(\/?)([\w-]+)([^&]*?)(\/?)(&gt;)/g,
    (_, open, slash, name, attrs, selfClose, close) => {
      const bracketOpen = `<span class="xml-bracket">${open}${slash}</span>`
      const tagName = `<span class="xml-tag">${name}</span>`
      const attrStr = attrs ? `<span class="xml-attr">${attrs}</span>` : ''
      const bracketClose = `<span class="xml-bracket">${selfClose}${close}</span>`
      return `${bracketOpen}${tagName}${attrStr}${bracketClose}`
    }
  )
})

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLTextAreaElement).value)
}

function getIndentLevel(text: string, cursorPos: number): number {
  const upToCursor = text.slice(0, cursorPos)
  const open = (upToCursor.match(/<(?![!/])/g) || []).length
  const close = (upToCursor.match(/<\//g) || []).length
  return Math.max(0, open - close)
}

function onKeydown(e: KeyboardEvent) {
  const ta = textareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd

  if (e.key === 'Tab') {
    e.preventDefault()
    const newVal = props.modelValue.slice(0, start) + '  ' + props.modelValue.slice(end)
    emit('update:modelValue', newVal)
    nextTick(() => {
      ta.selectionStart = ta.selectionEnd = start + 2
    })
    return
  }

  if (e.key === 'Enter') {
    e.preventDefault()
    const indent = getIndentLevel(props.modelValue, start)
    const indentStr = '\n' + '  '.repeat(indent)
    const newVal = props.modelValue.slice(0, start) + indentStr + props.modelValue.slice(end)
    emit('update:modelValue', newVal)
    nextTick(() => {
      ta.selectionStart = ta.selectionEnd = start + indentStr.length
    })
    return
  }
}

function syncScroll() {
  const ta = textareaRef.value
  if (!ta) return
  const pre = ta.parentElement?.querySelector('.xml-highlighted-pre') as HTMLElement | null
  if (pre) {
    pre.scrollTop = ta.scrollTop
    pre.scrollLeft = ta.scrollLeft
  }
}
</script>

<style scoped>
.xml-editor-wrapper {
  display: grid;
  min-height: 120px;
}

.xml-highlighted-pre,
.xml-textarea {
  grid-area: 1 / 1;
  margin: 0;
  padding: 12px 14px;
  border: 1px solid #3c3c3c;
  border-radius: 8px;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  tab-size: 2;
}

.xml-highlighted-pre {
  z-index: 1;
  pointer-events: none;
  overflow: auto;
  background: #1e1e1e;
  color: #d4d4d4;
}

.xml-highlighted-pre :deep(code) {
  display: block;
  font-family: inherit;
  color: #d4d4d4;
}

.xml-textarea {
  z-index: 2;
  color: transparent;
  caret-color: #d4d4d4;
  background: transparent;
  resize: vertical;
  outline: none;
  border-color: transparent;
}

.xml-textarea:focus {
  border-color: #007fd4;
}

.xml-bracket {
  color: #808080;
}

.xml-tag {
  color: #569cd6;
}

.xml-attr {
  color: #ce9178;
}
</style>
