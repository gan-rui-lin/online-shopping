<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getMyProducts } from '@/api/product'
import { generateTitle, generateSellingPoints, getProductEvaluation, getReviewSummary } from '@/api/ai'
import { importRagKnowledge } from '@/api/rag'
import type { ProductSimpleVO } from '@/types/product'
import type { ProductEvaluationVO, ReviewSummaryVO } from '@/types/intelligence'

const { t, locale } = useI18n()
const activeLocale = computed(() => locale.value || 'zh-CN')
const loading = ref(false)
const products = ref<ProductSimpleVO[]>([])
const selectedSpuId = ref<number>()
const generatedTitle = ref('')
const generatedSellingPoints = ref('')
const reviewSummary = ref<ReviewSummaryVO>()
const evaluation = ref<ProductEvaluationVO>()

const selectedProduct = computed(() => products.value.find((p) => p.spuId === selectedSpuId.value))

async function loadProducts() {
  loading.value = true
  try {
    const res = await getMyProducts(1, 100)
    products.value = res.list || []
    if (!selectedSpuId.value && products.value.length > 0) {
      selectedSpuId.value = products.value[0].spuId
    }
  } finally {
    loading.value = false
  }
}

async function handleGenerateTitle() {
  if (!selectedSpuId.value) return
  const res = await generateTitle(selectedSpuId.value, activeLocale.value)
  generatedTitle.value = res.content
  ElMessage.success(t('intelligence.generate'))
}

async function handleGenerateSellingPoints() {
  if (!selectedSpuId.value) return
  const res = await generateSellingPoints(selectedSpuId.value, activeLocale.value)
  generatedSellingPoints.value = res.content
  ElMessage.success(t('intelligence.generate'))
}

async function handleLoadReviewSummary() {
  if (!selectedSpuId.value) return
  reviewSummary.value = await getReviewSummary(selectedSpuId.value, activeLocale.value)
}

async function handleLoadEvaluation() {
  if (!selectedSpuId.value) return
  evaluation.value = await getProductEvaluation(selectedSpuId.value, activeLocale.value)
}

async function handleImportKnowledge() {
  if (!selectedSpuId.value) return
  await importRagKnowledge(selectedSpuId.value)
  ElMessage.success(t('merchant.knowledgeImported'))
}

onMounted(loadProducts)
</script>

<template>
  <div class="merchant-intelligence-page" v-loading="loading">
    <h2 class="page-title mb-16">{{ t('routeTitle.merchantIntelligence') }}</h2>

    <div class="card-box mb-16">
      <el-form label-width="120px">
        <el-form-item :label="t('intelligence.product')">
          <el-select v-model="selectedSpuId" style="width: 360px">
            <el-option
              v-for="item in products"
              :key="item.spuId"
              :label="item.title"
              :value="item.spuId"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="16">
      <el-col :span="12">
        <div class="card-box panel">
          <div class="panel-header">
            <h3>{{ t('intelligence.titleLabel') }}</h3>
            <el-button type="primary" size="small" @click="handleGenerateTitle">{{ t('intelligence.generate') }}</el-button>
          </div>
          <div class="panel-content">{{ generatedTitle || '-' }}</div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-box panel">
          <div class="panel-header">
            <h3>{{ t('intelligence.sellingPointsLabel') }}</h3>
            <el-button type="primary" size="small" @click="handleGenerateSellingPoints">{{ t('intelligence.generate') }}</el-button>
          </div>
          <div class="panel-content">{{ generatedSellingPoints || '-' }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="12">
        <div class="card-box panel">
          <div class="panel-header">
            <h3>{{ t('intelligence.reviewSummaryLabel') }}</h3>
            <el-button type="primary" size="small" @click="handleLoadReviewSummary">{{ t('intelligence.generate') }}</el-button>
          </div>
          <div class="panel-content">
            <template v-if="reviewSummary">
              <div>{{ reviewSummary.summary }}</div>
            </template>
            <template v-else>-</template>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-box panel">
          <div class="panel-header">
            <h3>{{ t('intelligence.evaluationSummary') }}</h3>
            <el-button type="primary" size="small" @click="handleLoadEvaluation">{{ t('intelligence.generate') }}</el-button>
          </div>
          <div class="panel-content">
            <template v-if="evaluation">
              <div>{{ evaluation.summary }}</div>
            </template>
            <template v-else>-</template>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card-box mt-16">
      <el-button type="success" :disabled="!selectedProduct" @click="handleImportKnowledge">
        {{ t('merchant.importKnowledge') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.panel-content {
  min-height: 80px;
  line-height: 1.6;
}
</style>
