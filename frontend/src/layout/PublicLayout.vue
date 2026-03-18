<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const nickname = computed(() => userStore.nickname)
const isMerchant = computed(() => userStore.isMerchant)
const isAdmin = computed(() => userStore.isAdmin)

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
</script>

<template>
  <div class="public-layout">
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
          <template v-if="isLoggedIn">
            <router-link to="/buyer/cart" class="cart-link">
              <el-icon :size="20"><ShoppingCart /></el-icon>
              <span>Cart</span>
            </router-link>
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                <span>{{ nickname }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">My Profile</el-dropdown-item>
                  <el-dropdown-item command="orders">My Orders</el-dropdown-item>
                  <el-dropdown-item command="cart">Shopping Cart</el-dropdown-item>
                  <el-dropdown-item v-if="isMerchant" command="merchant" divided>
                    Merchant Center
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin" divided>
                    Admin Panel
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login">
              <el-button type="primary" text>Login</el-button>
            </router-link>
            <router-link to="/register">
              <el-button text>Register</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </el-header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2026 Online Shopping Platform. All rights reserved.</p>
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

  .logo-text {
    white-space: nowrap;
  }
}

.nav-links {
  display: flex;
  gap: 24px;

  a {
    color: $text-regular;
    font-size: $font-size-base;
    transition: color 0.2s;

    &:hover,
    &.router-link-active {
      color: $primary-color;
      text-decoration: none;
    }
  }
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: $text-regular;

  &:hover {
    color: $primary-color;
    text-decoration: none;
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: $text-regular;
  font-size: $font-size-base;

  &:hover {
    color: $primary-color;
  }
}

.main-content {
  flex: 1;
}

.footer {
  background: $bg-white;
  border-top: 1px solid $border-lighter;
  padding: 20px 0;
  text-align: center;
  color: $text-secondary;
  font-size: $font-size-sm;
}
</style>
