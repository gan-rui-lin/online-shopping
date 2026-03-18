<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createReview } from '@/api/review'
import type { ReviewCreateDTO } from '@/types/review'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const orderItemId = Number(route.params.orderItemId)

const form = reactive<ReviewCreateDTO>({
  orderItemId,
  score: 5,
  content: '',
  imageUrls: [],
  anonymousFlag: 0,
})

const newImageUrl = ref('')

const rules: FormRules = {
  score: [{ required: true, message: 'Please rate', trigger: 'change' }],
}

function addImage() {
  if (!newImageUrl.value) return
  if (!form.imageUrls) form.imageUrls = []
  form.imageUrls.push(newImageUrl.value)
  newImageUrl.value = ''
}

function removeImage(idx: number) {
  form.imageUrls?.splice(idx, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await createReview({
      ...form,
      content: form.content || undefined,
      imageUrls: form.imageUrls?.length ? form.imageUrls : undefined,
    })
    ElMessage.success('Review submitted')
    router.back()
  } catch { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="review-create-page">
    <div class="page-header mb-24">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> Back
      </el-button>
      <h2 class="page-title">Write a Review</h2>
    </div>

    <div class="card-box">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px">
        <el-form-item label="Rating" prop="score">
          <el-rate v-model="form.score" show-text :texts="['Terrible', 'Bad', 'OK', 'Good', 'Excellent']" />
        </el-form-item>

        <el-form-item label="Review Content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="Share your experience..." />
        </el-form-item>

        <el-form-item label="Images">
          <div class="image-list">
            <div v-for="(url, idx) in form.imageUrls" :key="idx" class="image-item">
              <el-image :src="url" fit="cover" class="preview-img" />
              <el-button text type="danger" size="small" class="remove-btn" @click="removeImage(idx)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="add-image-row">
            <el-input v-model="newImageUrl" placeholder="Paste image URL" @keyup.enter="addImage" />
            <el-button @click="addImage">Add</el-button>
          </div>
        </el-form-item>

        <el-form-item label="Anonymous">
          <el-switch v-model="form.anonymousFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">Submit Review</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.image-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.preview-img {
  width: 80px;
  height: 80px;
  border-radius: $border-radius;
}

.remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
}

.add-image-row {
  display: flex;
  gap: 8px;
}
</style>
