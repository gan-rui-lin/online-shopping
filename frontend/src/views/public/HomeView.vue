<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCategoryTree } from '@/api/category'
import { getHotProducts, getPersonalProducts, type RecommendProductVO } from '@/api/recommend'
import type { CategoryVO, ProductSimpleVO } from '@/types/product'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()

const categories = ref<CategoryVO[]>([])
const hotProducts = ref<RecommendProductVO[]>([])
const personalProducts = ref<RecommendProductVO[]>([])
const loading = ref(true)

interface HeroKeywordItem {
  full: string
  display: string
}

function truncateText(text: string, maxLength = 12) {
  return text.length > maxLength ? text.substring(0, maxLength) + '…' : text
}

const heroKeywords = computed<HeroKeywordItem[]>(() => {
  if (hotProducts.value.length > 0) {
    return hotProducts.value
      .slice(0, 5)
      .map((p) => {
        const full = (p.title || '').trim()
        return {
          full,
          display: truncateText(full, 12),
        }
      })
      .filter((item) => item.full.length > 0)
  }

  return categories.value
    .slice(0, 5)
    .map((c) => {
      const full = (c.categoryName || '').trim()
      return {
        full,
        display: truncateText(full, 12),
      }
    })
    .filter((item) => item.full.length > 0)
})

const guessLikeProducts = computed(() => {
  if (personalProducts.value.length > 0) return personalProducts.value.slice(0, 3)
  return hotProducts.value.slice(0, 3)
})

const trendingProducts = computed(() => {
  const personalIds = new Set(guessLikeProducts.value.map((p) => p.spuId))
  const remaining = hotProducts.value.filter((p) => !personalIds.has(p.spuId))
  return remaining.slice(0, 3)
})

function toProduct(item: RecommendProductVO): ProductSimpleVO {
  return {
    spuId: item.spuId,
    title: item.title,
    subTitle: item.reason || '',
    mainImage: item.mainImage,
    minPrice: item.minPrice,
    maxPrice: item.minPrice,
    salesCount: item.salesCount,
    shopName: '',
  }
}

function toCategory(id?: number) {
  router.push({
    path: '/products',
    query: id ? { categoryId: String(id) } : undefined,
  })
}

function searchKeyword(keyword: string) {
  const fullKeyword = keyword.trim()
  if (!fullKeyword) return
  router.push({ path: '/products', query: { keyword: fullKeyword } })
}

onMounted(async () => {
  try {
    const fetchPersonal = userStore.isLoggedIn
      ? getPersonalProducts(12).catch(() => [] as RecommendProductVO[])
      : Promise.resolve([] as RecommendProductVO[])

    const [catRes, hotRes, personalRes] = await Promise.all([
      getCategoryTree().catch(() => [] as CategoryVO[]),
      getHotProducts(12).catch(() => [] as RecommendProductVO[]),
      fetchPersonal,
    ])

    categories.value = catRes
    hotProducts.value = hotRes
    personalProducts.value = personalRes
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <div class="container">
      <section class="hero-grid">
        <aside class="category-panel float-hover">
          <h3>{{ t('home.allCategories') }}</h3>
          <ul>
            <li v-for="cat in categories.slice(0, 10)" :key="cat.id" @click="toCategory(cat.id)">
              <span>{{ cat.categoryName }}</span>
              <el-icon><ArrowRight /></el-icon>
            </li>
          </ul>
        </aside>

        <main class="hero-main float-hover">
          <div class="hero-banner">
            <h1>{{ t('home.heroTitle') }}</h1>
            <p>{{ t('home.heroDesc') }}</p>

            <div class="keyword-line">
              <span
                v-for="(item, index) in heroKeywords"
                :key="`${item.full}-${index}`"
                class="keyword-tag"
                :title="item.full"
                @click.stop="searchKeyword(item.full)"
              >
                {{ item.display }}
              </span>
            </div>

            <div class="cta-row">
              <el-button type="primary" size="large" @click="toCategory()">
                {{ t('home.shopNow') }}
              </el-button>
              <router-link to="/buyer/intelligence">
                <el-button size="large" plain>
                  {{ t('home.intelligenceCenter') }}
                </el-button>
              </router-link>
            </div>
          </div>

          <div class="hero-cards">
            <article class="small-card clickable" @click="router.push('/products')">
              <h4>{{ t('home.guessLike') }}</h4>
              <div v-if="guessLikeProducts.length" class="mini-products">
                <div
                  v-for="item in guessLikeProducts"
                  :key="item.spuId"
                  class="mini-product"
                  @click.stop="router.push(`/products/${item.spuId}`)"
                >
                  <el-image :src="item.mainImage" fit="cover" lazy>
                    <template #error>
                      <div class="mini-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <span class="mini-price">¥{{ item.minPrice }}</span>
                </div>
              </div>
              <p v-else class="empty-hint">{{ t('home.guessLikeDesc') }}</p>
            </article>

            <article class="small-card clickable" @click="router.push('/products')">
              <h4>{{ t('home.hotProducts') }}</h4>
              <div v-if="trendingProducts.length" class="mini-products">
                <div
                  v-for="item in trendingProducts"
                  :key="item.spuId"
                  class="mini-product"
                  @click.stop="router.push(`/products/${item.spuId}`)"
                >
                  <el-image :src="item.mainImage" fit="cover" lazy>
                    <template #error>
                      <div class="mini-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <span class="mini-price">¥{{ item.minPrice }}</span>
                </div>
              </div>
              <p v-else class="empty-hint">{{ t('home.aiGuideDesc') }}</p>
            </article>
          </div>
        </main>

        <aside class="user-panel float-hover">
          <h3>{{ t('home.quickEntry') }}</h3>
          <router-link to="/buyer/orders">
            <el-button text>{{ t('publicLayout.orders') }}</el-button>
          </router-link>
          <router-link to="/buyer/cart">
            <el-button text>{{ t('common.cart') }}</el-button>
          </router-link>
          <router-link to="/buyer/intelligence">
            <el-button text>{{ t('publicLayout.aiHub') }}</el-button>
          </router-link>
          <router-link to="/buyer/merchant-apply">
            <el-button text>{{ t('home.merchantApply') }}</el-button>
          </router-link>
        </aside>
      </section>

      <section class="module-block">
        <div class="module-title">
          <h2>{{ t('home.hotProducts') }}</h2>
          <router-link to="/products">{{ t('home.viewMore') }}</router-link>
        </div>
        <div v-loading="loading" class="product-grid">
          <ProductCard
            v-for="item in hotProducts"
            :key="item.spuId"
            :product="toProduct(item)"
            class="float-hover"
          />
        </div>
      </section>

      <section v-if="personalProducts.length" class="module-block">
        <div class="module-title">
          <h2>{{ t('home.personalRecommend') }}</h2>
          <router-link to="/products">{{ t('home.viewMore') }}</router-link>
        </div>
        <div class="product-grid">
          <ProductCard
            v-for="item in personalProducts"
            :key="`p-${item.spuId}`"
            :product="toProduct(item)"
            class="float-hover"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  padding: 18px 0 32px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 260px 1fr 240px;
  gap: 14px;
  margin-bottom: 18px;
}

.category-panel,
.hero-main,
.user-panel,
.module-block {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.category-panel,
.user-panel {
  padding: 16px;

  h3 {
    color: var(--text-primary);
    margin-bottom: 12px;
  }
}

.category-panel ul {
  list-style: none;
  display: grid;
  gap: 8px;

  li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: var(--text-regular);
    padding: 8px 10px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: var(--primary-weak);
      color: var(--primary-color);
    }
  }
}

.hero-main {
  overflow: hidden;
}

.hero-banner {
  padding: 30px;
  min-height: 220px;
  background: linear-gradient(120deg, #ff5e00 0%, #ff9a2f 45%, #f6b73c 100%);
  color: #fff;
  animation: bg-flow 8s ease-in-out infinite alternate;

  h1 {
    font-size: 40px;
    line-height: 1.1;
    margin-bottom: 12px;
  }

  p {
    opacity: 0.94;
    margin-bottom: 16px;
  }
}

@keyframes bg-flow {
  from {
    background-position: 0% 50%;
  }
  to {
    background-position: 100% 50%;
  }
}

.keyword-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;

  span {
    border: 1px solid rgba(255, 255, 255, 0.45);
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 12px;
  }
}

.cta-row {
  display: flex;
  gap: 10px;
}

.keyword-tag {
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }
}

.hero-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 12px;

  .small-card {
    background: color-mix(in oklab, var(--bg-card) 92%, transparent);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 12px;

    h4 {
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    &.clickable {
      cursor: pointer;
      transition: box-shadow 0.2s, transform 0.15s;

      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-1px);
      }
    }
  }

  .empty-hint {
    color: var(--text-secondary);
    font-size: 13px;
  }

  .mini-products {
    display: flex;
    gap: 8px;
  }

  .mini-product {
    flex: 1;
    min-width: 0;
    cursor: pointer;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid var(--border-color);
    transition: box-shadow 0.15s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .el-image {
      width: 100%;
      aspect-ratio: 1;
      display: block;
    }

    .mini-placeholder {
      width: 100%;
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-color, #f5f5f5);
      color: var(--text-placeholder, #c0c4cc);
    }

    .mini-price {
      display: block;
      text-align: center;
      font-size: 12px;
      font-weight: 600;
      color: #ff5e00;
      padding: 4px 0;
    }
  }
}

.user-panel {
  display: grid;
  gap: 10px;

  :deep(.el-button.is-text) {
    justify-content: flex-start;
    color: var(--text-regular);
    border: 1px dashed var(--border-color);
    border-radius: 10px;
    padding: 8px 10px;
    margin: 0;

    &:hover {
      color: var(--primary-color);
      border-color: var(--primary-color);
      background: var(--primary-weak);
    }
  }
}

.module-block {
  padding: 16px;
  margin-bottom: 16px;
}

.module-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h2 {
    color: var(--text-primary);
    font-size: 22px;
  }

  a {
    color: var(--primary-color);
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;

  &.compact {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 1280px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .product-grid.compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>