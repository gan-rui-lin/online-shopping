<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const nickname = computed(() => userStore.nickname)
const isMerchant = computed(() => userStore.isMerchant)
const isAdmin = computed(() => userStore.isAdmin)

const menuItems = [
  { path: '/buyer/profile', label: 'My Profile', icon: 'User' },
  { path: '/buyer/orders', label: 'My Orders', icon: 'Document' },
  { path: '/buyer/cart', label: 'Shopping Cart', icon: 'ShoppingCart' },
  { path: '/buyer/addresses', label: 'My Addresses', icon: 'Location' },
  { path: '/buyer/merchant-apply', label: 'Become Merchant', icon: 'Shop' },
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
  <div class="buyer-layout">
    <el-header class="header">
      <div class="header-inner container">
        <div class="logo" @click="goHome">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span class="logo-text">Online Shopping</span>
        </div>
        <nav class="nav-links">
          <router-link to="/">Home</router-link>
          <router-link to="/products">Products</router-link>
        </nav>
        <div class="header-right">
          <el-dropdown @command="(cmd: string) => cmd === 'logout' ? handleLogout() : router.push(cmd === 'merchant' ? '/merchant' : cmd === 'admin' ? '/admin' : '/')" trigger="click">
            <span class="user-dropdown">
              <el-icon><User /></el-icon>
              <span>{{ nickname }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isMerchant" command="merchant">Merchant Center</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">Admin Panel</el-dropdown-item>
                <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
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
