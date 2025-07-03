<template>
  <div class="scan-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">扫描管理</h1>
        <p class="page-description">管理和执行安全扫描任务</p>
      </div>
      <div class="header-right">
        <a-button type="primary">
          <template #icon>
            <icon-plus />
          </template>
          新建扫描
        </a-button>
      </div>
    </div>

    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="总扫描数" :value="125" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="运行中" :value="3" value-style="color: #3b82f6" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="已完成" :value="118" value-style="color: #10b981" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="失败" :value="4" value-style="color: #ef4444" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div class="table-section">
      <a-card>
        <a-table :data="scans" :loading="false">
          <template #columns>
            <a-table-column title="ID" data-index="id" width="80" />
            <a-table-column title="扫描名称" data-index="name" width="200" />
            <a-table-column title="目标" data-index="target" width="200" />
            <a-table-column title="类型" data-index="type" width="120">
              <template #cell="{ record }">
                <a-tag>{{ getTypeLabel(record.type) }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="状态" data-index="status" width="100">
              <template #cell="{ record }">
                <a-tag :color="getStatusColor(record.status)">
                  {{ getStatusLabel(record.status) }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column title="进度" data-index="progress" width="120">
              <template #cell="{ record }">
                <a-progress :percent="record.progress" size="small" />
              </template>
            </a-table-column>
            <a-table-column title="开始时间" data-index="startTime" width="150">
              <template #cell="{ record }">
                {{ formatDate(record.startTime) }}
              </template>
            </a-table-column>
            <a-table-column title="操作" width="200" fixed="right">
              <template #cell="{ record }">
                <a-space size="small">
                  <a-button type="text" size="small">查看</a-button>
                  <a-button 
                    type="text" 
                    size="small" 
                    :disabled="record.status !== 'RUNNING'"
                  >
                    停止
                  </a-button>
                  <a-button type="text" size="small" status="danger">删除</a-button>
                </a-space>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'

const scans = ref([
  {
    id: 1,
    name: '月度安全扫描',
    target: '192.168.1.0/24',
    type: 'VULNERABILITY',
    status: 'COMPLETED',
    progress: 100,
    startTime: new Date(Date.now() - 86400000).toISOString()
  },
  {
    id: 2,
    name: 'Web应用扫描',
    target: 'https://example.com',
    type: 'WEB_APP',
    status: 'RUNNING',
    progress: 65,
    startTime: new Date(Date.now() - 3600000).toISOString()
  }
])

const getTypeLabel = (type: string) => {
  const labels = {
    VULNERABILITY: '漏洞扫描',
    WEB_APP: 'Web应用',
    PORT: '端口扫描',
    COMPLIANCE: '合规检查'
  }
  return labels[type as keyof typeof labels] || type
}

const getStatusColor = (status: string) => {
  const colors = {
    RUNNING: 'blue',
    COMPLETED: 'green',
    FAILED: 'red',
    PENDING: 'orange'
  }
  return colors[status as keyof typeof colors] || 'gray'
}

const getStatusLabel = (status: string) => {
  const labels = {
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败',
    PENDING: '等待中'
  }
  return labels[status as keyof typeof labels] || status
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.scan-page {
  width: 100%;
  padding: var(--spacing-lg);
  min-height: 100vh;
  background: var(--background);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-description {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.stats-section {
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  border-radius: var(--radius-xl);
}

.table-section {
  margin-bottom: var(--spacing-lg);
}
</style> 