<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminOrders,
  cancelAdminOrder,
  approveAdminRefund,
  rejectAdminRefund,
} from '@/api/admin'
import type { AdminOrderVO, AdminOrderQueryDTO } from '@/types/admin'
import { OrderStatus } from '@/constants/enums'
import { formatDate } from '@/utils/format'
import { getOrderStatusLabel, getPayStatusLabel } from '@/utils/i18nStatus'
import PriceDisplay from '@/components/PriceDisplay.vue'

const { t } = useI18n()
const loading = ref(false)
const list = ref<AdminOrderVO[]>([])
const total = ref(0)

const query = reactive<AdminOrderQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  userId: undefined,
  orderStatus: undefined,
})

const orderStatusOptions = [
  { value: OrderStatus.UNPAID, label: 'status.order.unpaid' },
  { value: OrderStatus.TO_SHIP, label: 'status.order.toShip' },
  { value: OrderStatus.TO_RECEIVE, label: 'status.order.toReceive' },
  { value: OrderStatus.COMPLETED, label: 'status.order.completed' },
  { value: OrderStatus.CANCELLED, label: 'status.order.cancelled' },
  { value: OrderStatus.REFUNDING, label: 'status.order.refunding' },
  { value: OrderStatus.REFUNDED, label: 'status.order.refunded' },
]

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getAdminOrders({
      ...query,
      orderNo: query.orderNo?.trim() || undefined,
    })
    list.value = res.list
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchOrders()
}

function handleReset() {
  query.orderNo = ''
  query.userId = undefined
  query.orderStatus = undefined
  query.pageNum = 1
  fetchOrders()
}

function handlePageChange(page: number) {
  query.pageNum = page
  fetchOrders()
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    [OrderStatus.UNPAID]: 'warning',
    [OrderStatus.TO_SHIP]: 'primary',
    [OrderStatus.TO_RECEIVE]: 'primary',
    [OrderStatus.COMPLETED]: 'success',
    [OrderStatus.CANCELLED]: 'info',
    [OrderStatus.REFUNDING]: 'danger',
    [OrderStatus.REFUNDED]: 'success',
  }
  return (map[status] || 'info') as any
}

async function handleCancel(row: AdminOrderVO) {
  const { value } = await ElMessageBox.prompt(
    t('admin.cancelOrderReason'),
    t('admin.orderIntervention'),
    { inputPlaceholder: t('admin.cancelOrderReasonOptional') },
  ).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await cancelAdminOrder(row.orderNo, value)
    ElMessage.success(t('admin.orderCancelledByAdmin'))
    fetchOrders()
  } catch {
    // handled globally
  }
}

async function handleApproveRefund(row: AdminOrderVO) {
  await ElMessageBox.confirm(t('admin.approveRefundByAdmin'), t('buyer.confirm'))
  try {
    await approveAdminRefund(row.orderNo)
    ElMessage.success(t('admin.refundApprovedByAdmin'))
    fetchOrders()
  } catch {
    // handled globally
  }
}

async function handleRejectRefund(row: AdminOrderVO) {
  const { value } = await ElMessageBox.prompt(
    t('buyer.refundReason'),
    t('admin.rejectRefundByAdmin'),
    { inputValidator: (v) => !!v || t('buyer.reasonRequired') },
  ).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await rejectAdminRefund(row.orderNo, value)
    ElMessage.success(t('admin.refundRejectedByAdmin'))
    fetchOrders()
  } catch {
    // handled globally
  }
}

onMounted(fetchOrders)
</script>

<template>
  <div class="order-intervention-page">
    <h2 class="page-title mb-24">{{ t('admin.orderIntervention') }}</h2>

    <div class="card-box mb-16">
      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-input v-model="query.orderNo" :placeholder="t('admin.orderNoPlaceholder')" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="query.userId" :min="1" :placeholder="t('admin.userId')" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.orderStatus" :placeholder="t('merchant.status')" clearable style="width: 160px">
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="t(item.label)"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="orderNo" :label="t('buyer.order')" width="180" />
        <el-table-column :label="t('admin.user')" width="170">
          <template #default="{ row }">{{ row.username }} (#{{ row.userId }})</template>
        </el-table-column>
        <el-table-column prop="shopName" :label="t('admin.shopName')" min-width="170" />
        <el-table-column :label="t('buyer.total')" width="120">
          <template #default="{ row }">
            <PriceDisplay :price="row.payAmount" size="small" />
          </template>
        </el-table-column>
        <el-table-column :label="t('merchant.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)" size="small">
              {{ getOrderStatusLabel(t, row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('buyer.paymentStatus')" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ getPayStatusLabel(t, row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('buyer.created')" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('admin.cancelReason')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.cancelReason || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('buyer.action')" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.orderStatus === OrderStatus.UNPAID"
              type="danger"
              text
              size="small"
              @click="handleCancel(row)"
            >
              {{ t('admin.cancelOrderByAdmin') }}
            </el-button>
            <el-button
              v-if="row.orderStatus === OrderStatus.REFUNDING"
              type="success"
              text
              size="small"
              @click="handleApproveRefund(row)"
            >
              {{ t('admin.approveRefundByAdmin') }}
            </el-button>
            <el-button
              v-if="row.orderStatus === OrderStatus.REFUNDING"
              type="danger"
              text
              size="small"
              @click="handleRejectRefund(row)"
            >
              {{ t('admin.rejectRefundByAdmin') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        :total="total"
        :page-size="query.pageSize"
        layout="total, prev, pager, next"
        class="mt-16"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
