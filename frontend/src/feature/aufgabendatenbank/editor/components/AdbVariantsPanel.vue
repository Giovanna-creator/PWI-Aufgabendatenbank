<template>
  <!-- Auch bei 0 Varianten zeigen, sonst lässt sich die erste Variante nie
       anlegen (der Erstell-Button hing vorher selbst am v-if). -->
  <div
    v-if="store.selectedInnerItem && !store.isCollectionSelected"
    class="variants-panel"
  >
    <div class="variants-header">
      <span class="variants-title">Varianten</span>
      <span class="variants-count">{{ store.variants.length }}</span>
    </div>

    <div class="variants-grid">
      <div
        v-for="(variant, vi) in store.variants"
        :key="variant.id"
        class="variant-card"
      >
        <div class="variant-card-header">
          <span class="variant-label">
            Variante {{ vi + 1 }}
          </span>
          <span class="variant-id">ID: {{ variant.id?.slice(0, 8) }}...</span>
        </div>

        <div class="variant-contents">
          <AdbContentEditor
            v-for="(content, ci) in variant.contents"
            :key="content.id ?? ci"
            :content="content"
            :index="ci"
            @update:text="(val) => store.updateVariantText(vi, ci, val)"
            @update:purpose="(val) => store.updateVariantPurpose(vi, ci, val)"
            @delete="store.removeVariantContent(vi, ci)"
          />

          <button
            type="button"
            class="add-content-btn"
            @click="store.addVariantContent(vi)"
          >
            <span class="btn-inner">
              <v-icon
                icon="mdi-plus"
                size="16"
              />
              <span>Inhalt hinzufügen</span>
            </span>
          </button>
        </div>
      </div>
    </div>

    <button
      type="button"
      class="add-variant-btn"
      @click="onAddVariant"
    >
      <span class="btn-inner">
        <v-icon
          icon="mdi-plus"
          size="18"
        />
        <span>Variante erstellen</span>
      </span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useExerciseStore } from '@/stores/exerciseStore'
import AdbContentEditor from './AdbContentEditor.vue'

const store = useExerciseStore()

function onAddVariant() {
  const inner = store.selectedInnerItem
  if (!inner) return
  const baseId = inner.rootItemId ?? inner.id
  store.createVariant(baseId)
}
</script>

<style scoped lang="scss">
.variants-panel {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--adb-border);
}

.variants-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.variants-title {
  font-size: 15px;
  font-weight: 600;
    color: var(--adb-text-primary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.variants-count {
  background: var(--adb-bg-field);
  color: var(--adb-text-tertiary);
  font-size: 11px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 10px;
}

.variants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.variant-card {
  background: var(--adb-bg-secondary);
  border: 1px solid var(--adb-border);
  border-radius: 16px;
  padding: 12px;
}

.variant-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--adb-border);
}

.variant-label {
  font-size: 13px;
  font-weight: 600;
    color: var(--adb-text-tertiary);
}

.variant-id {
  font-size: 10px;
  color: var(--adb-text-muted);
  font-family: monospace;
}

.variant-contents {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.add-content-btn {
  all: unset;
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
  min-height: 36px;
  padding: 0 12px;
  border-radius: 16px;
  color: var(--adb-text-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px dashed var(--adb-border);
  margin-top: 4px;

  &:hover {
  color: var(--adb-text-tertiary);
    border-color: var(--adb-icon);
    background: rgba(255, 255, 255, 0.02);
  }
}

.add-variant-btn {
  all: unset;
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 0 16px;
  border-radius: 22px;
  color: var(--adb-text-tertiary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px dashed var(--adb-border-light);

  &:hover {
  color: var(--adb-text-primary);
    border-color: var(--adb-text-tertiary);
    background: rgba(255, 255, 255, 0.02);
  }
}

.btn-inner {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
