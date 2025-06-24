<template>
  <a-layout class="layout">
    <a-layout-sider
      :width="200"
      :collapsed="collapsed"
      :collapsible="true"
      :breakpoint="'lg'"
      @collapse="onCollapse"
      @breakpoint="onBreakpoint"
    >
      <div class="logo logo-enhanced">
        <div class="logo-icon">
          <icon-safe />
        </div>
        <span v-if="!collapsed" class="logo-text gradient-text">VulnArk</span>
        <span v-else class="logo-text-short gradient-text">VA</span>
      </div>
      
      <a-menu
        :default-selected-keys="[currentRoute]"
        :style="{ width: '100%', border: 'none' }"
        theme="dark"
        mode="vertical"
        class="sidebar-menu-enhanced"
      >
        <a-menu-item key="dashboard" @click="$router.push('/dashboard')">
          <template #icon>
            <icon-dashboard />
          </template>
          仪表板
        </a-menu-item>
        
        <a-menu-item key="vulnerabilities" @click="$router.push('/dashboard/vulnerabilities')">
          <template #icon>
            <icon-bug />
          </template>
          漏洞管理
        </a-menu-item>
        
        <a-menu-item key="projects" @click="$router.push('/dashboard/projects')">
          <template #icon>
            <icon-folder />
          </template>
          项目管理
        </a-menu-item>

        <a-menu-item key="assets" @click="$router.push('/dashboard/assets')">
          <template #icon>
            <icon-desktop />
          </template>
          资产管理
        </a-menu-item>

        <a-menu-item key="scan" @click="$router.push('/dashboard/scan')">
          <template #icon>
            <icon-search />
          </template>
          漏洞扫描
        </a-menu-item>

        <a-menu-item
          v-if="authStore.isAdmin"
          key="users"
          @click="$router.push('/dashboard/users')"
        >
          <template #icon>
            <icon-user />
          </template>
          用户管理
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <a-layout>
      <a-layout-header class="header header-enhanced">
        <div class="header-left">
          <a-breadcrumb class="breadcrumb-enhanced">
            <a-breadcrumb-item>
              <span class="breadcrumb-title">{{ breadcrumbTitle }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 调试信息（开发环境） -->
          <div v-if="isDev" style="margin-right: 16px; font-size: 12px; color: #666; background: #f0f0f0; padding: 4px 8px; border-radius: 4px;">
            Admin: {{ authStore.isAdmin }} | Role: {{ authStore.user?.role }} | User: {{ authStore.user?.username }}
          </div>

          <a-dropdown>
            <a-avatar :size="36" class="user-avatar-enhanced">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </a-avatar>
            <template #content>
              <a-doption>
                <template #icon>
                  <icon-user />
                </template>
                个人信息
              </a-doption>
              <a-doption>
                <template #icon>
                  <icon-settings />
                </template>
                设置
              </a-doption>
              <a-doption @click="handleLogout">
                <template #icon>
                  <icon-poweroff />
                </template>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconDashboard,
  IconBug,
  IconFolder,
  IconDesktop,
  IconUser,
  IconSettings,
  IconPoweroff,
  IconSearch,
  IconSafe
} from '@arco-design/web-vue/es/icon'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const collapsed = ref(false)
const isDev = import.meta.env.DEV

const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('/vulnerabilities')) return 'vulnerabilities'
  if (path.includes('/projects')) return 'projects'
  if (path.includes('/assets')) return 'assets'
  if (path.includes('/scan')) return 'scan'
  if (path.includes('/users')) return 'users'
  return 'dashboard'
})

const breadcrumbTitle = computed(() => {
  const routeMap: Record<string, string> = {
    dashboard: '仪表板',
    vulnerabilities: '漏洞管理',
    projects: '项目管理',
    assets: '资产管理',
    scan: '漏洞扫描',
    users: '用户管理'
  }
  return routeMap[currentRoute.value] || '首页'
})

const onCollapse = (val: boolean) => {
  collapsed.value = val
}

const onBreakpoint = (broken: boolean) => {
  if (broken) {
    collapsed.value = true
  } else {
    collapsed.value = false
  }
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    Message.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    Message.error('退出登录失败')
  }
}

// 调试：监听认证状态变化
if (isDev) {
  watch(
    () => authStore.isAdmin,
    (newValue, oldValue) => {
      console.log('LayoutView: isAdmin changed', { oldValue, newValue, user: authStore.user })
    },
    { immediate: true }
  )

  watch(
    () => authStore.user,
    (newValue, oldValue) => {
      console.log('LayoutView: user changed', {
        oldValue: oldValue?.username,
        newValue: newValue?.username,
        role: newValue?.role
      })
    },
    { immediate: true }
  )
}
</script>

<style scoped>
.layout {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: var(--primary-gradient);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.logo::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: logoShimmer 3s infinite;
}

.logo-icon {
  font-size: 24px;
  color: var(--text-white);
  animation: float 3s ease-in-out infinite;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-white);
  letter-spacing: 1px;
}

.logo-text-short {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-white);
  letter-spacing: 1px;
}

@keyframes logoShimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 侧边栏菜单样式增强 */
:deep(.arco-layout-sider) {
  background: var(--bg-sidebar) !important;
  box-shadow: var(--shadow-lg) !important;
}

:deep(.arco-menu-dark) {
  background: var(--bg-sidebar) !important;
}

:deep(.arco-menu-item) {
  height: 52px !important;
  line-height: 52px !important;
  margin: 4px 8px !important;
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-fast) !important;
}

:deep(.arco-menu-item:hover) {
  background: rgba(59, 130, 246, 0.1) !important;
  transform: translateX(4px) !important;
}

:deep(.arco-menu-item-selected) {
  background: var(--primary-gradient) !important;
  color: var(--text-white) !important;
  box-shadow: var(--shadow-md) !important;
}

:deep(.arco-menu-item-inner) {
  padding: 0 16px !important;
  display: flex !important;
  align-items: center !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  border-radius: var(--radius-md) !important;
}

:deep(.arco-menu-icon) {
  margin-right: 12px !important;
  font-size: 18px !important;
  transition: all var(--transition-fast) !important;
}

:deep(.arco-menu-item:hover .arco-menu-icon) {
  transform: scale(1.1) !important;
}

.header {
  background: var(--bg-primary);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-md);
  border-bottom: 1px solid var(--border-light);
  position: relative;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.breadcrumb-enhanced {
  font-weight: 500;
}

.breadcrumb-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-avatar-enhanced {
  background: var(--primary-gradient) !important;
  color: var(--text-white) !important;
  font-weight: 600 !important;
  box-shadow: var(--shadow-md) !important;
  transition: all var(--transition-fast) !important;
  cursor: pointer;
}

.user-avatar-enhanced:hover {
  transform: scale(1.05) !important;
  box-shadow: var(--shadow-lg) !important;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  padding: 24px;
  background: var(--bg-secondary);
  overflow: auto;
  height: calc(100vh - 64px);
  min-height: 0;
  flex: 1;
  position: relative;
}

.content::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(30, 58, 138, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: -1;
}

/* 平板响应式 */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .content {
    padding: 16px;
  }

  .logo {
    font-size: 16px;
  }
}

/* 手机响应式 */
@media (max-width: 480px) {
  .header {
    padding: 0 12px;
  }

  .content {
    padding: 12px;
  }

  .logo {
    font-size: 14px;
  }

  .header-right {
    gap: 8px;
  }
}

/* 超小屏幕 */
@media (max-width: 320px) {
  .header {
    padding: 0 8px;
  }

  .content {
    padding: 8px;
  }
}
</style>
