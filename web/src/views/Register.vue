<template>
  <div class="register-container">
    <div class="register-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <el-card class="register-card" shadow="always">
      <template #header>
        <div class="card-header">
          <div class="logo-section">
            <div class="logo-icon">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <h2>创建账号</h2>
          </div>
          <p class="subtitle">加入智能视频内容审核平台</p>
        </div>
      </template>

      <el-form
          ref="registerFormRef"
          :model="form"
          :rules="rules"
          label-width="0"
          class="register-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入账号"
              size="large"
              prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="register"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="register"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <span>已有账号？</span>
          <el-button type="primary" link @click="$router.push('/login')">
            立即登录
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const register = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await axios.post('http://localhost:8080/user/register', null, {
        params: {
          username: form.username,
          password: form.password
        }
      })

      if (res.data.code === 200) {
        ElMessage.success('注册成功')
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.error(res.data.msg || '注册失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  position: relative;
  overflow: hidden;
}

.register-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 10%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.register-card {
  width: 450px;
  z-index: 10;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
}

.card-header {
  text-align: center;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  color: white;
}

.logo-section h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.register-form {
  padding: 10px 0;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.register-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
}

.register-btn:hover {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.form-footer {
  text-align: center;
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

.form-footer span {
  margin-right: 8px;
}

@media (max-width: 768px) {
  .register-card {
    width: 90%;
    margin: 0 20px;
  }
}
</style>
