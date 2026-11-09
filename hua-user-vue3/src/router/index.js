import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout/index'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/views/index'),
    redirect: '/index/chat/stream',
    children: [
      {
        path: 'index',
        component: () => import('@/views/HomePage/index'),
        children: [
          {
            path: 'profile',
            component: () => import('@/views/system/user/profile/index'),
            hidden: true
          },
          {
            path: 'chat',
            component: () => import('@/views/HomePage/ChatLayout.vue'),
            redirect: '/index/chat/stream',
            children: [
              {
                path: 'normal',
                component: () => import('@/views/HomePage/ChatWorkspace.vue'),
                name: 'ChatNormal',
                meta: { chatMode: 'normal', title: '普通对话' }
              },
              {
                path: 'rag',
                component: () => import('@/views/HomePage/ChatWorkspace.vue'),
                name: 'ChatRag',
                meta: { chatMode: 'rag', title: 'RAG对话' }
              },
              {
                path: 'report',
                component: () => import('@/views/HomePage/ChatWorkspace.vue'),
                name: 'ChatReport',
                meta: { chatMode: 'report', title: '报告生成' }
              },
              {
                path: 'stream',
                component: () => import('@/views/HomePage/ChatWorkspace.vue'),
                name: 'ChatStream',
                meta: { chatMode: 'stream', title: '流式对话' }
              }
            ]
          },
          {
            path: 'home',
            redirect: '/index/chat/stream',
            hidden: true
          },
          {
            path: 'knowledge',
            component: () => import('@/views/system/knowledge/KbShell.vue'),
            redirect: '/index/knowledge/doc',
            meta: { title: '知识库管理' },
            children: [
              {
                path: 'doc',
                component: () => import('@/views/system/knowledge/index.vue'),
                meta: { title: '文档管理' }
              },
              {
                path: 'ops',
                component: () => import('@/views/system/knowledge/ops.vue'),
                meta: { title: '知识库运维' }
              }
            ]
          }
        ],
        hidden: true
      }
    ]
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
]

// 动态路由：由 permission 按权限注册（与后端菜单外的隐藏页对齐）
export const dynamicRoutes = [
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

export default router
