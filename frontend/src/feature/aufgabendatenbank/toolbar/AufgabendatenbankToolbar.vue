<template>
  <v-toolbar
    class="adb-toolbar"
    color="surface"
  >
    <v-toolbar-title class="adb-toolbar-title">
      Aufgabendatenbank
    </v-toolbar-title>
    <v-spacer />

    <!-- Actions for creation and management -->
    <v-btn
      color="primary"
      variant="flat"
      prepend-icon="mdi-plus"
      class="adb-btn-spacing"
      @click="adb?.createItem()"
    >
      Aufgabe erstellen
    </v-btn>
    <v-btn
      color="secondary"
      variant="flat"
      prepend-icon="mdi-folder-plus"
      class="adb-btn-spacing"
      @click="adb?.createCollection()"
    >
      Kollektion erstellen
    </v-btn>
    <v-btn icon="mdi-dots-vertical"/>
  </v-toolbar>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import type { Item, Collection } from '@/lib/types'

const adb = inject<{ createItem: (rootItemId?: string | null) => Item, createCollection: () => Collection }>('adb')
</script>

<style lang="scss" scoped>
.adb-toolbar {
  border-radius: 0.25rem; /* rounded */
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06); /* shadow */
}

.adb-toolbar-title {
  font-weight: 700; /* font-bold */
  font-size: 1.125rem; /* text-lg */
  line-height: 1.75rem;
}

.adb-btn-spacing {
  margin-right: 0.5rem; /* mr-2 */
}
</style>
