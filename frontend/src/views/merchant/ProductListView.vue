<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyProducts, onShelfProduct, offShelfProduct, deleteProduct } from '@/api/product'
import type { ProductSimpleVO } from '@/types/product'
import { ProductStatus, ProductStatusMap } from '@/constants/enums'
import PriceDisplay from '@/components/PriceDisplay.vue'

const router = useRouter()
const list = ref<ProductSimpleVO[]>([])
const total = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getMyProducts(pageNum.value, pageSize.value)
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

async function handleOnShelf(spuId: number) {
  try {
    await onShelfProduct(spuId)
    ElMessage.success('Product is now on shelf')
    fetchProducts()
  } catch { /* handled */ }
}

async function handleOffShelf(spuId: number) {
  try {
    await offShelfProduct(spuId)
    ElMessage.success('Product taken off shelf')
    fetchProducts()
  } catch { /* handled */ }
}

async function handleDelete(spuId: number) {
  await ElMessageBox.confirm('Delete this product? This action cannot be undone.', 'Confirm')
  try {
    await deleteProduct(spuId)
    ElMessage.success('Product deleted')
    fetchProducts()
  } catch { /* handled */ }
}

function getStatusType(status: number) {
  return status === ProductStatus.ON_SHELF ? 'success' : status === ProductStatus.DRAFT ? 'info' : 'warning'
}

onMounted(fetchProducts)
</script>

<template>
  <div class="product-list-page">
    <div class="page-header mb-16">
      <h2 class="page-title">Product Management</h2>
      <el-button type="primary" @click="router.push('/merchant/products/create')">
        <el-icon><Plus /></el-icon> Create Product
      </el-button>
    </div>

    <div class="card-box">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="Image" width="80">
          <template #default="{ row }">
            <el-image :src="row.mainImage" fit="cover" style="width: 48px; height: 48px; border-radius: 4px">
              <template #error><div style="width: 48px; height: 48px; background: #f5f7fa; display: flex; align-items: center; justify-content: center;"><el-icon><Picture /></el-icon></div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="Title" min-width="200" show-overflow-tooltip />
        <el-table-column label="Price" width="140">
          <template #default="{ row }">
            <PriceDisplay :price="row.minPrice" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="Sales" width="80" />
        <el-table-column label="Actions" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="router.push(`/merchant/products/edit/${row.spuId}`)">Edit</el-button>
            <el-button v-if="row.status !== ProductStatus.ON_SHELF" text type="success" size="small" @click="handleOnShelf(row.spuId)">On Shelf</el-button>
            <el-button v-if="row.status === ProductStatus.ON_SHELF" text type="warning" size="small" @click="handleOffShelf(row.spuId)">Off Shelf</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row.spuId)">Delete</el-button>
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
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}
</style>
