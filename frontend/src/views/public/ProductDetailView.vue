<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductDetail } from '@/api/product'
import { getProductReviews, getReviewStatistics } from '@/api/review'
import { getSimilarProducts, type RecommendProductVO } from '@/api/recommend'
import { addToCart } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import PriceDisplay from '@/components/PriceDisplay.vue'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductDetailVO, ProductSkuVO, ProductSimpleVO } from '@/types/product'
import type { ReviewVO, ReviewStatisticVO } from '@/types/review'
import { formatDate, formatSpec } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const spuId = computed(() => Number(route.params.id))
const product = ref<ProductDetailVO | null>(null)
const selectedSku = ref<ProductSkuVO | null>(null)
const quantity = ref(1)
const loading = ref(true)
const addingToCart = ref(false)
const activeTab = ref('detail')

const reviews = ref<ReviewVO[]>([])
const reviewStats = ref<ReviewStatisticVO | null>(null)
const reviewLoading = ref(false)
const reviewPageNum = ref(1)
const reviewTotal = ref(0)

const similarProducts = ref<RecommendProductVO[]>([])
const { t } = useI18n()

const currentImage = ref('')
const imageList = computed(() => {
  if (!product.value) return []
  const images = product.value.imageList || []
  if (product.value.mainImage && !images.includes(product.value.mainImage)) {
    return [product.value.mainImage, ...images]
  }
  return images.length ? images : (product.value.mainImage ? [product.value.mainImage] : [])
})

function selectSku(sku: ProductSkuVO) {
  selectedSku.value = sku
  if (sku.imageUrl) {
    currentImage.value = sku.imageUrl
  }
}

async function handleAddToCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning(t('login.title'))
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!selectedSku.value) {
    ElMessage.warning(t('productDetail.selectSpec'))
    return
  }
  addingToCart.value = true
  try {
    await addToCart({ skuId: selectedSku.value.skuId, quantity: quantity.value })
    ElMessage.success(t('productDetail.addedToCart'))
  } catch {
    // handled by interceptor
  } finally {
    addingToCart.value = false
  }
}

async function fetchReviews() {
  reviewLoading.value = true
  try {
    const res = await getProductReviews(spuId.value, {
      pageNum: reviewPageNum.value,
      pageSize: 10,
    })
    reviews.value = res.list
    reviewTotal.value = res.total
  } catch {
    reviews.value = []
  } finally {
    reviewLoading.value = false
  }
}

function toSimpleVO(item: RecommendProductVO): ProductSimpleVO {
  return {
    spuId: item.spuId,
    title: item.title,
    subTitle: '',
    mainImage: item.mainImage,
    minPrice: item.minPrice,
    maxPrice: item.minPrice,
    salesCount: item.salesCount,
    shopName: '',
  }
}

onMounted(async () => {
  try {
    const [detail, stats, similar] = await Promise.all([
      getProductDetail(spuId.value),
      getReviewStatistics(spuId.value).catch(() => null),
      getSimilarProducts(spuId.value).catch(() => []),
    ])
    product.value = detail
    reviewStats.value = stats
    similarProducts.value = similar

    if (detail.skuList?.length === 1) {
      selectedSku.value = detail.skuList[0]
    }
    currentImage.value = detail.mainImage || ''

    fetchReviews()
  } catch {
    ElMessage.error(t('common.requestFailed'))
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="product-detail-page container">
    <template v-if="product">
      <div class="detail-top card-box">
        <div class="gallery">
          <div class="main-image">
            <el-image :src="currentImage" fit="contain" :preview-src-list="imageList">
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="48"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div v-if="imageList.length > 1" class="thumbnail-list">
            <div
              v-for="(img, idx) in imageList"
              :key="idx"
              class="thumbnail"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>

        <div class="info-panel">
          <h1 class="product-title">{{ product.title }}</h1>
          <p v-if="product.subTitle" class="product-subtitle">{{ product.subTitle }}</p>

          <div class="price-section">
            <PriceDisplay
              :price="selectedSku?.salePrice ?? product.minPrice"
              :origin-price="selectedSku?.originPrice"
              size="large"
            />
            <span v-if="!selectedSku && product.minPrice !== product.maxPrice" class="price-range">
              ¥{{ product.minPrice.toFixed(2) }} - ¥{{ product.maxPrice.toFixed(2) }}
            </span>
          </div>

          <div class="meta-row">
            <span>{{ t('productDetail.sales') }}: {{ product.salesCount }}</span>
            <span>{{ t('productDetail.favorites') }}: {{ product.favoriteCount }}</span>
            <span v-if="product.shopName">{{ t('productDetail.shop') }}: {{ product.shopName }}</span>
          </div>

          <div v-if="product.skuList?.length" class="sku-section">
            <h4>{{ t('productDetail.specifications') }}</h4>
            <div class="sku-list">
              <div
                v-for="sku in product.skuList"
                :key="sku.skuId"
                class="sku-item"
                :class="{ selected: selectedSku?.skuId === sku.skuId, disabled: sku.stock <= 0 }"
                @click="sku.stock > 0 && selectSku(sku)"
              >
                <span>{{ sku.skuName || formatSpec(sku.specJson) || sku.skuCode }}</span>
                <span v-if="sku.stock <= 0" class="out-of-stock">{{ t('productDetail.outOfStock') }}</span>
              </div>
            </div>
          </div>

          <div class="quantity-section">
            <span>{{ t('buyer.quantity') }}</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="selectedSku?.stock || 999"
              size="default"
            />
            <span v-if="selectedSku" class="stock-info">{{ t('productDetail.stock') }}: {{ selectedSku.stock }}</span>
          </div>

          <div class="action-section">
            <el-button type="primary" size="large" :loading="addingToCart" @click="handleAddToCart">
              <el-icon><ShoppingCart /></el-icon> {{ t('productDetail.addToCart') }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-bottom mt-24">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="t('productDetail.details')" name="detail">
            <div class="detail-content card-box">
              <div v-if="product.detailText" v-html="product.detailText" />
              <el-empty v-else :description="t('productDetail.noDetail')" />
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`${t('productDetail.reviewsTab')} (${reviewStats?.totalCount ?? 0})`" name="reviews">
            <div class="review-content card-box">
              <div v-if="reviewStats" class="review-summary mb-16">
                <div class="stat-item">
                  <span class="stat-value">{{ reviewStats.totalCount }}</span>
                  <span class="stat-label">{{ t('productDetail.total') }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value good">{{ reviewStats.goodRate }}%</span>
                  <span class="stat-label">{{ t('productDetail.positive') }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ reviewStats.goodCount }}</span>
                  <span class="stat-label">{{ t('productDetail.good') }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ reviewStats.mediumCount }}</span>
                  <span class="stat-label">{{ t('productDetail.medium') }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ reviewStats.badCount }}</span>
                  <span class="stat-label">{{ t('productDetail.bad') }}</span>
                </div>
              </div>

              <div v-loading="reviewLoading">
                <div v-for="review in reviews" :key="review.reviewId" class="review-item">
                  <div class="review-header">
                    <span class="reviewer">{{ review.anonymousFlag ? t('productDetail.anonymous') : review.nickname }}</span>
                    <el-rate :model-value="review.score" disabled />
                    <span class="review-date">{{ formatDate(review.createTime) }}</span>
                  </div>
                  <p class="review-text">{{ review.content }}</p>
                  <div v-if="review.imageUrls?.length" class="review-images">
                    <el-image
                      v-for="(img, idx) in review.imageUrls"
                      :key="idx"
                      :src="img"
                      :preview-src-list="review.imageUrls"
                      fit="cover"
                      class="review-img"
                    />
                  </div>
                  <div v-if="review.replyContent" class="review-reply">
                    <span class="reply-label">{{ t('productDetail.merchantReply') }}</span>
                    {{ review.replyContent }}
                  </div>
                </div>

                <el-empty v-if="!reviewLoading && !reviews.length" :description="t('productDetail.noReviews')" />

                <el-pagination
                  v-if="reviewTotal > 10"
                  v-model:current-page="reviewPageNum"
                  :total="reviewTotal"
                  :page-size="10"
                  layout="prev, pager, next"
                  class="mt-16"
                  @current-change="fetchReviews"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-if="similarProducts.length" class="similar-section mt-24">
        <h2 class="section-title">{{ t('productDetail.similarProducts') }}</h2>
        <div class="product-grid">
          <ProductCard v-for="item in similarProducts" :key="item.spuId" :product="toSimpleVO(item)" />
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.product-detail-page {
  padding: 20px;
  min-height: 400px;
}

.detail-top {
  display: flex;
  gap: 32px;
}

.gallery {
  width: 440px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: $border-radius;
  overflow: hidden;

  .el-image { width: 100%; height: 100%; }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-color;
    color: $text-placeholder;
  }
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  overflow-x: auto;
}

.thumbnail {
  width: 64px;
  height: 64px;
  border-radius: $border-radius;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;

  &.active { border-color: $primary-color; }
  .el-image { width: 100%; height: 100%; }
}

.info-panel {
  flex: 1;
  min-width: 0;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
}

.product-subtitle {
  font-size: $font-size-base;
  color: $text-secondary;
  margin-bottom: 16px;
}

.price-section {
  background: #fdf6ec;
  padding: 16px;
  border-radius: $border-radius;
  margin-bottom: 16px;
}

.price-range {
  font-size: 18px;
  color: $danger-color;
  font-weight: 600;
}

.meta-row {
  display: flex;
  gap: 24px;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 20px;
}

.sku-section {
  margin-bottom: 20px;

  h4 { margin-bottom: 12px; font-weight: 500; }
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sku-item {
  padding: 8px 16px;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-sm;
  display: flex;
  gap: 6px;
  align-items: center;
  transition: all 0.2s;

  &:hover { border-color: $primary-color; }
  &.selected { border-color: $primary-color; color: $primary-color; background: #ecf5ff; }
  &.disabled { opacity: 0.5; cursor: not-allowed; }
  .out-of-stock { color: $danger-color; font-size: 11px; }
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: $font-size-base;
}

.stock-info {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.action-section {
  display: flex;
  gap: 12px;
}

.detail-content {
  min-height: 200px;
  line-height: 1.8;

  :deep(img) { max-width: 100%; }
}

.review-summary {
  display: flex;
  gap: 32px;
  padding: 16px 0;
  border-bottom: 1px solid $border-lighter;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: $text-primary;

  &.good { color: $success-color; }
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid $border-lighter;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.reviewer {
  font-weight: 500;
}

.review-date {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-left: auto;
}

.review-text {
  color: $text-regular;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.review-img {
  width: 80px;
  height: 80px;
  border-radius: $border-radius;
}

.review-reply {
  background: $bg-color;
  padding: 8px 12px;
  border-radius: $border-radius;
  font-size: $font-size-sm;
  color: $text-regular;

  .reply-label { color: $primary-color; font-weight: 500; }
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 40px;
}
</style>
