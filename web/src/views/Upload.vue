<template>
  <div class="upload-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon :size="32" class="title-icon"><VideoCamera /></el-icon>
          视频上传
        </h1>
        <p class="page-subtitle">上传视频进行智能暴力内容检测</p>
      </div>
    </div>

    <div class="main-content">
      <el-row :gutter="24" class="content-row">
        <el-col :xs="24" :lg="16">
          <el-card class="upload-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><Upload /></el-icon>
                <span>上传视频文件</span>
              </div>
            </template>

            <div class="upload-area">
              <el-upload
                  class="video-uploader"
                  action="http://localhost:8080/video/upload"
                  :data="{ userId }"
                  :show-file-list="true"
                  :on-success="handleSuccess"
                  :before-upload="beforeUpload"
                  :on-error="handleError"
                  drag
                  accept=".mp4,.avi,.mov,.mkv,.wmv,.flv"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  将视频拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 MP4、AVI、MOV、MKV、WMV、FLV 格式
                  </div>
                </template>
              </el-upload>
            </div>

            <el-divider />

            <div class="upload-tips">
              <h3>
                <el-icon><InfoFilled /></el-icon>
                温馨提示
              </h3>
              <ul>
                <li>系统将自动分析视频内容，检测暴力场景</li>
                <li>分析过程可能需要几分钟时间，请耐心等待</li>
                <li>分析完成后可查看详细的结果报告</li>
                <li>支持多种常见视频格式</li>
              </ul>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <el-card class="stats-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><DataAnalysis /></el-icon>
                <span>快捷操作</span>
              </div>
            </template>

            <div class="quick-actions">
              <div class="quick-actions">
                <el-button
                    type="primary"
                    size="large"
                    class="action-btn"
                    @click="toList"
                >
                  <el-icon><List /></el-icon>
                  查看审核记录
                </el-button>
              </div>
            </div>

            <el-divider />

            <div class="help-section">
              <h3>
                <el-icon><QuestionFilled /></el-icon>
                需要帮助？
              </h3>
              <p class="help-text">
                如有任何问题，请查看审核记录了解详细分析结果。
              </p>
            </div>
          </el-card>

          <el-card class="guide-card" shadow="hover" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <el-icon :size="20"><Guide /></el-icon>
                <span>使用指南</span>
              </div>
            </template>

            <el-steps direction="vertical" :active="0" simple>
              <el-step title="上传视频" description="选择需要检测的视频文件" />
              <el-step title="等待分析" description="系统自动进行内容审核" />
              <el-step title="查看结果" description="获取详细的分析报告" />
            </el-steps>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoCamera,
  Upload,
  UploadFilled,
  InfoFilled,
  DataAnalysis,
  List,
  HomeFilled,
  QuestionFilled,
  Guide
} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userId = ref('')

onMounted(() => {
  userId.value = route.query.userId
})

const beforeUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt500M = file.size / 1024 / 1024 < 500

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt500M) {
    ElMessage.error('视频大小不能超过 500MB!')
    return false
  }
  return true
}

const handleSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success('上传成功，后台审核中...')
    setTimeout(() => {
      router.push(`/result/${res.data}`)
    }, 1500)
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败，请检查网络连接后重试')
}

const toList = () => {
  router.push(`/list/${userId.value}`)
}

const goToHome = () => {
  router.push(`/upload?userId=${userId.value}`)
}
</script>

<style scoped>
.upload-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
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

.upload-card,
.stats-card,
.guide-card {
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

.upload-area {
  padding: 20px 0;
}

.video-uploader {
  width: 100%;
}

.video-uploader :deep(.el-upload-dragger) {
  padding: 60px 40px;
  border-radius: 12px;
  border: 2px dashed #dcdfe6;
  transition: all 0.3s;
}

.video-uploader :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.el-icon--upload {
  font-size: 64px;
  color: #667eea;
  margin-bottom: 16px;
}

.el-upload__text {
  color: #606266;
  font-size: 14px;
}

.el-upload__text em {
  color: #667eea;
  font-style: normal;
  font-weight: 500;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.upload-tips {
  padding: 10px 0;
}

.upload-tips h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 16px;
  margin: 0 0 12px 0;
}

.upload-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 1.8;
}

.upload-tips li {
  margin-bottom: 4px;
}
.quick-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.action-btn {
  width: 100%;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  height: 48px;
  padding: 0 20px;
  white-space: nowrap;
}

.help-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 16px;
  margin: 0 0 8px 0;
}

.help-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.guide-card :deep(.el-step__title) {
  font-size: 14px;
  font-weight: 500;
}

.guide-card :deep(.el-step__description) {
  font-size: 12px;
}

@media (max-width: 768px) {
  .upload-page {
    padding: 20px 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-header {
    margin-bottom: 20px;
  }
}
</style>
