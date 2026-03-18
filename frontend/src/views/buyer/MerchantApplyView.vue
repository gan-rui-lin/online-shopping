<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { applyMerchant, getApplyList } from '@/api/merchant'
import type { MerchantApplyDTO, MerchantApplyVO } from '@/types/merchant'
import { MerchantApplyStatusMap } from '@/constants/enums'
import { formatDate } from '@/utils/format'

const formRef = ref<FormInstance>()
const loading = ref(false)
const applying = ref(false)
const applyRecords = ref<MerchantApplyVO[]>([])

const form = reactive<MerchantApplyDTO>({
  shopName: '',
  businessLicenseNo: '',
  contactName: '',
  contactPhone: '',
})

const rules: FormRules = {
  shopName: [{ required: true, message: 'Shop name is required', trigger: 'blur' }],
}

function getStatusType(status: number) {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger'
}

async function fetchApplyList() {
  loading.value = true
  try {
    applyRecords.value = await getApplyList()
  } catch {
    applyRecords.value = []
  } finally {
    loading.value = false
  }
}

async function handleApply() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  applying.value = true
  try {
    await applyMerchant(form)
    ElMessage.success('Application submitted')
    form.shopName = ''
    form.businessLicenseNo = ''
    form.contactName = ''
    form.contactPhone = ''
    fetchApplyList()
  } catch { /* handled */ } finally {
    applying.value = false
  }
}

onMounted(fetchApplyList)
</script>

<template>
  <div class="merchant-apply-page">
    <h2 class="page-title mb-24">Apply as Merchant</h2>

    <div class="card-box mb-24">
      <h3 class="section-title mb-16">Submit Application</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" style="max-width: 520px">
        <el-form-item label="Shop Name" prop="shopName">
          <el-input v-model="form.shopName" placeholder="Enter your shop name" />
        </el-form-item>
        <el-form-item label="Business License No">
          <el-input v-model="form.businessLicenseNo" placeholder="Optional" />
        </el-form-item>
        <el-form-item label="Contact Name">
          <el-input v-model="form.contactName" placeholder="Optional" />
        </el-form-item>
        <el-form-item label="Contact Phone">
          <el-input v-model="form.contactPhone" placeholder="Optional" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="applying" @click="handleApply">Submit Application</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-box">
      <h3 class="section-title mb-16">Application History</h3>
      <div v-loading="loading">
        <el-table :data="applyRecords" stripe>
          <el-table-column prop="shopName" label="Shop Name" />
          <el-table-column label="Status" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.applyStatus)" size="small">
                {{ MerchantApplyStatusMap[row.applyStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="Remark" />
          <el-table-column label="Applied At" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !applyRecords.length" description="No application records" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
