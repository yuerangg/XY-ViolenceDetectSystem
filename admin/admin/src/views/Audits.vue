<template>
  <div class="page-container">
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="24" color="#409eff"><DocumentChecked /></el-icon>
            <span class="header-title">审核报告管理</span>
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
        <el-table-column prop="totalFrameNum" label="总帧数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain">{{ row.totalFrameNum }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="violenceFrameNum" label="暴力帧数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="plain">{{ row.violenceFrameNum }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="violenceRatio" label="暴力占比" width="120" align="center">
          <template #default="{ row }">
            <el-progress
                :percentage="parseFloat((row.violenceRatio * 100).toFixed(2))"
                :color="getProgressColor(row.violenceRatio)"
                :stroke-width="12"
                :show-text="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="conclusion" label="审核结论" min-width="200">
          <template #default="{ row }">
            <div class="conclusion-cell">
              <el-icon :size="18" :color="getConclusionColor(row.violenceRatio)">
                <component :is="getConclusionIcon(row.violenceRatio)" />
              </el-icon>
              <span>{{ row.conclusion }}</span>
            </div>
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
        title="编辑审核报告"
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

        <el-form-item label="总帧数" prop="totalFrameNum">
          <el-input-number
              v-model="formData.totalFrameNum"
              :min="0"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="暴力帧数" prop="violenceFrameNum">
          <el-input-number
              v-model="formData.violenceFrameNum"
              :min="0"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="暴力占比" prop="violenceRatio">
          <el-slider
              v-model="formData.violenceRatio"
              :min="0"
              :max="1"
              :step="0.01"
              :format-tooltip="(val) => (val * 100).toFixed(2) + '%'"
              show-input
          />
        </el-form-item>

        <el-form-item label="审核结论" prop="conclusion">
          <el-input
              v-model="formData.conclusion"
              type="textarea"
              :rows="3"
              placeholder="请输入审核结论"
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
  DocumentChecked,
  Search,
  Refresh,
  Clock,
  Edit,
  Delete,
  CircleCheck,
  Warning,
  CircleClose
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  videoId: ''
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
  totalFrameNum: 0,
  violenceFrameNum: 0,
  violenceRatio: 0,
  conclusion: ''
})

const formRules = {
  videoId: [
    { required: true, message: '请输入视频ID', trigger: 'blur' }
  ],
  conclusion: [
    { required: true, message: '请输入审核结论', trigger: 'blur' }
  ]
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 获取进度条颜色
const getProgressColor = (ratio) => {
  if (ratio <= 0.05) return '#67c23a'
  if (ratio <= 0.1) return '#e6a23c'
  return '#f56c6c'
}

// 获取结论图标
const getConclusionIcon = (ratio) => {
  if (ratio <= 0.05) return CircleCheck
  if (ratio <= 0.1) return Warning
  return CircleClose
}

// 获取结论颜色
const getConclusionColor = (ratio) => {
  if (ratio <= 0.05) return '#67c23a'
  if (ratio <= 0.1) return '#e6a23c'
  return '#f56c6c'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/auditreport/page', {
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
  pagination.page = 1
  loadData()
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    videoId: row.videoId,
    totalFrameNum: row.totalFrameNum,
    violenceFrameNum: row.violenceFrameNum,
    violenceRatio: row.violenceRatio,
    conclusion: row.conclusion
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该审核报告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/auditreport/${row.id}`)
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
        await request.put('/auditreport', {
          id: formData.id,
          videoId: formData.videoId,
          totalFrameNum: formData.totalFrameNum,
          violenceFrameNum: formData.violenceFrameNum,
          violenceRatio: formData.violenceRatio,
          conclusion: formData.conclusion
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

.conclusion-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}

:deep(.el-button--link) {
  padding: 0 8px;
}
</style>
