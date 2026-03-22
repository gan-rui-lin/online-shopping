<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useLocaleStore } from '@/stores/locale'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const localeStore = useLocaleStore()
const { t } = useI18n()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const nickname = computed(() => userStore.nickname)
const isMerchant = computed(() => userStore.isMerchant)
const isAdmin = computed(() => userStore.isAdmin)
const isDark = computed(() => themeStore.mode === 'dark')
const isZhCN = computed(() => localeStore.isZhCN)
const keyword = defineModel<string>('keyword', { default: '' })

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

function handleCommand(cmd: string) {
  switch (cmd) {
    case 'profile':
      router.push('/buyer/profile')
      break
    case 'orders':
      router.push('/buyer/orders')
      break
    case 'cart':
      router.push('/buyer/cart')
      break
    case 'intelligence':
      router.push('/buyer/intelligence')
      break
    case 'merchant':
      router.push('/merchant')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

function doSearch() {
  router.push({ path: '/products', query: keyword.value ? { keyword: keyword.value } : undefined })
}
</script>

<template>
  <div class="public-layout">
    <el-header class="header glass-bar">
      <div class="header-inner container taobao-top">
        <div class="logo" @click="goHome">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span class="logo-text">{{ t('publicLayout.logo') }}</span>
        </div>

        <nav class="nav-links quick-nav">
          <router-link to="/">{{ t('common.home') }}</router-link>
          <router-link to="/products">{{ t('common.products') }}</router-link>
          <router-link to="/buyer/intelligence">{{ t('publicLayout.aiHub') }}</router-link>
        </nav>

        <div class="search-wrap">
          <el-input v-model="keyword" :placeholder="t('publicLayout.searchPlaceholder')" @keyup.enter="doSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="doSearch">{{ t('common.search') }}</el-button>
            </template>
          </el-input>
        </div>

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
          <el-button text @click="themeStore.toggleTheme">
            <el-icon><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
            <span>{{ isDark ? t('common.lightMode') : t('common.darkMode') }}</span>
          </el-button>
          <template v-if="isLoggedIn">
            <router-link to="/buyer/cart" class="cart-link">
              <el-icon :size="20"><ShoppingCart /></el-icon>
              <span>{{ t('common.cart') }}</span>
            </router-link>
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                <span>{{ nickname }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">{{ t('publicLayout.profile') }}</el-dropdown-item>
                  <el-dropdown-item command="orders">{{ t('publicLayout.orders') }}</el-dropdown-item>
                  <el-dropdown-item command="cart">{{ t('publicLayout.cart') }}</el-dropdown-item>
                  <el-dropdown-item command="intelligence">{{ t('publicLayout.intelligence') }}</el-dropdown-item>
                  <el-dropdown-item v-if="isMerchant" command="merchant" divided>
                    {{ t('publicLayout.merchantCenter') }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin" divided>
                    {{ t('publicLayout.adminPanel') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>{{ t('common.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login">
              <el-button type="primary" text>{{ t('common.login') }}</el-button>
            </router-link>
            <router-link to="/register">
              <el-button text>{{ t('common.register') }}</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </el-header>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2026 {{ t('publicLayout.logo') }}. {{ t('common.allRightsReserved') }}.</p>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.public-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 76px;
  padding: 0;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 700;
  font-size: $font-size-xl;
  flex-shrink: 0;

  .logo-text {
    white-space: nowrap;
  }
}

.nav-links {
  display: flex;
  gap: 16px;

  a {
    color: var(--text-regular);
    font-size: $font-size-base;
    transition: color 0.2s, transform 0.2s;

    &:hover,
    &.router-link-active {
      color: var(--primary-color);
      text-decoration: none;
      transform: translateY(-1px);
    }
  }
}

.search-wrap {
  flex: 1;
  max-width: 520px;

  :deep(.el-input-group__append .el-button) {
    background: linear-gradient(120deg, #ff7b00, #ff5000);
    color: #fff;
    border: none;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-regular);

  &:hover {
    color: var(--primary-color);
    text-decoration: none;
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--text-regular);
  font-size: $font-size-base;

  &:hover {
    color: var(--primary-color);
  }
}

.main-content {
  flex: 1;
}

.footer {
  background: var(--bg-header);
  border-top: 1px solid var(--border-color);
  padding: 20px 0;
  text-align: center;
  color: var(--text-secondary);
  font-size: $font-size-sm;
}

:deep(.page-fade-enter-active),
:deep(.page-fade-leave-active) {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

:deep(.page-fade-enter-from),
:deep(.page-fade-leave-to) {
  opacity: 0;
  transform: translateY(6px);
}

@media (max-width: 1200px) {
  .quick-nav {
    display: none;
  }
}
</style>
