<template>
  <div class="template-editor">
    <div class="template-editor-header">
      <span class="template-editor-label">Darstellung</span>
      <span
        v-if="validationMsg"
        class="template-validation"
        :class="{ valid: !validationMsg, invalid: validationMsg }"
      >{{ validationMsg }}</span>
      <span
        v-else
        class="template-validation valid"
      >✓ Alle Inhalte im Template</span>
      <v-btn
        icon="mdi-eye"
        variant="text"
        size="x-small"
        class="template-preview-btn"
        @click="$emit('preview')"
      />
      <v-btn
        :icon="editMode ? 'mdi-format-list-bulleted' : 'mdi-pencil'"
        variant="text"
        size="x-small"
        class="template-edit-btn"
        :class="{ active: editMode }"
        @click="toggleEditMode"
      />
    </div>
    <div v-if="!editMode">
      <Draggable
        :list="dndList"
        item-key="id"
        class="purpose-list"
        :animation="200"
        ghost-class="purpose-ghost"
        @start="isDragging = true"
        @end="isDragging = false"
        @change="onDndReorder"
      >
        <template #item="{ element }">
          <div
            class="purpose-card"
            :class="{
              split: element.kind === 'split',
              'drag-active': isDragging,
              highlighted: isHighlighted(element)
            }"
          >
            <div v-if="element.kind === 'standalone'" class="card-inner">
              <span class="card-name">{{ element.purposes[0] }}</span>
              <v-menu @update:model-value="(v: boolean) => v ? highlightStandalone(element.purposes[0]) : clearHighlight()">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon="mdi-table-column-plus-before"
                    variant="text"
                    size="x-small"
                    class="card-menu"
                    @mousedown.stop
                  />
                </template>
                <v-list density="compact">
                  <v-list-item @click="splitPurpose(element.purposes[0])">
                    <v-list-item-title>Mit Nächstem teilen</v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>
            </div>
            <div v-else class="card-inner split-inner">
              <div class="split-cell">
                <span class="card-name">{{ element.purposes[0] }}</span>
                <v-menu @update:model-value="(v: boolean) => v ? highlightSingle(element.purposes[0]) : clearHighlight()">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon="mdi-table-column-remove"
                      variant="text"
                      size="x-small"
                      class="card-menu"
                      @mousedown.stop
                    />
                  </template>
                  <v-list density="compact">
                    <v-list-item @click="unsplitPurpose(element.purposes[0])">
                      <v-list-item-title>Aus Split entfernen</v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
              </div>
              <div class="split-gap" />
              <div
                v-if="element.purposes.length >= 2"
                class="split-cell"
              >
                <span class="card-name">{{ element.purposes[1] }}</span>
                <v-menu @update:model-value="(v: boolean) => v ? highlightSingle(element.purposes[1]) : clearHighlight()">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon="mdi-table-column-remove"
                      variant="text"
                      size="x-small"
                      class="card-menu"
                      @mousedown.stop
                    />
                  </template>
                  <v-list density="compact">
                    <v-list-item @click="unsplitPurpose(element.purposes[1])">
                      <v-list-item-title>Aus Split entfernen</v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
              </div>
              <div v-else class="split-cell drop-zone">
                <v-menu>
                  <template #activator="{ props }">
                    <span
                      v-bind="props"
                      class="drop-label"
                    >+ ablegen</span>
                  </template>
                  <v-list density="compact">
                    <v-list-item
                      v-for="a in availablePurposes"
                      :key="a"
                      @click="fillSplitSlot(element, a)"
                    >
                      <v-list-item-title>{{ a }}</v-list-item-title>
                    </v-list-item>
                    <v-list-item
                      v-if="availablePurposes.length === 0"
                      disabled
                    >
                      <v-list-item-title>Keine verfügbar</v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
              </div>
            </div>
          </div>
        </template>
      </Draggable>
    </div>
    <AdbXmlEditor
      v-else
      :model-value="modelValue"
      @update:model-value="(v) => $emit('update:modelValue', v)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import draggable from 'vuedraggable'
import AdbXmlEditor from './AdbXmlEditor.vue'
import { getPurposesFromXml, getSplitsFromXml, buildXmlFromSplits, splitPurposeInXml, unsplitPurposeFromXml, type SplitGroup } from '../../representation/templateXml'
import { useExerciseStore } from '@/stores/exerciseStore'

const Draggable = draggable
const store = useExerciseStore()

const props = defineProps<{
  modelValue: string
  validationMsg: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  preview: []
}>()

interface DndItem {
  id: string
  kind: 'standalone' | 'split'
  purposes: string[]
}

const editMode = ref(false)
const isDragging = ref(false)
const dndList = ref<DndItem[]>([])
const highlighted = ref<Set<string>>(new Set())
let syncing = false

function buildList(xml: string): DndItem[] {
  const groups = getSplitsFromXml(xml)
  return groups.filter(g => g.purposes.length > 0).map(g => {
    const dnd: DndItem = {
      id: g.kind === 'standalone' ? 'p-' + g.purposes[0] : 'splt-' + g.purposes.join('-'),
      kind: g.kind,
      purposes: g.purposes.slice(0, 2)
    }
    return dnd
  })
}

watch(() => props.modelValue, (xml) => {
  if (!xml) return
  if (syncing) { syncing = false; return }
  dndList.value = buildList(xml)
}, { immediate: true })

function onDndReorder() {
  const out: SplitGroup[] = dndList.value.map(item => ({
    purposes: item.purposes.slice(0, 2),
    kind: item.kind
  }))
  const xml = buildXmlFromSplits(out)
  syncing = true
  emit('update:modelValue', xml)
}

function toggleEditMode() {
  editMode.value = !editMode.value
}

const currentPurposes = computed(() => {
  const item = store.selectedInnerItem
  if (!item) return []
  return item.contents.map(c => c.purpose)
})

const availablePurposes = computed(() => {
  const used = new Set<string>()
  for (const item of dndList.value) {
    for (const p of item.purposes) {
      used.add(p)
    }
  }
  return currentPurposes.value.filter(p => !used.has(p))
})

function splitPurpose(purpose: string) {
  clearHighlight()
  const purposes = getPurposesFromXml(props.modelValue)
  const idx = purposes.indexOf(purpose)
  const next = idx >= 0 && idx < purposes.length - 1 ? purposes[idx + 1] : undefined
  const xml = splitPurposeInXml(props.modelValue, purpose, next)
  emit('update:modelValue', xml)
}

function unsplitPurpose(purpose: string) {
  clearHighlight()
  const xml = unsplitPurposeFromXml(props.modelValue, purpose)
  emit('update:modelValue', xml)
}

function fillSplitSlot(item: DndItem, purpose: string) {
  clearHighlight()
  const splits = getSplitsFromXml(props.modelValue)
  const merged: SplitGroup[] = splits.map(g => {
    if (g.kind === 'split' && g.purposes.length === 1 && g.purposes[0] === item.purposes[0]) {
      return { purposes: [g.purposes[0], purpose], kind: 'split' }
    }
    return g
  })
  const xml = buildXmlFromSplits(merged)
  emit('update:modelValue', xml)
}

function highlightStandalone(purpose: string) {
  const purposes = getPurposesFromXml(props.modelValue)
  const idx = purposes.indexOf(purpose)
  const set = new Set([purpose])
  if (idx >= 0 && idx < purposes.length - 1) {
    set.add(purposes[idx + 1])
  }
  highlighted.value = set
}

function highlightSingle(purpose: string) {
  highlighted.value = new Set([purpose])
}

function clearHighlight() {
  highlighted.value = new Set()
}

function isHighlighted(element: DndItem): boolean {
  return element.purposes.some(p => highlighted.value.has(p))
}
</script>

<style scoped lang="scss">
.template-editor {
  margin-bottom: 20px;
}

.template-editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.template-editor-label {
  font-size: 11px;
  text-transform: uppercase;
  color: #969696;
  letter-spacing: 0.08em;
  user-select: none;
}

.template-validation {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  user-select: none;
}

.template-validation.valid {
  color: #4caf50;
  background: rgba(76, 175, 80, 0.1);
}

.template-validation.invalid {
  color: #ff7a84;
  background: rgba(255, 122, 132, 0.1);
}

.purpose-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.purpose-card {
  cursor: grab;
  user-select: none;
  background: #2a2a2a;
  border: 1px solid #3c3c3c;
  border-radius: 6px;
  transition: border-color 0.15s, background 0.15s;
}

.purpose-card:active {
  cursor: grabbing;
}

.purpose-card:hover {
  background: #303030;
  border-color: #555;
}

.purpose-card.split {
  border-left: 3px solid #0d6efd;
  background: #262a30;
}

.purpose-card.split:hover {
  background: #2b3038;
}

.purpose-card.highlighted {
  border-color: #ffc107;
  box-shadow: 0 0 0 2px rgba(255, 193, 7, 0.35);
}

.purpose-card.highlighted.split {
  border-left-color: #ffc107;
}

.purpose-card.drag-active .drop-zone {
  border-color: #0d6efd;
  background: rgba(13, 110, 253, 0.08);
}

.card-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
}

.split-inner {
  padding: 0;
}

.split-cell {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  min-width: 0;
}

.split-gap {
  width: 1px;
  align-self: stretch;
  background: #3c3c3c;
  flex-shrink: 0;
}

.drop-zone {
  border: 2px dashed #495057;
  border-radius: 4px;
  justify-content: center;
  margin: 4px;
  transition: border-color 0.15s, background 0.15s;
}

.drop-zone:hover {
  border-color: #0d6efd;
  background: rgba(13, 110, 253, 0.06);
}

.drop-label {
  font-size: 11px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  cursor: pointer;
  white-space: nowrap;
}

.card-name {
  flex: 1;
  font-size: 13px;
  color: #d4d4d4;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-menu {
  color: #555 !important;
  opacity: 0;
  transition: opacity 0.15s, color 0.15s;
  flex-shrink: 0;
}

.purpose-card:hover .card-menu {
  opacity: 1;
}

.card-menu:hover {
  color: #999 !important;
}

.purpose-ghost {
  opacity: 0.3;
  border: 2px dashed #007fd4;
  background: rgba(0, 127, 212, 0.08);
  border-radius: 6px;
}

.template-edit-btn {
  color: #666 !important;
  transition: color 0.15s;
  margin-left: auto !important;
}

.template-edit-btn:hover {
  color: #999 !important;
}

.template-edit-btn.active {
  color: #007fd4 !important;
}

.template-preview-btn {
  color: #666 !important;
  transition: color 0.15s;
}

.template-preview-btn:hover {
  color: #999 !important;
}
</style>
