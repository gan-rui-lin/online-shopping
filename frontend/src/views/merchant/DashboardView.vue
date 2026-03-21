<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getShopStatistics } from '@/api/merchant'
import type { ShopStatisticVO } from '@/types/merchant'
import { formatPrice } from '@/utils/format'

const stats = ref<ShopStatisticVO | null>(null)
const loading = ref(true)
const { t } = useI18n()

onMounted(async () => {
  try {
    stats.value = await getShopStatistics()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="merchant-dashboard">
    <h2 class="page-title mb-24">{{ t('merchant.dashboard') }}</h2>

    <template v-if="stats">
      <el-row :gutter="20" class="mb-24">
        <el-col :span="6">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #e6f7ff; color: #1890ff">
              <el-icon :size="28"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalProducts }}</span>
              <span class="stat-label">{{ t('merchant.totalProducts') }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #f6ffed; color: #52c41a">
              <el-icon :size="28"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.onShelfProducts }}</span>
              <span class="stat-label">{{ t('merchant.onShelf') }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #fff7e6; color: #fa8c16">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalOrders }}</span>
              <span class="stat-label">{{ t('merchant.totalOrders') }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #fff1f0; color: #f5222d">
              <el-icon :size="28"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.pendingOrders }}</span>
              <span class="stat-label">{{ t('merchant.pendingOrders') }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #f9f0ff; color: #722ed1">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatPrice(stats.totalRevenue) }}</span>
              <span class="stat-label">{{ t('merchant.totalRevenue') }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-card card-box">
            <div class="stat-icon" style="background: #e6fffb; color: #13c2c2">
              <el-icon :size="28"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.score?.toFixed(1) || '-' }}</span>
              <span class="stat-label">{{ t('merchant.shopRating') }}</span>
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
