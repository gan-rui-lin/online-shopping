<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCategoryTree, createCategory } from '@/api/category'
import type { CategoryVO, CategoryCreateDTO } from '@/types/product'

const treeData = ref<CategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const saving = ref(false)
const { t } = useI18n()

const form = reactive<CategoryCreateDTO>({
  parentId: 0,
  categoryName: '',
  sortOrder: 0,
})

const rules: FormRules = {
  categoryName: [{ required: true, message: t('buyer.required'), trigger: 'blur' }],
}

const treeProps = {
  children: 'children',
  label: 'categoryName',
}

async function fetchTree() {
  loading.value = true
  try {
    treeData.value = await getCategoryTree()
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
}

function openCreate(parentId = 0) {
  form.parentId = parentId
  form.categoryName = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createCategory(form)
    ElMessage.success(t('admin.categoryCreated'))
    dialogVisible.value = false
    fetchTree()
  } catch { /* handled */ } finally {
    saving.value = false
  }
}

onMounted(fetchTree)
</script>

<template>
  <div class="category-manage-page">
    <div class="page-header mb-24">
      <h2 class="page-title">{{ t('admin.categoryManagement') }}</h2>
      <el-button type="primary" @click="openCreate(0)">
        <el-icon><Plus /></el-icon> {{ t('admin.addTopCategory') }}
      </el-button>
    </div>

    <div class="card-box">
      <el-tree
        v-loading="loading"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        default-expand-all
      >
        <template #default="{ data }">
          <div class="tree-node">
            <span>{{ data.categoryName }}</span>
            <span class="tree-actions">
              <el-button text type="primary" size="small" @click.stop="openCreate(data.id)">
                {{ t('admin.addChild') }}
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!loading && !treeData.length" :description="t('admin.noCategories')" />
    </div>

    <el-dialog v-model="dialogVisible" :title="t('admin.createCategory')" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('admin.parent')">
          <el-input :model-value="form.parentId === 0 ? t('admin.root') : `${t('admin.id')}: ${form.parentId}`" disabled />
        </el-form-item>
        <el-form-item :label="t('admin.name')" prop="categoryName">
          <el-input v-model="form.categoryName" :placeholder="t('admin.categoryNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('admin.sortOrder')">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('buyer.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('admin.createCategory') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 8px;
}

.tree-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .tree-actions {
  opacity: 1;
}
</style>
