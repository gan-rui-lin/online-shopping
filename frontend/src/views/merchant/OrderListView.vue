<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantOrders, deliverOrder, approveRefund, rejectRefund } from '@/api/order'
import type { OrderListVO, OrderQueryDTO } from '@/types/order'
import { OrderStatus, OrderStatusMap } from '@/constants/enums'
import PriceDisplay from '@/components/PriceDisplay.vue'
import { formatDate } from '@/utils/format'

const orders = ref<OrderListVO[]>([])
const total = ref(0)
const loading = ref(false)

const query = ref<OrderQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  orderStatus: undefined,
})

const statusTabs = [
  { label: 'All', value: undefined },
  { label: 'To Ship', value: OrderStatus.TO_SHIP },
  { label: 'Shipped', value: OrderStatus.TO_RECEIVE },
  { label: 'Completed', value: OrderStatus.COMPLETED },
  { label: 'Refunding', value: OrderStatus.REFUNDING },
]

const activeTab = ref('all')

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getMerchantOrders(query.value)
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

async function handleDeliver(orderNo: string) {
  await ElMessageBox.confirm('Confirm delivery of this order?', 'Confirm')
  try {
    await deliverOrder(orderNo)
    ElMessage.success('Order marked as delivered')
    fetchOrders()
  } catch { /* handled */ }
}

async function handleApproveRefund(orderNo: string) {
  await ElMessageBox.confirm('Approve this refund request?', 'Confirm')
  try {
    await approveRefund(orderNo)
    ElMessage.success('Refund approved')
    fetchOrders()
  } catch { /* handled */ }
}

async function handleRejectRefund(orderNo: string) {
  const { value } = await ElMessageBox.prompt('Reject reason', 'Reject Refund', {
    confirmButtonText: 'Reject',
    inputValidator: (v) => !!v || 'Reason is required',
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await rejectRefund(orderNo, value)
    ElMessage.success('Refund rejected')
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
  <div class="merchant-orders-page">
    <h2 class="page-title mb-24">Order Management</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in statusTabs"
        :key="tab.value === undefined ? 'all' : tab.value"
        :label="tab.label"
        :name="tab.value === undefined ? 'all' : String(tab.value)"
      />
    </el-tabs>

    <div class="card-box">
      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="orderNo" label="Order No" width="200" />
        <el-table-column label="Items" min-width="240">
          <template #default="{ row }">
            <div v-for="item in row.itemList" :key="item.skuId" class="item-row">
              <span class="text-ellipsis">{{ item.productTitle }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Amount" width="120">
          <template #default="{ row }">
            <PriceDisplay :price="row.payAmount" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)" size="small">
              {{ OrderStatusMap[row.orderStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Created" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.orderStatus === OrderStatus.TO_SHIP" type="primary" text size="small" @click="handleDeliver(row.orderNo)">Ship</el-button>
            <el-button v-if="row.orderStatus === OrderStatus.REFUNDING" type="success" text size="small" @click="handleApproveRefund(row.orderNo)">Approve Refund</el-button>
            <el-button v-if="row.orderStatus === OrderStatus.REFUNDING" type="danger" text size="small" @click="handleRejectRefund(row.orderNo)">Reject Refund</el-button>
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

.item-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 1.8;
}

.item-qty {
  color: #909399;
  white-space: nowrap;
}
</style>
