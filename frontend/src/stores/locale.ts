import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import i18n, { DEFAULT_LOCALE, SUPPORTED_LOCALES, type AppLocale } from '@/i18n'

const LOCALE_KEY = 'online-shopping-locale'

function isSupportedLocale(locale: string): locale is AppLocale {
  return (SUPPORTED_LOCALES as readonly string[]).includes(locale)
}

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<AppLocale>(DEFAULT_LOCALE)

  function applyLocale(nextLocale: AppLocale) {
    locale.value = nextLocale
    i18n.global.locale.value = nextLocale
    localStorage.setItem(LOCALE_KEY, nextLocale)
    document.documentElement.setAttribute('lang', nextLocale)
  }

  function initLocale() {
    const saved = localStorage.getItem(LOCALE_KEY)
    if (saved && isSupportedLocale(saved)) {
      applyLocale(saved)
      return
    }

    const browser = navigator.language
    applyLocale(isSupportedLocale(browser) ? browser : DEFAULT_LOCALE)
  }

  const isZhCN = computed(() => locale.value === 'zh-CN')

  return {
    locale,
    isZhCN,
    initLocale,
    applyLocale,
  }
})
