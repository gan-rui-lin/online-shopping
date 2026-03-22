<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createProduct, updateProduct, getProductDetail } from '@/api/product'
import { getCategoryTree } from '@/api/category'
import type { ProductSpuCreateDTO, ProductSkuDTO, CategoryVO } from '@/types/product'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const categories = ref<CategoryVO[]>([])
const { t } = useI18n()

const isEdit = computed(() => !!route.params.id)
const editSpuId = computed(() => Number(route.params.id))

const form = reactive<ProductSpuCreateDTO>({
  categoryId: 0,
  brandName: '',
  title: '',
  subTitle: '',
  mainImage: '',
  detailText: '',
  imageList: [],
  skuList: [],
})

const newImageUrl = ref('')

const rules: FormRules = {
  title: [{ required: true, message: t('buyer.required'), trigger: 'blur' }],
  categoryId: [{ required: true, message: t('buyer.required'), trigger: 'change' }],
}

function addSku() {
  form.skuList = form.skuList || []
  form.skuList.push({
    skuCode: '',
    skuName: '',
    specJson: '',
    price: 0,
    originPrice: 0,
    stock: 0,
    imageUrl: '',
  })
}

function removeSku(idx: number) {
  form.skuList?.splice(idx, 1)
}

function addImage() {
  if (!newImageUrl.value) return
  form.imageList = form.imageList || []
  form.imageList.push(newImageUrl.value)
  newImageUrl.value = ''
}

function removeImage(idx: number) {
  form.imageList?.splice(idx, 1)
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(editSpuId.value, {
        categoryId: form.categoryId,
        brandName: form.brandName,
        title: form.title,
        subTitle: form.subTitle,
        mainImage: form.mainImage,
        detailText: form.detailText,
        imageList: form.imageList,
        skuList: form.skuList,
      })
      ElMessage.success(t('merchant.productUpdated'))
    } else {
      await createProduct(form)
      ElMessage.success(t('merchant.productCreated'))
    }
    router.push('/merchant/products')
  } catch { /* handled */ } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await getCategoryTree()
  } catch {
    categories.value = []
  }

  if (isEdit.value) {
    loading.value = true
    try {
      const detail = await getProductDetail(editSpuId.value)
      form.categoryId = 0
      form.brandName = detail.brandName || ''
      form.title = detail.title
      form.subTitle = detail.subTitle || ''
      form.mainImage = detail.mainImage || ''
      form.detailText = detail.detailText || ''
      form.imageList = detail.imageList || []
      form.skuList = detail.skuList?.map((s) => ({
        skuId: s.skuId,
        skuCode: s.skuCode,
        skuName: s.skuName,
        specJson: s.specJson || '',
        price: s.salePrice,
        originPrice: s.originPrice,
        stock: s.stock,
        imageUrl: s.imageUrl || '',
      })) || []
    } catch {
      ElMessage.error(t('common.requestFailed'))
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div v-loading="loading" class="product-create-page">
    <div class="page-header mb-24">
      <el-button text @click="router.push('/merchant/products')">
        <el-icon><ArrowLeft /></el-icon> {{ t('buyer.back') }}
      </el-button>
      <h2 class="page-title">{{ isEdit ? t('merchant.edit') : t('merchant.createProduct') }}</h2>
    </div>

    <div class="card-box">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" style="max-width: 800px">
        <el-form-item :label="t('merchant.productTitle')" prop="title">
          <el-input v-model="form.title" :placeholder="t('merchant.productTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('merchant.productSubtitle')">
          <el-input v-model="form.subTitle" :placeholder="t('merchant.productSubtitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('merchant.category')" prop="categoryId">
          <el-select v-model="form.categoryId" :placeholder="t('merchant.categoryPlaceholder')">
            <template v-for="cat in categories" :key="cat.id">
              <el-option :label="cat.categoryName" :value="cat.id" />
              <el-option
                v-for="child in cat.children"
                :key="child.id"
                :label="`  ${child.categoryName}`"
                :value="child.id"
              />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('merchant.brand')">
          <el-input v-model="form.brandName" :placeholder="t('merchant.brandPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('merchant.mainImageUrl')">
          <el-input v-model="form.mainImage" :placeholder="t('merchant.mainImagePlaceholder')" />
        </el-form-item>

        <el-form-item :label="t('merchant.imageGallery')">
          <div class="image-list mb-16">
            <div v-for="(url, idx) in form.imageList" :key="idx" class="image-thumb">
              <el-image :src="url" fit="cover" style="width: 80px; height: 80px; border-radius: 4px" />
              <el-button text type="danger" size="small" @click="removeImage(idx)"><el-icon><Close /></el-icon></el-button>
            </div>
          </div>
          <div class="add-row">
            <el-input v-model="newImageUrl" :placeholder="t('merchant.pasteImageUrl')" @keyup.enter="addImage" />
            <el-button @click="addImage">{{ t('merchant.add') }}</el-button>
          </div>
        </el-form-item>

        <el-form-item :label="t('merchant.detailContent')">
          <el-input v-model="form.detailText" type="textarea" :rows="6" :placeholder="t('merchant.detailContentPlaceholder')" />
        </el-form-item>

        <el-divider>{{ t('merchant.skuSpecifications') }}</el-divider>

        <div v-for="(sku, idx) in form.skuList" :key="idx" class="sku-card card-box mb-16">
          <div class="sku-header">
            <span>{{ t('merchant.skuIndex', { index: idx + 1 }) }}</span>
            <el-button text type="danger" size="small" @click="removeSku(idx)">{{ t('merchant.remove') }}</el-button>
          </div>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item :label="t('merchant.skuName')">
                <el-input v-model="sku.skuName" :placeholder="t('merchant.skuNamePlaceholder')" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="t('merchant.skuCode')">
                <el-input v-model="sku.skuCode" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="t('merchant.specJson')">
                <el-input v-model="sku.specJson" :placeholder="t('merchant.specJsonPlaceholder')" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item :label="t('intelligence.price')">
                <el-input-number v-model="sku.price" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="t('merchant.originPrice')">
                <el-input-number v-model="sku.originPrice" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="t('productDetail.stock')">
                <el-input-number v-model="sku.stock" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="t('merchant.imageUrl')">
            <el-input v-model="sku.imageUrl" :placeholder="t('merchant.skuImagePlaceholder')" />
          </el-form-item>
        </div>

        <el-button @click="addSku" class="mb-24">
          <el-icon><Plus /></el-icon> {{ t('merchant.addSku') }}
        </el-button>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ isEdit ? t('merchant.productUpdated') : t('merchant.createProduct') }}
          </el-button>
          <el-button @click="router.push('/merchant/products')">{{ t('buyer.cancel') }}</el-button>
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
}

.image-thumb {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.add-row {
  display: flex;
  gap: 8px;
  max-width: 400px;
}

.sku-card {
  padding: 16px;
  background: $bg-color;
}

.sku-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
}
</style>
