<template>
  <div class="result-page" style="padding:30px;">
    <el-card>
      <h2>视频审核结果</h2>
      <div v-if="loading">加载中...</div>
      <div v-else>
        <p>总帧数：{{ result.totalFrameNum }}</p>
        <p>暴力帧数：{{ result.violenceFrameNum }}</p>
        <p>违规占比：{{ (result.violenceRatio * 100).toFixed(2) }}%</p>
        <p :style="{ color: result.violenceRatio > 0.1 ? 'red' : 'green', fontWeight: 'bold', fontSize: '18px' }">
          {{ result.conclusion }}
        </p>
      </div>
      <el-button @click="$router.push(`/list/${userId}`)" style="margin-top:20px;">
        返回列表
      </el-button>
    </el-card>
  </div>
</template>

<script setup>import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const videoId = route.params.videoId
const result = ref({})
const loading = ref(true)
const userId = ref('')

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user'))
  userId.value = user.id
  loadResult()
})

const loadResult = async () => {
  const res = await axios.get(`http://localhost:8080/video/result/${videoId}`)
  result.value = res.data.data
  loading.value = false
}
</script>