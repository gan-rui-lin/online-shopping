<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getDashboard } from '@/api/admin'
import type { DashboardVO, OrderStatusStatVO, DashboardTrendVO } from '@/types/admin'
import { formatPrice } from '@/utils/format'
import { getOrderStatusLabel } from '@/utils/i18nStatus'

const { t } = useI18n()
const loading = ref(true)
const stats = ref<DashboardVO | null>(null)

const cards = [
  { key: 'userCount', label: 'admin.totalUsers', icon: 'User' },
  { key: 'merchantCount', label: 'admin.totalMerchants', icon: 'Shop' },
  { key: 'productCount', label: 'admin.totalProducts', icon: 'Goods' },
  { key: 'orderCount', label: 'admin.totalOrders', icon: 'Tickets' },
  { key: 'todayOrderCount', label: 'admin.todayOrders', icon: 'Timer' },
] as const

const statusStats = computed<OrderStatusStatVO[]>(() => stats.value?.orderStatusStats || [])
const maxStatusValue = computed(() =>
  Math.max(1, ...statusStats.value.map(item => item.count || 0)),
)

const orderTrend = computed<DashboardTrendVO[]>(() => stats.value?.orderTrend || [])
const gmvTrend = computed<DashboardTrendVO[]>(() => stats.value?.gmvTrend || [])

const maxOrderTrend = computed(() => Math.max(1, ...orderTrend.value.map(item => item.value || 0)))
const maxGmvTrend = computed(() => Math.max(1, ...gmvTrend.value.map(item => item.value || 0)))

function toPercent(value: number, max: number): number {
  if (!max) return 0
  return Math.round((value / max) * 100)
}

onMounted(async () => {
  try {
    stats.value = await getDashboard()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="admin-dashboard">
    <h2 class="page-title mb-24">{{ t('admin.platformDashboard') }}</h2>

    <template v-if="stats">
      <div class="kpi-grid mb-24">
        <article v-for="card in cards" :key="card.key" class="kpi-card">
          <div class="kpi-icon">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="kpi-info">
            <div class="kpi-label">{{ t(card.label) }}</div>
            <div class="kpi-value">{{ stats[card.key] }}</div>
          </div>
        </article>
      </div>

      <section class="gmv-panel mb-24">
        <div class="gmv-title">{{ t('admin.gmv') }}</div>
        <div class="gmv-value">{{ formatPrice(stats.gmv) }}</div>
      </section>

      <div class="insight-grid">
        <section class="card-box trend-card">
          <h3>{{ t('admin.orderTrend7d') }}</h3>
          <div class="trend-list">
            <div v-for="item in orderTrend" :key="`o-${item.date}`" class="trend-row">
              <span class="trend-label">{{ item.date.slice(5) }}</span>
              <div class="trend-bar">
                <div class="bar-fill order-bar" :style="{ width: `${toPercent(item.value, maxOrderTrend)}%` }" />
              </div>
              <span class="trend-value">{{ item.value }}</span>
            </div>
          </div>
        </section>

        <section class="card-box trend-card">
          <h3>{{ t('admin.gmvTrend7d') }}</h3>
          <div class="trend-list">
            <div v-for="item in gmvTrend" :key="`g-${item.date}`" class="trend-row">
              <span class="trend-label">{{ item.date.slice(5) }}</span>
              <div class="trend-bar">
                <div class="bar-fill gmv-bar" :style="{ width: `${toPercent(item.value, maxGmvTrend)}%` }" />
              </div>
              <span class="trend-value">{{ formatPrice(item.value) }}</span>
            </div>
          </div>
        </section>

        <section class="card-box trend-card">
          <h3>{{ t('admin.orderStatusDistribution') }}</h3>
          <div class="trend-list">
            <div v-for="item in statusStats" :key="`s-${item.status}`" class="trend-row">
              <span class="trend-label">{{ getOrderStatusLabel(t, item.status) }}</span>
              <div class="trend-bar">
                <div class="bar-fill status-bar" :style="{ width: `${toPercent(item.count, maxStatusValue)}%` }" />
              </div>
              <span class="trend-value">{{ item.count }}</span>
            </div>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.admin-dashboard {
  --card-bg-1: linear-gradient(135deg, #f8fbff 0%, #edf5ff 100%);
  --card-bg-2: linear-gradient(135deg, #f6fff6 0%, #e7f9e7 100%);
  --card-bg-3: linear-gradient(135deg, #fffaf1 0%, #fff1dd 100%);
  --card-bg-4: linear-gradient(135deg, #f9f7ff 0%, #efe9ff 100%);
  --card-bg-5: linear-gradient(135deg, #fff3f0 0%, #ffe4de 100%);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:nth-child(1) { background: var(--card-bg-1); }
  &:nth-child(2) { background: var(--card-bg-2); }
  &:nth-child(3) { background: var(--card-bg-3); }
  &:nth-child(4) { background: var(--card-bg-4); }
  &:nth-child(5) { background: var(--card-bg-5); }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  background: #fff;
  color: #ff6a00;
  display: flex;
  align-items: center;
  justify-content: center;
}

.kpi-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.kpi-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.gmv-panel {
  border-radius: 14px;
  padding: 20px 22px;
  color: #fff;
  background: linear-gradient(120deg, #ff5e00 0%, #ff8f1f 50%, #ffb03a 100%);
  box-shadow: var(--shadow-md);
}

.gmv-title {
  font-size: 14px;
  opacity: 0.9;
}

.gmv-value {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0.6px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.trend-card h3 {
  font-size: 15px;
  margin-bottom: 14px;
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trend-row {
  display: grid;
  grid-template-columns: 52px 1fr 82px;
  align-items: center;
  gap: 8px;
}

.trend-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.trend-bar {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: #edf0f5;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.35s ease;
}

.order-bar {
  background: linear-gradient(90deg, #3182f6 0%, #5f9cff 100%);
}

.gmv-bar {
  background: linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%);
}

.status-bar {
  background: linear-gradient(90deg, #10b981 0%, #34d399 100%);
}

.trend-value {
  font-size: 12px;
  color: var(--text-primary);
  text-align: right;
}

@media (max-width: 1280px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-grid {
    grid-template-columns: 1fr;
  }
}
</style>
