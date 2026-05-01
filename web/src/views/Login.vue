<template>
  <div class="login-box">
    <el-card class="box-card">
      <h2>暴力视频识别系统 - 登录</h2>
      <el-form v-model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ username: '', password: '' })

const login = async () => {
  const res = await axios.post('http://localhost:8080/user/login', null, {
    params: form.value
  })
  if (res.data.code === 200) {
    ElMessage.success('登录成功')
    localStorage.setItem('user', JSON.stringify(res.data.data))
    router.push(`/upload?userId=${res.data.data.id}`)
  } else {
    ElMessage.error(res.data.msg)
  }
}
</script>

<style scoped>
.login-box {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.box-card {
  width: 400px;
  padding: 20px;
}
</style>