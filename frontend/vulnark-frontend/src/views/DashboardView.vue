<template>
  <div class="dashboard dashboard-enhanced">
    <!-- 页面头部 -->
    <div class="dashboard-header dashboard-header-enhanced">
      <div class="header-content">
        <h1 class="dashboard-title gradient-text">
          <icon-dashboard class="title-icon" />
          系统仪表盘
        </h1>
        <p class="dashboard-subtitle">实时监控系统安全状态</p>
      </div>
      <a-space class="header-actions">
        <a-button type="outline" @click="refreshData" :loading="loading" class="btn-enhanced">
          <template #icon>
            <icon-refresh />
          </template>
          刷新数据
        </a-button>
        <a-tag color="green" v-if="lastUpdateTime" class="update-tag">
          <icon-clock-circle />
          最后更新: {{ formatDateTime(lastUpdateTime) }}
        </a-tag>
      </a-space>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="[24, 24]" class="stats-row">
      <!-- 漏洞统计 -->
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="stat-card-enhanced vulnerability-card-enhanced" :class="{ 'loading-enhanced': loading }">
          <div class="stat-icon vulnerability-icon">
            <icon-bug />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardStats.vulnerabilities?.total || 0 }}</div>
            <div class="stat-title">总漏洞数</div>
            <div class="stat-detail-enhanced">
              <span class="status-tag-critical">严重: {{ dashboardStats.vulnerabilities?.critical || 0 }}</span>
              <span class="status-tag-high">高危: {{ dashboardStats.vulnerabilities?.high || 0 }}</span>
            </div>
          </div>
          <div class="stat-trend">
            <icon-arrow-up v-if="(dashboardStats.vulnerabilities?.total || 0) > 0" class="trend-up" />
            <icon-minus v-else class="trend-stable" />
          </div>
        </div>
      </a-col>

      <!-- 项目统计 -->
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="stat-card-enhanced project-card-enhanced" :class="{ 'loading-enhanced': loading }">
          <div class="stat-icon project-icon">
            <icon-folder />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardStats.projects?.total || 0 }}</div>
            <div class="stat-title">项目总数</div>
            <div class="stat-detail-enhanced">
              <span class="status-tag-low">活跃: {{ dashboardStats.projects?.active || 0 }}</span>
              <span class="status-tag-info">完成: {{ dashboardStats.projects?.completed || 0 }}</span>
            </div>
          </div>
          <div class="stat-trend">
            <icon-arrow-up v-if="(dashboardStats.projects?.total || 0) > 0" class="trend-up" />
            <icon-minus v-else class="trend-stable" />
          </div>
        </div>
      </a-col>

      <!-- 资产统计 -->
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="stat-card-enhanced asset-card-enhanced" :class="{ 'loading-enhanced': loading }">
          <div class="stat-icon asset-icon">
            <icon-desktop />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardStats.assets?.total || 0 }}</div>
            <div class="stat-title">资产总数</div>
            <div class="stat-detail-enhanced">
              <span class="status-tag-low">在线: {{ dashboardStats.assets?.online || 0 }}</span>
              <span class="status-tag-critical">离线: {{ dashboardStats.assets?.offline || 0 }}</span>
            </div>
          </div>
          <div class="stat-trend">
            <icon-arrow-up v-if="(dashboardStats.assets?.total || 0) > 0" class="trend-up" />
            <icon-minus v-else class="trend-stable" />
          </div>
        </div>
      </a-col>

      <!-- 用户统计 -->
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="stat-card-enhanced user-card-enhanced" :class="{ 'loading-enhanced': loading }">
          <div class="stat-icon user-icon">
            <icon-user />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardStats.users?.total || 0 }}</div>
            <div class="stat-title">用户总数</div>
            <div class="stat-detail-enhanced">
              <span class="status-tag-low">活跃: {{ dashboardStats.users?.active || 0 }}</span>
              <span class="status-tag-medium">管理员: {{ dashboardStats.users?.admin || 0 }}</span>
            </div>
          </div>
          <div class="stat-trend">
            <icon-arrow-up v-if="(dashboardStats.users?.total || 0) > 0" class="trend-up" />
            <icon-minus v-else class="trend-stable" />
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="[24, 24]" class="charts-row">
      <!-- 漏洞趋势图 -->
      <a-col :xs="24" :sm="24" :md="24" :lg="16" :xl="16">
        <div class="chart-card-enhanced">
          <div class="chart-header">
            <div class="chart-title">
              <icon-bar-chart class="chart-title-icon" />
              漏洞趋势图
            </div>
            <a-select v-model="trendDays" @change="loadVulnerabilityTrends" class="chart-select">
              <a-option :value="7">最近7天</a-option>
              <a-option :value="30">最近30天</a-option>
              <a-option :value="90">最近90天</a-option>
            </a-select>
          </div>
          <div ref="trendChartRef" class="chart-container-enhanced" :class="{ 'loading-enhanced': chartsLoading }"></div>
        </div>
      </a-col>

      <!-- 漏洞严重程度分布 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <div class="chart-card-enhanced">
          <div class="chart-header">
            <div class="chart-title">
              <icon-apps class="chart-title-icon" />
              严重程度分布
            </div>
          </div>
          <div ref="severityChartRef" class="chart-container-enhanced" :class="{ 'loading-enhanced': chartsLoading }"></div>
        </div>
      </a-col>

      <!-- 项目漏洞排行榜 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <a-card title="项目漏洞排行榜（Top 10）" class="chart-card">
          <div ref="projectRankChartRef" class="chart-container" v-loading="chartsLoading"></div>
        </a-card>
      </a-col>

      <!-- 资产状态分布 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <a-card title="资产状态分布" class="chart-card">
          <div ref="assetStatusChartRef" class="chart-container" v-loading="chartsLoading"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近活动和快速操作 -->
    <a-row :gutter="[24, 16]" class="recent-row">
      <!-- 最近活动 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <a-card title="最近活动" class="recent-card">
          <template #extra>
            <a-button type="text" size="small" @click="loadRecentActivities">
              <icon-refresh />
            </a-button>
          </template>
          <a-list :data="recentActivities" :bordered="false" :loading="activitiesLoading">
            <template #item="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.description"
                >
                  <template #avatar>
                    <a-avatar :style="getActivityAvatarStyle(item.type)">
                      <icon-bug v-if="item.type === 'vulnerability'" />
                      <icon-folder v-else-if="item.type === 'project'" />
                      <icon-desktop v-else-if="item.type === 'asset'" />
                      <icon-search v-else-if="item.type === 'scan'" />
                      <icon-info-circle v-else />
                    </a-avatar>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <span class="activity-time">{{ formatRelativeTime(item.timestamp) }}</span>
                </template>
              </a-list-item>
            </template>
            <template #empty>
              <a-empty description="暂无活动记录" />
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 快速操作 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <a-card title="快速操作" class="quick-actions-card">
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <a-button type="primary" block @click="navigateTo('/vulnerabilities')">
                <template #icon>
                  <icon-plus />
                </template>
                新建漏洞
              </a-button>
            </a-col>
            <a-col :span="12">
              <a-button type="outline" block @click="navigateTo('/projects')">
                <template #icon>
                  <icon-plus />
                </template>
                新建项目
              </a-button>
            </a-col>
            <a-col :span="12">
              <a-button type="outline" block @click="navigateTo('/assets')">
                <template #icon>
                  <icon-plus />
                </template>
                新建资产
              </a-button>
            </a-col>
            <a-col :span="12">
              <a-button type="outline" block @click="navigateTo('/scan')">
                <template #icon>
                  <icon-play-arrow />
                </template>
                开始扫描
              </a-button>
            </a-col>
          </a-row>

          <!-- 系统状态 -->
          <a-divider>系统状态</a-divider>
          <div class="system-status" v-if="systemHealth">
            <div class="status-item">
              <span>CPU使用率:</span>
              <a-progress :percent="systemHealth.cpu" :status="getProgressStatus(systemHealth.cpu)" size="small" />
            </div>
            <div class="status-item">
              <span>内存使用率:</span>
              <a-progress :percent="systemHealth.memory" :status="getProgressStatus(systemHealth.memory)" size="small" />
            </div>
            <div class="status-item">
              <span>磁盘使用率:</span>
              <a-progress :percent="systemHealth.disk" :status="getProgressStatus(systemHealth.disk)" size="small" />
            </div>
            <div class="status-item">
              <span>数据库状态:</span>
              <a-tag :color="getDatabaseStatusColor(systemHealth.database)">
                {{ getDatabaseStatusText(systemHealth.database) }}
              </a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconBug,
  IconFolder,
  IconDesktop,
  IconUser,
  IconRefresh,
  IconPlus,
  IconPlayArrow,
  IconSearch,
  IconInfoCircle,
  IconDashboard,
  IconClockCircle,
  IconArrowUp,
  IconMinus,
  IconBarChart,
  IconApps
} from '@arco-design/web-vue/es/icon'
import * as echarts from 'echarts'
import { formatDateTime, formatRelativeTime } from '@/utils/date'
import {
  getDashboardStats,
  getVulnerabilityTrends,
  getVulnerabilitySeverityDistribution,
  getProjectVulnerabilityRanks,
  getAssetStatusDistribution,
  getRecentActivities,
  getSystemHealth,
  type DashboardStats,
  type VulnerabilityTrendData,
  type SeverityDistribution,
  type ProjectVulnerabilityRankData,
  type AssetStatusDistribution,
  type RecentActivity,
  type SystemHealth
} from '@/api/dashboard'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const chartsLoading = ref(false)
const activitiesLoading = ref(false)
const lastUpdateTime = ref<string>('')
const trendDays = ref(30)

// 统计数据
const dashboardStats = ref<DashboardStats>({
  vulnerabilities: {
    total: 0,
    critical: 0,
    high: 0,
    medium: 0,
    low: 0,
    info: 0,
    open: 0,
    inProgress: 0,
    resolved: 0,
    closed: 0,
    reopened: 0
  },
  projects: {
    total: 0,
    active: 0,
    completed: 0,
    archived: 0,
    overdue: 0
  },
  assets: {
    total: 0,
    online: 0,
    offline: 0,
    maintenance: 0,
    high: 0,
    critical: 0
  },
  users: {
    total: 0,
    active: 0,
    inactive: 0,
    admin: 0,
    analyst: 0,
    viewer: 0
  }
})

// 图表数据
const vulnerabilityTrends = ref<VulnerabilityTrendData[]>([])
const severityDistribution = ref<SeverityDistribution[]>([])
const projectRanks = ref<ProjectVulnerabilityRankData[]>([])
const assetStatusData = ref<AssetStatusDistribution[]>([])
const recentActivities = ref<RecentActivity[]>([])
const systemHealth = ref<SystemHealth | null>(null)

// 图表引用
const trendChartRef = ref<HTMLElement>()
const severityChartRef = ref<HTMLElement>()
const projectRankChartRef = ref<HTMLElement>()
const assetStatusChartRef = ref<HTMLElement>()

// 图表实例
let trendChart: echarts.ECharts | null = null
let severityChart: echarts.ECharts | null = null
let projectRankChart: echarts.ECharts | null = null
let assetStatusChart: echarts.ECharts | null = null

// 数据加载方法
const loadDashboardData = async () => {
  try {
    loading.value = true
    const response = await getDashboardStats()
    dashboardStats.value = response.data
    lastUpdateTime.value = new Date().toISOString()
  } catch (error) {
    console.error('加载仪表板统计数据失败:', error)
    Message.error('加载仪表板数据失败')
  } finally {
    loading.value = false
  }
}

const loadVulnerabilityTrends = async () => {
  try {
    chartsLoading.value = true
    const response = await getVulnerabilityTrends(trendDays.value)
    vulnerabilityTrends.value = response.data
    await nextTick()
    initTrendChart()
  } catch (error) {
    console.error('加载漏洞趋势数据失败:', error)
    Message.error('加载趋势数据失败')
  } finally {
    chartsLoading.value = false
  }
}

const loadSeverityDistribution = async () => {
  try {
    const response = await getVulnerabilitySeverityDistribution()
    severityDistribution.value = response.data
    await nextTick()
    initSeverityChart()
  } catch (error) {
    console.error('加载严重程度分布数据失败:', error)
  }
}

const loadProjectRanks = async () => {
  try {
    const response = await getProjectVulnerabilityRanks(10)
    projectRanks.value = response.data
    await nextTick()
    initProjectRankChart()
  } catch (error) {
    console.error('加载项目排行数据失败:', error)
  }
}

const loadAssetStatusData = async () => {
  try {
    const response = await getAssetStatusDistribution()
    assetStatusData.value = response.data
    await nextTick()
    initAssetStatusChart()
  } catch (error) {
    console.error('加载资产状态数据失败:', error)
    // 设置默认数据以防止图表初始化失败
    assetStatusData.value = []
    Message.error('加载资产状态数据失败，请稍后重试')
  }
}

const loadRecentActivities = async () => {
  try {
    activitiesLoading.value = true
    const response = await getRecentActivities(10)
    recentActivities.value = response.data
  } catch (error) {
    console.error('加载最近活动数据失败:', error)
  } finally {
    activitiesLoading.value = false
  }
}

const loadSystemHealth = async () => {
  try {
    const response = await getSystemHealth()
    systemHealth.value = response.data
  } catch (error) {
    console.error('加载系统健康状态失败:', error)
  }
}

// 图表初始化方法
const initTrendChart = () => {
  if (!trendChartRef.value || vulnerabilityTrends.value.length === 0) return

  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(trendChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['发现漏洞', '解决漏洞']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: vulnerabilityTrends.value.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '发现漏洞',
        type: 'line',
        data: vulnerabilityTrends.value.map(item => item.discovered),
        itemStyle: { color: '#f5222d' },
        smooth: true
      },
      {
        name: '解决漏洞',
        type: 'line',
        data: vulnerabilityTrends.value.map(item => item.resolved),
        itemStyle: { color: '#52c41a' },
        smooth: true
      }
    ]
  }

  trendChart.setOption(option)
}

const initSeverityChart = () => {
  if (!severityChartRef.value || severityDistribution.value.length === 0) return

  if (severityChart) {
    severityChart.dispose()
  }

  severityChart = echarts.init(severityChartRef.value)

  const severityColors = {
    'CRITICAL': '#722ed1',
    'HIGH': '#f5222d',
    'MEDIUM': '#fa8c16',
    'LOW': '#52c41a',
    'INFO': '#1890ff'
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '漏洞严重程度',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: severityDistribution.value.map(item => ({
          value: item.count,
          name: getSeverityLabel(item.severity),
          itemStyle: {
            color: severityColors[item.severity as keyof typeof severityColors] || '#1890ff'
          }
        }))
      }
    ]
  }

  severityChart.setOption(option)
}

const initProjectRankChart = () => {
  if (!projectRankChartRef.value || projectRanks.value.length === 0) return

  if (projectRankChart) {
    projectRankChart.dispose()
  }

  projectRankChart = echarts.init(projectRankChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: projectRanks.value.map(item => item.projectName).reverse()
    },
    series: [
      {
        name: '漏洞数量',
        type: 'bar',
        data: projectRanks.value.map(item => item.vulnerabilityCount).reverse(),
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }

  projectRankChart.setOption(option)
}

const initAssetStatusChart = () => {
  if (!assetStatusChartRef.value || assetStatusData.value.length === 0) return

  if (assetStatusChart) {
    assetStatusChart.dispose()
  }

  assetStatusChart = echarts.init(assetStatusChartRef.value)

  const statusColors = {
    'active': '#52c41a',
    'inactive': '#f5222d',
    'maintenance': '#fa8c16',
    'decommissioned': '#722ed1'
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: '70%',
        data: assetStatusData.value.map(item => ({
          value: item.count,
          name: getAssetStatusLabel(item.status),
          itemStyle: {
            color: statusColors[item.status as keyof typeof statusColors] || '#1890ff'
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  assetStatusChart.setOption(option)
}

// 工具方法
const getSeverityLabel = (severity: string) => {
  const labels = {
    'CRITICAL': '严重',
    'HIGH': '高危',
    'MEDIUM': '中危',
    'LOW': '低危',
    'INFO': '信息'
  }
  return labels[severity as keyof typeof labels] || severity
}

const getAssetStatusLabel = (status: string) => {
  const labels = {
    'active': '活跃',
    'inactive': '非活跃',
    'maintenance': '维护中',
    'decommissioned': '已停用'
  }
  return labels[status as keyof typeof labels] || status
}

const getActivityAvatarStyle = (type: string) => {
  const colors = {
    'vulnerability': { backgroundColor: '#f5222d' },
    'project': { backgroundColor: '#52c41a' },
    'asset': { backgroundColor: '#fa8c16' },
    'scan': { backgroundColor: '#1890ff' }
  }
  return colors[type as keyof typeof colors] || { backgroundColor: '#722ed1' }
}

const getProgressStatus = (percent: number) => {
  if (percent >= 90) return 'danger'
  if (percent >= 70) return 'warning'
  return 'normal'
}

const getDatabaseStatusColor = (status: string) => {
  const colors = {
    'healthy': 'green',
    'warning': 'orange',
    'error': 'red'
  }
  return colors[status as keyof typeof colors] || 'gray'
}

const getDatabaseStatusText = (status: string) => {
  const texts = {
    'healthy': '正常',
    'warning': '警告',
    'error': '错误'
  }
  return texts[status as keyof typeof texts] || '未知'
}

const navigateTo = (path: string) => {
  router.push(path)
}

const refreshData = async () => {
  await Promise.all([
    loadDashboardData(),
    loadVulnerabilityTrends(),
    loadSeverityDistribution(),
    loadProjectRanks(),
    loadAssetStatusData(),
    loadRecentActivities(),
    loadSystemHealth()
  ])
  Message.success('数据刷新成功')
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  trendChart?.resize()
  severityChart?.resize()
  projectRankChart?.resize()
  assetStatusChart?.resize()
}

// 生命周期
onMounted(async () => {
  await loadDashboardData()
  await nextTick()

  // 并行加载图表数据
  Promise.all([
    loadVulnerabilityTrends(),
    loadSeverityDistribution(),
    loadProjectRanks(),
    loadAssetStatusData(),
    loadRecentActivities(),
    loadSystemHealth()
  ])

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  trendChart?.dispose()
  severityChart?.dispose()
  projectRankChart?.dispose()
  assetStatusChart?.dispose()

  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 0;
  width: 100%;
  overflow-x: hidden;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  min-height: 120px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-detail {
  margin-top: 8px;
  display: flex;
  justify-content: space-around;
  font-size: 12px;
}

.stat-detail span {
  padding: 2px 6px;
  border-radius: 4px;
  background: #f7f8fa;
}

.stat-detail .critical {
  color: #722ed1;
  background: #f9f0ff;
}

.stat-detail .high {
  color: #f5222d;
  background: #fff1f0;
}

.stat-detail .active {
  color: #52c41a;
  background: #f6ffed;
}

.stat-detail .completed {
  color: #1890ff;
  background: #e6f7ff;
}

.stat-detail .online {
  color: #52c41a;
  background: #f6ffed;
}

.stat-detail .offline {
  color: #f5222d;
  background: #fff1f0;
}

.stat-detail .admin {
  color: #722ed1;
  background: #f9f0ff;
}

.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  height: 350px;
  min-height: 300px;
}

.chart-container {
  height: 280px;
  width: 100%;
}

.recent-row {
  margin-bottom: 24px;
}

.recent-card {
  height: 450px;
  min-height: 400px;
}

.quick-actions-card {
  height: 450px;
  min-height: 400px;
}

.activity-time {
  font-size: 12px;
  color: #86909c;
}

.system-status {
  margin-top: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-item span:first-child {
  font-size: 14px;
  color: #4e5969;
  min-width: 80px;
}

.status-item .arco-progress {
  flex: 1;
  margin-left: 12px;
}

/* 平板响应式 */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dashboard-header h1 {
    font-size: 20px;
  }

  .stats-row,
  .charts-row,
  .recent-row {
    margin-bottom: 16px;
  }

  .chart-card {
    height: 300px;
    min-height: 250px;
  }

  .chart-container {
    height: 230px;
  }

  .recent-card,
  .quick-actions-card {
    height: 350px;
    min-height: 300px;
  }

  .stat-detail {
    flex-direction: column;
    gap: 4px;
  }
}

/* 手机响应式 */
@media (max-width: 480px) {
  .dashboard-header h1 {
    font-size: 18px;
  }

  .stat-card {
    min-height: 100px;
  }

  .chart-card {
    height: 250px;
    min-height: 200px;
  }

  .chart-container {
    height: 180px;
  }

  .recent-card,
  .quick-actions-card {
    height: 300px;
    min-height: 250px;
  }

  .stats-row,
  .charts-row,
  .recent-row {
    margin-bottom: 12px;
  }

  .stat-detail span {
    font-size: 11px;
    padding: 1px 4px;
  }

  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .status-item .arco-progress {
    width: 100%;
    margin-left: 0;
  }
}

/* 超小屏幕 */
@media (max-width: 320px) {
  .stat-card {
    min-height: 80px;
  }

  .chart-card {
    height: 200px;
  }

  .chart-container {
    height: 130px;
  }

  .recent-card,
  .quick-actions-card {
    height: 250px;
  }

  .dashboard-header {
    margin-bottom: 16px;
  }
}

/* 增强样式 */
.dashboard-enhanced {
  background: var(--bg-secondary);
}

.dashboard-header-enhanced {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
  padding: var(--spacing-xl);
}

.dashboard-header-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.dashboard-title {
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.title-icon {
  font-size: 32px;
  color: var(--primary-color);
  animation: pulse 2s infinite;
}

.dashboard-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 400;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.update-tag {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  color: var(--status-success);
}

/* 统计卡片增强样式 */
.stat-card-enhanced {
  position: relative;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  min-height: 140px;
}

.stat-card-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.stat-card-enhanced:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-4px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--text-white);
  flex-shrink: 0;
}

.vulnerability-icon {
  background: linear-gradient(135deg, var(--security-critical), var(--security-high));
}

.project-icon {
  background: linear-gradient(135deg, var(--status-success), var(--security-low));
}

.asset-icon {
  background: linear-gradient(135deg, var(--security-medium), var(--security-high));
}

.user-icon {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-title {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-detail-enhanced {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-tertiary);
}

.trend-up {
  color: var(--status-success);
  font-size: 16px;
}

.trend-stable {
  color: var(--text-secondary);
  font-size: 16px;
}

/* 图表卡片增强样式 */
.chart-card-enhanced {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.chart-card-enhanced:hover {
  box-shadow: var(--shadow-lg);
}

.chart-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-tertiary);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.chart-title-icon {
  font-size: 18px;
  color: var(--primary-color);
}

.chart-select {
  min-width: 120px;
}

.chart-container-enhanced {
  height: 300px;
  width: 100%;
  padding: var(--spacing-lg);
}
</style>
