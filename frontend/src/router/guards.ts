import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export function setupGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    const appTitle = import.meta.env.VITE_APP_TITLE || 'Online Shopping'
    document.title = to.meta.title ? `${to.meta.title} - ${appTitle}` : appTitle

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
}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    requiredRole?: string
    guest?: boolean
  }
}
