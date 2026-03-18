<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { LoginDTO } from '@/types/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginDTO>({
  username: '',
  password: '',
})

const rules: FormRules<LoginDTO> = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('Login successful')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card card-box">
      <h2 class="login-title">Welcome Back</h2>
      <p class="login-subtitle">Sign in to your account</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="Username" prop="username">
          <el-input
            v-model="form.username"
            placeholder="Enter your username"
            :prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Enter your password"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            Login
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>Don't have an account?</span>
        <router-link to="/register">Register now</router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { User, Lock } from '@element-plus/icons-vue'
export default { components: { User, Lock } }
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - $header-height - 80px);
  padding: 40px 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: 4px;
}

.login-subtitle {
  text-align: center;
  color: $text-secondary;
  margin-bottom: 32px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  color: $text-secondary;
  font-size: $font-size-sm;

  a {
    margin-left: 4px;
  }
}
</style>
