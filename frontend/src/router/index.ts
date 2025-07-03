import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
        redirect: '/dashboard'
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '仪表板' }
      },
      {
        path: '/assets',
        name: 'Assets',
        component: () => import('@/views/AssetView.vue'),
        meta: { title: '资产管理' }
      },
      {
        path: '/vulnerabilities',
        name: 'Vulnerabilities',
        component: () => import('@/views/VulnerabilityView.vue'),
        meta: { title: '漏洞管理' }
      },
      {
        path: '/scans',
        name: 'Scans',
        component: () => import('@/views/ScanView.vue'),
        meta: { title: '扫描管理' }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/UserView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: '/asset-discovery',
        name: 'AssetDiscovery',
        component: () => import('@/views/AssetDiscoveryView.vue'),
        meta: { title: '资产发现' }
      },
      {
        path: '/asset-dependency',
        name: 'AssetDependency',
        component: () => import('@/views/AssetDependencyView.vue'),
        meta: { title: '资产依赖' }
      },
      {
        path: '/baseline-check',
        name: 'BaselineCheck',
        component: () => import('@/views/BaselineCheckView.vue'),
        meta: { title: '基线检查' }
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
  
  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router 