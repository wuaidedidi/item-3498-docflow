import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'
import Layout from '@/layout/Layout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'el-icon-s-home' }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/document/DocumentList.vue'),
        meta: { title: '文档管理', icon: 'el-icon-document' }
      },
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import('@/views/document/DocumentDetail.vue'),
        meta: { title: '文档详情', hidden: true }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: { title: '分类管理', icon: 'el-icon-folder', roles: ['admin'] }
      },
      {
        path: 'approval/flows',
        name: 'ApprovalFlows',
        component: () => import('@/views/approval/FlowList.vue'),
        meta: { title: '审批流程', icon: 'el-icon-s-check', roles: ['admin'] }
      },
      {
        path: 'approval/my',
        name: 'MyApprovals',
        component: () => import('@/views/approval/MyApprovals.vue'),
        meta: { title: '我的审批', icon: 'el-icon-s-claim' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'el-icon-user', roles: ['admin'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  },
  {
    path: '*',
    redirect: '/dashboard'
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + 'DocFlow'

  const token = store.getters.token

  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    const user = store.getters.user
    if (user && to.meta.roles.includes(user.role)) {
      next()
    } else {
      next('/dashboard')
    }
    return
  }

  next()
})

export default router
