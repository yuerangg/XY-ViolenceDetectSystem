<template>
  <div class="reg-box">
    <el-card class="box-card">
      <h2>注册</h2>
      <el-form v-model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="register">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
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

const register = async () => {
  const res = await axios.post('http://localhost:8080/user/register', null, {
    params: form.value
  })
  if (res.data.code === 200) {
    ElMessage.success('注册成功')
    router.push('/login')
  } else {
    ElMessage.error(res.data.msg)
  }
}
</script>

<style scoped>
.reg-box {
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