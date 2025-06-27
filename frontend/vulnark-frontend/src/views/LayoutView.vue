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
        :default-open-keys="defaultOpenKeys"
        :style="{ width: '100%', border: 'none' }"
        theme="dark"
        mode="vertical"
        class="sidebar-menu-enhanced"
      >
        <!-- 仪表板 -->
        <a-menu-item key="dashboard" @click="$router.push('/')">
          <template #icon>
            <icon-dashboard />
          </template>
          仪表板
        </a-menu-item>

        <!-- 项目管理 -->
        <a-menu-item
          v-if="canAccessManagement"
          key="projects"
          @click="$router.push('/projects')"
        >
          <template #icon>
            <icon-folder />
          </template>
          项目管理
        </a-menu-item>

        <!-- 漏洞管理分组 -->
        <a-sub-menu key="vulnerability-group">
          <template #icon>
            <icon-safe />
          </template>
          <template #title>漏洞管理</template>

          <a-menu-item key="vulnerabilities" @click="$router.push('/vulnerabilities')">
            <template #icon>
              <icon-bug />
            </template>
            漏洞管理
          </a-menu-item>

          <a-menu-item
            v-if="canAccessManagement"
            key="scan"
            @click="$router.push('/scan')"
          >
            <template #icon>
              <icon-search />
            </template>
            漏洞扫描
          </a-menu-item>

          <a-menu-item
            v-if="canAccessManagement"
            key="baseline"
            @click="$router.push('/baseline')"
          >
            <template #icon>
              <icon-safe />
            </template>
            基线检查
          </a-menu-item>
        </a-sub-menu>

        <!-- 资产管理分组 -->
        <a-sub-menu v-if="canAccessManagement" key="asset-group">
          <template #icon>
            <icon-desktop />
          </template>
          <template #title>资产管理</template>

          <a-menu-item key="assets" @click="$router.push('/assets')">
            <template #icon>
              <icon-desktop />
            </template>
            资产管理
          </a-menu-item>

          <a-menu-item key="asset-dependencies" @click="navigateTo('/asset-dependencies')">
            <template #icon>
              <icon-relation />
            </template>
            资产依赖关系
          </a-menu-item>

          <a-menu-item key="asset-discovery" @click="navigateTo('/asset-discovery')">
            <template #icon>
              <icon-scan />
            </template>
            资产发现
          </a-menu-item>
        </a-sub-menu>

        <!-- 用户管理 -->
        <a-menu-item
          v-if="isAdmin"
          key="users"
          @click="$router.push('/users')"
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
  IconSafe,
  IconRelation,
  IconScan
} from '@arco-design/web-vue/es/icon'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const collapsed = ref(false)
const isDev = import.meta.env.DEV

// 角色权限检查
const isViewer = computed(() => authStore.user?.role === 'VIEWER')
const isAnalyst = computed(() => authStore.user?.role === 'ANALYST')
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const canAccessManagement = computed(() => isAdmin.value || isAnalyst.value)

// 默认展开的子菜单
const defaultOpenKeys = computed(() => {
  const path = route.path
  const openKeys = []

  // 如果当前路径属于漏洞管理分组，展开漏洞管理子菜单
  if (path.includes('/vulnerabilities') || path.includes('/scan') || path.includes('/baseline')) {
    openKeys.push('vulnerability-group')
  }

  // 如果当前路径属于资产管理分组，展开资产管理子菜单
  if (path.includes('/assets') || path.includes('/asset-dependencies') || path.includes('/asset-discovery')) {
    openKeys.push('asset-group')
  }

  return openKeys
})

const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('/vulnerabilities')) return 'vulnerabilities'
  if (path.includes('/projects')) return 'projects'
  if (path.includes('/asset-dependencies')) return 'asset-dependencies'
  if (path.includes('/asset-discovery')) return 'asset-discovery'
  if (path.includes('/assets')) return 'assets'
  if (path.includes('/scan')) return 'scan'
  if (path.includes('/baseline')) return 'baseline'
  if (path.includes('/users')) return 'users'
  return 'dashboard'
})

const breadcrumbTitle = computed(() => {
  const routeMap: Record<string, string> = {
    dashboard: '仪表板',
    vulnerabilities: '漏洞管理',
    projects: '项目管理',
    assets: '资产管理',
    'asset-dependencies': '资产依赖关系',
    'asset-discovery': '资产发现',
    scan: '漏洞扫描',
    baseline: '基线检查',
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

const navigateTo = (path: string) => {
  router.push(path)
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

/* 子菜单样式增强 */
:deep(.arco-menu-sub) {
  background: rgba(0, 0, 0, 0.2) !important;
  border-radius: var(--radius-md) !important;
  margin: 4px 8px !important;
  overflow: hidden !important;
}

:deep(.arco-menu-sub-header) {
  height: 52px !important;
  line-height: 52px !important;
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-fast) !important;
  margin-bottom: 4px !important;
}

:deep(.arco-menu-sub-header:hover) {
  background: rgba(59, 130, 246, 0.1) !important;
  transform: translateX(4px) !important;
}

:deep(.arco-menu-sub-header-selected) {
  background: var(--primary-gradient) !important;
  color: var(--text-white) !important;
  box-shadow: var(--shadow-md) !important;
}

:deep(.arco-menu-sub-content) {
  background: transparent !important;
  padding: 0 !important;
}

:deep(.arco-menu-sub .arco-menu-item) {
  height: 44px !important;
  line-height: 44px !important;
  margin: 2px 12px !important;
  padding-left: 16px !important;
  border-radius: var(--radius-sm) !important;
  background: rgba(255, 255, 255, 0.05) !important;
}

:deep(.arco-menu-sub .arco-menu-item:hover) {
  background: rgba(59, 130, 246, 0.15) !important;
  transform: translateX(8px) !important;
}

:deep(.arco-menu-sub .arco-menu-item-selected) {
  background: rgba(59, 130, 246, 0.3) !important;
  color: var(--text-white) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

:deep(.arco-menu-sub .arco-menu-item .arco-menu-icon) {
  font-size: 16px !important;
  margin-right: 10px !important;
}

/* 子菜单展开/收起动画 */
:deep(.arco-menu-sub-content) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

/* 子菜单标题图标旋转动画 */
:deep(.arco-menu-sub-header .arco-menu-sub-icon) {
  transition: transform 0.3s ease !important;
}

:deep(.arco-menu-sub-open .arco-menu-sub-header .arco-menu-sub-icon) {
  transform: rotate(90deg) !important;
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
