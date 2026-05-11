<template>
  <div class="page-container">
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="24" color="#409eff"><VideoCamera /></el-icon>
            <span class="header-title">视频管理</span>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
              v-model="searchForm.keyword"
              placeholder="请输入视频名称"
              clearable
              prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="暴力" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
          :data="tableData"
          border
          stripe
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="videoName" label="视频名称" min-width="200">
          <template #default="{ row }">
            <div class="video-name-cell">
              <el-icon :size="18" color="#409eff"><Film /></el-icon>
              <span>{{ row.videoName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="videoSuffix" label="格式" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.videoSuffix }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" align="center">
          <template #default="{ row }">
            <el-icon><Clock /></el-icon>
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
                type="success"
                size="small"
                @click="handlePlay(row)"
                link
            >
              <el-icon><VideoPlay /></el-icon>
              播放
            </el-button>
            <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                link
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                link
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          class="pagination"
      />
    </el-card>

    <!-- 视频播放对话框 -->
    <el-dialog
        v-model="playDialogVisible"
        :title="currentVideo.videoName || '视频播放'"
        width="900px"
        :close-on-click-modal="false"
        @closed="handleCloseVideo"
    >
      <div class="video-player-container">
        <div class="video-wrapper">
          <video
              ref="videoPlayerRef"
              :src="videoUrl"
              controls
              preload="metadata"
              class="video-player"
              crossorigin="anonymous"
              @error="handleVideoError"
              @loadeddata="handleVideoLoaded"
              @canplay="handleVideoCanPlay"
          >
            您的浏览器不支持视频播放
          </video>

          <!-- 视频加载状态提示 -->
          <div v-if="videoLoading" class="video-loading">
            <el-icon class="is-loading" :size="48"><Loading /></el-icon>
            <p>视频加载中，首次播放可能需要转码...</p>
          </div>

          <!-- 视频错误提示 -->
          <div v-if="videoError" class="video-error">
            <el-icon :size="48"><CircleClose /></el-icon>
            <p>{{ videoErrorMessage }}</p>
            <el-button type="primary" size="small" @click="retryLoadVideo">
              重试
            </el-button>
          </div>
        </div>

        <div class="video-info-bar" v-if="!videoError">
          <el-tag type="info" size="small">
            <el-icon><Document /></el-icon>
            {{ currentVideo.videoName }}.{{ currentVideo.videoSuffix }}
          </el-tag>
          <el-tag type="success" size="small" v-if="videoLoaded">
            <el-icon><Check /></el-icon>
            视频加载成功
          </el-tag>
        </div>
      </div>

      <div class="video-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="视频名称">
            {{ currentVideo.videoName }}
          </el-descriptions-item>
          <el-descriptions-item label="视频格式">
            {{ currentVideo.videoSuffix }}
          </el-descriptions-item>
          <el-descriptions-item label="视频时长">
            {{ currentVideo.videoDuration }} 秒
          </el-descriptions-item>
          <el-descriptions-item label="视频状态">
            <el-tag :type="getStatusType(currentVideo.status)">
              {{ getStatusText(currentVideo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上传时间" :span="2">
            {{ formatTime(currentVideo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="编辑视频信息"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
      >
        <el-form-item label="视频名称" prop="videoName">
          <el-input v-model="formData.videoName" placeholder="请输入视频名称" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="暴力" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  VideoCamera,
  Search,
  Refresh,
  VideoPlay,
  Loading,
  CircleClose,
  Check,
  Document,
  Film,
  Timer,
  Clock,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const playDialogVisible = ref(false)
const formRef = ref(null)
const videoPlayerRef = ref(null)
const videoUrl = ref('')
const currentVideo = reactive({
  id: null,
  videoName: '',
  videoSuffix: '',
  videoDuration: 0,
  status: 0,
  createTime: ''
})

// 视频状态
const videoLoading = ref(false)
const videoError = ref(false)
const videoLoaded = ref(false)
const videoErrorMessage = ref('')
const isClosingDialog = ref(false)

const searchForm = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  id: null,
  videoName: '',
  status: 0
})

const formRules = {
  videoName: [
    {required: true, message: '请输入视频名称', trigger: 'blur'}
  ],
  status: [
    {required: true, message: '请选择状态', trigger: 'change'}
  ]
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {0: '待审核', 1: '正常', 2: '暴力'}
  return map[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {0: 'info', 1: 'success', 2: 'danger'}
  return map[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/info/page', {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword
    })

    if (res.data) {
      let records = res.data.records || []

      // 如果选择了状态，进行过滤
      if (searchForm.status !== null && searchForm.status !== '') {
        records = records.filter(item => item.status === searchForm.status)
      }

      tableData.value = records
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  loadData()
}

// 播放视频
const handlePlay = async (row) => {
  currentVideo.id = row.id
  currentVideo.videoName = row.videoName
  currentVideo.videoSuffix = row.videoSuffix
  currentVideo.videoDuration = row.videoDuration
  currentVideo.status = row.status
  currentVideo.createTime = row.createTime

  // 重置视频状态
  videoLoading.value = false
  videoError.value = false
  videoLoaded.value = false
  videoErrorMessage.value = ''
  isClosingDialog.value = false

  try {
    // 先获取视频的详细信息，确保文件名正确
    const res = await request.get(`/info/${row.id}`)

    if (res.data) {
      const videoInfo = res.data

      console.log('=== 视频详细信息 ===')
      console.log(videoInfo)
      console.log('videoSavePath:', videoInfo.videoSavePath)

      // 从 videoSavePath 中提取文件名
      let fileName = ''

      if (videoInfo.videoSavePath) {
        const lastSeparatorIndex = Math.max(
            videoInfo.videoSavePath.lastIndexOf('/'),
            videoInfo.videoSavePath.lastIndexOf('\\')
        )

        if (lastSeparatorIndex >= 0 && lastSeparatorIndex < videoInfo.videoSavePath.length - 1) {
          fileName = videoInfo.videoSavePath.substring(lastSeparatorIndex + 1)
        } else {
          fileName = videoInfo.videoSavePath
        }
      } else {
        fileName = `${videoInfo.videoName}.${videoInfo.videoSuffix}`
      }

      console.log('最终使用的文件名:', fileName)

      videoUrl.value = `http://localhost:8080/video/play/${encodeURIComponent(fileName)}`
      console.log('视频URL:', videoUrl.value)

      playDialogVisible.value = true

      setTimeout(() => {
        if (videoPlayerRef.value) {
          videoLoading.value = true
          videoPlayerRef.value.load()
        }
      }, 100)
    } else {
      throw new Error('获取视频信息失败')
    }
  } catch (error) {
    console.error('获取视频详情失败:', error)
    ElMessage.error('获取视频信息失败')

    const fileName = `${row.videoName}.${row.videoSuffix}`
    videoUrl.value = `http://localhost:8080/video/play/${encodeURIComponent(fileName)}`

    playDialogVisible.value = true

    setTimeout(() => {
      if (videoPlayerRef.value) {
        videoLoading.value = true
        videoPlayerRef.value.load()
      }
    }, 100)
  }
}

// 视频加载错误处理
const handleVideoError = (event) => {
  // 如果正在关闭对话框，忽略错误
  if (isClosingDialog.value) {
    return
  }

  console.error('视频加载错误:', event)

  videoLoading.value = false
  videoError.value = true
  videoLoaded.value = false

  const video = event.target
  if (video.error) {
    switch (video.error.code) {
      case 1:
        videoErrorMessage.value = '视频加载被中止'
        break
      case 2:
        videoErrorMessage.value = '网络错误，请检查网络连接'
        break
      case 3:
        videoErrorMessage.value = '视频解码失败'
        break
      case 4:
        videoErrorMessage.value = '视频文件不存在或路径错误'
        break
      default:
        videoErrorMessage.value = '视频加载失败'
    }
  } else {
    videoErrorMessage.value = '视频加载失败'
  }

  ElMessage.error('视频加载失败：' + videoErrorMessage.value)
}

// 视频数据加载成功
const handleVideoLoaded = () => {
  console.log('视频数据加载成功')
  videoLoading.value = false
}

// 视频可以播放
const handleVideoCanPlay = () => {
  console.log('视频可以播放')
  videoLoading.value = false
  videoLoaded.value = true
  videoError.value = false
}

// 重试加载视频
const retryLoadVideo = () => {
  console.log('重试加载视频')
  videoError.value = false
  videoLoading.value = true

  if (videoPlayerRef.value) {
    videoPlayerRef.value.load()
  }
}

// 关闭视频播放器
const handleCloseVideo = () => {
  // 标记正在关闭对话框
  isClosingDialog.value = true

  if (videoPlayerRef.value) {
    videoPlayerRef.value.pause()
    videoPlayerRef.value.src = ''
  }
  videoUrl.value = ''
  videoLoading.value = false
  videoError.value = false
  videoLoaded.value = false

  // 延迟重置标记，确保不会触发错误
  setTimeout(() => {
    isClosingDialog.value = false
  }, 500)
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    videoName: row.videoName,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该视频吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/info/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.put('/info', {
          id: formData.id,
          videoName: formData.videoName,
          status: formData.status
        })

        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.video-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}

.video-player-container {
  margin-bottom: 20px;
}

.video-wrapper {
  position: relative;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  background: #000;
  min-height: 300px;
}

.video-player {
  width: 100%;
  height: auto;
  display: block;
  max-height: 500px;
}

.video-loading,
.video-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
}

.video-loading p,
.video-error p {
  margin: 0;
  font-size: 16px;
}

.video-error .el-icon {
  color: #f56c6c;
}

.video-info-bar {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.video-info-bar .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-details {
  margin-top: 20px;
}

:deep(.el-button--link) {
  padding: 0 8px;
}
</style>
