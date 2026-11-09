<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
    :ellipsis="false"
  >
    <template v-for="(item, index) in topMenus">
      <el-menu-item :style="{'--theme': theme}" :index="normalizeMenuPath(item.path)" :key="index" v-if="index < visibleNumber">
        <svg-icon
        v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
        :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
      </el-menu-item>
    </template>

    <!-- 顶部菜单超出数量折叠 -->
    <el-sub-menu :style="{'--theme': theme}" index="more" v-if="topMenus.length > visibleNumber">
      <template #title>更多菜单</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="normalizeMenuPath(item.path)"
          :key="index"
          v-if="index >= visibleNumber">
        <svg-icon
          v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
          :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
        </el-menu-item>
      </template>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { constantRoutes } from "@/router"
import { isHttp } from '@/utils/validate'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

/**
 * 混合布局下 TopNav 用「/system」与后端路由 path「system」比较会失败，导致侧栏子菜单为空、主区异常。
 * 统一为单前导斜杠、无尾斜杠（外链原样返回）。
 */
function normalizeMenuPath(p) {
  if (p == null || p === '') return ''
  if (isHttp(p)) return p
  let s = String(p).trim().replace(/\/+/g, '/')
  if (!s.startsWith('/')) s = '/' + s
  if (s.length > 1 && s.endsWith('/')) s = s.slice(0, -1)
  return s
}

// 顶部栏初始数
const visibleNumber = ref(null)
// 当前激活菜单的 index
const currentIndex = ref(null)
// 隐藏侧边栏路由
const hideList = ['/index', '/user/profile']

const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

// 主题颜色
const theme = computed(() => settingsStore.theme)
// 所有的路由信息
const routers = computed(() => permissionStore.topbarRouters)

// 顶部显示菜单
const topMenus = computed(() => {
  let topMenus = []
  routers.value.map((menu) => {
    if (menu.hidden !== true) {
      // 兼容顶部栏一级菜单内部跳转
      if (menu.path === '/' && menu.children) {
          topMenus.push(menu.children[0])
      } else {
          topMenus.push(menu)
      }
    }
  })
  return topMenus
})

// 扁平子菜单列表：仅用于混合布局匹配 parentPath / 侧栏展示；浅拷贝且不改写 vue-router 里的原始 route，避免破坏匹配
const childrenMenus = computed(() => {
  const list = []
  routers.value.forEach((router) => {
    if (!router.children?.length) return
    const baseRaw = router.path === '/' ? '' : router.path
    const base = normalizeMenuPath(baseRaw || '/')
    router.children.forEach((child) => {
      let fullPath
      if (router.path === '/') {
        fullPath = normalizeMenuPath('/' + child.path)
      } else if (isHttp(child.path)) {
        fullPath = child.path
      } else {
        const rel = String(child.path || '').replace(/^\//, '')
        fullPath = normalizeMenuPath(`${base}/${rel}`)
      }
      list.push({
        ...child,
        path: fullPath,
        parentPath: base
      })
    })
  })
  return constantRoutes.concat(list)
})

// 默认激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  let activePath = path
  if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
    const tmpPath = path.substring(1, path.length)
    if (!route.meta.link) {
      const slash = tmpPath.indexOf('/')
      const firstSeg = slash >= 0 ? tmpPath.substring(0, slash) : tmpPath
      activePath = normalizeMenuPath('/' + firstSeg)
      appStore.toggleSideBarHide(false)
    }
  } else if (!route.children) {
    activePath = path
    appStore.toggleSideBarHide(true)
  }
  activeRoutes(activePath)
  return activePath
})

function setVisibleNumber() {
  const width = document.body.getBoundingClientRect().width / 3
  visibleNumber.value = Math.max(1, parseInt(width / 85))
}

function handleSelect(key, keyPath) {
  currentIndex.value = key
  const normKey = normalizeMenuPath(key)
  const route = routers.value.find(item => normalizeMenuPath(item.path) === normKey)
  if (isHttp(key)) {
    // http(s):// 路径新窗口打开
    window.open(key, "_blank")
  } else if (!route || !route.children) {
    // 没有子路由路径内部打开
    const routeMenu = childrenMenus.value.find(item => normalizeMenuPath(item.path) === normKey)
    if (routeMenu && routeMenu.query) {
      let query = JSON.parse(routeMenu.query)
      router.push({ path: normKey, query: query })
    } else {
      router.push({ path: normKey })
    }
    appStore.toggleSideBarHide(true)
  } else {
    // 显示左侧联动菜单
    activeRoutes(normKey)
    appStore.toggleSideBarHide(false)
  }
}

function activeRoutes(key) {
  const normKey = normalizeMenuPath(key)
  let routes = []
  if (childrenMenus.value && childrenMenus.value.length > 0) {
    childrenMenus.value.forEach((item) => {
      const p = normalizeMenuPath(item.parentPath)
      if (normKey === p || (normKey === '/index' && item.path === '')) {
        routes.push(item)
      }
    })
  }
  if (routes.length > 0) {
    permissionStore.setSidebarRouters(routes)
  } else {
    appStore.toggleSideBarHide(true)
  }
  return routes
}

onMounted(() => {
  window.addEventListener('resize', setVisibleNumber)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setVisibleNumber)
})

onMounted(() => {
  setVisibleNumber()
})
</script>

<style lang="scss">
.topmenu-container.el-menu--horizontal {
  height: 50px !important;
  border-bottom: none;
}

.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #303133 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active, .el-menu--horizontal > .el-sub-menu.is-active .el-submenu__title {
  border-bottom: 2px solid #{'var(--theme)'} !important;
  color: #303133;
}

/* sub-menu item */
.topmenu-container.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #303133 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

/* 背景色隐藏 */
.topmenu-container.el-menu--horizontal>.el-menu-item:not(.is-disabled):focus, .topmenu-container.el-menu--horizontal>.el-menu-item:not(.is-disabled):hover, .topmenu-container.el-menu--horizontal>.el-submenu .el-submenu__title:hover {
  background-color: #ffffff;
}

/* 图标右间距 */
.topmenu-container .svg-icon {
  margin-right: 4px;
}

/* topmenu more arrow */
.topmenu-container .el-sub-menu .el-sub-menu__icon-arrow {
  position: static;
  vertical-align: middle;
  margin-left: 8px;
  margin-top: 0px;
}
</style>
