<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)

interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  nickname: string
  phone: string
  email: string
}

const form = reactive<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error(t('register.passwordNotMatch')))
  } else {
    callback()
  }
}

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: t('register.validationUsernameRequired'), trigger: 'blur' },
    { min: 3, max: 64, message: t('register.validationUsernameLength'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('register.validationPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 64, message: t('register.validationPasswordLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('register.validationConfirmRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined,
      phone: form.phone || undefined,
      email: form.email || undefined,
    })
    ElMessage.success(t('register.success'))
    router.push('/login')
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card card-box">
      <h2 class="register-title">{{ t('register.title') }}</h2>
      <p class="register-subtitle">{{ t('register.subtitle') }}</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item :label="t('register.username')" prop="username">
          <el-input v-model="form.username" :placeholder="t('register.usernamePlaceholder')" />
        </el-form-item>

        <el-form-item :label="t('register.password')" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="t('register.passwordPlaceholder')" show-password />
        </el-form-item>

        <el-form-item :label="t('register.confirmPassword')" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" :placeholder="t('register.confirmPasswordPlaceholder')" show-password />
        </el-form-item>

        <el-form-item :label="t('register.nickname')">
          <el-input v-model="form.nickname" :placeholder="t('register.optional')" />
        </el-form-item>

        <el-form-item :label="t('register.phone')">
          <el-input v-model="form.phone" :placeholder="t('register.optional')" />
        </el-form-item>

        <el-form-item :label="t('register.email')">
          <el-input v-model="form.email" :placeholder="t('register.optional')" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            {{ t('common.register') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>{{ t('register.haveAccount') }}</span>
        <router-link to="/login">{{ t('common.login') }}</router-link>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - $header-height - 80px);
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 480px;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: 4px;
}

.register-subtitle {
  text-align: center;
  color: $text-secondary;
  margin-bottom: 32px;
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: $text-secondary;
  font-size: $font-size-sm;

  a {
    margin-left: 4px;
  }
}
</style>
