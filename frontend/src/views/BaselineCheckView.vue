<template>
  <div class="baseline-scan-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">基线扫描</h1>
        <p class="page-description">执行安全基线扫描和合规性检查</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button @click="handleShowAgentModal">
            <template #icon>
              <icon-robot />
            </template>
            Agent管理
          </a-button>
          <a-button type="primary" @click="showCreateModal = true">
            <template #icon>
              <icon-plus />
            </template>
            创建扫描
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 功能说明 -->
    <div class="feature-notice">
      <a-alert
        type="info"
        message="基线扫描功能"
        description="支持Windows和Linux主流系统版本的安全基线检查，包括系统配置、用户账户、密码策略、服务配置等多个维度的合规性检测。"
        show-icon
        closable
      />
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-card class="stat-card">
        <a-statistic title="总扫描数" :value="statistics.totalScans" />
      </a-card>
      <a-card class="stat-card">
        <a-statistic title="已完成" :value="statistics.completedScans" />
      </a-card>
      <a-card class="stat-card">
        <a-statistic title="执行中" :value="statistics.runningScans" />
      </a-card>
      <a-card class="stat-card">
        <a-statistic
          title="平均合规得分"
          :value="statistics.averageComplianceScore"
          suffix="%"
          :precision="1"
        />
      </a-card>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model="searchForm.scanName"
            placeholder="搜索扫描名称"
            allow-clear
            @press-enter="loadScans"
          >
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model="searchForm.status"
            placeholder="扫描状态"
            allow-clear
            @change="loadScans"
          >
            <a-option value="PENDING">待执行</a-option>
            <a-option value="RUNNING">执行中</a-option>
            <a-option value="COMPLETED">已完成</a-option>
            <a-option value="FAILED">执行失败</a-option>
            <a-option value="CANCELLED">已取消</a-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model="searchForm.scanType"
            placeholder="扫描类型"
            allow-clear
            @change="loadScans"
          >
            <a-option value="WINDOWS_SERVER_2019">Windows Server 2019</a-option>
            <a-option value="WINDOWS_SERVER_2022">Windows Server 2022</a-option>
            <a-option value="WINDOWS_10">Windows 10</a-option>
            <a-option value="WINDOWS_11">Windows 11</a-option>
            <a-option value="UBUNTU_18_04">Ubuntu 18.04</a-option>
            <a-option value="UBUNTU_20_04">Ubuntu 20.04</a-option>
            <a-option value="UBUNTU_22_04">Ubuntu 22.04</a-option>
            <a-option value="CENTOS_7">CentOS 7</a-option>
            <a-option value="CENTOS_8">CentOS 8</a-option>
            <a-option value="RHEL_7">RHEL 7</a-option>
            <a-option value="RHEL_8">RHEL 8</a-option>
            <a-option value="RHEL_9">RHEL 9</a-option>
            <a-option value="DEBIAN_10">Debian 10</a-option>
            <a-option value="DEBIAN_11">Debian 11</a-option>
            <a-option value="SUSE_15">SUSE 15</a-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-button type="primary" @click="loadScans">
            <template #icon>
              <icon-search />
            </template>
            搜索
          </a-button>
        </a-col>
        <a-col :span="4">
          <a-button @click="resetSearch">
            <template #icon>
              <icon-refresh />
            </template>
            重置
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 扫描列表 -->
    <div class="scan-table">
      <a-table
        :data="scans"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #empty>
          <a-empty description="暂无基线扫描记录">
            <template #image>
              <icon-check-circle />
            </template>
            <a-button type="primary" @click="showCreateModal = true">
              创建第一个扫描任务
            </a-button>
          </a-empty>
        </template>
        <template #columns>
          <a-table-column title="扫描名称" data-index="scanName" />
          <a-table-column title="资产" data-index="assetName" />
          <a-table-column title="扫描类型" data-index="scanType">
            <template #cell="{ record }">
              <a-tag>{{ getScanTypeDisplayName(record.scanType) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="status">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusDisplayName(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="合规得分" data-index="complianceScore">
            <template #cell="{ record }">
              <span v-if="record.status === 'COMPLETED'">
                {{ record.complianceScore }}%
              </span>
              <span v-else>-</span>
            </template>
          </a-table-column>
          <a-table-column title="检查项统计" data-index="checks">
            <template #cell="{ record }">
              <div v-if="record.status === 'COMPLETED'" class="check-stats">
                <a-tag color="green">通过: {{ record.passedChecks }}</a-tag>
                <a-tag color="red">失败: {{ record.failedChecks }}</a-tag>
                <a-tag color="orange">警告: {{ record.warningChecks }}</a-tag>
              </div>
              <span v-else>-</span>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createdTime">
            <template #cell="{ record }">
              {{ formatDateTime(record.createdTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="200">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  size="small"
                  @click="viewScanDetails(record)"
                >
                  详情
                </a-button>
                <a-button
                  v-if="record.status === 'PENDING'"
                  size="small"
                  type="primary"
                  @click="executeScan(record.id)"
                >
                  执行
                </a-button>
                <a-button
                  v-if="record.status === 'RUNNING'"
                  size="small"
                  status="warning"
                  @click="cancelScan(record.id)"
                >
                  取消
                </a-button>
                <a-button
                  v-if="record.status === 'COMPLETED' || record.status === 'FAILED'"
                  size="small"
                  @click="rerunScan(record.id)"
                >
                  重新执行
                </a-button>
                <a-popconfirm
                  content="确定要删除这个扫描任务吗？"
                  @ok="deleteScan(record.id)"
                >
                  <a-button size="small" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 创建扫描模态框 -->
    <a-modal
      v-model:visible="showCreateModal"
      title="创建基线扫描"
      @ok="createScan"
      @cancel="resetCreateForm"
      :ok-loading="createLoading"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="扫描名称" required>
          <a-input v-model="createForm.scanName" placeholder="请输入扫描名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model="createForm.description" placeholder="请输入扫描描述" />
        </a-form-item>
        <a-form-item label="目标资产" required>
          <a-select
            v-model="createForm.assetId"
            placeholder="请选择目标资产"
            @focus="loadAssets"
          >
            <a-option
              v-for="asset in assets"
              :key="asset.id"
              :value="asset.id"
            >
              {{ asset.name }} ({{ asset.ipAddress }})
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="扫描类型" required>
          <a-select v-model="createForm.scanType" placeholder="请选择扫描类型">
            <a-optgroup label="Windows">
              <a-option value="WINDOWS_SERVER_2019">Windows Server 2019</a-option>
              <a-option value="WINDOWS_SERVER_2022">Windows Server 2022</a-option>
              <a-option value="WINDOWS_10">Windows 10</a-option>
              <a-option value="WINDOWS_11">Windows 11</a-option>
            </a-optgroup>
            <a-optgroup label="Linux">
              <a-option value="UBUNTU_18_04">Ubuntu 18.04</a-option>
              <a-option value="UBUNTU_20_04">Ubuntu 20.04</a-option>
              <a-option value="UBUNTU_22_04">Ubuntu 22.04</a-option>
              <a-option value="CENTOS_7">CentOS 7</a-option>
              <a-option value="CENTOS_8">CentOS 8</a-option>
              <a-option value="RHEL_7">Red Hat Enterprise Linux 7</a-option>
              <a-option value="RHEL_8">Red Hat Enterprise Linux 8</a-option>
              <a-option value="RHEL_9">Red Hat Enterprise Linux 9</a-option>
              <a-option value="DEBIAN_10">Debian 10</a-option>
              <a-option value="DEBIAN_11">Debian 11</a-option>
              <a-option value="SUSE_15">SUSE Linux Enterprise 15</a-option>
            </a-optgroup>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model="createForm.executeImmediately">
            创建后立即执行
          </a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Agent管理模态框 -->
    <a-modal
      v-model:visible="showAgentModal"
      title="Agent管理"
      :footer="false"
      width="90%"
      :body-style="{ padding: '20px' }"
    >
      <div class="agent-management">
        <!-- Agent统计 -->
        <div class="agent-stats">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-card class="stat-card">
                <a-statistic title="总Agent数" :value="agentStats.totalAgents" />
              </a-card>
            </a-col>
            <a-col :span="6">
              <a-card class="stat-card">
                <a-statistic title="在线Agent" :value="agentStats.onlineAgents" />
              </a-card>
            </a-col>
            <a-col :span="6">
              <a-card class="stat-card">
                <a-statistic title="离线Agent" :value="agentStats.offlineAgents" />
              </a-card>
            </a-col>
            <a-col :span="6">
              <a-card class="stat-card">
                <a-statistic title="异常Agent" :value="agentStats.errorAgents" />
              </a-card>
            </a-col>
          </a-row>
        </div>

        <!-- Agent操作区 -->
        <div class="agent-actions">
          <a-row :gutter="16" style="margin-bottom: 16px;">
            <a-col :span="8">
              <a-input
                v-model="agentSearchForm.keyword"
                placeholder="搜索Agent名称或IP"
                allow-clear
                @press-enter="loadAgents"
              >
                <template #prefix>
                  <icon-search />
                </template>
              </a-input>
            </a-col>
            <a-col :span="4">
              <a-select
                v-model="agentSearchForm.status"
                placeholder="状态筛选"
                allow-clear
                @change="loadAgents"
              >
                <a-option value="ONLINE">在线</a-option>
                <a-option value="OFFLINE">离线</a-option>
                <a-option value="ERROR">异常</a-option>
                <a-option value="MAINTENANCE">维护</a-option>
              </a-select>
            </a-col>
            <a-col :span="4">
              <a-select
                v-model="agentSearchForm.platform"
                placeholder="平台筛选"
                allow-clear
                @change="loadAgents"
              >
                <a-option value="WINDOWS">Windows</a-option>
                <a-option value="LINUX">Linux</a-option>
              </a-select>
            </a-col>
            <a-col :span="4">
              <a-button type="primary" @click="loadAgents">
                <template #icon>
                  <icon-search />
                </template>
                搜索
              </a-button>
            </a-col>
            <a-col :span="4">
              <a-button @click="showAgentDownloadModal = true">
                <template #icon>
                  <icon-download />
                </template>
                下载Agent
              </a-button>
            </a-col>
          </a-row>
        </div>

        <!-- Agent列表 -->
        <div class="agent-table">
          <a-table
            :data="agents"
            :loading="agentLoading"
            :pagination="agentPagination"
            @page-change="handleAgentPageChange"
            @page-size-change="handleAgentPageSizeChange"
          >
            <template #empty>
              <a-empty description="暂无Agent">
                <template #image>
                  <icon-robot />
                </template>
                <a-button type="primary" @click="showAgentDownloadModal = true">
                  下载Agent客户端
                </a-button>
              </a-empty>
            </template>
            <template #columns>
              <a-table-column title="Agent名称" data-index="name" />
              <a-table-column title="主机名" data-index="hostname" />
              <a-table-column title="IP地址" data-index="ipAddress" />
              <a-table-column title="平台" data-index="platform">
                <template #cell="{ record }">
                  <a-tag :color="record.platform === 'WINDOWS' ? 'blue' : 'green'">
                    {{ record.platform }}
                  </a-tag>
                </template>
              </a-table-column>
              <a-table-column title="操作系统" data-index="osVersion" />
              <a-table-column title="状态" data-index="status">
                <template #cell="{ record }">
                  <a-tag :color="getAgentStatusColor(record.status)">
                    {{ getAgentStatusDisplayName(record.status) }}
                  </a-tag>
                </template>
              </a-table-column>
              <a-table-column title="最后心跳" data-index="lastHeartbeat">
                <template #cell="{ record }">
                  {{ formatDateTime(record.lastHeartbeat) }}
                </template>
              </a-table-column>
              <a-table-column title="操作" :width="200">
                <template #cell="{ record }">
                  <a-space>
                    <a-button
                      size="small"
                      @click="viewAgentDetails(record)"
                    >
                      详情
                    </a-button>
                    <a-button
                      size="small"
                      type="primary"
                      @click="createTaskForAgent(record)"
                    >
                      创建任务
                    </a-button>
                    <a-popconfirm
                      content="确定要删除这个Agent吗？"
                      @ok="deleteAgent(record.agentId)"
                    >
                      <a-button size="small" status="danger">删除</a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </div>
    </a-modal>

    <!-- Agent下载模态框 -->
    <a-modal
      v-model:visible="showAgentDownloadModal"
      title="下载Agent客户端"
      :footer="false"
      width="70%"
    >
      <div class="agent-download">
        <a-alert
          type="info"
          message="Agent客户端说明"
          description="Agent客户端用于在目标系统上执行基线检查。请根据目标系统选择对应的版本下载。"
          show-icon
          style="margin-bottom: 20px;"
        />

        <!-- 配置返回地址 -->
        <div class="server-config" style="margin-bottom: 20px;">
          <h3>服务器配置</h3>
          <a-form layout="inline">
            <a-form-item label="服务器地址">
              <a-input
                v-model="serverConfig.url"
                placeholder="http://your-server:8080"
                style="width: 300px;"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="updateServerConfig">
                更新配置
              </a-button>
            </a-form-item>
          </a-form>
          <p style="color: #666; margin-top: 8px;">
            Agent将连接到此地址进行注册和通信。请确保地址可从目标系统访问。
          </p>
        </div>

        <!-- 下载链接 -->
        <div class="download-links">
          <h3>下载Agent客户端</h3>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="Windows版本" class="download-card">
                <div class="download-item">
                  <div class="download-info">
                    <h4>Windows x64</h4>
                    <p>适用于 Windows 7/Server 2008 R2 及以上版本</p>
                  </div>
                  <a-button type="primary" @click="downloadAgent('windows', 'amd64')">
                    <template #icon>
                      <icon-download />
                    </template>
                    下载
                  </a-button>
                </div>
                <a-divider />
                <div class="download-item">
                  <div class="download-info">
                    <h4>Windows x86</h4>
                    <p>适用于 32位 Windows 系统</p>
                  </div>
                  <a-button type="primary" @click="downloadAgent('windows', '386')">
                    <template #icon>
                      <icon-download />
                    </template>
                    下载
                  </a-button>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="Linux版本" class="download-card">
                <div class="download-item">
                  <div class="download-info">
                    <h4>Linux x64</h4>
                    <p>适用于 Ubuntu、CentOS、RHEL 等发行版</p>
                  </div>
                  <a-button type="primary" @click="downloadAgent('linux', 'amd64')">
                    <template #icon>
                      <icon-download />
                    </template>
                    下载
                  </a-button>
                </div>
                <a-divider />
                <div class="download-item">
                  <div class="download-info">
                    <h4>Linux ARM64</h4>
                    <p>适用于 ARM64 架构的 Linux 系统</p>
                  </div>
                  <a-button type="primary" @click="downloadAgent('linux', 'arm64')">
                    <template #icon>
                      <icon-download />
                    </template>
                    下载
                  </a-button>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>

        <!-- 安装说明 -->
        <div class="install-instructions" style="margin-top: 20px;">
          <h3>安装说明</h3>
          <a-tabs>
            <a-tab-pane key="windows" title="Windows">
              <div class="instruction-content">
                <h4>1. 下载并解压</h4>
                <p>下载对应版本的Agent客户端，解压到目标目录。</p>

                <h4>2. 配置Agent</h4>
                <p>编辑 <code>config.yaml</code> 文件，设置服务器地址：</p>
                <pre><code>server:
  url: "{{ serverConfig.url || 'http://your-server:8080' }}"</code></pre>

                <h4>3. 运行Agent</h4>
                <p>以管理员权限运行：</p>
                <pre><code>vulnark-agent.exe</code></pre>

                <h4>4. 安装为服务（可选）</h4>
                <p>运行 <code>install-service.bat</code> 将Agent安装为Windows服务。</p>
              </div>
            </a-tab-pane>
            <a-tab-pane key="linux" title="Linux">
              <div class="instruction-content">
                <h4>1. 下载并解压</h4>
                <pre><code>wget {{ getDownloadUrl('linux', 'amd64') }}
tar -xzf vulnark-agent-linux-amd64.tar.gz
cd vulnark-agent-linux-amd64</code></pre>

                <h4>2. 配置Agent</h4>
                <p>编辑 <code>config.yaml</code> 文件：</p>
                <pre><code>server:
  url: "{{ serverConfig.url || 'http://your-server:8080' }}"</code></pre>

                <h4>3. 运行Agent</h4>
                <pre><code>chmod +x vulnark-agent
sudo ./vulnark-agent</code></pre>

                <h4>4. 安装为服务（可选）</h4>
                <pre><code>sudo ./install-systemd.sh</code></pre>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>
    </a-modal>

    <!-- 扫描详情模态框 -->
    <a-modal
      v-model:visible="showDetailModal"
      title="扫描详情"
      :footer="false"
      width="80%"
    >
      <div v-if="selectedScan">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="扫描名称">
            {{ selectedScan.scanName }}
          </a-descriptions-item>
          <a-descriptions-item label="扫描类型">
            {{ getScanTypeDisplayName(selectedScan.scanType) }}
          </a-descriptions-item>
          <a-descriptions-item label="目标资产">
            {{ selectedScan.assetName }} ({{ selectedScan.assetIpAddress }})
          </a-descriptions-item>
          <a-descriptions-item label="扫描状态">
            <a-tag :color="getStatusColor(selectedScan.status)">
              {{ getStatusDisplayName(selectedScan.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="开始时间">
            {{ formatDateTime(selectedScan.startTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="结束时间">
            {{ formatDateTime(selectedScan.endTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="合规得分">
            <span v-if="selectedScan.status === 'COMPLETED'">
              {{ selectedScan.complianceScore }}%
            </span>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="检查项统计">
            <div v-if="selectedScan.status === 'COMPLETED'">
              总计: {{ selectedScan.totalChecks }} |
              通过: {{ selectedScan.passedChecks }} |
              失败: {{ selectedScan.failedChecks }} |
              警告: {{ selectedScan.warningChecks }}
            </div>
            <span v-else>-</span>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 检查结果 -->
        <div v-if="selectedScan.status === 'COMPLETED'" class="scan-results">
          <h3>检查结果</h3>
          <a-alert
            type="info"
            message="扫描结果功能正在开发中，敬请期待！"
            show-icon
            style="margin-bottom: 16px;"
          />
        </div>

        <!-- 扫描状态说明 -->
        <div v-else-if="selectedScan.status === 'RUNNING'" class="scan-status">
          <a-alert
            type="info"
            message="扫描正在执行中，请稍后查看结果"
            show-icon
          />
        </div>

        <div v-else-if="selectedScan.status === 'PENDING'" class="scan-status">
          <a-alert
            type="warning"
            message="扫描任务等待执行中"
            show-icon
          />
        </div>

        <div v-else-if="selectedScan.status === 'FAILED'" class="scan-status">
          <a-alert
            type="error"
            message="扫描执行失败"
            show-icon
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconCheckCircle,
  IconRobot,
  IconDownload
} from '@arco-design/web-vue/es/icon'
import { baselineScanApi } from '@/api/baselineScan'
import { assetApi } from '@/api/asset'
import { agentApi, type Agent, type AgentStats } from '@/api/agent'

// 响应式数据
const loading = ref(false)
const createLoading = ref(false)
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const showAgentModal = ref(false)
const showAgentDownloadModal = ref(false)
const agentLoading = ref(false)
const scans = ref([])
const assets = ref([])
const agents = ref([])
const selectedScan = ref(null)
const selectedAgent = ref(null)

// 统计数据
const statistics = reactive({
  totalScans: 0,
  completedScans: 0,
  runningScans: 0,
  failedScans: 0,
  averageComplianceScore: 0
})

// Agent统计数据
const agentStats = reactive({
  totalAgents: 0,
  onlineAgents: 0,
  offlineAgents: 0,
  errorAgents: 0,
  windowsAgents: 0,
  linuxAgents: 0
})

// 服务器配置
const serverConfig = reactive({
  url: window.location.origin
})

// 搜索表单
const searchForm = reactive({
  scanName: '',
  status: '',
  scanType: ''
})

// Agent搜索表单
const agentSearchForm = reactive({
  keyword: '',
  status: '',
  platform: ''
})

// 创建表单
const createForm = reactive({
  scanName: '',
  description: '',
  assetId: null,
  scanType: '',
  executeImmediately: true
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// Agent分页
const agentPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// 生命周期
onMounted(() => {
  loadScans()
  loadStatistics()
})

// 加载扫描列表
const loadScans = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      ...searchForm
    }

    const response = await baselineScanApi.getScans(params)
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      scans.value = data.content || []
      pagination.total = data.totalElements || 0
    } else {
      const message = response?.data?.message || '获取扫描列表失败'
      Message.error(message)
      scans.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载扫描列表失败:', error)
    Message.error('加载扫描列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await baselineScanApi.getStatistics()
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      statistics.totalScans = data.totalScans || 0
      statistics.completedScans = data.statusDistribution?.COMPLETED || 0
      statistics.runningScans = data.statusDistribution?.RUNNING || 0
      statistics.failedScans = data.statusDistribution?.FAILED || 0
      statistics.averageComplianceScore = data.averageComplianceScore || 0
    } else {
      console.warn('获取统计信息失败:', response?.data?.message)
      // 设置默认值
      statistics.totalScans = 0
      statistics.completedScans = 0
      statistics.runningScans = 0
      statistics.failedScans = 0
      statistics.averageComplianceScore = 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载资产列表
const loadAssets = async () => {
  try {
    const response = await assetApi.getAllAssets()
    if (response && response.data && response.data.code === 200) {
      assets.value = response.data.data || []
    } else {
      console.warn('获取资产列表失败:', response?.data?.message)
      assets.value = []
    }
  } catch (error) {
    console.error('加载资产列表失败:', error)
    assets.value = []
  }
}

// 创建扫描
const createScan = async () => {
  // 验证必填字段
  if (!createForm.scanName?.trim()) {
    Message.error('请输入扫描名称')
    return
  }
  if (!createForm.assetId) {
    Message.error('请选择目标资产')
    return
  }
  if (!createForm.scanType) {
    Message.error('请选择扫描类型')
    return
  }

  createLoading.value = true
  try {
    const result = await baselineScanApi.createScan(createForm)
    Message.success('扫描任务创建成功')
    showCreateModal.value = false
    resetCreateForm()
    loadScans()
    loadStatistics()

    // 如果设置了立即执行，提示用户
    if (createForm.executeImmediately) {
      Message.info('扫描任务已开始执行，请稍后查看结果')
    }
  } catch (error) {
    console.error('创建扫描失败:', error)
    if (error.response?.data?.message) {
      Message.error(error.response.data.message)
    } else {
      Message.error('创建扫描失败，请检查网络连接')
    }
  } finally {
    createLoading.value = false
  }
}

// 执行扫描
const executeScan = async (scanId: number) => {
  try {
    await baselineScanApi.executeScan(scanId)
    Message.success('扫描已开始执行')
    loadScans()
    loadStatistics()
  } catch (error) {
    console.error('执行扫描失败:', error)
    Message.error('执行扫描失败')
  }
}

// 取消扫描
const cancelScan = async (scanId: number) => {
  try {
    await baselineScanApi.cancelScan(scanId)
    Message.success('扫描已取消')
    loadScans()
    loadStatistics()
  } catch (error) {
    console.error('取消扫描失败:', error)
    Message.error('取消扫描失败')
  }
}

// 重新执行扫描
const rerunScan = async (scanId: number) => {
  try {
    await baselineScanApi.rerunScan(scanId)
    Message.success('重新执行已开始')
    loadScans()
    loadStatistics()
  } catch (error) {
    console.error('重新执行失败:', error)
    Message.error('重新执行失败')
  }
}

// 删除扫描
const deleteScan = async (scanId: number) => {
  try {
    await baselineScanApi.deleteScan(scanId)
    Message.success('删除成功')
    loadScans()
    loadStatistics()
  } catch (error) {
    console.error('删除扫描失败:', error)
    Message.error('删除扫描失败')
  }
}

const viewScanDetails = (scan: any) => {
  selectedScan.value = scan
  showDetailModal.value = true
}

const resetSearch = () => {
  Object.assign(searchForm, {
    scanName: '',
    status: '',
    scanType: ''
  })
  loadScans()
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    scanName: '',
    description: '',
    assetId: null,
    scanType: '',
    executeImmediately: true
  })
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadScans()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadScans()
}

// 工具方法
const getScanTypeDisplayName = (type: string) => {
  const typeMap = {
    'WINDOWS_SERVER_2019': 'Windows Server 2019',
    'WINDOWS_SERVER_2022': 'Windows Server 2022',
    'WINDOWS_10': 'Windows 10',
    'WINDOWS_11': 'Windows 11',
    'UBUNTU_18_04': 'Ubuntu 18.04',
    'UBUNTU_20_04': 'Ubuntu 20.04',
    'UBUNTU_22_04': 'Ubuntu 22.04',
    'CENTOS_7': 'CentOS 7',
    'CENTOS_8': 'CentOS 8',
    'RHEL_7': 'RHEL 7',
    'RHEL_8': 'RHEL 8',
    'RHEL_9': 'RHEL 9',
    'DEBIAN_10': 'Debian 10',
    'DEBIAN_11': 'Debian 11',
    'SUSE_15': 'SUSE 15'
  }
  return typeMap[type] || type
}

const getStatusDisplayName = (status: string) => {
  const statusMap = {
    'PENDING': '待执行',
    'RUNNING': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '执行失败',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusColor = (status: string) => {
  const colorMap = {
    'PENDING': 'gray',
    'RUNNING': 'blue',
    'COMPLETED': 'green',
    'FAILED': 'red',
    'CANCELLED': 'orange'
  }
  return colorMap[status] || 'gray'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

// Agent相关方法
const loadAgents = async () => {
  agentLoading.value = true
  try {
    const params = {
      page: agentPagination.current - 1,
      size: agentPagination.pageSize,
      ...agentSearchForm
    }

    const response = await agentApi.getAgents(params)
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      agents.value = data.content || []
      agentPagination.total = data.totalElements || 0
    } else {
      const message = response?.data?.message || '获取Agent列表失败'
      Message.error(message)
      agents.value = []
      agentPagination.total = 0
    }
  } catch (error) {
    console.error('加载Agent列表失败:', error)
    Message.error('加载Agent列表失败')
  } finally {
    agentLoading.value = false
  }
}

const loadAgentStats = async () => {
  try {
    const response = await agentApi.getAgentStats()
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      Object.assign(agentStats, data)
    } else {
      console.warn('获取Agent统计信息失败:', response?.data?.message)
    }
  } catch (error) {
    console.error('加载Agent统计数据失败:', error)
  }
}

const viewAgentDetails = (agent: Agent) => {
  selectedAgent.value = agent
  // 这里可以添加Agent详情模态框
  Message.info(`查看Agent详情: ${agent.name}`)
}

const createTaskForAgent = (agent: Agent) => {
  // 这里可以添加为特定Agent创建任务的功能
  Message.info(`为Agent ${agent.name} 创建任务`)
}

const deleteAgent = async (agentId: string) => {
  try {
    await agentApi.deleteAgent(agentId)
    Message.success('Agent删除成功')
    loadAgents()
    loadAgentStats()
  } catch (error) {
    console.error('删除Agent失败:', error)
    Message.error('删除Agent失败')
  }
}

const handleAgentPageChange = (page: number) => {
  agentPagination.current = page
  loadAgents()
}

const handleAgentPageSizeChange = (pageSize: number) => {
  agentPagination.pageSize = pageSize
  agentPagination.current = 1
  loadAgents()
}

const getAgentStatusColor = (status: string) => {
  const colorMap = {
    'ONLINE': 'green',
    'OFFLINE': 'gray',
    'ERROR': 'red',
    'MAINTENANCE': 'orange'
  }
  return colorMap[status] || 'gray'
}

const getAgentStatusDisplayName = (status: string) => {
  const statusMap = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'ERROR': '异常',
    'MAINTENANCE': '维护'
  }
  return statusMap[status] || status
}

const updateServerConfig = () => {
  Message.success('服务器配置已更新')
}

const downloadAgent = async (platform: string, arch: string) => {
  try {
    const response = await agentApi.downloadAgent(platform, arch)

    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `vulnark-agent-${platform}-${arch}.${platform === 'windows' ? 'zip' : 'tar.gz'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    Message.success('Agent客户端下载已开始')
  } catch (error) {
    console.error('下载Agent失败:', error)
    Message.error('下载Agent失败')
  }
}

const getDownloadUrl = (platform: string, arch: string) => {
  return agentApi.getDownloadUrl(platform, arch)
}

// 当显示Agent模态框时加载数据
const handleShowAgentModal = () => {
  showAgentModal.value = true
  loadAgents()
  loadAgentStats()
}
</script>

<style scoped>
.baseline-scan-page {
  width: 100%;
  padding: var(--spacing-lg);
  min-height: 100vh;
  background: var(--background);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
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

.feature-notice {
  margin-bottom: var(--spacing-lg);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  text-align: center;
}

.search-section {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.scan-table {
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.check-stats {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.scan-results {
  margin-top: var(--spacing-lg);
}

.scan-results h3 {
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
}

/* Agent管理样式 */
.agent-management {
  width: 100%;
}

.agent-stats {
  margin-bottom: var(--spacing-lg);
}

.agent-actions {
  margin-bottom: var(--spacing-lg);
}

.agent-table {
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.agent-download {
  width: 100%;
}

.server-config {
  padding: var(--spacing-lg);
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.download-links {
  margin-top: var(--spacing-lg);
}

.download-card {
  height: 100%;
}

.download-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) 0;
}

.download-info h4 {
  margin: 0 0 var(--spacing-xs) 0;
  color: var(--text-primary);
}

.download-info p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.install-instructions {
  padding: var(--spacing-lg);
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.instruction-content h4 {
  margin: var(--spacing-lg) 0 var(--spacing-md) 0;
  color: var(--text-primary);
}

.instruction-content p {
  margin-bottom: var(--spacing-md);
  color: var(--text-secondary);
}

.instruction-content pre {
  background: var(--background);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}

.instruction-content code {
  background: var(--background);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}
</style> 