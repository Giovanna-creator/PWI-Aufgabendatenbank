import { defineStore } from 'pinia'
import { useTheme } from 'vuetify'
import { watch } from 'vue'

const STORAGE_KEY = 'adb-theme'

function getInitialTheme(): string {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'dark' || stored === 'light') return stored
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark'
  return 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const theme = useTheme()
  const current = theme.global.name

  function init() {
    const saved = getInitialTheme()
    theme.global.name.value = saved
  }

  function toggle() {
    const next = theme.global.name.value === 'dark' ? 'light' : 'dark'
    theme.global.name.value = next
    localStorage.setItem(STORAGE_KEY, next)
  }

  const isDark = () => theme.global.name.value === 'dark'

  watch(() => theme.global.name.value, (val) => {
    document.documentElement.setAttribute('data-theme', val as string)
  }, { immediate: true })

  return { init, toggle, isDark, current }
})
