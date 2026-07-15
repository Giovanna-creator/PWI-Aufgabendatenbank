<template>
  <v-card class="adb-editor-card">
    <v-card-title class="adb-editor-title">
      <span>Editor</span>
    </v-card-title>
    <v-card-text class="adb-editor-text">
      <div v-if="store.selectedItem">
        <EditorMetadataBar />

        <div
          v-if="currentTemplateXml !== null"
          class="template-editor-slot"
        >
          <TemplateEditorPanel
            v-model="editedXml"
            :validation-msg="validationMsg"
            @preview="showPreview = true"
          />
        </div>

        <AdbTagsSection
          v-if="inner"
          :item="inner"
        />

        <AdbContentList />
        <AdbValidatorEditor v-if="!store.isCollectionSelected" />
        <AdbVariantsPanel />

        <PreviewDialog
          v-model="showPreview"
          :template-xml="editedXml"
        />

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
import EditorMetadataBar from './components/EditorMetadataBar.vue'
import TemplateEditorPanel from './components/TemplateEditorPanel.vue'
import PreviewDialog from './components/PreviewDialog.vue'
import AdbContentList from './components/AdbContentList.vue'
import AdbValidatorEditor from './components/AdbValidatorEditor.vue'
import AdbTagsSection from './components/AdbTagsSection.vue'
import AdbVariantsPanel from './components/AdbVariantsPanel.vue'
import { useExerciseStore } from '@/stores/exerciseStore'
import { getPurposesFromXml } from '../representation/templateXml'

const store = useExerciseStore()
const editedXml = ref('')
const saveTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const showPreview = ref(false)

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

watch(editedXml, (xml) => {
  if (xml) debounceSave(xml)
})
</script>

<style lang="scss" scoped>
.adb-editor-card {
  height: 100%;
  width: 100%;
  border-radius: 0;
  box-shadow: none !important;
  background-color: var(--adb-bg-primary) !important;
  color: var(--adb-text-primary) !important;
}

.adb-editor-title {
  font-weight: 500;
  font-size: 0.75rem;
  line-height: 16px;
  text-transform: uppercase;
  color: var(--adb-text-secondary);
  border-bottom: 1px solid var(--adb-border);
  padding: 8px 16px !important;
  background-color: var(--adb-bg-secondary);
  user-select: none;
}

.adb-editor-text {
  padding: 28px 36px;
  font-size: 13px;
  color: var(--adb-text-primary);
  overflow-y: auto;
  max-height: 100%;
}

.template-editor-slot {
  margin-bottom: 20px;
}

:deep(.v-text-field) {
  .v-field {
    background-color: var(--adb-bg-field) !important;
    color: var(--adb-text-primary) !important;
    border-radius: 2px;
  }
  .v-label {
    color: var(--adb-text-secondary) !important;
  }
}
</style>
