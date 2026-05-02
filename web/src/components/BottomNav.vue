<template>
  <div class="bottom-nav">
    <div class="nav-container">
      <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="getNavPath(item.path)"
          class="nav-item"
          active-class="active"
      >
        <el-icon :size="24">
          <component :is="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, DocumentChecked, User } from '@element-plus/icons-vue'

const route = useRoute()

const navItems = [
  {
    label: '首页',
    path: '/upload',
    icon: HomeFilled
  },
  {
    label: '审核记录',
    path: '/list',
    icon: DocumentChecked
  },
  {
    label: '我的',
    path: '/profile',
    icon: User
  }
]

const getNavPath = (path) => {
  const user = JSON.parse(localStorage.getItem('user'))
  const userId = user?.id || ''

  if (path === '/upload') {
    return `/upload?userId=${userId}`
  }
  if (path === '/list') {
    return `/list/${userId}`
  }
  if (path === '/profile') {
    return `/profile/${userId}`
  }
  return path
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  padding: 8px 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0 20px;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  text-decoration: none;
  color: #909399;
  transition: all 0.3s ease;
  border-radius: 8px;
  cursor: pointer;
}

.nav-item:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.nav-item.active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.12);
}

.nav-item.active .el-icon {
  transform: scale(1.1);
}

.nav-label {
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.el-icon {
  transition: transform 0.3s ease;
}

@media (max-width: 768px) {
  .nav-container {
    padding: 0 10px;
  }

  .nav-item {
    padding: 6px 12px;
  }

  .nav-label {
    font-size: 11px;
  }
}
</style>
