import type { Router } from 'vue-router'
import { watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import i18n from '@/i18n'

function getPageTitle(router: Router) {
  const currentRoute = router.currentRoute.value
  const appTitle = import.meta.env.VITE_APP_TITLE || 'Online Shopping'
  const titleKey = currentRoute.meta.titleKey
  const fallbackTitle = currentRoute.meta.title

  if (typeof titleKey === 'string' && titleKey.trim()) {
    const localizedTitle = i18n.global.t(titleKey)
    return `${localizedTitle} - ${appTitle}`
  }

  if (typeof fallbackTitle === 'string' && fallbackTitle.trim()) {
    return `${fallbackTitle} - ${appTitle}`
  }

  return appTitle
}

function applyPageTitle(router: Router) {
  document.title = getPageTitle(router)
}

export function setupGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    const appTitle = import.meta.env.VITE_APP_TITLE || 'Online Shopping'

    if (typeof to.meta.titleKey === 'string' && to.meta.titleKey.trim()) {
      document.title = `${i18n.global.t(to.meta.titleKey)} - ${appTitle}`
    } else {
      document.title = to.meta.title ? `${to.meta.title} - ${appTitle}` : appTitle
    }

    const userStore = useUserStore()

    if (to.meta.guest && userStore.isLoggedIn) {
      return next('/')
    }

    if (to.meta.requiresAuth || to.matched.some((r) => r.meta.requiresAuth)) {
      if (!userStore.isLoggedIn) {
        ElMessage.warning('Please login first')
        return next({ path: '/login', query: { redirect: to.fullPath } })
      }

      const requiredRole =
        to.meta.requiredRole || to.matched.find((r) => r.meta.requiredRole)?.meta.requiredRole

      if (requiredRole && typeof requiredRole === 'string' && !userStore.hasRole(requiredRole)) {
        ElMessage.error('You do not have permission to access this page')
        return next('/')
      }
    }

    next()
  })

  watch(
    () => i18n.global.locale.value,
    () => {
      applyPageTitle(router)
    },
  )
}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    titleKey?: string
    requiresAuth?: boolean
    requiredRole?: string
    guest?: boolean
  }
}
