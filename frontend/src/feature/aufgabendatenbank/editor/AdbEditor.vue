<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="store.selectedItem">
        <div class="editor-header-row">
          <h3 class="text-h6 mb-2">
            {{ itemTypeLabel }}
          </h3>
          <div
            v-if="store.isCollectionSelected"
            class="order-toggle-area"
          >
            <span class="order-hint">Geordnete Liste</span>
            <div
              class="order-card"
              :class="{ visible: store.isOrdered }"
            >
              <v-btn
                icon="mdi-format-list-numbered"
                variant="text"
                size="small"
                :ripple="false"
                class="order-btn"
                :class="{ active: store.isOrdered }"
                @click="toggleOrder"
              />
            </div>
            <div class="delete-card">
              <v-btn
                variant="text"
                :ripple="false"
                class="delete-btn"
                @click="showDeleteDialog = true"
              >
                Löschen
              </v-btn>
            </div>
          </div>

          <AdbDeleteDialog
            v-model:visible="showDeleteDialog"
            @confirm="deleteSelectedItem"
          />
        </div>

        <div
          v-if="inner"
          class="meta-row"
        >
          <AdbRefSelect
            :model-value="inner.itemTypeId"
            :items="store.itemTypes"
            item-title="name"
            label="Typ"
            type="itemType"
            class="meta-select"
            @update:model-value="(v) => onMeta({ itemTypeId: v })"
          />
          <AdbRefSelect
            :model-value="inner.authorId"
            :items="store.authors"
            item-title="descriptor"
            label="Autor"
            type="author"
            class="meta-select"
            @update:model-value="(v) => onMeta({ authorId: v })"
          />
          <AdbRefSelect
            :model-value="inner.licenseId"
            :items="store.licenses"
            item-title="name"
            label="Lizenz"
            type="license"
            class="meta-select"
            @update:model-value="(v) => onMeta({ licenseId: v })"
          />

        </div>

        <div
          v-if="currentTemplateXml !== null"
          class="template-editor"
        >
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
              @click="showPreview = true"
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
            :model-value="editedXml"
            @update:model-value="onXmlEditorInput"
          />
        </div>

        <AdbContentList />

        <AdbValidatorEditor />

        <AdbVariantsPanel />

        <v-dialog
          v-model="showPreview"
          max-width="960"
          scrollable
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
                @click="showPreview = false"
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
                      class="preview-filename"
                    >{{ c.blobContent }}</div>
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
                    class="preview-filename"
                  >{{ group.contents[0]?.blobContent }}</div>
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

        <p class="text-caption text-grey">
          ID: {{ store.selectedItem.id }}
        </p>
      </div>
      <div v-else>
        Bitte wählen Sie eine Aufgabe oder Kollektion aus dem Struktur-Baum aus.
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue'
import draggable from 'vuedraggable'
import AdbXmlEditor from './components/AdbXmlEditor.vue'
import AdbContentList from './components/AdbContentList.vue'
import AdbVariantsPanel from './components/AdbVariantsPanel.vue'
import AdbValidatorEditor from './components/AdbValidatorEditor.vue'
import AdbDeleteDialog from './components/AdbDeleteDialog.vue'
import AdbRefSelect from '../toolbar/AdbRefSelect.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { getPurposesFromXml, getSplitsFromXml, buildXmlFromSplits, splitPurposeInXml, unsplitPurposeFromXml, type SplitGroup } from '../representation/templateXml'
import { applyTemplateOrder } from '../representation/applyTemplateOrder'

const Draggable = draggable

const store = useExerciseStore()
const showDeleteDialog = ref(false)
const editedXml = ref('')
const saveTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const editMode = ref(false)

interface DndItem {
  id: string
  kind: 'standalone' | 'split'
  purposes: string[]
}

const isDragging = ref(false)
const dndList = ref<DndItem[]>([])
const highlighted = ref<Set<string>>(new Set())

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

let syncing = false

watch(editedXml, (xml) => {
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
  editedXml.value = xml
  debounceSave(xml)
}

const inner = computed(() => store.selectedInnerItem)

function buildDefaultXml(item: { contents: { purpose: string }[] }): string {
  if (item.contents.length === 0) return '<layout>\n</layout>'
  const purposes = item.contents.map(c => `  <purpose>${c.purpose}</purpose>`).join('\n')
  return `<layout>\n${purposes}\n</layout>`
}

const currentTemplateXml = computed(() => {
  const item = inner.value
  if (!item) return null
  return store.templateById(item.representationTemplate) ?? buildDefaultXml(item)
})

watch(currentTemplateXml, (xml) => {
  if (xml !== null && xml !== editedXml.value) editedXml.value = xml
}, { immediate: true })

watch(editedXml, (xml) => {
  store.setLiveTemplateXml(xml)
}, { immediate: true })

onUnmounted(() => {
  store.setLiveTemplateXml(null)
})

const hasDuplicatePurposes = computed(() => {
  if (!editedXml.value) return ''
  const purposes = getPurposesFromXml(editedXml.value)
  const seen = new Set<string>()
  const dupes = new Set<string>()
  for (const p of purposes) {
    if (seen.has(p)) dupes.add(p)
    seen.add(p)
  }
  return dupes.size > 0
    ? `Doppelte Purposes: ${[...dupes].join(', ')}`
    : ''
})

const validationMsg = computed(() => {
  if (hasDuplicatePurposes.value) return hasDuplicatePurposes.value
  const item = inner.value
  if (!item || !editedXml.value) return ''
  const templatePurposes = getPurposesFromXml(editedXml.value)
  const contentPurposes = item.contents.map(c => c.purpose)
  const missing = contentPurposes.filter(p => !templatePurposes.includes(p))
  const orphaned = templatePurposes.filter(p => !contentPurposes.includes(p))
  const parts: string[] = []
  if (missing.length > 0) {
    parts.push(`${missing.length} Inhalt${missing.length > 1 ? 'e' : ''} fehlen im Template: ${missing.join(', ')}`)
  }
  if (orphaned.length > 0) {
    parts.push(`${orphaned.length} Purpose${orphaned.length > 1 ? 's' : ''} im Template ohne passenden Inhalt: ${orphaned.join(', ')}`)
  }
  return parts.join(' | ')
})

function debounceSave(xml: string) {
  if (hasDuplicatePurposes.value) return
  if (saveTimer.value) clearTimeout(saveTimer.value)
  saveTimer.value = setTimeout(() => {
    store.saveEditedTemplateXml(xml)
  }, 800)
}

function onXmlEditorInput(val: string) {
  editedXml.value = val
  debounceSave(val)
}

function toggleEditMode() {
  editMode.value = !editMode.value
}

function onMeta(meta: { authorId?: string; licenseId?: string; itemTypeId?: string }) {
  if (inner.value) store.updateItemMeta(inner.value, meta)
}

const itemTypeLabel = computed(() => {
  if (!store.selectedInnerItem) return ''
  const type = store.selectedInnerItem.item_type
  const label = type === 'collection' ? 'Kollektion' : 'Aufgabe'
  const firstText = store.selectedInnerItem?.contents?.[0]?.jsonContent?.text
  return firstText ? `${label}: ${firstText}` : label
})

function toggleOrder() {
  const coll = store.selectedCollection
  if (coll) {
    store.toggleCollectionOrder(coll)
  }
}

function deleteSelectedItem() {
  if (store.selectedInnerItem) {
    store.deleteItem(store.selectedInnerItem)
  }
}

const currentPurposes = computed(() => {
  const item = inner.value
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

const showPreview = ref(false)
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
  const template = editedXml.value || store.templateById(item.representationTemplate)
  return applyTemplateOrder(item.contents ?? [], template)
})

function splitPurpose(purpose: string) {
  clearHighlight()
  const purposes = getPurposesFromXml(editedXml.value)
  const idx = purposes.indexOf(purpose)
  const next = idx >= 0 && idx < purposes.length - 1 ? purposes[idx + 1] : undefined
  const xml = splitPurposeInXml(editedXml.value, purpose, next)
  editedXml.value = xml
  debounceSave(xml)
}

function unsplitPurpose(purpose: string) {
  clearHighlight()
  const xml = unsplitPurposeFromXml(editedXml.value, purpose)
  editedXml.value = xml
  debounceSave(xml)
}

function fillSplitSlot(item: DndItem, purpose: string) {
  clearHighlight()
  const splits = getSplitsFromXml(editedXml.value)
  const merged: SplitGroup[] = splits.map(g => {
    if (g.kind === 'split' && g.purposes.length === 1 && g.purposes[0] === item.purposes[0]) {
      return { purposes: [g.purposes[0], purpose], kind: 'split' }
    }
    return g
  })
  const xml = buildXmlFromSplits(merged)
  editedXml.value = xml
  debounceSave(xml)
}

function highlightStandalone(purpose: string) {
  const purposes = getPurposesFromXml(editedXml.value)
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

<style lang="scss" scoped>
.adb-editor-card {
  height: 100%;
  width: 100%;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #1e1e1e !important;
  color: #cccccc !important;
}

.adb-editor-title {
  font-weight: 500;
  font-size: 0.75rem;
  line-height: 16px;
  text-transform: uppercase;
  color: #969696;
  border-bottom: 1px solid #333333;
  padding: 8px 16px !important;
  background-color: #252526;
  user-select: none;
}

.adb-editor-text {
  padding: 28px 36px;
  font-size: 13px;
  color: #cccccc;
  overflow-y: auto;
  max-height: 100%;
}

.editor-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

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

.meta-select {
  flex: 1 1 240px;
  min-width: 220px;
}

.order-toggle-area {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-hint {
  font-size: 11px;
  color: #777;
  user-select: none;
  white-space: nowrap;
}

.order-card {
  transition: all 0.15s;
  border-radius: 4px;
  padding: 0;
  line-height: 0;

  &.visible {
    background-color: #0d2b45;
    box-shadow: 0 0 0 1px #007fd4;
  }
}

.delete-card {
  border-radius: 4px;
  padding: 0 14px;
  line-height: 0;
  background-color: #4a1a1a;
  box-shadow: 0 0 0 1px #c04040;
  display: flex;
  align-items: center;
  height: 32px;
  margin-left: 12px;
}

.delete-btn {
  color: #c04040 !important;
  font-size: 13px;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  transition: color 0.15s;
  margin: 0 !important;
  padding: 0 !important;
  min-width: 0 !important;
  height: 32px !important;
  --v-btn-height: 32px;
  background: transparent !important;

  :deep(.v-btn__overlay) {
    display: none;
  }

  :deep(.v-btn__content) {
    padding: 0;
  }

  &:hover {
    color: #e06060 !important;
  }
}

.order-btn {
  color: #666 !important;
  transition: color 0.15s;
  margin: 0 !important;
  padding: 1rem !important;
  min-width: 0 !important;
  width: 22px;
  height: 22px;
  background: transparent !important;

  :deep(.v-btn__overlay) {
    display: none;
  }

  :deep(.v-btn__content) {
    padding: 0;
  }

  :deep(.v-icon) {
    font-size: 22px;
  }

  &:hover {
    color: #999 !important;
  }

  &.active {
    color: #007fd4 !important;

    &:hover {
      color: #1a9aff !important;
    }
  }
}

:deep(.v-text-field) {
  .v-field {
    background-color: #3c3c3c !important;
    color: #cccccc !important;
    border-radius: 2px;
  }
  .v-label {
    color: #969696 !important;
  }
}

.template-preview-btn {
  color: #666 !important;
  transition: color 0.15s;
}

.template-preview-btn:hover {
  color: #999 !important;
}

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

.preview-filename {
  font-size: 13px;
  color: #888;
  font-style: italic;
}

.preview-empty {
  color: #999;
  text-align: center;
  padding: 60px 0;
}
</style>
