<template>
  <div class="notification-container">
    <v-slide-y-reverse-transition group>
      <v-alert
        v-for="notif in store.notifications"
        :key="notif.id"
        :type="notif.type"
        closable
        dismissible
        class="notification-alert"
        density="compact"
        variant="tonal"
        @click:close="store.dismiss(notif.id)"
      >
        {{ notif.message }}
      </v-alert>
    </v-slide-y-reverse-transition>
  </div>
</template>

<script setup lang="ts">
import { useNotificationStore } from '@/stores/useNotificationStore'

const store = useNotificationStore()
</script>

<style scoped lang="scss">
.notification-container {
  position: fixed;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 600px;
  width: 90%;
  pointer-events: none;
}

.notification-alert {
  pointer-events: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
</style>
