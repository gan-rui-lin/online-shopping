<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { searchProducts } from '@/api/product'
import { getCategoryTree } from '@/api/category'
import type { ProductSimpleVO, CategoryVO, ProductSearchDTO } from '@/types/product'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const { t } = useI18n()

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
  { label: 'productList.defaultSort', value: '' },
  { label: 'productList.priceAsc', value: 'price_asc' },
  { label: 'productList.priceDesc', value: 'price_desc' },
  { label: 'productList.salesFirst', value: 'sales_desc' },
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

function handleSortChange(val: string) {
  currentSort.value = val
  searchForm.value.pageNum = 1
  fetchProducts()
}

function selectCategory(id?: number) {
  searchForm.value.categoryId = id
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
  <div class="product-page container">
    <section class="search-banner float-hover">
      <div class="search-inner">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="t('productList.searchPlaceholder')"
          clearable
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">{{ t('common.search') }}</el-button>
          </template>
        </el-input>
      </div>
    </section>

    <section class="list-layout">
      <aside class="left-filter float-hover">
        <h3>{{ t('productList.categoryFilter') }}</h3>
        <el-button class="cat-btn" :type="!searchForm.categoryId ? 'primary' : 'default'" @click="selectCategory()">{{ t('productList.allCategories') }}</el-button>
        <el-button
          v-for="cat in categories"
          :key="cat.id"
          class="cat-btn"
          :type="searchForm.categoryId === cat.id ? 'primary' : 'default'"
          @click="selectCategory(cat.id)"
        >
          {{ cat.categoryName }}
        </el-button>
      </aside>

      <main class="right-content">
        <div class="toolbar float-hover">
          <div class="sort-group">
            <el-radio-group v-model="currentSort" @change="handleSortChange">
              <el-radio-button v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
                {{ t(opt.label) }}
              </el-radio-button>
            </el-radio-group>
          </div>
          <div class="price-range">
            <el-input v-model.number="searchForm.minPrice" type="number" :placeholder="t('productList.minPrice')" />
            <span>-</span>
            <el-input v-model.number="searchForm.maxPrice" type="number" :placeholder="t('productList.maxPrice')" />
            <el-button @click="handleSearch">{{ t('common.apply') }}</el-button>
          </div>
          <div class="count">{{ t('productList.totalCount', { count: total }) }}</div>
        </div>

        <div v-loading="loading" class="product-grid">
          <ProductCard v-for="item in list" :key="item.spuId" :product="item" class="float-hover" />
        </div>

        <EmptyState v-if="!loading && !list.length" :description="t('productList.empty')" />

        <div v-if="total > 0" class="pagination-wrapper">
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
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.product-page {
  padding: 16px 0 32px;
}

.search-banner {
  background: linear-gradient(120deg, #ff7a00, #ff5000);
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 16px;

  .search-inner {
    max-width: 760px;

    :deep(.el-input-group__append .el-button) {
      background: #fff;
      color: #ff5000;
      border: none;
      font-weight: 600;
    }
  }
}

.list-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 14px;
}

.left-filter,
.toolbar {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  box-shadow: var(--shadow-sm);
}

.left-filter {
  padding: 14px;
  display: grid;
  align-content: start;
  gap: 10px;

  h3 {
    margin-bottom: 4px;
    color: var(--text-primary);
  }

  .cat-btn {
    margin: 0;
    justify-content: flex-start;
  }
}

.right-content {
  min-width: 0;
}

.toolbar {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;

  :deep(.el-input) {
    width: 110px;
  }

  span {
    color: var(--text-secondary);
  }
}

.count {
  color: var(--text-secondary);
  font-size: 13px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  min-height: 220px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 1280px) {
  .list-layout {
    grid-template-columns: 1fr;
  }

  .toolbar {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
