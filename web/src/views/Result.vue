<template>
  <div class="result-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon :size="32" class="title-icon"><DataAnalysis /></el-icon>
          审核结果
        </h1>
        <p class="page-subtitle">视频内容智能分析详细报告</p>
      </div>
    </div>

    <el-card class="result-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon :size="20"><DocumentChecked /></el-icon>
          <span>分析报告</span>
        </div>
      </template>

      <div v-if="!loading && result" class="result-content">
        <!-- 结论区域 -->
        <div class="conclusion-section">
          <el-alert
              :title="result.conclusion"
              :type="getAlertType()"
              :icon="getConclusionIcon()"
              show-icon
              :closable="false"
              class="conclusion-alert"
          />
        </div>

        <el-divider />

        <!-- 统计数据 -->
        <el-row :gutter="24" class="stats-row">
          <el-col :xs="24" :sm="8">
            <el-statistic title="总帧数" :value="result.totalFrameNum || 0">
              <template #prefix>
                <el-icon color="#409eff"><Film /></el-icon>
              </template>
            </el-statistic>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-statistic title="暴力帧数" :value="result.violenceFrameNum || 0">
              <template #prefix>
                <el-icon :color="getViolenceColor()">
                  <Warning />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-statistic
                title="违规占比"
                :value="(result.violenceRatio * 100).toFixed(2)"
                suffix="%"
            >
              <template #prefix>
                <el-icon :color="getRatioColor()">
                  <TrendCharts />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>

        <el-divider />

        <!-- 可视化进度条 -->
        <div class="progress-section">
          <h3 class="section-title">
            <el-icon><PieChart /></el-icon>
            违规比例可视化
          </h3>
          <el-progress
              :percentage="parseFloat((result.violenceRatio * 100).toFixed(2))"
              :color="getProgressColor()"
              :stroke-width="24"
              :format="formatProgress"
          />
        </div>

        <el-divider />

        <!-- 详细说明 -->
        <div class="detail-section">
          <h3 class="section-title">
            <el-icon><InfoFilled /></el-icon>
            详细说明
          </h3>
          <el-descriptions :column="1" border class="detail-descriptions">
            <el-descriptions-item label="分析状态">
              <el-tag :type="getStatusType()" effect="light">
                {{ getStatusText() }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="安全评级">
              <el-rate
                  v-model="safetyRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  :score-template="getSafetyText()"
              />
            </el-descriptions-item>
            <el-descriptions-item label="建议操作">
              <span :style="{ color: getSuggestionColor() }">
                {{ getSuggestion() }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
              type="primary"
              size="large"
              @click="goBackToList"
          >
            <el-icon><Back /></el-icon>
            返回列表
          </el-button>

          <el-button
              size="large"
              @click="uploadNew"
          >
            <el-icon><Plus /></el-icon>
            上传新视频
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis,
  DocumentChecked,
  Film,
  Warning,
  TrendCharts,
  PieChart,
  InfoFilled,
  Back,
  Plus
} from '@element-plus/icons-vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const videoId = route.params.videoId
const result = ref({})
const loading = ref(true)
const userId = ref('')

const safetyRating = computed(() => {
  if (!result.value.violenceRatio) return 5
  const ratio = result.value.violenceRatio
  if (ratio <= 0.05) return 5
  if (ratio <= 0.1) return 4
  if (ratio <= 0.2) return 3
  if (ratio <= 0.3) return 2
  return 1
})

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user'))
  userId.value = user?.id || ''
  loadResult()
})

const loadResult = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/video/result/${videoId}`)
    result.value = res.data.data || {}
  } catch (error) {
    ElMessage.error('加载结果失败')
  } finally {
    loading.value = false
  }
}

const getAlertType = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return 'success'
  if (ratio <= 0.1) return 'warning'
  return 'error'
}

const getConclusionIcon = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return 'CircleCheck'
  if (ratio <= 0.1) return 'Warning'
  return 'CircleClose'
}

const getViolenceColor = () => {
  const count = result.value.violenceFrameNum || 0
  if (count === 0) return '#67c23a'
  if (count <= 10) return '#e6a23c'
  return '#f56c6c'
}

const getRatioColor = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return '#67c23a'
  if (ratio <= 0.1) return '#e6a23c'
  return '#f56c6c'
}

const getProgressColor = () => {
  const percentage = (result.value.violenceRatio || 0) * 100
  if (percentage <= 5) return '#67c23a'
  if (percentage <= 10) return '#e6a23c'
  return '#f56c6c'
}

const formatProgress = (percentage) => {
  return `${percentage.toFixed(2)}%`
}

const getStatusType = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return 'success'
  if (ratio <= 0.1) return 'warning'
  return 'danger'
}

const getStatusText = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return '安全'
  if (ratio <= 0.1) return '低风险'
  if (ratio <= 0.2) return '中风险'
  return '高风险'
}

const getSafetyText = () => {
  const texts = {
    5: '非常安全',
    4: '较为安全',
    3: '一般安全',
    2: '存在风险',
    1: '高风险'
  }
  return texts[safetyRating.value] || '未知'
}

const getSuggestion = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return '视频内容安全，可以公开发布'
  if (ratio <= 0.1) return '视频存在少量可疑内容，建议人工复核'
  if (ratio <= 0.2) return '视频包含较多违规内容，不建议发布'
  return '视频包含大量违规内容，禁止发布'
}

const getSuggestionColor = () => {
  const ratio = result.value.violenceRatio || 0
  if (ratio <= 0.05) return '#67c23a'
  if (ratio <= 0.1) return '#e6a23c'
  if (ratio <= 0.2) return '#f56c6c'
  return '#f56c6c'
}

const goBackToList = () => {
  router.push(`/list/${userId.value}`)
}

const uploadNew = () => {
  router.push(`/upload?userId=${userId.value}`)
}
</script>

<style scoped>
.result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 100px;
}

.page-header {
  max-width: 900px;
  margin: 0 auto 30px;
}

.header-content {
  text-align: center;
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

.result-card {
  max-width: 900px;
  margin: 0 auto;
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

.result-content {
  padding: 10px 0;
}

.conclusion-section {
  margin-bottom: 20px;
}

.conclusion-alert :deep(.el-alert__title) {
  font-size: 18px;
  font-weight: 600;
}

.stats-row {
  margin: 30px 0;
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

.progress-section {
  margin: 30px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 16px;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.detail-section {
  margin: 30px 0;
}

.detail-descriptions {
  margin-top: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 30px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  min-width: 140px;
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

@media (max-width: 768px) {
  .result-page {
    padding: 20px 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .stats-row :deep(.el-statistic__content) {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
