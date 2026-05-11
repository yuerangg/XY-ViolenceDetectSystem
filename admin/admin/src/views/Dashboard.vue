<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1 class="banner-title">
            <el-icon :size="36" class="title-icon"><DataAnalysis /></el-icon>
            视频审核管理系统
          </h1>
          <p class="banner-subtitle">智能视频内容分析与管理平台</p>
        </div>
        <div class="banner-time">
          <el-icon><Clock /></el-icon>
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card user-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon :size="48"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
              <div class="stat-trend">
                <el-icon color="#67c23a"><Top /></el-icon>
                <span>活跃用户</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card video-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon :size="48"><VideoCamera /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.videoCount }}</div>
              <div class="stat-label">视频总数</div>
              <div class="stat-trend">
                <el-icon color="#409eff"><Film /></el-icon>
                <span>待审核</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card audit-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon :size="48"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.auditCount }}</div>
              <div class="stat-label">审核报告</div>
              <div class="stat-trend">
                <el-icon color="#e6a23c"><Tickets /></el-icon>
                <span>已完成</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card frame-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.frameCount }}</div>
              <div class="stat-label">帧记录数</div>
              <div class="stat-trend">
                <el-icon color="#f56c6c"><View /></el-icon>
                <span>已分析</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和系统信息 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :md="16">
        <el-card class="quick-actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20"><Operation /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>

          <div class="action-grid">
            <div class="action-item" @click="$router.push('/users')">
              <div class="action-icon user-bg">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="action-text">用户管理</div>
            </div>

            <div class="action-item" @click="$router.push('/videos')">
              <div class="action-icon video-bg">
                <el-icon :size="32"><VideoCamera /></el-icon>
              </div>
              <div class="action-text">视频管理</div>
            </div>

            <div class="action-item" @click="$router.push('/audits')">
              <div class="action-icon audit-bg">
                <el-icon :size="32"><DocumentChecked /></el-icon>
              </div>
              <div class="action-text">审核报告</div>
            </div>

            <div class="action-item" @click="$router.push('/frames')">
              <div class="action-icon frame-bg">
                <el-icon :size="32"><Picture /></el-icon>
              </div>
              <div class="action-text">帧详情</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card class="system-info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20"><Monitor /></el-icon>
              <span>系统信息</span>
            </div>
          </template>

          <div class="info-list">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">技术栈</span>
              <span class="info-value">Vue3 + Element Plus</span>
            </div>
            <div class="info-item">
              <span class="info-label">后端框架</span>
              <span class="info-value">Spring Boot</span>
            </div>
            <div class="info-item">
              <span class="info-label">AI引擎</span>
              <span class="info-value">YOLOv8</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  User,
  VideoCamera,
  DocumentChecked,
  Picture,
  DataAnalysis,
  Clock,
  Top,
  Film,
  Tickets,
  View,
  Operation,
  Monitor
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const stats = ref({
  userCount: 0,
  videoCount: 0,
  auditCount: 0,
  frameCount: 0
})

const currentTime = ref('')

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

let timer = null

const loadStats = async () => {
  try {
    const [users, videos, audits, frames] = await Promise.all([
      request.get('/user'),
      request.get('/info'),
      request.get('/auditreport'),
      request.get('/framedetail')
    ])

    stats.value = {
      userCount: users.data?.length || 0,
      videoCount: videos.data?.length || 0,
      auditCount: audits.data?.length || 0,
      frameCount: frames.data?.length || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 30px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.banner-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-title {
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
  backdrop-filter: blur(10px);
}

.banner-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
}

.banner-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.15);
  padding: 10px 20px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

/* 统计卡片行 */
.stats-row {
  padding: 0 24px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}

.stat-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.user-card .stat-icon-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.video-card .stat-icon-wrapper {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.audit-card .stat-icon-wrapper {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
}

.frame-card .stat-icon-wrapper {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.4);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

/* 内容行 */
.content-row {
  padding: 0 24px 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

/* 快捷操作卡片 */
.quick-actions-card {
  border-radius: 12px;
  border: none;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f5f7fa;
}

.action-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  background: white;
}

.action-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.video-bg {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.audit-bg {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.frame-bg {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  text-align: center;
}

/* 系统信息卡片 */
.system-info-card {
  border-radius: 12px;
  border: none;
}

.info-list {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .banner-title {
    font-size: 24px;
  }

  .stats-row,
  .content-row {
    padding: 0 12px;
  }

  .stat-value {
    font-size: 28px;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
