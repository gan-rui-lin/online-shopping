<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotProducts, type RecommendProductVO } from '@/api/recommend'
import { getCategoryTree } from '@/api/category'
import type { CategoryVO } from '@/types/product'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductSimpleVO } from '@/types/product'

const router = useRouter()
const hotProducts = ref<RecommendProductVO[]>([])
const categories = ref<CategoryVO[]>([])
const loading = ref(true)

function toProductCard(item: RecommendProductVO): ProductSimpleVO {
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

function searchByCategory(id: number) {
  router.push({ path: '/products', query: { categoryId: String(id) } })
}

onMounted(async () => {
  try {
    const [prods, cats] = await Promise.all([
      getHotProducts(12).catch(() => []),
      getCategoryTree().catch(() => []),
    ])
    hotProducts.value = prods
    categories.value = cats
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="container">
        <h1>One-Stop Intelligent Shopping Platform</h1>
        <p>Discover amazing products at great prices</p>
        <router-link to="/products">
          <el-button type="primary" size="large">Browse All Products</el-button>
        </router-link>
      </div>
    </section>

    <div class="container">
      <section v-if="categories.length" class="section categories-section">
        <h2 class="section-title">Shop by Category</h2>
        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-item"
            @click="searchByCategory(cat.id)"
          >
            <el-icon :size="28"><Goods /></el-icon>
            <span>{{ cat.categoryName }}</span>
          </div>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">Hot Products</h2>
        <div v-loading="loading" class="product-grid">
          <ProductCard
            v-for="item in hotProducts"
            :key="item.spuId"
            :product="toProductCard(item)"
          />
        </div>
        <div v-if="!loading && !hotProducts.length" class="text-center mt-24">
          <el-empty description="No products yet" />
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 80px 20px;
  text-align: center;

  h1 {
    font-size: 40px;
    font-weight: 700;
    margin-bottom: 12px;
  }

  p {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 32px;
  }
}

.section {
  padding: 40px 0;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 24px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: $bg-white;
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  box-shadow: $shadow-sm;

  span {
    font-size: $font-size-sm;
    color: $text-regular;
  }

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
    color: $primary-color;

    span { color: $primary-color; }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 200px;
}
</style>
