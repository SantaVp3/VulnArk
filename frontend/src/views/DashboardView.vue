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
          <div class="stat-change" :class="stat.changeType">
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
      <div class="chart-card">
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

      <!-- 严重程度分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>严重程度分布</h3>
        </div>
        <div class="chart-content" ref="severityChartRef"></div>
      </div>

      <!-- 资产状态 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>资产状态</h3>
        </div>
        <div class="chart-content" ref="assetChartRef"></div>
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
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  IconBug,
  IconDesktop,
  IconScan,
  IconUser,
  IconArrowUp,
  IconArrowDown,
  IconPlus,
  IconSearch,
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
const trendChartRef = ref<HTMLElement>()
const severityChartRef = ref<HTMLElement>()
const assetChartRef = ref<HTMLElement>()

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
    label: '资产总数',
    value: '0',
    change: '0%',
    changeType: 'increase',
    color: 'linear-gradient(135deg, #3b82f6, #2563eb)',
    icon: IconDesktop
  },
  {
    key: 'projects',
    label: '项目总数',
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

// 图表数据
const trendData = ref<VulnerabilityTrendData[]>([])
const severityData = ref<SeverityDistribution[]>([])
const assetTypeData = ref<AssetStatusDistribution[]>([])

// 最近活动
const activities = ref([])

// 快速操作
const quickActions = [
  {
    key: 'new-scan',
    label: '新建扫描',
    icon: IconPlus,
    handler: () => router.push('/scans')
  },
  {
    key: 'asset-discovery',
    label: '资产发现',
    icon: IconSearch,
    handler: () => router.push('/asset-discovery')
  },
  {
    key: 'vulnerability-review',
    label: '漏洞审核',
    icon: IconBug,
    handler: () => router.push('/vulnerabilities')
  },
  {
    key: 'system-settings',
    label: '系统设置',
    icon: IconSettings,
    handler: () => {}
  }
]

// 加载仪表盘统计数据
const loadDashboardData = async () => {
  try {
    const data = await getDashboardStats()
    
    // 更新统计卡片数据
    stats.value[0].value = data.vulnerabilities.total.toLocaleString()
    stats.value[1].value = data.assets.total.toLocaleString()
    stats.value[2].value = data.projects.total.toLocaleString()
    stats.value[3].value = data.users.total.toLocaleString()
    
    return data
  } catch (error) {
    console.error('加载仪表盘统计数据失败:', error)
    Message.error('加载统计数据失败')
  }
}

// 加载漏洞趋势数据
const loadVulnerabilityTrends = async () => {
  try {
    const days = trendPeriod.value === '7d' ? 7 : trendPeriod.value === '30d' ? 30 : 90
    const data = await getVulnerabilityTrend(days)
    trendData.value = data
    return data
  } catch (error) {
    console.error('加载漏洞趋势数据失败:', error)
    return []
  }
}

// 加载严重程度分布数据
const loadSeverityData = async () => {
  try {
    const data = await getSeverityDistribution()
    severityData.value = data
    return data
  } catch (error) {
    console.error('加载严重程度分布数据失败:', error)
    return []
  }
}

// 加载资产类型分布数据
const loadAssetTypeData = async () => {
  try {
    const data = await getAssetStatusDistribution()
    assetTypeData.value = data
    return data
  } catch (error) {
    console.error('加载资产类型分布数据失败:', error)
    return []
  }
}

// 加载最近活动数据
const loadRecentActivitiesData = async () => {
  try {
    const data = await getRecentActivities(10)
    activities.value = data || []
    return data
  } catch (error) {
    console.error('加载最近活动数据失败:', error)
    activities.value = []
    return []
  }
}

// 格式化时间
const formatTime = (time: Date | string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  
  // 使用真实数据
  const dates = trendData.value.map(item => dayjs(item.date).format('MM-DD'))
  const discoveredData = trendData.value.map(item => item.discovered)
  const resolvedData = trendData.value.map(item => item.resolved)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['新增漏洞', '修复漏洞']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['暂无数据']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增漏洞',
        type: 'line',
        smooth: true,
        data: discoveredData.length > 0 ? discoveredData : [0],
        itemStyle: {
          color: '#ef4444'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
            { offset: 1, color: 'rgba(239, 68, 68, 0)' }
          ])
        }
      },
      {
        name: '修复漏洞',
        type: 'line',
        smooth: true,
        data: resolvedData.length > 0 ? resolvedData : [0],
        itemStyle: {
          color: '#10b981'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0)' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => chart.resize())
}

// 初始化严重程度图表
const initSeverityChart = () => {
  if (!severityChartRef.value) return
  
  const chart = echarts.init(severityChartRef.value)
  
  const data = severityData.value && severityData.value.length > 0 ? 
    severityData.value.map(item => ({
      value: item.count,
      name: getSeverityDisplayName(item.severity),
      itemStyle: { color: getSeverityColor(item.severity) }
    })) : [
      { value: 1, name: '暂无数据', itemStyle: { color: '#e5e7eb' } }
    ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
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
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  chart.setOption(option)
  
  window.addEventListener('resize', () => chart.resize())
}

// 获取严重程度显示名称
const getSeverityDisplayName = (severity: string) => {
  const nameMap: Record<string, string> = {
    'CRITICAL': '严重',
    'HIGH': '高危',
    'MEDIUM': '中危',
    'LOW': '低危',
    'INFO': '信息'
  }
  return nameMap[severity] || severity
}

// 获取严重程度颜色
const getSeverityColor = (severity: string) => {
  const colorMap: Record<string, string> = {
    'CRITICAL': '#ef4444',
    'HIGH': '#f59e0b',
    'MEDIUM': '#3b82f6',
    'LOW': '#10b981',
    'INFO': '#6b7280'
  }
  return colorMap[severity] || '#6b7280'
}

// 初始化资产状态图表
const initAssetChart = () => {
  if (!assetChartRef.value) return
  
  const chart = echarts.init(assetChartRef.value)
  
  const data = assetTypeData.value && assetTypeData.value.length > 0 ? {
    categories: assetTypeData.value.map(item => getAssetStatusDisplayName(item.status)),
    values: assetTypeData.value.map(item => item.count)
  } : {
    categories: ['暂无数据'],
    values: [0]
  }
  
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
      type: 'category',
      data: data.categories
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '资产数量',
        type: 'bar',
        data: data.values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#1d4ed8' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
  
  window.addEventListener('resize', () => chart.resize())
}

// 获取资产状态显示名称
const getAssetStatusDisplayName = (status: string) => {
  const nameMap: Record<string, string> = {
    'active': '在线',
    'inactive': '离线',
    'maintenance': '维护中',
    'decommissioned': '已停用'
  }
  return nameMap[status] || status
}

// 加载所有数据
const loadAllData = async () => {
  try {
    loading.value = true
    await Promise.all([
      loadDashboardData(),
      loadVulnerabilityTrends(),
      loadSeverityData(),
      loadAssetTypeData(),
      loadRecentActivitiesData()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
    Message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新图表
const refreshCharts = () => {
  nextTick(() => {
    initTrendChart()
    initSeverityChart()
    initAssetChart()
  })
}

// 监听时间周期变化
watch(trendPeriod, async () => {
  await loadVulnerabilityTrends()
  refreshCharts()
})

// 监听数据变化
watch([trendData, severityData, assetTypeData], () => {
  refreshCharts()
})

onMounted(async () => {
  await loadAllData()
  refreshCharts()
})
</script>

<style scoped>
.dashboard-container {
  width: 100%;
  padding: var(--spacing-lg);
  min-height: 100vh;
  background: var(--background);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  transition: var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: var(--spacing-xs);
}

.stat-change {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.8rem;
  font-weight: 600;
}

.stat-change.increase {
  color: var(--success-500);
}

.stat-change.decrease {
  color: var(--error-500);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.chart-card,
.activity-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.chart-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-content {
  height: 320px;
}

.activity-list {
  max-height: 320px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.activity-icon.vulnerability {
  background: var(--error-500);
}

.activity-icon.scan {
  background: var(--success-500);
}

.activity-icon.asset {
  background: var(--info-500);
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.activity-time {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.no-activities {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-secondary);
}

.no-activities p {
  margin: 0;
  font-size: 0.9rem;
}

.quick-actions {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.quick-actions h3 {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--spacing-md);
}

.action-button {
  height: 60px;
  border-radius: var(--radius-lg);
  font-weight: 500;
  transition: var(--transition-fast);
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: var(--spacing-sm);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }
  
  .chart-content {
    height: 280px;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm);
  }
}
</style> 