<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts } from '@/api/product'
import { getCategoryTree } from '@/api/category'
import type { ProductSimpleVO, CategoryVO, ProductSearchDTO } from '@/types/product'
import type { PageResult } from '@/types/common'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const list = ref<ProductSimpleVO[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<CategoryVO[]>([])

const searchForm = ref<ProductSearchDTO>({
  keyword: '',
  categoryId: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  sortField: undefined,
  sortOrder: undefined,
  pageNum: 1,
  pageSize: 20,
})

const sortOptions = [
  { label: 'Default', value: '' },
  { label: 'Price Low to High', value: 'price_asc' },
  { label: 'Price High to Low', value: 'price_desc' },
  { label: 'Best Selling', value: 'sales_desc' },
]

const currentSort = ref('')

async function fetchProducts() {
  loading.value = true
  try {
    const params: ProductSearchDTO = {
      ...searchForm.value,
    }
    if (currentSort.value) {
      const [field, order] = currentSort.value.split('_')
      params.sortField = field
      params.sortOrder = order
    }
    const res = await searchProducts(params)
    list.value = res.list
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = await getCategoryTree()
  } catch {
    categories.value = []
  }
}

function handleSearch() {
  searchForm.value.pageNum = 1
  fetchProducts()
}

function handleCategoryClick(catId: number | undefined) {
  searchForm.value.categoryId = catId
  searchForm.value.pageNum = 1
  fetchProducts()
}

function handleSortChange(val: string) {
  currentSort.value = val
  searchForm.value.pageNum = 1
  fetchProducts()
}

function handlePageChange(page: number) {
  searchForm.value.pageNum = page
  fetchProducts()
}

function handleSizeChange(size: number) {
  searchForm.value.pageSize = size
  searchForm.value.pageNum = 1
  fetchProducts()
}

onMounted(() => {
  if (route.query.categoryId) {
    searchForm.value.categoryId = Number(route.query.categoryId)
  }
  if (route.query.keyword) {
    searchForm.value.keyword = String(route.query.keyword)
  }
  fetchCategories()
  fetchProducts()
})
</script>

<template>
  <div class="product-list-page container">
    <div class="filter-bar card-box mb-16">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="Search products..."
            clearable
            style="width: 260px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.categoryId"
            placeholder="All Categories"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model.number="searchForm.minPrice"
            placeholder="Min price"
            type="number"
            style="width: 110px"
          />
        </el-form-item>
        <el-form-item>
          <span style="color: #999"> - </span>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model.number="searchForm.maxPrice"
            placeholder="Max price"
            type="number"
            style="width: 110px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="sort-bar mb-16">
      <el-radio-group v-model="currentSort" @change="handleSortChange">
        <el-radio-button v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
      <span class="total-count">{{ total }} products found</span>
    </div>

    <div v-loading="loading" class="product-grid">
      <ProductCard v-for="item in list" :key="item.spuId" :product="item" />
    </div>

    <EmptyState v-if="!loading && !list.length" description="No products found" />

    <div v-if="total > 0" class="pagination-wrapper mt-24">
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 40, 60]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.product-list-page {
  padding: 20px;
}

.filter-bar {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-count {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-bottom: 40px;
}
</style>
