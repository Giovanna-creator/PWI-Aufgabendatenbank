import { defineStore } from 'pinia'

export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface AppNotification {
  id: string
  message: string
  type: NotificationType
  timeout: number | null
}

interface State {
  notifications: AppNotification[]
}

let nextId = 1

export const useNotificationStore = defineStore('notification', {
  state: (): State => ({
    notifications: []
  }),

  actions: {
    push(
      message: string,
      type: NotificationType = 'info',
      timeout: number | null = 5000
    ) {
      const id = 'notif-' + nextId++
      const notification: AppNotification = { id, message, type, timeout }
      this.notifications.push(notification)
      if (timeout !== null && timeout > 0) {
        setTimeout(() => this.dismiss(id), timeout)
      }
      return id
    },

    dismiss(id: string) {
      const idx = this.notifications.findIndex((n) => n.id === id)
      if (idx !== -1) {
        this.notifications.splice(idx, 1)
      }
    }
  }
})
