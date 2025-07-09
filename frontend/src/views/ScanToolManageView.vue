<template>
  <div class="scan-tool-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">扫描工具管理</h1>
        <p class="page-description">管理和配置漏洞扫描工具，支持自动下载和版本更新</p>
      </div>
      <div class="header-right">
        <a-button type="outline" @click="refreshTools" :loading="loading">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
      </div>
    </div>

    <!-- 可用工具列表 -->
    <div class="tools-section">
      <a-card title="可下载的扫描工具" :loading="loading">
        <div class="tools-grid">
          <div v-for="tool in availableTools" :key="tool.id" class="tool-card">
            <a-card>
              <div class="tool-header">
                <div class="tool-info">
                  <h3 class="tool-name">{{ tool.name }}</h3>
                  <p class="tool-description">{{ tool.description }}</p>
                </div>
                <div class="tool-status">
                  <a-tag :color="getToolStatusColor(tool)">
                    {{ getToolStatusText(tool) }}
                  </a-tag>
                </div>
              </div>

              <div class="tool-content">
                <!-- 版本选择 -->
                <div class="version-selector">
                  <a-form-item label="版本">
                    <a-select 
                      v-model="getToolSelection(tool.id).version" 
                      placeholder="选择版本"
                    >
                      <a-option v-for="version in tool.versions" :key="version" :value="version">
                        {{ version }}
                      </a-option>
                    </a-select>
                  </a-form-item>
                </div>

                <!-- 平台选择 -->
                <div class="platform-selector">
                  <a-form-item label="平台">
                    <a-select 
                      v-model="getToolSelection(tool.id).platform" 
                      placeholder="选择平台"
                    >
                      <a-option v-for="platform in tool.platforms" :key="platform" :value="platform">
                        {{ getPlatformName(platform) }}
                      </a-option>
                    </a-select>
                  </a-form-item>
                </div>

                <!-- 架构选择 -->
                <div class="arch-selector">
                  <a-form-item label="架构">
                    <a-select 
                      v-model="getToolSelection(tool.id).arch" 
                      placeholder="选择架构"
                    >
                      <a-option v-for="arch in tool.architectures" :key="arch" :value="arch">
                        {{ getArchName(arch) }}
                      </a-option>
                    </a-select>
                  </a-form-item>
                </div>

                <!-- 操作按钮 -->
                <div class="tool-actions">
                  <a-button 
                    type="primary"
                    @click="getDownloadLink(tool.id)"
                    :loading="isOperating(tool.id)"
                  >
                    获取下载链接
                  </a-button>
                  
                  <a-button 
                    type="outline"
                    @click="openToolWebsite(tool.website)"
                  >
                    官网
                  </a-button>
                </div>
              </div>
            </a-card>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getAvailableTools, getToolDownloadUrl, checkToolStatus } from '@/api/scanTool'

// 响应式数据
const loading = ref(false)
const availableTools = ref<any[]>([])
const operatingTools = ref<Set<string>>(new Set())
const toolSelections = reactive<Record<string, {
  version: string,
  platform: string,
  arch: string
}>>({})

// 生命周期
onMounted(() => {
  loadAvailableTools()
})

// 方法
const loadAvailableTools = async () => {
  loading.value = true
  try {
    const response = await getAvailableTools()
    if (response && response.data && response.data.code === 200) {
      availableTools.value = response.data.data || []
      
      // 初始化工具选择
      availableTools.value.forEach(tool => {
        if (tool.versions && tool.versions.length > 0) {
          const selection = getToolSelection(tool.id)
          selection.version = tool.versions[0]
        }
      })
    } else {
      const message = response?.data?.message || '获取可用工具列表失败'
      Message.error(message)
      availableTools.value = []
    }
  } catch (error) {
    console.error('加载可用工具列表失败:', error)
    Message.error('加载可用工具列表失败，请检查网络连接')
    availableTools.value = []
  } finally {
    loading.value = false
  }
}

const refreshTools = () => {
  loadAvailableTools()
}

const getDownloadLink = async (toolId: string) => {
  operatingTools.value.add(toolId)
  try {
    const selection = getToolSelection(toolId)
    
    const response = await getToolDownloadUrl(
      toolId,
      selection.version,
      selection.platform,
      selection.arch
    )
    
    if (response && response.data && response.data.code === 200) {
      const downloadUrl = response.data.data
      if (downloadUrl) {
        // 打开下载链接
        window.open(downloadUrl, '_blank')
        Message.success('已获取下载链接')
      } else {
        Message.error('获取下载链接失败')
      }
    } else {
      const message = response?.data?.message || '获取下载链接失败'
      Message.error(message)
    }
  } catch (error) {
    console.error('获取下载链接失败:', error)
    Message.error('获取下载链接失败')
  } finally {
    operatingTools.value.delete(toolId)
  }
}

const openToolWebsite = (website: string) => {
  if (website) {
    window.open(website, '_blank')
  }
}

// 工具方法
const detectPlatform = () => {
  const platform = navigator.platform.toLowerCase()
  if (platform.includes('win')) return 'windows'
  if (platform.includes('mac')) return 'darwin'
  if (platform.includes('linux')) return 'linux'
  return 'linux' // 默认
}

const detectArch = () => {
  const userAgent = navigator.userAgent.toLowerCase()
  if (userAgent.includes('arm') || userAgent.includes('aarch64')) return 'arm64'
  return 'amd64' // 默认
}

const getPlatformName = (platform: string) => {
  const names = {
    'windows': 'Windows',
    'linux': 'Linux',
    'darwin': 'macOS'
  }
  return names[platform] || platform
}

const getArchName = (arch: string) => {
  const names = {
    'amd64': 'x86_64 (Intel/AMD)',
    'arm64': 'ARM64'
  }
  return names[arch] || arch
}

const getToolStatusColor = (tool: any) => {
  return 'blue'
}

const getToolStatusText = (tool: any) => {
  return '可下载'
}

const isOperating = (toolId: string) => {
  return operatingTools.value.has(toolId)
}

const getToolSelection = (toolId: string) => {
  if (!toolSelections[toolId]) {
    toolSelections[toolId] = {
      version: 'latest',
      platform: detectPlatform(),
      arch: detectArch()
    }
  }
  return toolSelections[toolId]
}
</script>

<style scoped>
.scan-tool-manage {
  padding: var(--spacing-lg);
  background: var(--color-bg-1);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-xl);
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-description {
  color: var(--color-text-3);
  font-size: 1rem;
  margin: 0;
}

.header-right {
  display: flex;
  gap: var(--spacing-md);
}

.tools-section {
  margin-bottom: var(--spacing-xl);
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: var(--spacing-lg);
}

.tool-card {
  height: 100%;
}

.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 var(--spacing-xs) 0;
}

.tool-description {
  color: var(--color-text-3);
  font-size: 0.875rem;
  margin: 0;
  line-height: 1.4;
}

.tool-status {
  margin-left: var(--spacing-md);
}

.tool-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.tool-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }
}
</style>
