import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Root',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '仪表板', requiresRole: ['ADMIN', 'MANAGER', 'ANALYST'] }
      },
      {
        path: '/assets',
        name: 'Assets',
        component: () => import('@/views/AssetView.vue'),
        meta: { title: '资产管理', requiresRole: ['ADMIN', 'MANAGER', 'ANALYST'] }
      },
      {
        path: '/vulnerabilities',
        name: 'Vulnerabilities',
        component: () => import('@/views/VulnerabilityView.vue'),
        meta: { title: '漏洞管理' }
      },

      {
        path: '/baseline-check',
        name: 'BaselineCheck',
        component: () => import('@/views/BaselineCheckView.vue'),
        meta: { title: '基线扫描', requiresRole: ['ADMIN', 'MANAGER', 'ANALYST'] }
      },
      {
        path: '/scan-tools',
        name: 'ScanToolManage',
        component: () => import('@/views/ScanToolManageView.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '工具管理' }
      },
      {
        path: '/vulnerability-scan',
        name: 'VulnerabilityScan',
        component: () => import('@/views/VulnerabilityScanView.vue'),
        meta: { requiresAuth: true, title: '漏洞扫描', requiresRole: ['ADMIN', 'MANAGER', 'ANALYST'] }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/UserView.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    next('/login')
    return
  } 
  
  // 已登录用户尝试访问登录页，重定向到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    // 根据用户角色决定重定向到哪个页面
    if (authStore.user?.role === UserRole.USER) {
      next('/vulnerabilities')
    } else {
      next('/dashboard')
    }
    return
  }
  
  // 处理根路径重定向
  if (to.path === '/') {
    if (authStore.user?.role === UserRole.USER) {
      next('/vulnerabilities')
    } else {
      next('/dashboard')
    }
    return
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && authStore.user) {
    const userRole = authStore.user.role
    if (userRole !== UserRole.ADMIN && userRole !== UserRole.MANAGER) {
      // 非管理员用户尝试访问管理员页面，重定向到漏洞页面
      next('/vulnerabilities')
      return
    }
  }
  
  // 检查是否需要特定角色
  if (to.meta.requiresRole && authStore.user) {
    const requiredRoles = to.meta.requiresRole as string[]
    const userRole = authStore.user.role
    if (!requiredRoles.includes(userRole)) {
      // 用户没有所需角色，重定向到漏洞页面
      next('/vulnerabilities')
      return
    }
  }
  
  next()
})

export default router 