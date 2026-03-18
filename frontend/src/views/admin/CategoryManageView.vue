<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCategoryTree, createCategory } from '@/api/category'
import type { CategoryVO, CategoryCreateDTO } from '@/types/product'

const treeData = ref<CategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive<CategoryCreateDTO>({
  parentId: 0,
  categoryName: '',
  sortOrder: 0,
})

const rules: FormRules = {
  categoryName: [{ required: true, message: 'Category name is required', trigger: 'blur' }],
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
    ElMessage.success('Category created')
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
      <h2 class="page-title">Category Management</h2>
      <el-button type="primary" @click="openCreate(0)">
        <el-icon><Plus /></el-icon> Add Top Category
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
                Add Child
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!loading && !treeData.length" description="No categories yet" />
    </div>

    <el-dialog v-model="dialogVisible" title="Create Category" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Parent">
          <el-input :model-value="form.parentId === 0 ? 'Root' : `ID: ${form.parentId}`" disabled />
        </el-form-item>
        <el-form-item label="Name" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="Category name" />
        </el-form-item>
        <el-form-item label="Sort Order">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">Create</el-button>
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
