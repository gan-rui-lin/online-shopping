<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { askRag } from '@/api/rag'
import { createAgentTask, addAgentResultToCart } from '@/api/agent'
import { generateSellingPoints, getProductEvaluation, getReviewSummary } from '@/api/ai'
import { getFavoriteList, toggleFavorite } from '@/api/favorite'
import { clearBrowseHistory, getBrowseHistory } from '@/api/behavior'
import { createShoppingPlan, executeShoppingPlan, getShoppingPlanList } from '@/api/plan'
import { resolveImageUrl } from '@/utils/image'
import type {
  AgentTaskCreateDTO,
  AgentTaskVO,
  ProductEvaluationVO,
  ChatMessageVO,
  FavoriteVO,
  BrowseHistoryVO,
  ShoppingPlanVO,
  ReviewSummaryVO,
} from '@/types/intelligence'

const activeTab = ref('rag')
const { t } = useI18n()
const router = useRouter()

const ragForm = ref({ sourceType: 'favorite', question: '' })
const ragSelectedKeys = ref<string[]>([])
const ragAnswer = ref('')
const ragSessionId = ref<number>()
const ragHistory = ref<ChatMessageVO[]>([])

const agentTaskType = ref<'NECESSITY' | 'INTENTION'>('NECESSITY')
const necessityForm = ref({
  frequency: '每月一次',
  bindSpuId: undefined as number | undefined,
  quantity: 1,
})
const intentionForm = ref({
  productName: '',
  requirementPreference: '',
  budgetLimit: undefined as number | undefined,
})
const agentTask = ref<AgentTaskVO>()
const selectedSkuIds = ref<number[]>([])
const isNecessityMode = computed(() => agentTaskType.value === 'NECESSITY')

const aiSpuId = ref<number | undefined>(undefined)
const aiSelectedKey = ref('')
const aiSourceType = ref<'favorite' | 'history'>('favorite')
const aiSellingPoints = ref<string[]>([])
const aiReviewSummary = ref<ReviewSummaryVO>()
const aiEvaluation = ref<ProductEvaluationVO>()

const favorites = ref<FavoriteVO[]>([])
const histories = ref<BrowseHistoryVO[]>([])
const plans = ref<ShoppingPlanVO[]>([])

const loading = ref(false)

const ragProductOptions = computed(() => {
  const source = ragForm.value.sourceType
  const list = source === 'favorite' ? favorites.value : histories.value
  return list.map((item, index) => ({
    uiKey: `${source}-${item.spuId}-${'createTime' in item ? item.createTime : item.browseTime}-${index}`,
    spuId: item.spuId,
    title: item.title,
    mainImage: item.mainImage,
    minPrice: item.minPrice,
    shopName: 'shopName' in item ? item.shopName : '精选商品',
    time: 'createTime' in item ? item.createTime : item.browseTime,
  }))
})

const ragSelectedSpuIds = computed(() => {
  const selected = ragProductOptions.value
    .filter((item) => ragSelectedKeys.value.includes(item.uiKey))
    .map((item) => item.spuId)
  return Array.from(new Set(selected))
})

const bindProductOptions = computed(() => {
  const map = new Map<number, { title: string; mainImage: string; minPrice: number; shopName: string; from: string }>()
  ;[...favorites.value, ...histories.value].forEach((item) => {
    if (!map.has(item.spuId)) {
      map.set(item.spuId, {
        title: item.title,
        mainImage: item.mainImage,
        minPrice: item.minPrice,
        shopName: 'shopName' in item ? item.shopName : '精选商品',
        from: 'createTime' in item ? '收藏' : '历史',
      })
    }
  })
  return Array.from(map.entries()).map(([spuId, meta]) => ({ spuId, ...meta }))
})


const aiProductOptions = computed(() => {
  const source = aiSourceType.value
  const list = source === 'favorite' ? favorites.value : histories.value
  return list.map((item, index) => ({
    uiKey: `${source}-${item.spuId}-${'createTime' in item ? item.createTime : item.browseTime}-${index}`,
    spuId: item.spuId,
    title: item.title,
    mainImage: item.mainImage,
    minPrice: item.minPrice,
    shopName: 'shopName' in item ? item.shopName : '精选商品',
    time: 'createTime' in item ? item.createTime : item.browseTime,
  }))
})

function normalizeLines(text: string): string[] {
  return text
    .split('\n')
    .map((line) => line.replace(/^\s*\d+[.)、]\s*/, '').trim())
    .filter((line) => !!line)
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderMarkdown(mdText: string): string {
  if (!mdText) {
    return ''
  }

  const lines = mdText.split(/\r?\n/)
  const htmlLines: string[] = []
  let inList = false

  const closeListIfNeeded = () => {
    if (inList) {
      htmlLines.push('</ul>')
      inList = false
    }
  }

  const inline = (text: string) =>
    escapeHtml(text)
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/`(.+?)`/g, '<code>$1</code>')
      .replace(/\[(.+?)\]\((https?:\/\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) {
      closeListIfNeeded()
      continue
    }

    if (/^[-*]\s+/.test(trimmed)) {
      if (!inList) {
        htmlLines.push('<ul>')
        inList = true
      }
      htmlLines.push(`<li>${inline(trimmed.replace(/^[-*]\s+/, ''))}</li>`)
      continue
    }

    closeListIfNeeded()

    if (/^###\s+/.test(trimmed)) {
      htmlLines.push(`<h3>${inline(trimmed.replace(/^###\s+/, ''))}</h3>`)
      continue
    }
    if (/^##\s+/.test(trimmed)) {
      htmlLines.push(`<h2>${inline(trimmed.replace(/^##\s+/, ''))}</h2>`)
      continue
    }
    if (/^#\s+/.test(trimmed)) {
      htmlLines.push(`<h1>${inline(trimmed.replace(/^#\s+/, ''))}</h1>`)
      continue
    }

    htmlLines.push(`<p>${inline(trimmed)}</p>`)
  }

  closeListIfNeeded()
  return htmlLines.join('')
}

const aiReviewSummaryHtml = computed(() => renderMarkdown(aiReviewSummary.value?.summary || ''))
const ragAnswerHtml = computed(() => renderMarkdown(ragAnswer.value || ''))


function selectRagProduct(item: { uiKey: string }) {
  if (ragSelectedKeys.value.includes(item.uiKey)) {
    ragSelectedKeys.value = ragSelectedKeys.value.filter((key) => key !== item.uiKey)
    return
  }
  ragSelectedKeys.value = [...ragSelectedKeys.value, item.uiKey]
}

function goProductDetail(spuId: number) {
  router.push(`/products/${spuId}`)
}

function selectNecessityBindProduct(spuId: number) {
  necessityForm.value.bindSpuId = spuId
}

function selectAiProduct(item: { spuId: number; uiKey: string }) {
  aiSpuId.value = item.spuId
  aiSelectedKey.value = item.uiKey
}

async function loadExtraPanels() {
  const [favRes, histRes, planRes] = await Promise.all([
    getFavoriteList(1, 6).catch(() => ({ list: [] } as any)),
    getBrowseHistory(1, 6).catch(() => ({ list: [] } as any)),
    getShoppingPlanList().catch(() => []),
  ])

  favorites.value = favRes.list || []
  histories.value = histRes.list || []
  plans.value = planRes || []

  const firstRagItem = ragProductOptions.value[0]
  if (firstRagItem && ragSelectedKeys.value.length === 0) {
    ragSelectedKeys.value = [firstRagItem.uiKey]
  }

  const defaultSpuId = bindProductOptions.value[0]?.spuId
  if (!necessityForm.value.bindSpuId && defaultSpuId) {
    necessityForm.value.bindSpuId = defaultSpuId
  }

  const firstAiItem = aiProductOptions.value[0]
  if (firstAiItem && !aiSelectedKey.value) {
    aiSpuId.value = firstAiItem.spuId
    aiSelectedKey.value = firstAiItem.uiKey
  }
}

async function submitRagQuestion() {
  if (ragSelectedSpuIds.value.length === 0) {
    ElMessage.warning('请先从收藏或历史中选择至少一个商品')
    return
  }
  if (!ragForm.value.question.trim()) {
    ElMessage.warning(t('intelligence.questionRequired'))
    return
  }

  loading.value = true
  try {
    const askedQuestion = ragForm.value.question.trim()
    const result = await askRag({
      spuIds: ragSelectedSpuIds.value,
      question: askedQuestion,
      sessionId: ragSessionId.value,
    })
    ragAnswer.value = result.answer
    ragSessionId.value = result.sessionId
    const now = new Date().toLocaleString()
    ragHistory.value = [...ragHistory.value, { role: 'user', content: askedQuestion, createTime: now }, { role: 'assistant', content: result.answer, createTime: now }]
  } finally {
    loading.value = false
  }
}

watch(ragProductOptions, (options) => {
  const validKeys = new Set(options.map((item) => item.uiKey))
  ragSelectedKeys.value = ragSelectedKeys.value.filter((key) => validKeys.has(key))
  if (options.length > 0 && ragSelectedKeys.value.length === 0) {
    ragSelectedKeys.value = [options[0].uiKey]
  }
})

async function runAgentTask() {
  if (agentTaskType.value === 'NECESSITY') {
    if (!necessityForm.value.bindSpuId) {
      ElMessage.warning('必需品代购需要先选择绑定商品')
      return
    }
    const bindItem = bindProductOptions.value.find((item) => item.spuId === necessityForm.value.bindSpuId)
    const keyword = bindItem?.title || '必需品'
    loading.value = true
    try {
      await createShoppingPlan({
        planName: `${keyword}-${necessityForm.value.frequency}`,
        remark: `频次：${necessityForm.value.frequency}；商品：${keyword}`,
        items: [
          {
            keyword,
            quantity: necessityForm.value.quantity || 1,
          },
        ],
      })
      agentTask.value = undefined
      selectedSkuIds.value = []
      await loadExtraPanels()
      ElMessage.success('已加入定期计划')
    } finally {
      loading.value = false
    }
    return
  }

  if (!intentionForm.value.productName.trim()) {
    ElMessage.warning('请先输入商品名称')
    return
  }
  if (!intentionForm.value.requirementPreference.trim()) {
    ElMessage.warning('请先输入需求、偏好')
    return
  }

  const payload: AgentTaskCreateDTO = {
    taskType: 'INTENTION',
    intentRequirement: `${intentionForm.value.productName} ${intentionForm.value.requirementPreference}`.trim(),
    preference: intentionForm.value.requirementPreference,
    budgetLimit: intentionForm.value.budgetLimit,
  }

  loading.value = true
  try {
    const task = await createAgentTask(payload)
    agentTask.value = task
    selectedSkuIds.value = []
  } finally {
    loading.value = false
  }
}

async function addSelectedToCart() {
  if (!agentTask.value || selectedSkuIds.value.length === 0) {
    ElMessage.warning(t('intelligence.selectSkuRequired'))
    return
  }
  await addAgentResultToCart(agentTask.value.taskId, selectedSkuIds.value)
  ElMessage.success(t('intelligence.addedToCart'))
}

async function runAiCopywriting() {
  if (!aiSpuId.value) {
    ElMessage.warning('请先从收藏或历史中选择商品')
    return
  }
  const selected = aiProductOptions.value.find((item) => item.uiKey === aiSelectedKey.value)
    || aiProductOptions.value.find((item) => item.spuId === aiSpuId.value)
  const autoKeywords = selected?.title || '商品'

  loading.value = true
  try {
    const [selling, summary, evaluation] = await Promise.all([
      generateSellingPoints(aiSpuId.value).catch(() => ({ content: t('intelligence.needMerchant'), variants: [] })),
      getReviewSummary(aiSpuId.value).catch(() => undefined),
      getProductEvaluation(aiSpuId.value).catch(() => undefined),
    ])
    aiSellingPoints.value = normalizeLines(selling.content)
    aiReviewSummary.value = summary
    aiEvaluation.value = evaluation
  } finally {
    loading.value = false
  }
}

async function handleToggleFavorite(spuId: number) {
  await toggleFavorite(spuId)
  await loadExtraPanels()
  ElMessage.success(t('intelligence.favoriteUpdated'))
}

async function handleClearHistory() {
  await clearBrowseHistory()
  await loadExtraPanels()
  ElMessage.success(t('intelligence.historyCleared'))
}

async function quickCreatePlan() {
  await createShoppingPlan({
    planName: t('intelligence.quickPlanName'),
    budgetAmount: 1000,
    remark: t('intelligence.quickPlanRemark'),
    items: [{ keyword: '耳机', expectedPriceMin: 100, expectedPriceMax: 500, quantity: 1 }],
  })
  await loadExtraPanels()
  ElMessage.success(t('intelligence.planCreated'))
}

async function quickExecutePlan(planId: string) {
  await executeShoppingPlan(planId)
  ElMessage.success(t('intelligence.planExecuted'))
}

onMounted(loadExtraPanels)
</script>

<template>
  <div class="intelligence-page">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="header-row">
          <h2>{{ t('intelligence.title') }}</h2>
          <span class="desc">{{ t('intelligence.subtitle') }}</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="int-tabs">
        <el-tab-pane :label="t('intelligence.tabRag')" name="rag">
          <div class="source-switch">
            <el-radio-group v-model="ragForm.sourceType">
              <el-radio-button label="favorite">我的收藏</el-radio-button>
              <el-radio-button label="history">浏览历史</el-radio-button>
            </el-radio-group>
          </div>

          <div v-if="ragProductOptions.length" class="product-browser">
            <button
              v-for="item in ragProductOptions"
              :key="item.uiKey"
              class="product-card"
              :class="{ selected: ragSelectedKeys.includes(item.uiKey) }"
              @click="selectRagProduct(item)"
            >
              <img :src="resolveImageUrl(item.mainImage)" :alt="item.title" class="cover" />
              <div class="meta">
                <div class="title">{{ item.title }}</div>
                <div class="sub">{{ item.shopName }}</div>
                <div class="price-row">
                  <span class="price">￥{{ item.minPrice }}</span>
                  <span class="time">{{ item.time }}</span>
                </div>
                <div class="action-row">
                  <el-button link type="primary" @click.stop="goProductDetail(item.spuId)">查看详情</el-button>
                </div>
              </div>
            </button>
          </div>
          <el-empty v-else description="当前来源暂无商品，可先去收藏或浏览商品" />

          <div class="rag-input-panel">
            <el-input
              v-model="ragForm.question"
              type="textarea"
              :rows="4"
              resize="none"
              :placeholder="t('intelligence.ragQuestionPlaceholder')"
            />
            <el-button type="primary" size="large" :loading="loading" @click="submitRagQuestion">
              {{ t('intelligence.askQuestion') }}
            </el-button>
          </div>
          <div v-if="ragAnswer" class="rag-answer">
            <div class="rag-answer-title">AI 回答</div>
            <div class="rag-answer-content markdown-content" v-html="ragAnswerHtml"></div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="t('intelligence.tabAgent')" name="agent">
          <div class="toolbar wrap">
            <el-select v-model="agentTaskType" placeholder="任务类型">
              <el-option label="定期补货" value="NECESSITY" />
              <el-option label="意向类智能推荐" value="INTENTION" />
            </el-select>

            <template v-if="agentTaskType === 'NECESSITY'">
              <el-select v-model="necessityForm.frequency" placeholder="频次">
                <el-option label="每周一次" value="每周一次" />
                <el-option label="每半月一次" value="每半月一次" />
                <el-option label="每月一次" value="每月一次" />
                <el-option label="每季度一次" value="每季度一次" />
              </el-select>
              <el-input-number v-model="necessityForm.quantity" :min="1" controls-position="right" />
            </template>

            <template v-else>
              <div class="intention-panel">
                <div class="intention-main">
                  <div class="intention-card">
                    <div class="intention-label">想买什么</div>
                    <el-input v-model="intentionForm.productName" placeholder="商品名称，如：蓝牙耳机" clearable />
                  </div>
                  <div class="intention-card">
                    <div class="intention-label">需求偏好</div>
                    <el-input
                      v-model="intentionForm.requirementPreference"
                      type="textarea"
                      :rows="3"
                      resize="none"
                      placeholder="需求、偏好，如：降噪、通勤、长续航"
                    />
                  </div>
                </div>
                <div class="intention-card budget">
                  <div class="intention-label">预算上限</div>
                  <el-input-number v-model="intentionForm.budgetLimit" :min="1" controls-position="right" placeholder="预算上限" />
                  <span class="budget-tip">可选</span>
                </div>
              </div>
            </template>

            <el-button type="primary" :loading="loading" @click="runAgentTask">
              {{ isNecessityMode ? '加入定期计划' : t('intelligence.runTask') }}
            </el-button>
            <el-button v-if="!isNecessityMode" :disabled="!agentTask" @click="addSelectedToCart">{{ t('intelligence.addToCart') }}</el-button>
          </div>

          <div v-if="agentTaskType === 'NECESSITY'" class="agent-bind-panel">
            <div class="panel-head">选择绑定商品</div>
            <div v-if="bindProductOptions.length" class="product-browser">
              <button
                v-for="item in bindProductOptions"
                :key="item.spuId"
                class="product-card"
                :class="{ selected: necessityForm.bindSpuId === item.spuId }"
                @click="selectNecessityBindProduct(item.spuId)"
              >
                <img :src="resolveImageUrl(item.mainImage)" :alt="item.title" class="cover" />
                <div class="meta">
                  <div class="title">{{ item.title }}</div>
                  <div class="sub">{{ item.shopName }}</div>
                  <div class="price-row">
                    <span class="price">￥{{ item.minPrice }}</span>
                    <span class="time">来源：{{ item.from }}</span>
                  </div>
                </div>
              </button>
            </div>
            <el-empty v-else description="暂无可绑定商品" />
          </div>

          <el-table v-if="agentTask?.recommendations?.length" :data="agentTask.recommendations" stripe>
            <el-table-column width="56">
              <template #default="scope">
                <el-checkbox v-model="selectedSkuIds" :value="scope.row.skuId" />
              </template>
            </el-table-column>
            <el-table-column :label="t('intelligence.product')" min-width="280">
              <template #default="scope">
                <div class="agent-product-cell">
                  <img :src="resolveImageUrl(scope.row.mainImage)" :alt="scope.row.title" class="agent-thumb" />
                  <div class="agent-product-meta">
                    <el-button link type="primary" class="agent-link" @click="goProductDetail(scope.row.spuId)">
                      {{ scope.row.title }}
                    </el-button>
                    <span class="agent-sku">SKU: {{ scope.row.skuId }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" :label="t('intelligence.price')" width="120" />
            <el-table-column prop="reason" :label="t('intelligence.reason')" min-width="220" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="t('intelligence.tabCopywriting')" name="copywriting">
          <div class="source-switch">
            <el-radio-group v-model="aiSourceType">
              <el-radio-button label="favorite">我的收藏</el-radio-button>
              <el-radio-button label="history">浏览历史</el-radio-button>
            </el-radio-group>
          </div>

          <div v-if="aiProductOptions.length" class="product-browser">
            <button
              v-for="item in aiProductOptions"
              :key="item.uiKey"
              class="product-card"
              :class="{ selected: aiSelectedKey === item.uiKey }"
              @click="selectAiProduct(item)"
            >
              <img :src="resolveImageUrl(item.mainImage)" :alt="item.title" class="cover" />
              <div class="meta">
                <div class="title">{{ item.title }}</div>
                <div class="sub">{{ item.shopName }}</div>
                <div class="price-row">
                  <span class="price">￥{{ item.minPrice }}</span>
                  <span class="time">{{ item.time }}</span>
                </div>
                <div class="action-row">
                  <el-button link type="primary" @click.stop="goProductDetail(item.spuId)">查看详情</el-button>
                </div>
              </div>
            </button>
          </div>
          <el-empty v-else description="当前来源暂无商品，可先去收藏或浏览商品" />

          <div class="copywriting-actions">
            <el-button type="primary" :loading="loading" @click="runAiCopywriting">{{ t('intelligence.generate') }}</el-button>
          </div>

          <div class="ai-evaluation">
            <div class="ai-panel">
              <div class="panel-title">{{ t('intelligence.sellingPointsLabel') }}</div>
              <div v-if="aiSellingPoints.length" class="bullet-list">
                <div v-for="(item, index) in aiSellingPoints" :key="index">{{ item }}</div>
              </div>
              <span v-else>—</span>
            </div>
            <div class="ai-panel">
              <div class="panel-title">{{ t('intelligence.reviewSummaryLabel') }}</div>
              <div v-if="aiReviewSummary?.summary" class="markdown-content" v-html="aiReviewSummaryHtml"></div>
              <span v-else>—</span>
            </div>
            <div class="ai-metrics">
              <div class="metric-card">
                <div class="metric-label">{{ t('intelligence.evaluationOverall') }}</div>
                <div class="metric-value">{{ aiEvaluation?.overallLevel || '—' }}</div>
              </div>
              <div class="metric-card">
                <div class="metric-label">{{ t('intelligence.qualityScore') }}</div>
                <div class="metric-value">{{ aiEvaluation?.qualityScore ?? '—' }}</div>
              </div>
              <div class="metric-card">
                <div class="metric-label">{{ t('intelligence.valueScore') }}</div>
                <div class="metric-value">{{ aiEvaluation?.valueScore ?? '—' }}</div>
              </div>
              <div class="metric-card">
                <div class="metric-label">{{ t('intelligence.scenarioFit') }}</div>
                <div class="metric-value">{{ aiEvaluation?.scenarioFit || '—' }}</div>
              </div>
              <div class="metric-card wide">
                <div class="metric-label">{{ t('intelligence.potentialRisks') }}</div>
                <div class="metric-value">{{ aiEvaluation?.potentialRisks?.join('；') || '—' }}</div>
              </div>
              <div class="metric-card wide">
                <div class="metric-label">{{ t('intelligence.evaluationSummary') }}</div>
                <div class="metric-value">{{ aiEvaluation?.summary || '—' }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <div class="extra-grid">
      <el-card shadow="never" class="panel-card" :header="t('intelligence.myFavorites')">
        <el-empty v-if="!favorites.length" :description="t('intelligence.emptyFavorites')" />
        <el-space v-else direction="vertical" fill>
          <div v-for="item in favorites" :key="item.spuId" class="line-item">
            <span>{{ item.title }}</span>
            <el-button link type="primary" @click="handleToggleFavorite(item.spuId)">{{ t('intelligence.toggleFavorite') }}</el-button>
          </div>
        </el-space>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="header-row">
            <span>{{ t('intelligence.browseHistory') }}</span>
            <el-button link type="danger" @click="handleClearHistory">{{ t('intelligence.clear') }}</el-button>
          </div>
        </template>
        <el-empty v-if="!histories.length" :description="t('intelligence.emptyHistory')" />
        <el-space v-else direction="vertical" fill>
          <div v-for="item in histories" :key="item.spuId" class="line-item">
            <span>{{ item.title }}</span>
            <small>{{ item.browseTime }}</small>
          </div>
        </el-space>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="header-row">
            <span>{{ t('intelligence.shoppingPlans') }}</span>
            <el-button link type="primary" @click="quickCreatePlan">{{ t('intelligence.quickCreate') }}</el-button>
          </div>
        </template>
        <el-empty v-if="!plans.length" :description="t('intelligence.emptyPlans')" />
        <el-space v-else direction="vertical" fill>
          <div v-for="plan in plans" :key="plan.planId" class="line-item">
            <span>{{ plan.planName }}</span>
            <el-button link @click="quickExecutePlan(plan.planId)">{{ t('common.execute') }}</el-button>
          </div>
        </el-space>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="scss">
.intelligence-page {
  display: grid;
  gap: 16px;
}

.panel-card {
  border-radius: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;

  .desc {
    color: var(--text-secondary);
    font-size: 12px;
  }
}

.toolbar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  margin-bottom: 16px;

  &.wrap {
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  }
}

.source-switch {
  margin-bottom: 14px;
}

.product-browser {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(240px, 260px);
  gap: 12px;
  overflow-x: auto;
  padding: 6px 2px 14px;
  margin-bottom: 14px;
}

.product-card {
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  padding: 10px;
  cursor: pointer;
  display: grid;
  gap: 10px;
  text-align: left;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #d7e4ff;
    box-shadow: 0 8px 18px rgba(31, 77, 183, 0.12);
  }

  &.selected {
    border-color: #3b82f6;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.14);
    background: linear-gradient(180deg, #f5f9ff 0%, #eef5ff 100%);
  }

  .cover {
    width: 100%;
    height: 140px;
    object-fit: cover;
    border-radius: 10px;
    background-color: #f0f2f5;
  }

  .meta {
    display: grid;
    gap: 6px;
  }

  .title {
    font-size: 14px;
    font-weight: 600;
    line-height: 1.35;
    color: var(--text-primary);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .sub,
  .time {
    font-size: 12px;
    color: var(--text-secondary);
  }

  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }

  .action-row {
    margin-top: 2px;
    display: flex;
    justify-content: flex-end;
  }

  .price {
    color: #dc2626;
    font-weight: 700;
  }
}

.rag-input-panel {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  margin-bottom: 16px;
  padding: 14px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: linear-gradient(120deg, #f8fbff 0%, #f4f7fc 100%);

  :deep(.el-textarea__inner) {
    min-height: 132px;
    border-radius: 12px;
    padding: 12px 14px;
    font-size: 14px;
    line-height: 1.6;
  }
}

.agent-bind-panel {
  margin-bottom: 16px;

  .panel-head {
    margin: 2px 0 10px;
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.copywriting-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.ai-evaluation {
  display: grid;
  gap: 14px;
}

.ai-panel {
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 14px;
  background: linear-gradient(120deg, #fffaf5 0%, #fff5f0 100%);
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.ai-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.metric-card {
  border-radius: 12px;
  border: 1px solid #eef2f7;
  padding: 12px;
  background: #ffffff;
  display: grid;
  gap: 6px;
}

.metric-card.wide {
  grid-column: span 2;
}

.metric-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.metric-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

@media (max-width: 900px) {
  .metric-card.wide {
    grid-column: span 1;
  }
}

.intention-panel {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: linear-gradient(120deg, #fff7ed 0%, #fffaf0 100%);
}

.intention-main {
  display: grid;
  gap: 10px;
}

.intention-card {
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #f1f5f9;
}

.intention-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.intention-card :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.intention-card :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.6;
}

.intention-card :deep(.el-input-number) {
  width: 100%;
}

.budget {
  align-content: start;
}

.budget-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 900px) {
  .intention-panel {
    grid-template-columns: 1fr;
  }
}

.agent-product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  background: #f3f4f6;
  flex: 0 0 auto;
}

.agent-product-meta {
  display: grid;
  gap: 2px;
}

.agent-link {
  justify-content: flex-start;
  padding: 0;
}

.agent-sku {
  font-size: 12px;
  color: var(--text-secondary);
}

.rag-answer {
  margin-bottom: 16px;
  border: 1px solid #cfe2ff;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
  border-radius: 14px;
  padding: 12px 14px;
}

.rag-answer-title {
  font-size: 13px;
  color: #1d4ed8;
  font-weight: 600;
  margin-bottom: 8px;
}

.rag-answer-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--text-primary);
}

.bullet-list {
  display: grid;
  gap: 8px;
}

.markdown-content {
  line-height: 1.7;

  :deep(h1),
  :deep(h2),
  :deep(h3) {
    margin: 6px 0;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(p) {
    margin: 0 0 6px;
  }

  :deep(ul) {
    margin: 4px 0 8px;
    padding-left: 18px;
  }

  :deep(li) {
    margin: 3px 0;
  }

  :deep(code) {
    background: #f2f4f8;
    padding: 1px 5px;
    border-radius: 4px;
    font-size: 12px;
  }
}

.extra-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.line-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-color);
}

@media (max-width: 1024px) {
  .toolbar,
  .toolbar.wrap {
    grid-template-columns: 1fr;
  }

  .rag-input-panel {
    grid-template-columns: 1fr;
  }

  .extra-grid {
    grid-template-columns: 1fr;
  }
}
</style>
