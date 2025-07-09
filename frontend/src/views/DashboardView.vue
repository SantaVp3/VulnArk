<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.color }">
          <component :is="stat.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.changeType" v-if="stat.change">
            <icon-arrow-up v-if="stat.changeType === 'increase'" />
            <icon-arrow-down v-if="stat.changeType === 'decrease'" />
            {{ stat.change }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 漏洞趋势图 -->
      <div class="chart-card trend-card">
        <div class="chart-header">
          <h3>漏洞趋势</h3>
          <a-radio-group v-model="trendPeriod" type="button" size="small">
            <a-radio value="7d">7天</a-radio>
            <a-radio value="30d">30天</a-radio>
            <a-radio value="90d">90天</a-radio>
          </a-radio-group>
        </div>
        <div class="chart-content" ref="trendChartRef"></div>
      </div>

      <!-- 资产状态 -->
      <div class="chart-card asset-card">
        <div class="chart-header">
          <h3>资产状态</h3>
        </div>
        <div class="chart-content" ref="assetChartRef"></div>
      </div>

      <!-- 严重程度分布 -->
      <div class="chart-card severity-card">
        <div class="chart-header">
          <h3>严重程度分布</h3>
        </div>
        <div class="chart-content" ref="severityChartRef"></div>
      </div>

      <!-- 最近活动 -->
      <div class="activity-card">
        <div class="chart-header">
          <h3>最近活动</h3>
          <a-link>查看全部</a-link>
        </div>
        <div class="activity-list">
          <div v-if="activities.length === 0" class="no-activities">
            <p>暂无最近活动</p>
          </div>
          <div v-else v-for="activity in activities" :key="activity.id" class="activity-item">
            <div class="activity-icon" :class="activity.type">
              <component :is="activity.icon || IconBug" />
            </div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title || activity.description }}</div>
              <div class="activity-time">{{ formatTime(activity.time || activity.createdTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h3>快速操作</h3>
      <div class="actions-grid">
        <a-button
          v-for="action in quickActions"
          :key="action.key"
          class="action-button"
          size="large"
          @click="action.handler"
        >
          <template #icon>
            <component :is="action.icon" />
          </template>
          {{ action.label }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  IconBug,
  IconDesktop,
  IconScan,
  IconUser,
  IconArrowUp,
  IconArrowDown,
  IconSettings,
  IconRefresh
} from '@arco-design/web-vue/es/icon'
import { 
  getDashboardStats, 
  getVulnerabilityTrend, 
  getSeverityDistribution,
  getAssetStatusDistribution,
  getRecentActivities,
  type DashboardStats,
  type VulnerabilityTrendData,
  type SeverityDistribution,
  type AssetStatusDistribution
} from '@/api/dashboard'
import dayjs from 'dayjs'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

// 图表引用
const trendChartRef = ref<HTMLElement | null>(null)
const severityChartRef = ref<HTMLElement | null>(null)
const assetChartRef = ref<HTMLElement | null>(null)

// 数据状态
const loading = ref(false)
const trendPeriod = ref('30d')

// 统计数据
const stats = ref([
  {
    key: 'vulnerabilities',
    label: '总漏洞数',
    value: '0',
    change: '0%',
    changeType: 'increase',
    color: 'linear-gradient(135deg, #ef4444, #dc2626)',
    icon: IconBug
  },
  {
    key: 'assets',
    label: '资产数量',
    value: '0',
    change: '0%',
    changeType: 'increase',
    color: 'linear-gradient(135deg, #3b82f6, #2563eb)',
    icon: IconDesktop
  },
  {
    key: 'scans',
    label: '扫描任务',
    value: '0',
    change: '0%',
    changeType: 'increase',
    color: 'linear-gradient(135deg, #10b981, #059669)',
    icon: IconScan
  },
  {
    key: 'users',
    label: '用户数量',
    value: '0',
    change: '0%',
    changeType: 'increase',
    color: 'linear-gradient(135deg, #f59e0b, #d97706)',
    icon: IconUser
  }
])

// 最近活动
const activities = ref([])

// 图表实例
let trendChart: echarts.ECharts | null = null
let severityChart: echarts.ECharts | null = null
let assetChart: echarts.ECharts | null = null

// 快速操作
const quickActions = [
  {
    key: 'add-vulnerability',
    label: '新增漏洞',
    icon: IconBug,
    handler: () => router.push('/vulnerabilities?action=add')
  },
  {
    key: 'add-asset',
    label: '添加资产',
    icon: IconDesktop,
    handler: () => router.push('/assets?action=add')
  },
  {
    key: 'start-scan',
    label: '开始扫描',
    icon: IconScan,
    handler: () => router.push('/vulnerability-scan?action=new')
  },
  {
    key: 'system-settings',
    label: '系统设置',
    icon: IconSettings,
    handler: () => router.push('/settings')
  }
]

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '未知时间'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 生命周期钩子
onMounted(() => {
  // 简单加载数据，不使用复杂的Promise.all
  loadBasicData()
})

// 在组件卸载前清理图表实例
onBeforeUnmount(() => {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  
  if (severityChart) {
    severityChart.dispose()
    severityChart = null
  }
  
  if (assetChart) {
    assetChart.dispose()
    assetChart = null
  }
})

// 简化的数据加载函数
const loadBasicData = async () => {
  loading.value = true
  
  try {
    // 加载基本统计数据
    const statsResponse = await getDashboardStats()
    if (statsResponse?.data?.data) {
      updateStatsData(statsResponse.data.data)
    }
    
    // 加载活动数据
    const activitiesResponse = await getRecentActivities()
    if (activitiesResponse?.data?.data) {
      activities.value = activitiesResponse.data.data
    }
    
    // 加载并渲染图表（使用setTimeout避免立即渲染）
    setTimeout(async () => {
      try {
        // 加载趋势数据
        const days = trendPeriod.value === '7d' ? 7 : trendPeriod.value === '30d' ? 30 : 90
        const trendResponse = await getVulnerabilityTrend(days)
        if (trendResponse?.data?.data && Array.isArray(trendResponse.data.data) && trendChartRef.value) {
          renderTrendChart(trendResponse.data.data)
        }
        
        // 加载严重程度数据
        const severityResponse = await getSeverityDistribution()
        if (severityResponse?.data?.data && Array.isArray(severityResponse.data.data) && severityChartRef.value) {
          renderSeverityChart(severityResponse.data.data)
        }
        
        // 加载资产数据
        const assetResponse = await getAssetStatusDistribution()
        if (assetResponse?.data?.data && Array.isArray(assetResponse.data.data) && assetChartRef.value) {
          renderAssetChart(assetResponse.data.data)
        }
      } catch (chartError) {
        console.error('加载图表数据失败:', chartError)
      }
    }, 100)
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    Message.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 更新统计卡片数据
const updateStatsData = (data: DashboardStats) => {
  if (!data) return
  
  // 更新漏洞统计
  if (data.vulnerabilities) {
    const vulnStat = stats.value[0]
    vulnStat.value = data.vulnerabilities.total.toString()
    vulnStat.change = `${data.vulnerabilities.open || 0}%`
  }
  
  // 更新资产统计
  if (data.assets) {
    const assetStat = stats.value[1]
    assetStat.value = data.assets.total.toString()
    assetStat.change = `${data.assets.online || 0}%`
  }
  
  // 更新用户统计
  if (data.users) {
    const userStat = stats.value[3]
    userStat.value = data.users.total.toString()
    userStat.change = `${data.users.active || 0}%`
  }
}

// 获取漏洞严重程度的显示名称
const getSeverityDisplayName = (severity: string) => {
  const names = {
    'INFO': '信息',
    'LOW': '低危',
    'MEDIUM': '中危',
    'HIGH': '高危',
    'CRITICAL': '严重'
  }
  return names[severity] || severity
}

// 获取资产状态的显示名称
const getAssetStatusDisplayName = (status: string) => {
  const names = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'UNKNOWN': '未知'
  }
  return names[status] || status
}

// 渲染趋势图表
const renderTrendChart = (data: VulnerabilityTrendData[]) => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['发现', '修复']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '发现',
        type: 'line',
        data: data.map(item => item.discovered),
        smooth: true
      },
      {
        name: '修复',
        type: 'line',
        data: data.map(item => item.resolved),
        smooth: true
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 渲染严重程度图表
const renderSeverityChart = (data: SeverityDistribution[]) => {
  if (!severityChartRef.value) return
  
  if (severityChart) {
    severityChart.dispose()
  }
  
  severityChart = echarts.init(severityChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '漏洞严重程度',
        type: 'pie',
        radius: '50%',
        data: data.map(item => ({
          value: item.count,
          name: getSeverityDisplayName(item.severity)
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
  
  severityChart.setOption(option)
}

// 渲染资产状态图表
const renderAssetChart = (data: AssetStatusDistribution[]) => {
  if (!assetChartRef.value) return
  
  if (assetChart) {
    assetChart.dispose()
  }
  
  assetChart = echarts.init(assetChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => getAssetStatusDisplayName(item.status))
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '资产数量',
        type: 'bar',
        data: data.map(item => item.count)
      }
    ]
  }
  
  assetChart.setOption(option)
}

// 监听趋势周期变化
watch(trendPeriod, async (newValue) => {
  try {
    const days = newValue === '7d' ? 7 : newValue === '30d' ? 30 : 90
    const trendResponse = await getVulnerabilityTrend(days)
    
    // 使用setTimeout确保DOM更新后再渲染
    setTimeout(() => {
      if (trendResponse?.data?.data && Array.isArray(trendResponse.data.data) && trendChartRef.value) {
        renderTrendChart(trendResponse.data.data)
      }
    }, 100)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--stat-color, #e5e7eb);
  opacity: 0.7;
}

.stat-card:nth-child(1)::before {
  --stat-color: #ef4444;
}

.stat-card:nth-child(2)::before {
  --stat-color: #3b82f6;
}

.stat-card:nth-child(3)::before {
  --stat-color: #10b981;
}

.stat-card:nth-child(4)::before {
  --stat-color: #f59e0b;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.05);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
}

.stat-change {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin-top: 8px;
  font-weight: 500;
}

.stat-change.increase {
  color: #10b981;
}

.stat-change.decrease {
  color: #ef4444;
}

.stat-change.normal {
  color: #f59e0b;
}

.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-areas: 
    "trend asset severity"
    "activity activity activity";
  gap: 16px;
  margin-bottom: 24px;
}

.trend-card {
  grid-area: trend;
  min-height: 300px;
}

.asset-card {
  grid-area: asset;
  min-height: 300px;
}

.severity-card {
  grid-area: severity;
  min-height: 300px;
}

.activity-card {
  grid-area: activity;
  min-height: 200px;
}

.chart-card, .activity-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.chart-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-list {
  flex: 1;
  overflow-y: auto;
  max-height: 180px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #111827;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: #6b7280;
}

.activity-icon.vulnerability {
  background: #ef4444;
}

.activity-icon.asset {
  background: #3b82f6;
}

.activity-icon.user {
  background: #f59e0b;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #6b7280;
}

.no-activities {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
}

.quick-actions {
  margin-bottom: 24px;
}

.quick-actions h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.action-button {
  height: auto;
  padding: 16px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
    grid-template-areas: 
      "trend"
      "asset"
      "severity"
      "activity";
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 