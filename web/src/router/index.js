import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Upload from '../views/Upload.vue'
import Result from '../views/Result.vue'
import List from '../views/List.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/upload', component: Upload },
    { path: '/result/:videoId', component: Result },
    { path: '/list/:userId', component: List }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router