<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, cancelOrder, confirmReceive, applyRefund } from '@/api/order'
import type { OrderDetailVO } from '@/types/order'
import { OrderStatus, OrderStatusMap, PayStatusMap } from '@/constants/enums'
import PriceDisplay from '@/components/PriceDisplay.vue'
import { formatDate, formatSpec } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const orderNo = computed(() => route.params.orderNo as string)
const order = ref<OrderDetailVO | null>(null)
const loading = ref(true)

async function fetchOrder() {
  loading.value = true
  try {
    order.value = await getOrderDetail(orderNo.value)
  } catch {
    ElMessage.error('Failed to load order')
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  try {
    await payOrder(orderNo.value)
    ElMessage.success('Payment successful')
    fetchOrder()
  } catch { /* handled */ }
}

async function handleCancel() {
  const { value } = await ElMessageBox.prompt('Cancel reason (optional)', 'Cancel Order', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Back',
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await cancelOrder(orderNo.value, value || undefined)
    ElMessage.success('Order cancelled')
    fetchOrder()
  } catch { /* handled */ }
}

async function handleConfirmReceive() {
  await ElMessageBox.confirm('Confirm you have received the goods?', 'Confirm')
  try {
    await confirmReceive(orderNo.value)
    ElMessage.success('Receipt confirmed')
    fetchOrder()
  } catch { /* handled */ }
}

async function handleRefund() {
  const { value } = await ElMessageBox.prompt('Refund reason', 'Apply Refund', {
    confirmButtonText: 'Submit',
    cancelButtonText: 'Cancel',
    inputValidator: (v) => !!v || 'Reason is required',
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await applyRefund(orderNo.value, { reason: value })
    ElMessage.success('Refund applied')
    fetchOrder()
  } catch { /* handled */ }
}

function goReview(orderItemId: number) {
  router.push(`/buyer/review/${orderItemId}`)
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

onMounted(fetchOrder)
</script>

<template>
  <div v-loading="loading" class="order-detail-page">
    <template v-if="order">
      <div class="page-header mb-24">
        <el-button text @click="router.push('/buyer/orders')">
          <el-icon><ArrowLeft /></el-icon> Back to Orders
        </el-button>
        <h2 class="page-title">Order Detail</h2>
      </div>

      <div class="status-bar card-box mb-16">
        <el-tag :type="getStatusType(order.orderStatus)" size="large">
          {{ OrderStatusMap[order.orderStatus] }}
        </el-tag>
        <div class="status-actions">
          <el-button v-if="order.orderStatus === OrderStatus.UNPAID" type="primary" @click="handlePay">Pay Now</el-button>
          <el-button v-if="order.orderStatus === OrderStatus.UNPAID" @click="handleCancel">Cancel</el-button>
          <el-button v-if="order.orderStatus === OrderStatus.TO_RECEIVE" type="primary" @click="handleConfirmReceive">Confirm Receive</el-button>
          <el-button v-if="order.orderStatus === OrderStatus.TO_SHIP || order.orderStatus === OrderStatus.TO_RECEIVE" type="warning" @click="handleRefund">Apply Refund</el-button>
        </div>
      </div>

      <div class="info-section card-box mb-16">
        <h3 class="section-title">Order Info</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Order No">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="Payment Status">{{ PayStatusMap[order.payStatus] }}</el-descriptions-item>
          <el-descriptions-item label="Created">{{ formatDate(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="Paid">{{ formatDate(order.payTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Shipped">{{ formatDate(order.deliveryTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Completed">{{ formatDate(order.finishTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="order.cancelTime" label="Cancelled">{{ formatDate(order.cancelTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="order.cancelReason" label="Cancel Reason">{{ order.cancelReason }}</el-descriptions-item>
          <el-descriptions-item v-if="order.remark" label="Remark">{{ order.remark }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="info-section card-box mb-16">
        <h3 class="section-title">Shipping Address</h3>
        <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
        <p class="address-text">{{ order.receiverAddress }}</p>
      </div>

      <div class="info-section card-box mb-16">
        <h3 class="section-title">Order Items</h3>
        <div v-for="item in order.itemList" :key="item.skuId" class="detail-item">
          <el-image :src="item.productImage" fit="cover" class="item-img">
            <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
          </el-image>
          <div class="item-info">
            <router-link :to="`/products/${item.spuId}`" class="item-title">{{ item.productTitle }}</router-link>
            <span class="item-spec">{{ item.skuName || formatSpec(item.skuSpecJson) }}</span>
          </div>
          <div class="item-qty">x{{ item.quantity }}</div>
          <PriceDisplay :price="item.totalAmount" />
          <el-button
            v-if="order.orderStatus === OrderStatus.COMPLETED && item.reviewStatus === 0"
            text
            type="primary"
            size="small"
            @click="goReview(item.skuId)"
          >
            Write Review
          </el-button>
        </div>
      </div>

      <div class="price-summary card-box">
        <div class="price-row">
          <span>Subtotal</span>
          <PriceDisplay :price="order.totalAmount" />
        </div>
        <div class="price-row">
          <span>Discount</span>
          <span>-{{ order.discountAmount?.toFixed(2) || '0.00' }}</span>
        </div>
        <div class="price-row">
          <span>Shipping</span>
          <PriceDisplay :price="order.freightAmount" />
        </div>
        <div class="price-row total">
          <span>Total</span>
          <PriceDisplay :price="order.payAmount" size="large" />
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-actions {
  display: flex;
  gap: 8px;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  margin-bottom: 16px;
}

.address-text {
  color: $text-secondary;
  margin-top: 4px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid $border-lighter;

  &:last-child { border-bottom: none; }
}

.item-img {
  width: 64px;
  height: 64px;
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
  gap: 4px;
  min-width: 0;
}

.item-title {
  color: $text-primary;
  font-weight: 500;
  &:hover { color: $primary-color; text-decoration: none; }
}

.item-spec {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.item-qty {
  width: 50px;
  color: $text-secondary;
}

.price-summary {
  max-width: 360px;
  margin-left: auto;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: $text-regular;

  &.total {
    border-top: 1px solid $border-lighter;
    margin-top: 8px;
    padding-top: 12px;
    font-weight: 600;
  }
}
</style>
