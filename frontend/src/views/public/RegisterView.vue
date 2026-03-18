<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
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
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 64, message: '3 to 64 characters', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, max: 64, message: '6 to 64 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
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
    ElMessage.success('Registration successful. Please login.')
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
      <h2 class="register-title">Create Account</h2>
      <p class="register-subtitle">Fill in the information below to get started</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="3 to 64 characters" />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6 to 64 characters" show-password />
        </el-form-item>

        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="Re-enter password" show-password />
        </el-form-item>

        <el-form-item label="Nickname">
          <el-input v-model="form.nickname" placeholder="Optional" />
        </el-form-item>

        <el-form-item label="Phone">
          <el-input v-model="form.phone" placeholder="Optional" />
        </el-form-item>

        <el-form-item label="Email">
          <el-input v-model="form.email" placeholder="Optional" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            Register
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>Already have an account?</span>
        <router-link to="/login">Login</router-link>
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
