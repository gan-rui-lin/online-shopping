<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { applyMerchant, getApplyList } from '@/api/merchant'
import type { MerchantApplyDTO, MerchantApplyVO } from '@/types/merchant'
import { formatDate } from '@/utils/format'
import { getMerchantApplyStatusLabel } from '@/utils/i18nStatus'

const formRef = ref<FormInstance>()
const loading = ref(false)
const applying = ref(false)
const applyRecords = ref<MerchantApplyVO[]>([])
const { t } = useI18n()

const form = reactive<MerchantApplyDTO>({
  shopName: '',
  businessLicenseNo: '',
  contactName: '',
  contactPhone: '',
})

const rules: FormRules = {
  shopName: [{ required: true, message: t('buyer.required'), trigger: 'blur' }],
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
    ElMessage.success(t('buyer.applicationSubmitted'))
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
    <h2 class="page-title mb-24">{{ t('buyer.merchantApply') }}</h2>

    <div class="card-box mb-24">
      <h3 class="section-title mb-16">{{ t('buyer.submitApplication') }}</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" style="max-width: 520px">
        <el-form-item :label="t('buyer.shopName')" prop="shopName">
          <el-input v-model="form.shopName" :placeholder="t('buyer.shopName')" />
        </el-form-item>
        <el-form-item :label="t('buyer.businessLicenseNo')">
          <el-input v-model="form.businessLicenseNo" :placeholder="t('register.optional')" />
        </el-form-item>
        <el-form-item :label="t('buyer.contactName')">
          <el-input v-model="form.contactName" :placeholder="t('register.optional')" />
        </el-form-item>
        <el-form-item :label="t('buyer.contactPhone')">
          <el-input v-model="form.contactPhone" :placeholder="t('register.optional')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="applying" @click="handleApply">{{ t('buyer.submitApplication') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-box">
      <h3 class="section-title mb-16">{{ t('buyer.applicationHistory') }}</h3>
      <div v-loading="loading">
        <el-table :data="applyRecords" stripe>
          <el-table-column prop="shopName" :label="t('buyer.shopName')" />
          <el-table-column :label="t('merchant.status')" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.applyStatus)" size="small">
                {{ getMerchantApplyStatusLabel(t, row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" :label="t('admin.remark')" />
          <el-table-column :label="t('admin.appliedAt')" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !applyRecords.length" :description="t('buyer.noApplicationRecords')" />
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
