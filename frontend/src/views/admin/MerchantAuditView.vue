<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplyList, auditMerchantApply } from '@/api/merchant'
import type { MerchantApplyVO } from '@/types/merchant'
import { MerchantApplyStatusMap } from '@/constants/enums'
import { formatDate } from '@/utils/format'

const list = ref<MerchantApplyVO[]>([])
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    list.value = await getApplyList()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleApprove(id: number) {
  await ElMessageBox.confirm('Approve this merchant application?', 'Confirm')
  try {
    await auditMerchantApply(id, { auditStatus: 1 })
    ElMessage.success('Approved')
    fetchList()
  } catch { /* handled */ }
}

async function handleReject(id: number) {
  const { value } = await ElMessageBox.prompt('Rejection reason', 'Reject Application', {
    confirmButtonText: 'Reject',
    inputValidator: (v) => !!v || 'Reason is required',
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await auditMerchantApply(id, { auditStatus: 2, remark: value })
    ElMessage.success('Rejected')
    fetchList()
  } catch { /* handled */ }
}

function getStatusType(status: number) {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger'
}

onMounted(fetchList)
</script>

<template>
  <div class="merchant-audit-page">
    <h2 class="page-title mb-24">Merchant Audit</h2>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="User" width="120" />
        <el-table-column prop="shopName" label="Shop Name" min-width="160" />
        <el-table-column prop="businessLicenseNo" label="License No" width="160" />
        <el-table-column prop="contactName" label="Contact" width="120" />
        <el-table-column prop="contactPhone" label="Phone" width="140" />
        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applyStatus)" size="small">
              {{ MerchantApplyStatusMap[row.applyStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="Remark" width="160" show-overflow-tooltip />
        <el-table-column label="Applied At" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.applyStatus === 0">
              <el-button type="success" text size="small" @click="handleApprove(row.id)">Approve</el-button>
              <el-button type="danger" text size="small" @click="handleReject(row.id)">Reject</el-button>
            </template>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="No merchant applications" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.text-secondary {
  color: #909399;
}
</style>
