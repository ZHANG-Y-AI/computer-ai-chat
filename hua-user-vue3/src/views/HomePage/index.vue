<template>
    <div>
        <!--头部-->
        <div class="header-container">
            <div class="header">
                <div class="header-left">
                    <img src="../../assets/logo/logo.png" alt="">
                    <div class="titles" @click="router.push('/index/chat/stream')">AI知识库对话系统</div>
                </div>
                <div class="header-center">
                    <div class="front-header-nav">
                        <el-menu :key="headerMenuActive" :default-active="headerMenuActive" mode="horizontal" :router="true">
                            <el-menu-item index="/index/chat/stream">首页</el-menu-item>
                            <el-menu-item index="/index/profile">个人中心</el-menu-item>
                            <el-menu-item
                              v-if="hasKbPerm"
                              index="/index/knowledge/doc"
                            >知识库管理</el-menu-item>
                            <el-menu-item v-if="showSystemAdmin" index="/system/account/user">系统管理</el-menu-item>
                        </el-menu>
                    </div>
                </div>
                <div class="header-right">
                    <div v-if="!nickName" class="auth-buttons">
                        <el-button @click="router.push('/login')">登录</el-button>
                        <el-button @click="router.push('/register')">注册</el-button>
                    </div>
                    <div v-else class="user-info">
                        <el-dropdown>
                            <div class="header-dropdown">
                                <img :src="avatar" alt="">
                                <div class="user-name">
                                    <span>{{ nickName }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
                                </div>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item>
                                        <el-button type="text" style="text-decoration: none" @click.native="logout">退出</el-button>
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>
            </div>
        </div>

        <div>
            <router-view/>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from "@/api/system/user"
import useUserStore from "@/store/modules/user.js";
import {ElMessageBox} from "element-plus";
import auth from '@/plugins/auth'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const nickName = ref(null)

const hasKbPerm = computed(() => auth.hasPermi('kb:knowledge:list'))

/** 知识库 / 系统管理子路由下高亮对应顶栏项 */
const headerMenuActive = computed(() => {
  if (route.path.startsWith('/index/knowledge')) {
    return '/index/knowledge/doc'
  }
  if (route.path.startsWith('/system/account')) {
    return '/system/account/user'
  }
  return route.path
})
const top = ref('')
const title = ref(route.query.title)

const avatar = computed(() => userStore.avatar)

/** 与后端 SysUser.isAdmin() 一致：仅内置超级管理员 userId = 1 */
const showSystemAdmin = computed(() => Number(userStore.id) === 1)

onMounted(() => {
    getList()
})

const getList = () => {
    getUser(userStore.id).then(res => {
        nickName.value = res.data.nickName
    })
}

const logout = () => {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        userStore.logOut().then(() => {
            location.href = '/index'
        })
    }).catch(() => { })
}
</script>

<style scoped>

</style>
