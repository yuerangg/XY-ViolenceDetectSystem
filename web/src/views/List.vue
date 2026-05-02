<template>
  <div class="list-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon :size="32" class="title-icon"><Document /></el-icon>
          审核记录
        </h1>
        <p class="page-subtitle">查看您的视频审核历史记录</p>
      </div>
      <div class="header-actions">
        <el-button
            type="primary"
            size="large"
            @click="goToUpload"
        >
          <el-icon><Plus /></el-icon>
          上传新视频
        </el-button>
      </div>
    </div>

    <el-card class="table-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><List /></el-icon>
            <span>视频列表</span>
            <el-badge :value="list.length" type="primary" class="badge" />
          </div>
          <div class="header-right">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索视频名称"
                :prefix-icon="Search"
                clearable
                style="width: 250px"
                @input="filterList"
            />
          </div>
        </div>
      </template>

      <el-empty
          v-if="filteredList.length === 0 && !loading"
          description="暂无审核记录"
      >
        <el-button type="primary" @click="goToUpload">
          立即上传
        </el-button>
      </el-empty>

      <el-table
          v-else
          :data="filteredList"
          border
          stripe
          class="video-table"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column
            prop="videoName"
            label="视频名称"
            min-width="200"
            show-overflow-tooltip
        >
          <template #default="scope">
            <div class="video-name-cell">
              <el-icon :size="20" color="#667eea"><VideoPlay /></el-icon>
              <span>{{ scope.row.videoName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
            prop="status"
            label="审核状态"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-tag
                :type="getStatusType(scope.row.status)"
                effect="light"
                size="large"
                round
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="createTime"
            label="上传时间"
            width="180"
            align="center"
        >
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
            label="操作"
            width="140"
            align="center"
            fixed="right"
        >
          <template #default="scope">
            <el-button
                type="primary"
                link
                @click="viewResult(scope.row.id)"
                :disabled="scope.row.status === 0"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  Plus,
  List,
  Search,
  VideoPlay,
  Clock,
  View
} from '@element-plus/icons-vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userId = route.params.userId
const list = ref([])
const loading = ref(true)
const searchKeyword = ref('')

const filteredList = computed(() => {
  if (!searchKeyword.value) return list.value
  return list.value.filter(item =>
      item.videoName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

onMounted(() => {
  loadList()
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/video/list/${userId}`)
    list.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '审核中',
    1: '正常',
    2: '违规'
  }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const viewResult = (videoId) => {
  router.push(`/result/${videoId}`)
}

const goToUpload = () => {
  router.push(`/upload?userId=${userId}`)
}

const filterList = () => {
  // Computed property will automatically update
}
</script>

<style scoped>
.list-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 100px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  color: white;
  font-size: 32px;
  font-weight: 600;
  display: flex;
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

.table-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 16px;
  border: none;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.98);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.badge {
  margin-left: 8px;
}

.video-table {
  border-radius: 8px;
  overflow: hidden;
}

.video-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
}

@media (max-width: 768px) {
  .list-page {
    padding: 20px 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
  }

  .header-right :deep(.el-input) {
    width: 100% !important;
  }
}
</style>
