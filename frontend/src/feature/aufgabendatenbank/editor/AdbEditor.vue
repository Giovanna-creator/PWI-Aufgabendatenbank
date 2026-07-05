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
              v-model="purposeOrder"
              item-key="p"
              class="purpose-draggable"
              :animation="200"
              ghost-class="purpose-ghost"
              handle=".drag-handle"
            >
              <template #item="{ element }">
                <div class="purpose-drag-item">
                  <v-icon icon="mdi-drag" class="drag-handle" size="x-small" />
                  <span class="purpose-name">{{ element }}</span>
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
import { computed, ref, watch } from 'vue'
import draggable from 'vuedraggable'
import AdbXmlEditor from './components/AdbXmlEditor.vue'
import AdbContentList from './components/AdbContentList.vue'
import AdbVariantsPanel from './components/AdbVariantsPanel.vue'
import AdbValidatorEditor from './components/AdbValidatorEditor.vue'
import AdbDeleteDialog from './components/AdbDeleteDialog.vue'
import AdbRefSelect from '../toolbar/AdbRefSelect.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { getPurposesFromXml, buildXmlFromPurposes } from '../representation/templateXml'

const Draggable = draggable

const store = useExerciseStore()
const showDeleteDialog = ref(false)
const editedXml = ref('')
const saveTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const editMode = ref(false)

const purposeOrder = computed({
  get: () => getPurposesFromXml(editedXml.value),
  set: (val) => {
    const xml = buildXmlFromPurposes(val)
    editedXml.value = xml
    debounceSave(xml)
  }
})

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
  if (xml !== null) editedXml.value = xml
}, { immediate: true })

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
  return missing.length > 0
    ? `${missing.length} Inhalt${missing.length > 1 ? 'e' : ''} fehlen im Template: ${missing.join(', ')}`
    : ''
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

.template-textarea {
  width: 100%;
  min-height: 120px;
  box-sizing: border-box;
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 8px;
  color: #d4d4d4;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
  padding: 12px 14px;
  resize: vertical;
  tab-size: 2;
  outline: none;
  transition: border-color 0.15s;
}

.template-textarea:focus {
  border-color: #007fd4;
}

.template-textarea::placeholder {
  color: #555;
}

.purpose-draggable {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.purpose-drag-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #2a2a2a;
  border: 1px solid #3c3c3c;
  border-radius: 6px;
  cursor: default;
  transition: border-color 0.15s, background 0.15s;
  user-select: none;
}

.purpose-drag-item:hover {
  background: #303030;
  border-color: #555;
}

.purpose-ghost {
  opacity: 0.4;
  border-style: dashed;
  border-color: #007fd4;
  background: rgba(0, 127, 212, 0.08);
}

.drag-handle {
  color: #666;
  cursor: grab;
  flex-shrink: 0;
}

.drag-handle:active {
  cursor: grabbing;
}

.purpose-name {
  font-size: 13px;
  color: #d4d4d4;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
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
</style>
