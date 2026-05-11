<template>
  <div class="monitor-container">
    <div class="monitor-header">
      <h2>📹 实时暴力监测</h2>
      <p class="subtitle">启用摄像头进行实时暴力行为检测</p>
    </div>

    <div class="monitor-content">
      <div class="video-section">
        <div class="video-wrapper">
          <video
              ref="videoRef"
              autoplay
              playsinline
              class="camera-video"
              :class="{ 'video-active': isRunning }"
          ></video>

          <div v-if="!isRunning && !streamStarted" class="video-placeholder">
            <el-icon :size="64"><VideoCamera /></el-icon>
            <p>点击"启动监测"开启摄像头</p>
          </div>

          <div v-if="isRunning" class="detection-overlay">
            <div class="detection-badge" :class="{ warning: currentDetection.isViolence }">
              <el-icon :size="24">
                <Warning v-if="currentDetection.isViolence" />
                <CircleCheck v-else />
              </el-icon>
              <span class="badge-text">
                {{ currentDetection.isViolence ? '检测到暴力！' : '正常画面' }}
              </span>
            </div>

            <div class="confidence-bar">
              <div
                  class="confidence-fill"
                  :style="{
                  width: `${currentDetection.confidence * 100}%`,
                  background: currentDetection.isViolence ? '#f56c6c' : '#67c23a'
                }"
              ></div>
              <span class="confidence-text">
                置信度: {{ (currentDetection.confidence * 100).toFixed(1) }}%
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="control-section">
        <div class="status-card">
          <div class="status-indicator" :class="{ active: isRunning }">
            <el-icon :size="48" v-if="isRunning">
              <VideoCamera />
            </el-icon>
            <el-icon :size="48" v-else>
              <SwitchButton />
            </el-icon>
          </div>
          <div class="status-text">
            <h3>{{ isRunning ? '监测进行中' : '监测未启动' }}</h3>
            <p>{{ statusMessage }}</p>
          </div>
        </div>

        <div class="control-buttons">
          <el-button
              type="primary"
              size="large"
              :disabled="isRunning"
              @click="startMonitoring"
              class="control-btn start-btn"
          >
            <el-icon><VideoPlay /></el-icon>
            启动监测
          </el-button>

          <el-button
              type="danger"
              size="large"
              :disabled="!isRunning"
              @click="stopMonitoring"
              class="control-btn stop-btn"
          >
            <el-icon><VideoPause /></el-icon>
            停止监测
          </el-button>
        </div>
      </div>

      <div class="detection-log" v-if="logs.length > 0">
        <h4>
          <el-icon><Document /></el-icon>
          检测日志
        </h4>
        <div class="log-list">
          <div
              v-for="(log, index) in recentLogs"
              :key="index"
              class="log-item"
              :class="{ warning: log.isWarning }"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>

      <div class="tips-card">
        <h4>💡 使用说明</h4>
        <ul>
          <li>点击"启动监测"按钮开启摄像头实时检测</li>
          <li>系统会自动识别视频中的暴力行为</li>
          <li>检测到暴力行为时会发出警告提示</li>
          <li>请确保浏览器有摄像头访问权限</li>
          <li>建议在光线充足的环境下使用</li>
        </ul>
      </div>
    </div>
  </div>
</template>
<script setup>import {ref, computed, onMounted, onUnmounted} from 'vue'
import {ElMessage} from 'element-plus'
import {
  VideoCamera,
  SwitchButton,
  VideoPlay,
  VideoPause,
  Warning,
  CircleCheck,
  Document
} from '@element-plus/icons-vue'
import axios from 'axios'

const videoRef = ref(null)
const isRunning = ref(false)
const streamStarted = ref(false)
const statusMessage = ref('点击启动按钮开始监测')
const logs = ref([])
const currentDetection = ref({
  isViolence: false,
  confidence: 0,
  label: 'unknown'
})
let localStream = null
let pollTimer = null
let statusTimer = null

const recentLogs = computed(() => {
  return logs.value.slice(-20).reverse()
})

const startMonitoring = async () => {
  try {
    await requestCameraAccess()

    const response = await axios.get('http://localhost:8080/camera/start')
    if (response.data.success) {
      isRunning.value = true
      statusMessage.value = '正在实时监测中...'
      ElMessage.success(response.data.message)
      addLog('✅ 监测已启动', false)
      startPolling()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('启动监测失败:', error)
    if (error.name === 'NotAllowedError') {
      ElMessage.error('摄像头权限被拒绝，请允许浏览器访问摄像头')
    } else if (error.name === 'NotFoundError') {
      ElMessage.error('未找到摄像头设备')
    } else {
      ElMessage.error('启动监测失败，请检查后端服务是否运行')
    }
  }
}

const stopMonitoring = async () => {
  try {
    const response = await axios.get('http://localhost:8080/camera/stop')
    if (response.data.success) {
      isRunning.value = false
      statusMessage.value = '监测已停止'
      ElMessage.success(response.data.message)
      addLog('🛑 监测已停止', false)
      stopPolling()
      stopCamera()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('停止监测失败:', error)
    ElMessage.error('停止监测失败')
  }
}

const requestCameraAccess = async () => {
  if (streamStarted.value) return

  try {
    localStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: {ideal: 1280},
        height: {ideal: 720},
        facingMode: 'user'
      },
      audio: false
    })

    if (videoRef.value) {
      videoRef.value.srcObject = localStream
      streamStarted.value = true
    }
  } catch (error) {
    throw error
  }
}

const stopCamera = () => {
  if (localStream) {
    localStream.getTracks().forEach(track => track.stop())
    localStream = null
  }
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
  streamStarted.value = false
}
const fetchDetectionResult = async () => {
  try {
    const response = await axios.get('http://localhost:8080/camera/result')
    const data = response.data

    console.log('=== 后端返回的检测结果 ===')
    console.log('完整数据:', data)
    console.log('isViolence:', data.isViolence)
    console.log('confidence:', data.confidence)
    console.log('label:', data.label)

    currentDetection.value = {
      isViolence: data.isViolence,
      confidence: data.confidence,
      label: data.label
    }

    if (data.isViolence) {
      console.log('🚨 检测到暴力！')
      addLog(`🚨 检测到暴力行为！置信度: ${(data.confidence * 100).toFixed(1)}%`, true)
    }
  } catch (error) {
    console.error('获取检测结果失败:', error)
  }
}

const checkStatus = async () => {
  try {
    const response = await axios.get('http://localhost:8080/camera/status')
    isRunning.value = response.data.running
    if (response.data.running) {
      statusMessage.value = '正在实时监测中...'
    } else {
      statusMessage.value = '监测未启动'
    }
  } catch (error) {
    console.error('检查状态失败:', error)
  }
}

const startPolling = () => {
  // 每 500ms 获取一次真实的检测结果
  pollTimer = setInterval(fetchDetectionResult, 500)

  // 每 5 秒检查一次状态
  statusTimer = setInterval(checkStatus, 5000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const addLog = (message, isWarning) => {
  const now = new Date()
  const timeStr = now.toLocaleTimeString('zh-CN', {hour12: false})
  logs.value.push({
    time: timeStr,
    message: message,
    isWarning: isWarning
  })

  if (logs.value.length > 50) {
    logs.value.shift()
  }
}

onMounted(() => {
  checkStatus()
})

onUnmounted(() => {
  stopPolling()
  stopCamera()
})
</script>

<style scoped>
.monitor-container {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  padding-bottom: 100px;
}

.monitor-header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.monitor-header h2 {
  font-size: 28px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.monitor-content {
  max-width: 1000px;
  margin: 0 auto;
}

.video-section {
  margin-bottom: 20px;
}

.video-wrapper {
  position: relative;
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  background: #000;
  aspect-ratio: 16 / 9;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.video-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
  gap: 16px;
}

.video-placeholder p {
  font-size: 16px;
  margin: 0;
}

.detection-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
}

.detection-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 24px;
  background: rgba(103, 194, 58, 0.9);
  color: white;
  font-weight: 600;
  margin-bottom: 12px;
  backdrop-filter: blur(10px);
}

.detection-badge.warning {
  background: rgba(245, 108, 108, 0.9);
  animation: pulse-warning 1s infinite;
}

@keyframes pulse-warning {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.badge-text {
  font-size: 16px;
}

.confidence-bar {
  position: relative;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.confidence-fill {
  height: 100%;
  transition: width 0.3s ease, background 0.3s ease;
  display: flex;
  align-items: center;
}

.confidence-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.control-section {
  margin-bottom: 20px;
}

.status-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.status-indicator {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #909399;
  transition: all 0.3s ease;
}

.status-indicator.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(102, 126, 234, 0);
  }
}

.status-text {
  flex: 1;
}

.status-text h3 {
  margin: 0 0 8px 0;
  font-size: 22px;
  color: #303133;
}

.status-text p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.control-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.control-btn {
  flex: 1;
  height: 52px;
  font-size: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.start-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.stop-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(245, 108, 108, 0.4);
}

.detection-log {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.detection-log h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.log-list {
  max-height: 250px;
  overflow-y: auto;
}

.log-item {
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #f5f7fa;
  display: flex;
  gap: 12px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.log-item.warning {
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
}

.log-time {
  color: #909399;
  min-width: 70px;
  font-family: monospace;
}

.log-message {
  color: #606266;
  flex: 1;
}

.log-item.warning .log-message {
  color: #f56c6c;
  font-weight: 500;
}

.tips-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.tips-card h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.tips-card ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.tips-card li {
  margin-bottom: 8px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .monitor-container {
    padding: 15px;
    padding-bottom: 100px;
  }

  .status-card {
    flex-direction: column;
    text-align: center;
    padding: 24px 20px;
  }

  .control-buttons {
    flex-direction: column;
  }

  .monitor-header h2 {
    font-size: 24px;
  }
}
</style>
