<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplyList, auditMerchantApply } from '@/api/merchant'
import type { MerchantApplyVO } from '@/types/merchant'
import { formatDate } from '@/utils/format'
import { getMerchantApplyStatusLabel } from '@/utils/i18nStatus'

const list = ref<MerchantApplyVO[]>([])
const loading = ref(false)
const { t } = useI18n()

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
  await ElMessageBox.confirm(t('admin.approve'), t('buyer.confirm'))
  try {
    await auditMerchantApply(id, { auditStatus: 1 })
    ElMessage.success(t('admin.approved'))
    fetchList()
  } catch { /* handled */ }
}

async function handleReject(id: number) {
  const { value } = await ElMessageBox.prompt(t('buyer.refundReason'), t('admin.reject'), {
    confirmButtonText: t('admin.reject'),
    inputValidator: (v) => !!v || t('buyer.reasonRequired'),
  }).catch(() => ({ value: null }))
  if (value === null) return
  try {
    await auditMerchantApply(id, { auditStatus: 2, remark: value })
    ElMessage.success(t('admin.rejected'))
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
    <h2 class="page-title mb-24">{{ t('admin.merchantAudit') }}</h2>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" :label="t('admin.id')" width="80" />
        <el-table-column prop="username" :label="t('admin.user')" width="120" />
        <el-table-column prop="shopName" :label="t('admin.shopName')" min-width="160" />
        <el-table-column prop="businessLicenseNo" :label="t('admin.licenseNo')" width="160" />
        <el-table-column prop="contactName" :label="t('admin.contact')" width="120" />
        <el-table-column prop="contactPhone" :label="t('admin.phone')" width="140" />
        <el-table-column :label="t('merchant.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applyStatus)" size="small">
              {{ getMerchantApplyStatusLabel(t, row.applyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="t('admin.remark')" width="160" show-overflow-tooltip />
        <el-table-column :label="t('admin.appliedAt')" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('buyer.action')" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.applyStatus === 0">
              <el-button type="success" text size="small" @click="handleApprove(row.id)">{{ t('admin.approve') }}</el-button>
              <el-button type="danger" text size="small" @click="handleReject(row.id)">{{ t('admin.reject') }}</el-button>
            </template>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" :description="t('admin.noMerchantApplications')" />
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
