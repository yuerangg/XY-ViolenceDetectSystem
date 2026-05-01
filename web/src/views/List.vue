<template>
  <div style="padding:30px;">
    <el-card>
      <h2>我的视频审核记录</h2>
      <el-table :data="list" border>
        <el-table-column prop="videoName" label="视频名" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span :style="{ color: scope.row.status === 2 ? 'red' : 'green' }">
              {{ scope.row.status === 0 ? '审核中' : scope.row.status === 1 ? '正常' : '暴力' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="$router.push(`/result/${scope.row.id}`)">
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const userId = route.params.userId
const list = ref([])

onMounted(() => {
  loadList()
})

const loadList = async () => {
  const res = await axios.get(`http://localhost:8080/video/list/${userId}`)
  list.value = res.data.data
}
</script>