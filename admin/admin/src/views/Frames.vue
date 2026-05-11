<template>
  <div class="page-container">
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="24" color="#409eff"><Picture /></el-icon>
            <span class="header-title">帧详情管理</span>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="视频ID">
          <el-input
              v-model="searchForm.videoId"
              placeholder="请输入视频ID"
              clearable
              prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="是否暴力">
          <el-select v-model="searchForm.isViolence" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="暴力" :value="1" />
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
        <el-table-column prop="videoId" label="视频ID" width="100" align="center" />
        <el-table-column prop="frameSavePath" label="图片路径" min-width="300">
          <template #default="{ row }">
            <div class="path-cell">
              <el-icon :size="16" color="#909399"><Folder /></el-icon>
              <el-tooltip :content="row.frameSavePath" placement="top">
                <span class="path-text">{{ row.frameSavePath }}</span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="videoSecond" label="视频秒数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ row.videoSecond }}s</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isViolence" label="是否暴力" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isViolence === 1 ? 'danger' : 'success'" effect="light">
              {{ row.isViolence === 1 ? '暴力' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detectLabels" label="检测标签" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.detectLabels" type="warning" effect="plain">
              {{ row.detectLabels }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <el-icon><Clock /></el-icon>
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
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

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="编辑帧详情"
        width="600px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
      >
        <el-form-item label="视频ID" prop="videoId">
          <el-input-number
              v-model="formData.videoId"
              :min="1"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="图片路径" prop="frameSavePath">
          <el-input
              v-model="formData.frameSavePath"
              placeholder="请输入图片路径"
          />
        </el-form-item>

        <el-form-item label="视频秒数" prop="videoSecond">
          <el-input-number
              v-model="formData.videoSecond"
              :min="0"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="是否暴力" prop="isViolence">
          <el-radio-group v-model="formData.isViolence">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">暴力</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="检测标签" prop="detectLabels">
          <el-input
              v-model="formData.detectLabels"
              placeholder="请输入检测标签，如：fight,knife"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture,
  Search,
  Refresh,
  Folder,
  Clock,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  videoId: '',
  isViolence: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  id: null,
  videoId: 1,
  frameSavePath: '',
  videoSecond: 0,
  isViolence: 0,
  detectLabels: ''
})

const formRules = {
  videoId: [
    { required: true, message: '请输入视频ID', trigger: 'blur' }
  ],
  frameSavePath: [
    { required: true, message: '请输入图片路径', trigger: 'blur' }
  ]
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
    const res = await request.post('/framedetail/page', {
      page: pagination.page,
      size: pagination.size,
      keyword: ''
    })

    if (res.data) {
      let records = res.data.records || []

      // 如果输入了视频ID，进行过滤
      if (searchForm.videoId) {
        records = records.filter(item => item.videoId == searchForm.videoId)
      }

      // 如果选择了是否暴力，进行过滤
      if (searchForm.isViolence !== null && searchForm.isViolence !== '') {
        records = records.filter(item => item.isViolence === searchForm.isViolence)
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
  searchForm.videoId = ''
  searchForm.isViolence = null
  pagination.page = 1
  loadData()
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    videoId: row.videoId,
    frameSavePath: row.frameSavePath,
    videoSecond: row.videoSecond,
    isViolence: row.isViolence,
    detectLabels: row.detectLabels
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该帧记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/framedetail/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.put('/framedetail', {
          id: formData.id,
          videoId: formData.videoId,
          frameSavePath: formData.frameSavePath,
          videoSecond: formData.videoSecond,
          isViolence: formData.isViolence,
          detectLabels: formData.detectLabels
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

.path-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.path-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}

:deep(.el-button--link) {
  padding: 0 8px;
}
</style>
