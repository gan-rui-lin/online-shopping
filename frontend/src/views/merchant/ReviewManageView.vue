<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getMyProducts } from '@/api/product'
import { getProductReviews, replyReview } from '@/api/review'
import type { ProductSimpleVO } from '@/types/product'
import type { ReviewVO } from '@/types/review'

const { t } = useI18n()
const loading = ref(false)
const products = ref<ProductSimpleVO[]>([])
const reviews = ref<ReviewVO[]>([])
const total = ref(0)
const selectedSpuId = ref<number>()
const pageNum = ref(1)
const pageSize = ref(10)
const replyDraft = ref<Record<number, string>>({})

const hasSpu = computed(() => !!selectedSpuId.value)

async function loadProducts() {
  const res = await getMyProducts(1, 100)
  products.value = res.list || []
  if (!selectedSpuId.value && products.value.length > 0) {
    selectedSpuId.value = products.value[0].spuId
  }
}

async function loadReviews() {
  if (!selectedSpuId.value) {
    reviews.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await getProductReviews(selectedSpuId.value, { pageNum: pageNum.value, pageSize: pageSize.value })
    reviews.value = res.list || []
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function handleReply(reviewId: number) {
  const content = (replyDraft.value[reviewId] || '').trim()
  if (!content) {
    ElMessage.warning(t('merchant.replyRequired'))
    return
  }
  await replyReview(reviewId, { replyContent: content })
  ElMessage.success(t('merchant.replySuccess'))
  replyDraft.value[reviewId] = ''
  await loadReviews()
}

function handleSpuChange() {
  pageNum.value = 1
  loadReviews()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadReviews()
}

onMounted(async () => {
  await loadProducts()
  await loadReviews()
})
</script>

<template>
  <div class="merchant-reviews-page">
    <h2 class="page-title mb-16">{{ t('routeTitle.merchantReviews') }}</h2>

    <div class="card-box mb-16">
      <el-form label-width="120px">
        <el-form-item :label="t('intelligence.product')">
          <el-select v-model="selectedSpuId" style="width: 360px" @change="handleSpuChange">
            <el-option v-for="item in products" :key="item.spuId" :label="item.title" :value="item.spuId" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-box">
      <el-table v-loading="loading" :data="reviews" stripe>
        <el-table-column prop="nickname" :label="t('buyer.username')" width="140" />
        <el-table-column prop="score" :label="t('buyer.rating')" width="80" />
        <el-table-column prop="content" :label="t('buyer.reviewContent')" min-width="220" show-overflow-tooltip />
        <el-table-column :label="t('merchant.reply')" min-width="260">
          <template #default="{ row }">
            <template v-if="row.replyContent">
              <span>{{ row.replyContent }}</span>
            </template>
            <template v-else>
              <el-input
                v-model="replyDraft[row.reviewId]"
                :placeholder="t('merchant.replyPlaceholder')"
                size="small"
              />
            </template>
          </template>
        </el-table-column>
        <el-table-column :label="t('buyer.action')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.replyContent"
              text
              type="primary"
              size="small"
              @click="handleReply(row.reviewId)"
            >
              {{ t('merchant.reply') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !hasSpu" :description="t('intelligence.noBindProduct')" />

      <el-pagination
        v-if="hasSpu && total > 0"
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

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}
</style>
