<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getDashboard } from '@/api/admin'
import type { DashboardVO } from '@/types/admin'
import { formatPrice } from '@/utils/format'

const stats = ref<DashboardVO | null>(null)
const loading = ref(true)
const { t } = useI18n()

const cards = [
  { key: 'userCount', label: 'admin.totalUsers', icon: 'User', bg: '#e6f7ff', color: '#1890ff' },
  { key: 'merchantCount', label: 'admin.totalMerchants', icon: 'Shop', bg: '#f6ffed', color: '#52c41a' },
  { key: 'productCount', label: 'admin.totalProducts', icon: 'Goods', bg: '#fff7e6', color: '#fa8c16' },
  { key: 'orderCount', label: 'admin.totalOrders', icon: 'Document', bg: '#f9f0ff', color: '#722ed1' },
  { key: 'todayOrderCount', label: 'admin.todayOrders', icon: 'Timer', bg: '#fff1f0', color: '#f5222d' },
] as const

onMounted(async () => {
  try {
    stats.value = await getDashboard()
  } catch { /* handled */ } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="admin-dashboard">
    <h2 class="page-title mb-24">{{ t('admin.platformDashboard') }}</h2>

    <template v-if="stats">
      <el-row :gutter="20" class="mb-24">
        <el-col v-for="card in cards" :key="card.key" :span="4" :lg="4" :md="8" :sm="12">
          <div class="stat-card card-box">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats[card.key] }}</span>
              <span class="stat-label">{{ t(card.label) }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #e6fffb; color: #13c2c2">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatPrice(stats.gmv) }}</span>
              <span class="stat-label">{{ t('admin.gmv') }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}
</style>
