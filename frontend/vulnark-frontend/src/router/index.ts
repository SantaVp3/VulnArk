import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/responsive-test',
      name: 'responsive-test',
      component: () => import('../views/ResponsiveTestView.vue')
    },
    {
      path: '/debug',
      name: 'debug',
      component: () => import('../views/DebugView.vue')
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'layout',
      component: () => import('../views/LayoutView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'vulnerabilities',
          name: 'Vulnerabilities',
          component: () => import('../views/VulnerabilityView.vue')
        },
        {
          path: 'projects',
          name: 'Projects',
          component: () => import('../views/ProjectView.vue')
        },
        {
          path: 'assets',
          name: 'Assets',
          component: () => import('../views/AssetView.vue')
        },
        {
          path: 'scan',
          name: 'Scan',
          component: () => import('../views/ScanView.vue')
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/UserView.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'asset-detection-test',
          name: 'AssetDetectionTest',
          component: () => import('../views/AssetDetectionTestView.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 如果有token但没有用户信息，等待初始化完成
  if (authStore.token && !authStore.user) {
    try {
      await authStore.initializeAuth()
    } catch (error) {
      console.error('Auth initialization failed:', error)
    }
  }

  // 调试信息（开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log('Route guard check:', {
      to: to.path,
      requiresAuth: to.meta.requiresAuth,
      requiresAdmin: to.meta.requiresAdmin,
      isLoggedIn: authStore.isLoggedIn,
      isAdmin: authStore.isAdmin,
      user: authStore.user?.username,
      role: authStore.user?.role
    })
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isLoggedIn) {
    next('/dashboard')
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    console.warn('Access denied: Admin role required')
    next('/dashboard')
  } else {
    next()
  }
})

export default router
