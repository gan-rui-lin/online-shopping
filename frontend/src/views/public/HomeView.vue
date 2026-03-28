<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { getCategoryTree } from '@/api/category'
import { getHotProducts, getSimilarProducts, getPersonalProducts, type RecommendProductVO } from '@/api/recommend'
import type { CategoryVO, ProductSimpleVO } from '@/types/product'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()

const categories = ref<CategoryVO[]>([])
const hotProducts = ref<RecommendProductVO[]>([])
const similarProducts = ref<RecommendProductVO[]>([])
const personalProducts = ref<RecommendProductVO[]>([])
const heroKeywords = computed(() =>
  locale.value === 'zh-CN'
    ? ['九分直筒裤', '游戏本', '空气炸锅', '跑步耳机', '健身手环']
    : ['Straight Pants', 'Gaming Laptop', 'Air Fryer', 'Running Earbuds', 'Fitness Band'],
)
const loading = ref(true)

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
  router.push({ path: '/products', query: id ? { categoryId: String(id) } : undefined })
}

onMounted(async () => {
  try {
    const personalPromise = userStore.isLoggedIn ? getPersonalProducts(6).catch(() => []) : Promise.resolve([])
    const [catRes, hotRes, simRes, personalRes] = await Promise.all([
      getCategoryTree().catch(() => []),
      getHotProducts(12).catch(() => []),
      getSimilarProducts(1000, 6).catch(() => []),
      personalPromise,
    ])
    categories.value = catRes
    hotProducts.value = hotRes
    similarProducts.value = simRes
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
              <span v-for="key in heroKeywords" :key="key">{{ key }}</span>
            </div>
            <div class="cta-row">
              <el-button type="primary" size="large" @click="toCategory()">{{ t('home.shopNow') }}</el-button>
              <router-link to="/buyer/intelligence">
                <el-button size="large" plain>{{ t('home.intelligenceCenter') }}</el-button>
              </router-link>
            </div>
          </div>

          <div class="hero-cards">
            <article class="small-card">
              <h4>{{ t('home.guessLike') }}</h4>
              <p>{{ t('home.guessLikeDesc') }}</p>
            </article>
            <article class="small-card">
              <h4>{{ t('home.aiGuide') }}</h4>
              <p>{{ t('home.aiGuideDesc') }}</p>
            </article>
          </div>
        </main>

        <aside class="user-panel float-hover">
          <h3>{{ t('home.quickEntry') }}</h3>
          <router-link to="/buyer/orders"><el-button text>{{ t('publicLayout.orders') }}</el-button></router-link>
          <router-link to="/buyer/cart"><el-button text>{{ t('common.cart') }}</el-button></router-link>
          <router-link to="/buyer/intelligence"><el-button text>{{ t('publicLayout.aiHub') }}</el-button></router-link>
          <router-link to="/buyer/merchant-apply"><el-button text>{{ t('home.merchantApply') }}</el-button></router-link>
        </aside>
      </section>

      <section class="module-block">
        <div class="module-title">
          <h2>{{ t('home.hotProducts') }}</h2>
          <router-link to="/products">{{ t('home.viewMore') }}</router-link>
        </div>
        <div v-loading="loading" class="product-grid">
          <ProductCard v-for="item in hotProducts" :key="item.spuId" :product="toProduct(item)" class="float-hover" />
        </div>
      </section>

      <section class="module-block two-col">
        <div>
          <div class="module-title"><h2>{{ t('home.similarRecommend') }}</h2></div>
          <div class="product-grid compact">
            <ProductCard v-for="item in similarProducts" :key="`s-${item.spuId}`" :product="toProduct(item)" class="float-hover" />
          </div>
        </div>
        <div>
          <div class="module-title"><h2>{{ t('home.personalRecommend') }}</h2></div>
          <div class="product-grid compact">
            <ProductCard v-for="item in personalProducts" :key="`p-${item.spuId}`" :product="toProduct(item)" class="float-hover" />
          </div>
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
  from { background-position: 0% 50%; }
  to { background-position: 100% 50%; }
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
      margin-bottom: 6px;
    }

    p {
      color: var(--text-secondary);
      font-size: 13px;
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
