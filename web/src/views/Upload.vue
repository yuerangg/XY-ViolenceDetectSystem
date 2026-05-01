<template>
  <div class="upload-page">
    <el-card>
      <h2>视频上传 - 暴力识别</h2>
      <el-upload
          class="upload-demo"
          action="http://localhost:8080/video/upload"
          :data="{ userId }"
          :show-file-list="true"
          :on-success="handleSuccess"
      >
        <el-button type="primary">点击上传视频</el-button>
      </el-upload>
    </el-card>

    <el-card style="margin-top:20px;">
      <el-button @click="toList">查看我的视频审核记录</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userId = ref('')

onMounted(() => {
  userId.value = route.query.userId
})

const handleSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success('上传成功，后台审核中...')
    router.push(`/result/${res.data}`)
  } else {
    ElMessage.error(res.msg)
  }
}

const toList = () => {
  router.push(`/list/${userId.value}`)
}
</script>

<style scoped>
.upload-page {
  padding: 30px;
}
</style>