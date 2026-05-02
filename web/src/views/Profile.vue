<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon :size="32" class="title-icon"><User /></el-icon>
          个人中心
        </h1>
        <p class="page-subtitle">查看和管理您的个人信息</p>
      </div>
    </div>

    <div class="main-content">
      <el-row :gutter="24" class="content-row">
        <el-col :xs="24" :lg="16">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><UserFilled /></el-icon>
                <span>基本信息</span>
                <el-button
                    type="primary"
                    link
                    size="small"
                    class="edit-btn"
                    @click="openEditDialog"
                >
                  <el-icon><Edit /></el-icon>
                  编辑资料
                </el-button>
              </div>
            </template>

            <div class="profile-info">
              <div class="avatar-section">
                <el-avatar :size="100" class="user-avatar">
                  <el-icon :size="50"><UserFilled /></el-icon>
                </el-avatar>
                <div class="user-details">
                  <h2 class="username">{{ userInfo.username || '未设置' }}</h2>
                  <p class="user-id">用户ID: {{ userId }}</p>
                  <el-tag type="success" effect="light" size="large">
                    <el-icon><CircleCheck /></el-icon>
                    已认证
                  </el-tag>
                </div>
              </div>

              <el-divider />

              <el-descriptions :column="1" border class="info-descriptions">
                <el-descriptions-item label="用户名">
                  <el-icon><User /></el-icon>
                  {{ userInfo.username || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="用户ID">
                  <el-icon><Postcard /></el-icon>
                  {{ userId }}
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(userInfo.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="账号状态">
                  <el-tag type="success" effect="light">正常</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>

          <el-card class="stats-card" shadow="hover" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><DataAnalysis /></el-icon>
                <span>使用统计</span>
              </div>
            </template>

            <el-row :gutter="16" class="stats-row">
              <el-col :span="8">
                <el-statistic title="上传视频" :value="stats.uploadCount">
                  <template #prefix>
                    <el-icon color="#409eff"><VideoCamera /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="审核通过" :value="stats.approvedCount">
                  <template #prefix>
                    <el-icon color="#67c23a"><CircleCheck /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="违规视频" :value="stats.violationCount">
                  <template #prefix>
                    <el-icon color="#f56c6c"><CircleClose /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <el-card class="actions-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><Setting /></el-icon>
                <span>操作选项</span>
              </div>
            </template>

            <div class="action-buttons">
              <el-button
                  type="primary"
                  size="large"
                  class="action-btn"
                  @click="goToUpload"
              >
                <el-icon><Upload /></el-icon>
                上传视频
              </el-button>

              <el-button
                  size="large"
                  class="action-btn"
                  @click="goToList"
              >
                <el-icon><Document /></el-icon>
                查看记录
              </el-button>

              <el-button
                  type="danger"
                  size="large"
                  class="action-btn"
                  @click="handleLogout"
              >
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-button>
            </div>

            <el-divider />

            <div class="help-section">
              <h3>
                <el-icon><InfoFilled /></el-icon>
                温馨提示
              </h3>
              <ul class="help-list">
                <li>妥善保管您的账号信息</li>
                <li>定期查看审核记录</li>
                <li>遵守平台使用规范</li>
                <li>如有疑问请联系管理员</li>
              </ul>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog
        v-model="editDialogVisible"
        title="编辑个人资料"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-width="100px"
          class="edit-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="editForm.username"
              placeholder="请输入用户名"
              clearable
          />
        </el-form-item>

        <el-form-item label="新密码" prop="password">
          <el-input
              v-model="editForm.password"
              type="password"
              placeholder="不修改请留空"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="editForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  CircleCheck,
  CircleClose,
  Postcard,
  Clock,
  DataAnalysis,
  VideoCamera,
  Setting,
  Upload,
  Document,
  SwitchButton,
  InfoFilled,
  Edit
} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const userId = ref('')
const userInfo = ref({})
const stats = ref({
  uploadCount: 0,
  approvedCount: 0,
  violationCount: 0
})

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const updating = ref(false)
const editForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (editForm.value.password && value !== editForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  userId.value = route.params.userId || ''
  loadUserInfo()
  loadStats()
})

const loadUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user'))
  if (user) {
    userInfo.value = {
      username: user.username,
      createTime: user.createTime || new Date().toLocaleString('zh-CN')
    }
  }
}

const loadStats = async () => {
  try {
    const res = await fetch(`http://localhost:8080/video/list/${userId.value}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      const videos = data.data
      stats.value.uploadCount = videos.length
      stats.value.approvedCount = videos.filter(v => v.status === 1).length
      stats.value.violationCount = videos.filter(v => v.status === 2).length
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openEditDialog = () => {
  editForm.value = {
    username: userInfo.value.username || '',
    password: '',
    confirmPassword: ''
  }
  editDialogVisible.value = true
}

const handleUpdateProfile = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return

    updating.value = true
    try {
      const updateData = {
        id: userId.value,
        username: editForm.value.username
      }

      if (editForm.value.password) {
        updateData.password = editForm.value.password
      }

      const res = await axios.put('http://localhost:8080/user', updateData)

      if (res.data.code === 200) {
        ElMessage.success('个人信息更新成功')
        editDialogVisible.value = false

        const user = JSON.parse(localStorage.getItem('user'))
        user.username = editForm.value.username
        localStorage.setItem('user', JSON.stringify(user))

        loadUserInfo()
      } else {
        ElMessage.error(res.data.msg || '更新失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      updating.value = false
    }
  })
}

const goToUpload = () => {
  router.push(`/upload?userId=${userId.value}`)
}

const goToList = () => {
  router.push(`/list/${userId.value}`)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 100px;
}

.page-header {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 30px;
  text-align: center;
}

.header-content {
  display: inline-block;
}

.page-title {
  margin: 0 0 8px 0;
  color: white;
  font-size: 32px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 12px;
}

.page-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
}

.main-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.content-row {
  justify-content: center;
}

.profile-card,
.stats-card,
.actions-card {
  border-radius: 16px;
  border: none;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.98);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.edit-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-info {
  padding: 10px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.username {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #1f2937;
  font-weight: 600;
}

.user-id {
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 14px;
}

.user-details :deep(.el-tag) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.info-descriptions {
  margin-top: 16px;
}

.info-descriptions :deep(.el-descriptions__label) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.stats-row {
  margin: 10px 0;
}

.stats-row :deep(.el-statistic__head) {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stats-row :deep(.el-statistic__content) {
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.action-btn {
  width: 100% !important;
  min-width: 100% !important;
  max-width: 100% !important;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  height: 48px !important;
  padding: 0 20px !important;
  white-space: nowrap;
  box-sizing: border-box;
  margin: 0 !important;
}

.action-btn :deep(.el-button__content) {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.help-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 16px;
  margin: 0 0 12px 0;
}

.help-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 1.8;
}

.help-list li {
  margin-bottom: 4px;
}

.edit-form {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 20px 10px 100px;
  }

  .page-title {
    font-size: 24px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .stats-row :deep(.el-statistic__content) {
    font-size: 24px;
  }
}
</style>
