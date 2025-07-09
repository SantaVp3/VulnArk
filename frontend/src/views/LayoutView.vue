<template>
  <a-layout class="layout-container">
    <!-- 侧边栏 -->
    <a-layout-sider
      :width="250"
      :collapsed="collapsed"
      :collapsed-width="80"
      collapsible
      @collapse="onCollapse"
      class="layout-sider"
    >
      <div class="logo">
        <div class="logo-icon">V</div>
        <div v-if="!collapsed" class="logo-text">VulnArk</div>
      </div>
      
      <a-menu
        :selected-keys="selectedKeys"
        :default-open-keys="openKeys"
        mode="vertical"
        theme="light"
        class="layout-menu"
        @menu-item-click="onMenuClick"
      >
        <!-- 管理员、经理和分析师可见的仪表盘 -->
        <a-menu-item key="/dashboard" v-if="canViewDashboard">
          <template #icon>
            <icon-dashboard />
          </template>
          仪表板
        </a-menu-item>
        
        <!-- 管理员、经理和分析师可见的资产管理 -->
        <a-menu-item key="/assets" v-if="canViewAssets">
          <template #icon>
            <icon-desktop />
          </template>
          资产管理
        </a-menu-item>
        
        <!-- 所有用户可见的漏洞管理 -->
        <a-menu-item key="/vulnerabilities">
          <template #icon>
            <icon-bug />
          </template>
          漏洞管理
        </a-menu-item>

        <!-- 管理员、经理和分析师可见的基线扫描 -->
        <a-menu-item key="/baseline-check" v-if="canViewScans">
          <template #icon>
            <icon-check-circle />
          </template>
          基线扫描
        </a-menu-item>

        <!-- 管理员、经理和分析师可见的漏洞扫描 -->
        <a-menu-item key="/vulnerability-scan" v-if="canViewScans">
          <template #icon>
            <icon-bug />
          </template>
          漏洞扫描
        </a-menu-item>

        <!-- 管理员和经理才能看到的菜单 -->
        <a-menu-item key="/scan-tools" v-if="hasAdminAccess">
          <template #icon>
            <icon-tool />
          </template>
          工具管理
        </a-menu-item>

        <a-menu-item key="/users" v-if="hasAdminAccess">
          <template #icon>
            <icon-user />
          </template>
          用户管理
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <!-- 主内容区域 -->
    <a-layout class="layout-main">
      <!-- 头部 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <a-breadcrumb>
            <a-breadcrumb-item>VulnArk</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <a-space size="large">
            <!-- 主题切换 -->
            <a-button
              type="text"
              :icon="isDark ? IconSun : IconMoon"
              @click="toggleTheme"
            />
            
            <!-- 用户下拉菜单 -->
            <a-dropdown>
              <div class="user-info">
                <a-avatar :size="32" class="user-avatar">
                  {{ userStore.user?.fullName?.charAt(0) || 'U' }}
                </a-avatar>
                <span v-if="userStore.user" class="user-name">
                  {{ userStore.user.fullName }}
                </span>
              </div>
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
          </a-space>
        </div>
      </a-layout-header>
      
      <!-- 内容区域 -->
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconDashboard,
  IconDesktop,
  IconBug,
  IconCheckCircle,
  IconTool,
  IconUser,
  IconSettings,
  IconPoweroff,
  IconSun,
  IconMoon
} from '@arco-design/web-vue/es/icon'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 侧边栏状态
const collapsed = ref(false)
const selectedKeys = ref([route.path])
const openKeys = ref(['discovery'])

// 主题状态
const isDark = ref(false)

// 当前页面标题
const currentPageTitle = computed(() => {
  return route.meta?.title as string || '首页'
})

// 用户信息
const userStore = computed(() => authStore)

// 判断是否有管理员权限
const hasAdminAccess = computed(() => {
  return userStore.value.user?.role === UserRole.ADMIN || userStore.value.user?.role === UserRole.MANAGER
})

// 判断是否可以查看资产管理
const canViewAssets = computed(() => {
  const role = userStore.value.user?.role
  return role === UserRole.ADMIN || role === UserRole.MANAGER || role === UserRole.ANALYST
})

// 判断是否可以查看扫描功能
const canViewScans = computed(() => {
  const role = userStore.value.user?.role
  return role === UserRole.ADMIN || role === UserRole.MANAGER || role === UserRole.ANALYST
})

// 判断是否可以查看仪表盘
const canViewDashboard = computed(() => {
  const role = userStore.value.user?.role
  return role === UserRole.ADMIN || role === UserRole.MANAGER || role === UserRole.ANALYST
})

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
  }
)

// 侧边栏折叠
const onCollapse = (value: boolean) => {
  collapsed.value = value
}

// 菜单点击
const onMenuClick = (key: string) => {
  router.push(key)
}

// 主题切换
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute(
    'data-theme',
    isDark.value ? 'dark' : 'light'
  )
}

// 退出登录
const handleLogout = () => {
  authStore.logout()
  Message.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-sider {
  background: var(--surface) !important;
  border-right: 1px solid var(--border);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: white;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.2rem;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.layout-menu {
  border-right: none;
  background: transparent !important;
}

.layout-menu :deep(.arco-menu-item) {
  margin: 4px 8px;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.layout-menu :deep(.arco-menu-item:hover) {
  background: var(--primary-50) !important;
  color: var(--primary-600) !important;
}

.layout-menu :deep(.arco-menu-item-selected) {
  background: var(--primary-100) !important;
  color: var(--primary-700) !important;
  font-weight: 600;
}

.layout-menu :deep(.arco-menu-sub) {
  background: transparent !important;
}

.layout-menu :deep(.arco-menu-sub .arco-menu-item) {
  margin: 2px 16px;
  padding-left: 24px;
}

.layout-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-lg);
  transition: var(--transition-fast);
}

.user-info:hover {
  background: var(--surface-hover);
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: white;
  font-weight: 600;
}

.user-name {
  color: var(--text-primary);
  font-weight: 500;
}

.layout-content {
  background: var(--background);
  padding: 0;
  overflow-y: auto;
  min-height: calc(100vh - 64px);
}

/* 暗色主题适配 */
[data-theme="dark"] .layout-sider {
  background: var(--gray-900) !important;
  border-right-color: var(--gray-800);
}

[data-theme="dark"] .logo {
  border-bottom-color: var(--gray-800);
}

[data-theme="dark"] .logo-text {
  color: var(--text-primary);
}

[data-theme="dark"] .layout-header {
  background: var(--surface);
  border-bottom-color: var(--border);
}

[data-theme="dark"] .layout-menu :deep(.arco-menu-item) {
  color: var(--gray-300);
}

[data-theme="dark"] .layout-menu :deep(.arco-menu-item:hover) {
  background: var(--gray-800) !important;
  color: var(--primary-400) !important;
}

[data-theme="dark"] .layout-menu :deep(.arco-menu-item-selected) {
  background: var(--primary-800) !important;
  color: var(--primary-300) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 var(--spacing-md);
  }
  
  .user-name {
    display: none;
  }
}
</style> 