import { defineStore } from 'pinia'
import { ref } from 'vue'

const THEME_KEY = 'online-shopping-theme'

type ThemeMode = 'light' | 'dark'

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>('light')

  function applyTheme(nextMode: ThemeMode) {
    mode.value = nextMode
    document.documentElement.setAttribute('data-theme', nextMode)
    localStorage.setItem(THEME_KEY, nextMode)
  }

  function initTheme() {
    const saved = localStorage.getItem(THEME_KEY) as ThemeMode | null
    if (saved === 'dark' || saved === 'light') {
      applyTheme(saved)
      return
    }

    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    applyTheme(prefersDark ? 'dark' : 'light')
  }

  function toggleTheme() {
    applyTheme(mode.value === 'dark' ? 'light' : 'dark')
  }

  return {
    mode,
    initTheme,
    toggleTheme,
    applyTheme,
  }
})
