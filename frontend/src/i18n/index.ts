import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

export const DEFAULT_LOCALE = 'zh-CN'
export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const
export type AppLocale = (typeof SUPPORTED_LOCALES)[number]

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
}

const i18n = createI18n({
  legacy: false,
  locale: DEFAULT_LOCALE,
  fallbackLocale: 'zh-CN',
  messages,
})

export default i18n




// import { createI18n } from 'vue-i18n'
// import enUS from './locales/en-US'

// export const DEFAULT_LOCALE = 'en-US'
// export const SUPPORTED_LOCALES = ['en-US'] as const
// export type AppLocale = (typeof SUPPORTED_LOCALES)[number]

// const messages = {
//   'en-US': enUS,
// }

// const i18n = createI18n({
//   legacy: false,
//   locale: DEFAULT_LOCALE,
//   fallbackLocale: 'en-US',
//   messages,
// })

// export default i18n