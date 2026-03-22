<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useLocaleStore } from '@/stores/locale'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const localeStore = useLocaleStore()
const { t } = useI18n()
const nickname = computed(() => userStore.nickname)
const isMerchant = computed(() => userStore.isMerchant)
const isAdmin = computed(() => userStore.isAdmin)
const isZhCN = computed(() => localeStore.isZhCN)

const menuItems = computed(() => [
  { path: '/buyer/profile', label: t('buyerLayout.myProfile'), icon: 'User' },
  { path: '/buyer/orders', label: t('buyerLayout.myOrders'), icon: 'Document' },
  { path: '/buyer/cart', label: t('buyerLayout.shoppingCart'), icon: 'ShoppingCart' },
  { path: '/buyer/intelligence', label: t('buyerLayout.intelligenceHub'), icon: 'MagicStick' },
  { path: '/buyer/addresses', label: t('buyerLayout.myAddresses'), icon: 'Location' },
  { path: '/buyer/merchant-apply', label: t('buyerLayout.becomeMerchant'), icon: 'Shop' },
])

function switchLocale(locale: 'zh-CN' | 'en-US') {
  localeStore.applyLocale(locale)
}

function goHome() {
  router.push('/')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="buyer-layout">
    <el-header class="header">
      <div class="header-inner container">
        <div class="logo" @click="goHome">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span class="logo-text">{{ t('publicLayout.logo') }}</span>
        </div>
        <nav class="nav-links">
          <router-link to="/">{{ t('common.home') }}</router-link>
          <router-link to="/products">{{ t('common.products') }}</router-link>
        </nav>
        <div class="header-right">
          <el-dropdown trigger="click" @command="switchLocale">
            <el-button text>
              <el-icon><Operation /></el-icon>
              <span>{{ isZhCN ? t('common.chinese') : t('common.english') }}</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-CN">{{ t('common.chinese') }}</el-dropdown-item>
                <el-dropdown-item command="en-US">{{ t('common.english') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown @command="(cmd: string) => cmd === 'logout' ? handleLogout() : router.push(cmd === 'merchant' ? '/merchant' : cmd === 'admin' ? '/admin' : '/')" trigger="click">
            <span class="user-dropdown">
              <el-icon><User /></el-icon>
              <span>{{ nickname }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isMerchant" command="merchant">{{ t('buyerLayout.merchantCenter') }}</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">{{ t('buyerLayout.adminPanel') }}</el-dropdown-item>
                <el-dropdown-item command="logout" divided>{{ t('common.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <div class="body-wrapper container">
      <aside class="sidebar">
        <el-menu :default-active="route.path" router>
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </aside>
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.buyer-layout {
  min-height: 100vh;
  background: $bg-color;
}

.header {
  background: $bg-white;
  box-shadow: $shadow-sm;
  position: sticky;
  top: 0;
  z-index: 100;
  height: $header-height;
  padding: 0;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: $primary-color;
  font-weight: 700;
  font-size: $font-size-xl;
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  gap: 24px;

  a {
    color: $text-regular;
    &:hover, &.router-link-active { color: $primary-color; text-decoration: none; }
  }
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: $text-regular;
  &:hover { color: $primary-color; }
}

.body-wrapper {
  display: flex;
  gap: 20px;
  padding-top: 20px;
  padding-bottom: 20px;
}

.sidebar {
  width: $sidebar-width;
  flex-shrink: 0;

  .el-menu {
    border-right: none;
    border-radius: $border-radius-lg;
    background: $bg-white;
    box-shadow: $shadow-sm;
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}
</style>
