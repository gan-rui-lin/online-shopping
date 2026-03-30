<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder, payOrder, confirmReceive } from '@/api/order'
import type { OrderListVO, OrderQueryDTO } from '@/types/order'
import { OrderStatus } from '@/constants/enums'
import PriceDisplay from '@/components/PriceDisplay.vue'
import { formatDate } from '@/utils/format'
import { getOrderStatusLabel } from '@/utils/i18nStatus'

const router = useRouter()
const orders = ref<OrderListVO[]>([])
const total = ref(0)
const loading = ref(false)
const { t } = useI18n()

const query = ref<OrderQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  orderStatus: undefined,
})

const statusTabs = [
  { label: 'buyer.all', value: undefined },
  { label: 'buyer.unpaid', value: OrderStatus.UNPAID },
  { label: 'buyer.toShip', value: OrderStatus.TO_SHIP },
  { label: 'buyer.toReceive', value: OrderStatus.TO_RECEIVE },
  { label: 'buyer.completed', value: OrderStatus.COMPLETED },
]

const activeTab = ref<string>('all')

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderList(query.value)
    orders.value = res.list
    total.value = res.total
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab: string) {
  const found = statusTabs.find((t) => (t.value === undefined ? 'all' : String(t.value)) === tab)
  query.value.orderStatus = found?.value
  query.value.pageNum = 1
  fetchOrders()
}

function handlePageChange(page: number) {
  query.value.pageNum = page
  fetchOrders()
}

function goDetail(orderNo: string) {
  router.push(`/buyer/orders/${orderNo}`)
}

async function handlePay(orderNo: string) {
  try {
    await payOrder(orderNo)
    ElMessage.success(t('buyer.paymentSuccess'))
    fetchOrders()
  } catch { /* handled */ }
}

async function handleCancel(orderNo: string) {
  const { value } = await ElMessageBox.prompt(t('buyer.cancelReasonOptional'), t('buyer.cancelOrder'), {
    confirmButtonText: t('buyer.confirm'),
    cancelButtonText: t('buyer.back'),
    inputPlaceholder: t('buyer.enterReason'),
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await cancelOrder(orderNo, value || undefined)
    ElMessage.success(t('buyer.orderCancelled'))
    fetchOrders()
  } catch { /* handled */ }
}

async function handleConfirmReceive(orderNo: string) {
  await ElMessageBox.confirm(t('buyer.confirmReceive'), t('buyer.confirm'))
  try {
    await confirmReceive(orderNo)
    ElMessage.success(t('buyer.receiptConfirmed'))
    fetchOrders()
  } catch { /* handled */ }
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    [OrderStatus.UNPAID]: 'warning',
    [OrderStatus.TO_SHIP]: 'primary',
    [OrderStatus.TO_RECEIVE]: 'primary',
    [OrderStatus.COMPLETED]: 'success',
    [OrderStatus.CANCELLED]: 'info',
    [OrderStatus.REFUNDING]: 'danger',
    [OrderStatus.REFUNDED]: 'info',
  }
  return (map[status] || 'info') as any
}

onMounted(fetchOrders)
</script>

<template>
  <div class="order-list-page">
    <h2 class="page-title mb-24">{{ t('buyer.myOrders') }}</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in statusTabs"
        :key="tab.value === undefined ? 'all' : tab.value"
        :label="t(tab.label)"
        :name="tab.value === undefined ? 'all' : String(tab.value)"
      />
    </el-tabs>

    <div v-loading="loading">
      <div v-for="order in orders" :key="order.orderNo" class="order-card card-box mb-16">
        <div class="order-header">
          <span class="order-no">{{ t('buyer.order') }}: {{ order.orderNo }}</span>
          <span class="order-time">{{ formatDate(order.createTime) }}</span>
          <el-tag :type="getStatusType(order.orderStatus)" size="small">
            {{ getOrderStatusLabel(t, order.orderStatus) }}
          </el-tag>
        </div>

        <div class="order-items" @click="goDetail(order.orderNo)">
          <div v-for="item in order.itemList" :key="item.skuId" class="order-item">
            <el-image :src="item.productImage" fit="cover" class="item-img">
              <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="item-info">
              <span class="item-title text-ellipsis">{{ item.productTitle }}</span>
              <span class="item-spec">{{ item.skuName }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
            <PriceDisplay :price="item.totalAmount" size="small" />
          </div>
        </div>

        <div class="order-footer">
          <div class="order-total">
            <span>{{ t('buyer.total') }}: </span>
            <PriceDisplay :price="order.payAmount" />
          </div>
          <div class="order-actions">
            <el-button size="small" @click="goDetail(order.orderNo)">{{ t('buyer.detail') }}</el-button>
            <el-button v-if="order.orderStatus === OrderStatus.UNPAID" type="primary" size="small" @click="handlePay(order.orderNo)">{{ t('buyer.payNow') }}</el-button>
            <el-button v-if="order.orderStatus === OrderStatus.UNPAID" size="small" @click="handleCancel(order.orderNo)">{{ t('buyer.cancel') }}</el-button>
            <el-button v-if="order.orderStatus === OrderStatus.TO_RECEIVE" type="primary" size="small" @click="handleConfirmReceive(order.orderNo)">{{ t('buyer.confirmReceive') }}</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !orders.length" :description="t('buyer.noOrders')" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        :total="total"
        :page-size="query.pageSize"
        layout="prev, pager, next"
        class="mt-16"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.order-card {
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid $border-lighter;
  margin-bottom: 12px;
}

.order-no {
  font-weight: 500;
}

.order-time {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-left: auto;
}

.order-items {
  cursor: pointer;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.item-img {
  width: 60px;
  height: 60px;
  border-radius: $border-radius;
  flex-shrink: 0;
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-color;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.item-title {
  font-size: $font-size-base;
}

.item-spec, .item-qty {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid $border-lighter;
  margin-top: 12px;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
