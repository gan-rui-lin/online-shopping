            <el-image :src="row.mainImage" fit="cover" style="width: 48px; height: 48px; border-radius: 4px">
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchProducts } from '@/api/product'
import { approveProduct, rejectProduct } from '@/api/admin'
import type { ProductSimpleVO } from '@/types/product'
import { ProductStatus } from '@/constants/enums'
import PriceDisplay from '@/components/PriceDisplay.vue'
import { resolveImageUrl } from '@/utils/image'
import { getProductStatusLabel } from '@/utils/i18nStatus'
const list = ref<ProductSimpleVO[]>([])
const total = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const { t } = useI18n()

async function fetchProducts() {
  loading.value = true
  try {
    const res = await searchProducts({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.list
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchProducts()
}

async function handleApprove(spuId: number) {
  await ElMessageBox.confirm(t('admin.approve'), t('buyer.confirm'))
  try {
    await approveProduct(spuId)
    ElMessage.success(t('admin.productApproved'))
    fetchProducts()
  } catch { /* handled */ }
}

async function handleReject(spuId: number) {
  await ElMessageBox.confirm(t('admin.reject'), t('buyer.confirm'))
  try {
    await rejectProduct(spuId)
    ElMessage.success(t('admin.productRejected'))
    fetchProducts()
  } catch { /* handled */ }
}

function getStatusType(status: number) {
  return status === ProductStatus.ON_SHELF ? 'success' : status === ProductStatus.DRAFT ? 'info' : 'warning'
}

onMounted(fetchProducts)
</script>

<template>
  <div class="product-audit-page">
    <h2 class="page-title mb-24">{{ t('admin.productAudit') }}</h2>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column :label="t('merchant.image')" width="80">
          <template #default="{ row }">
            <el-image :src="resolveImageUrl(row.mainImage)" fit="cover" style="width: 48px; height: 48px; border-radius: 4px">
              <template #error><div style="width: 48px; height: 48px; background: #f5f7fa; display: flex; align-items: center; justify-content: center"><el-icon><Picture /></el-icon></div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" :label="t('intelligence.product')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="shopName" :label="t('merchantLayout.shopInfo')" width="140" />
        <el-table-column :label="t('intelligence.price')" width="120">
          <template #default="{ row }">
            <PriceDisplay :price="row.minPrice" size="small" />
          </template>
        </el-table-column>
        <el-table-column :label="t('merchant.status')" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getProductStatusLabel(t, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" :label="t('merchant.sales')" width="80" />
        <el-table-column :label="t('buyer.action')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" text size="small" @click="handleApprove(row.spuId)">{{ t('admin.approve') }}</el-button>
            <el-button type="danger" text size="small" @click="handleReject(row.spuId)">{{ t('admin.reject') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        :total="total"
        :page-size="pageSize"
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
</style>
