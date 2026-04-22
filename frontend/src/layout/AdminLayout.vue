<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()
const nickname = computed(() => userStore.nickname)

const menuItems = [
  { path: '/admin', label: 'adminLayout.dashboard', icon: 'DataLine' },
  { path: '/admin/members', label: 'adminLayout.members', icon: 'User' },
  { path: '/admin/orders', label: 'adminLayout.orders', icon: 'Tickets' },
  { path: '/admin/action-logs', label: 'adminLayout.actionLogs', icon: 'Document' },
  { path: '/admin/security', label: 'adminLayout.security', icon: 'Lock' },
  { path: '/admin/merchants', label: 'adminLayout.merchantAudit', icon: 'OfficeBuilding' },
  { path: '/admin/products', label: 'adminLayout.productAudit', icon: 'Goods' },
  { path: '/admin/categories', label: 'adminLayout.categories', icon: 'Menu' },
]

function goHome() {
  router.push('/')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header" @click="goHome">
        <el-icon :size="20"><Setting /></el-icon>
        <span>{{ t('adminLayout.adminPanel') }}</span>
      </div>
      <el-menu :default-active="route.path" router class="sidebar-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ t(item.label) }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-wrapper">
      <el-header class="header">
        <div class="header-inner">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">{{ t('adminLayout.admin') }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div class="header-right">
            <router-link to="/" class="back-link">
              <el-icon><HomeFilled /></el-icon> {{ t('adminLayout.store') }}
            </router-link>
            <el-dropdown @command="(cmd: string) => cmd === 'logout' ? handleLogout() : null" trigger="click">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                <span>{{ nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">{{ t('common.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.admin-layout {
  display: flex;
  min-height: 100vh;
  background:
    radial-gradient(1200px 400px at -10% -10%, rgba(255, 128, 0, 0.08), transparent 55%),
    radial-gradient(800px 300px at 110% 0%, rgba(45, 110, 255, 0.08), transparent 60%),
    var(--bg-color);
}

.sidebar {
  width: $sidebar-width;
  background: linear-gradient(180deg, #152238 0%, #1f2a40 45%, #212d46 100%);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 26px rgba(16, 24, 40, 0.22);
  z-index: 1;
}

.sidebar-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: $font-size-lg;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding: 10px 8px;

  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.65);
    border-radius: 10px;
    margin-bottom: 4px;

    &:hover, &.is-active {
      color: #fff;
      background: linear-gradient(90deg, #ff6a00 0%, #ff920d 100%);
    }
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: $bg-color;
}

.header {
  background: color-mix(in oklab, var(--bg-card) 86%, transparent);
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 18px rgba(0, 0, 0, 0.06);
  height: $header-height;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-color);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: $text-regular;
  font-size: $font-size-sm;
  &:hover { color: $primary-color; text-decoration: none; }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: $text-regular;
  &:hover { color: $primary-color; }
}

.main-content {
  flex: 1;
  padding: 20px 24px 28px;
}
</style>
