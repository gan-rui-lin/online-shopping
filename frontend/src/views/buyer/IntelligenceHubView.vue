<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { askRag, getRagHistory } from '@/api/rag'
import { createAgentTask, getAgentTask, addAgentResultToCart } from '@/api/agent'
import { generateDescription, generateSellingPoints, generateTitle, getReviewSummary } from '@/api/ai'
import { getFavoriteList, toggleFavorite } from '@/api/favorite'
import { clearBrowseHistory, getBrowseHistory } from '@/api/behavior'
import { createShoppingPlan, executeShoppingPlan, getShoppingPlanList } from '@/api/plan'
import type {
  AgentTaskVO,
  ChatMessageVO,
  FavoriteVO,
  BrowseHistoryVO,
  ShoppingPlanVO,
  ReviewSummaryVO,
} from '@/types/intelligence'

const activeTab = ref('rag')
const { t } = useI18n()

const ragForm = ref({ spuId: 1000, question: '' })
const ragAnswer = ref('')
const ragSessionId = ref<number>()
const ragHistory = ref<ChatMessageVO[]>([])

const agentPrompt = ref('')
const agentTask = ref<AgentTaskVO>()
const selectedSkuIds = ref<number[]>([])

const aiSpuId = ref(1000)
const aiDescriptionForm = ref({ keywords: '轻薄 笔记本 学生', targetAudience: '大学生', style: '简洁' })
const aiTitle = ref('')
const aiDescription = ref('')
const aiSellingPoints = ref('')
const aiReviewSummary = ref<ReviewSummaryVO>()

const favorites = ref<FavoriteVO[]>([])
const histories = ref<BrowseHistoryVO[]>([])
const plans = ref<ShoppingPlanVO[]>([])

const loading = ref(false)

async function loadExtraPanels() {
  const [favRes, histRes, planRes] = await Promise.all([
    getFavoriteList(1, 6).catch(() => ({ list: [] } as any)),
    getBrowseHistory(1, 6).catch(() => ({ list: [] } as any)),
    getShoppingPlanList().catch(() => []),
  ])

  favorites.value = favRes.list || []
  histories.value = histRes.list || []
  plans.value = planRes || []
}

async function submitRagQuestion() {
  if (!ragForm.value.question.trim()) {
    ElMessage.warning(t('intelligence.questionRequired'))
    return
  }

  loading.value = true
  try {
    const result = await askRag({
      spuId: ragForm.value.spuId,
      question: ragForm.value.question,
      sessionId: ragSessionId.value,
    })
    ragAnswer.value = result.answer
    ragSessionId.value = result.sessionId
    ragHistory.value = await getRagHistory(result.sessionId)
  } finally {
    loading.value = false
  }
}

async function runAgentTask() {
  loading.value = true
  try {
    const task = await createAgentTask(agentPrompt.value)
    agentTask.value = await getAgentTask(task.taskId)
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
  loading.value = true
  try {
    const [title, desc, selling, summary] = await Promise.all([
      generateTitle(aiSpuId.value).catch(() => ({ content: t('intelligence.needMerchant'), variants: [] })),
      generateDescription(aiDescriptionForm.value).catch(() => ({ content: t('intelligence.needMerchant'), variants: [] })),
      generateSellingPoints(aiSpuId.value).catch(() => ({ content: t('intelligence.needMerchant'), variants: [] })),
      getReviewSummary(aiSpuId.value).catch(() => undefined),
    ])
    aiTitle.value = title.content
    aiDescription.value = desc.content
    aiSellingPoints.value = selling.content
    aiReviewSummary.value = summary
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

async function quickExecutePlan(planId: number) {
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
          <div class="toolbar">
            <el-input-number v-model="ragForm.spuId" :min="1" controls-position="right" />
            <el-input v-model="ragForm.question" :placeholder="t('intelligence.ragQuestionPlaceholder')" />
            <el-button type="primary" :loading="loading" @click="submitRagQuestion">{{ t('intelligence.askQuestion') }}</el-button>
          </div>
          <el-alert v-if="ragAnswer" type="success" :closable="false" :title="ragAnswer" show-icon />
          <el-timeline v-if="ragHistory.length" class="mt-16">
            <el-timeline-item v-for="(msg, idx) in ragHistory" :key="idx" :timestamp="msg.createTime">
              <strong>{{ msg.role }}：</strong>{{ msg.content }}
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <el-tab-pane :label="t('intelligence.tabAgent')" name="agent">
          <div class="toolbar">
            <el-input v-model="agentPrompt" :placeholder="t('intelligence.agentPromptPlaceholder')" />
            <el-button type="primary" :loading="loading" @click="runAgentTask">{{ t('intelligence.runTask') }}</el-button>
            <el-button :disabled="!agentTask" @click="addSelectedToCart">{{ t('intelligence.addToCart') }}</el-button>
          </div>
          <el-table v-if="agentTask?.recommendations?.length" :data="agentTask.recommendations" stripe>
            <el-table-column width="56">
              <template #default="scope">
                <el-checkbox v-model="selectedSkuIds" :label="scope.row.skuId" />
              </template>
            </el-table-column>
            <el-table-column prop="title" :label="t('intelligence.product')" min-width="180" />
            <el-table-column prop="price" :label="t('intelligence.price')" width="120" />
            <el-table-column prop="reason" :label="t('intelligence.reason')" min-width="220" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="t('intelligence.tabCopywriting')" name="copywriting">
          <div class="toolbar wrap">
            <el-input-number v-model="aiSpuId" :min="1" controls-position="right" />
            <el-input v-model="aiDescriptionForm.keywords" :placeholder="t('intelligence.keywords')" />
            <el-input v-model="aiDescriptionForm.targetAudience" :placeholder="t('intelligence.targetAudience')" />
            <el-input v-model="aiDescriptionForm.style" :placeholder="t('intelligence.style')" />
            <el-button type="primary" :loading="loading" @click="runAiCopywriting">{{ t('intelligence.generate') }}</el-button>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="t('intelligence.titleLabel')">{{ aiTitle || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('intelligence.descriptionLabel')">{{ aiDescription || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('intelligence.sellingPointsLabel')">{{ aiSellingPoints || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('intelligence.reviewSummaryLabel')">{{ aiReviewSummary?.summary || '—' }}</el-descriptions-item>
          </el-descriptions>
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
  grid-template-columns: 120px 1fr auto auto;
  gap: 12px;
  margin-bottom: 16px;

  &.wrap {
    grid-template-columns: 120px repeat(3, 1fr) auto;
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

  .extra-grid {
    grid-template-columns: 1fr;
  }
}
</style>
