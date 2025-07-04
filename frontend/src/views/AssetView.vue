<template>
  <div class="asset-view">
    <a-card title="资产管理" :bordered="false">
      <!-- 操作栏 -->
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><icon-plus /></template>
            新增资产
          </a-button>
          <a-button @click="handleBatchDelete" :disabled="selectedRowKeys.length === 0">
            <template #icon><icon-delete /></template>
            批量删除
          </a-button>
          <a-button @click="handleImport">
            <template #icon><icon-import /></template>
            导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><icon-export /></template>
            导出
          </a-button>
        </a-space>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar" style="margin-bottom: 16px;">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input v-model="searchForm.name" placeholder="资产名称" allow-clear />
          </a-col>
          <a-col :span="4">
            <a-select v-model="searchForm.type" placeholder="资产类型" allow-clear>
              <a-option value="SERVER">服务器</a-option>
              <a-option value="WORKSTATION">工作站</a-option>
              <a-option value="NETWORK_DEVICE">网络设备</a-option>
              <a-option value="DATABASE">数据库</a-option>
              <a-option value="WEB_APPLICATION">Web应用</a-option>
              <a-option value="MOBILE_APPLICATION">移动应用</a-option>
              <a-option value="IOT_DEVICE">物联网设备</a-option>
              <a-option value="CLOUD_SERVICE">云服务</a-option>
              <a-option value="OTHER">其他</a-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select v-model="searchForm.status" placeholder="状态" allow-clear>
              <a-option value="ACTIVE">活跃</a-option>
              <a-option value="INACTIVE">非活跃</a-option>
              <a-option value="MAINTENANCE">维护中</a-option>
              <a-option value="DECOMMISSIONED">已退役</a-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select v-model="searchForm.importance" placeholder="重要性" allow-clear>
              <a-option value="LOW">低</a-option>
              <a-option value="MEDIUM">中</a-option>
              <a-option value="HIGH">高</a-option>
              <a-option value="CRITICAL">关键</a-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><icon-search /></template>
                查询
              </a-button>
              <a-button @click="handleReset">
                <template #icon><icon-refresh /></template>
                重置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data="assets"
        :pagination="pagination"
        :loading="loading"
        :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectionChange }"
        :scroll="{ x: 'max-content' }"
        row-key="id"
        size="medium"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
      >
        <template #type="{ record }">
          <a-tag :color="getTypeColor(record.type)">{{ getTypeText(record.type) }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
        </template>
        <template #importance="{ record }">
          <a-tag :color="getImportanceColor(record.importance)">{{ getImportanceText(record.importance) }}</a-tag>
        </template>
        <template #owner="{ record }">
          <span v-if="record.ownerId">
            {{ getUserName(record.ownerId) }}
          </span>
          <a-tag v-else color="gray">未分配</a-tag>
        </template>
        <template #vulnerabilityCount="{ record }">
          <span v-if="!record.vulnerabilityCount || record.vulnerabilityCount === 0" style="color: #999;">
            0
          </span>
          <a-button v-else type="text" @click="handleViewVulnerabilities(record)" style="color: #1890ff; padding: 0;">
            {{ record.vulnerabilityCount }}
          </a-button>
        </template>
        <template #operations="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="handleView(record)">查看</a-button>
            <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="text" size="small" status="danger" @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑资产模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleSave"
      @cancel="handleCancel"
    >
      <a-form :model="formData" :rules="rules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="资产名称" field="name" required>
              <a-input v-model="formData.name" placeholder="请输入资产名称" />
              <template #help>
                <span style="color: #999;">必填项，不能为空</span>
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="资产类型" field="type" required>
              <a-select v-model="formData.type" placeholder="请选择资产类型">
                <a-option value="SERVER">服务器</a-option>
                <a-option value="WORKSTATION">工作站</a-option>
                <a-option value="NETWORK_DEVICE">网络设备</a-option>
                <a-option value="DATABASE">数据库</a-option>
                <a-option value="WEB_APPLICATION">Web应用</a-option>
                <a-option value="MOBILE_APPLICATION">移动应用</a-option>
                <a-option value="IOT_DEVICE">物联网设备</a-option>
                <a-option value="CLOUD_SERVICE">云服务</a-option>
                <a-option value="OTHER">其他</a-option>
              </a-select>
              <template #help>
                <span style="color: #999;">必填项，请选择一个类型</span>
              </template>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="IP地址" field="ipAddress" required>
              <a-input v-model="formData.ipAddress" placeholder="请输入IP地址" />
              <template #help>
                <span style="color: #999;">格式如：192.168.1.1，不能为空</span>
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="域名" field="domain">
              <a-input v-model="formData.domain" placeholder="请输入域名（可选）" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="端口" field="port">
              <a-input-number v-model="formData.port" placeholder="端口号" :min="1" :max="65535" />
              <template #help>
                <span style="color: #999;">范围：1-65535，可以留空</span>
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="协议" field="protocol">
              <a-input v-model="formData.protocol" placeholder="协议（可选）" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="服务" field="service">
              <a-input v-model="formData.service" placeholder="服务（可选）" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="版本" field="version">
              <a-input v-model="formData.version" placeholder="版本" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="操作系统" field="operatingSystem">
              <a-input v-model="formData.operatingSystem" placeholder="操作系统" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="重要性" field="importance">
              <a-select v-model="formData.importance" placeholder="重要性">
                <a-option value="LOW">低</a-option>
                <a-option value="MEDIUM">中</a-option>
                <a-option value="HIGH">高</a-option>
                <a-option value="CRITICAL">关键</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="负责人" field="ownerId">
              <a-select v-model="formData.ownerId" placeholder="请选择负责人" allow-clear>
                <a-option v-for="option in userOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="位置" field="location">
              <a-input v-model="formData.location" placeholder="物理位置" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="供应商" field="vendor">
              <a-input v-model="formData.vendor" placeholder="供应商" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标签" field="tags">
          <a-input v-model="formData.tags" placeholder="标签，多个标签用逗号分隔" />
        </a-form-item>
        <a-form-item label="描述" field="description">
          <a-textarea v-model="formData.description" placeholder="资产描述" :rows="3" />
        </a-form-item>
        <a-form-item label="备注" field="notes">
          <a-textarea v-model="formData.notes" placeholder="备注信息" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 资产详情查看模态框 -->
    <a-modal
      v-model:visible="viewModalVisible"
      title="资产详情"
      width="1000px"
      :footer="false"
      @cancel="viewModalVisible = false"
    >
      <div v-if="viewingAsset" class="asset-detail">
        <!-- 基本信息 -->
        <a-card title="基本信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="资产名称">
              <span style="font-weight: 600;">{{ viewingAsset.name }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="资产类型">
              <a-tag :color="getTypeColor(viewingAsset.type)">{{ getTypeText(viewingAsset.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(viewingAsset.status)">{{ getStatusText(viewingAsset.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="重要性">
              <a-tag :color="getImportanceColor(viewingAsset.importance)">{{ getImportanceText(viewingAsset.importance) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="负责人">
              <span v-if="viewingAsset.ownerId">
                {{ getUserName(viewingAsset.ownerId) }}
              </span>
              <span v-else style="color: #999;">未分配</span>
            </a-descriptions-item>
            <a-descriptions-item label="位置">
              {{ viewingAsset.location || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="供应商">
              {{ viewingAsset.vendor || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="漏洞数量">
              <a-button v-if="viewingAsset.vulnerabilityCount && viewingAsset.vulnerabilityCount > 0" 
                        type="text" 
                        @click="handleViewVulnerabilities(viewingAsset)" 
                        style="color: #1890ff; padding: 0;">
                {{ viewingAsset.vulnerabilityCount }}
              </a-button>
              <span v-else style="color: #999;">0</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 网络信息 -->
        <a-card title="网络信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="IP地址">
              {{ viewingAsset.ipAddress || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="域名">
              {{ viewingAsset.domain || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="端口">
              {{ viewingAsset.port || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="协议">
              {{ viewingAsset.protocol || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="服务">
              {{ viewingAsset.service || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="版本">
              {{ viewingAsset.version || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 系统信息 -->
        <a-card title="系统信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="1" bordered>
            <a-descriptions-item label="操作系统">
              {{ viewingAsset.operatingSystem || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <div v-if="viewingAsset.tags">
                <a-tag v-for="tag in viewingAsset.tags.split(',')" :key="tag.trim()" style="margin: 2px;">
                  {{ tag.trim() }}
                </a-tag>
              </div>
              <span v-else style="color: #999;">-</span>
            </a-descriptions-item>
            <a-descriptions-item label="描述">
              <div class="content-box">{{ viewingAsset.description || '-' }}</div>
            </a-descriptions-item>
            <a-descriptions-item label="备注">
              <div class="content-box">{{ viewingAsset.notes || '-' }}</div>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 时间信息 -->
        <a-card title="时间信息" :bordered="false" style="margin-bottom: 16px;">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="创建时间">
              {{ viewingAsset.createdTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="更新时间">
              {{ viewingAsset.updatedTime || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 操作按钮 -->
        <div style="text-align: center; margin-top: 24px;">
          <a-space>
            <a-button type="primary" @click="handleEditFromView">
              <template #icon><icon-edit /></template>
              编辑资产
            </a-button>
            <a-button v-if="viewingAsset.vulnerabilityCount && viewingAsset.vulnerabilityCount > 0" 
                      @click="handleViewVulnerabilities(viewingAsset)">
              <template #icon><icon-eye /></template>
              查看漏洞
            </a-button>
            <a-button @click="viewModalVisible = false">关闭</a-button>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- 导入模态框 -->
    <a-modal
      v-model:visible="importModalVisible"
      title="导入资产"
      @ok="handleImportConfirm"
      @cancel="importModalVisible = false"
    >
      <a-upload
        :custom-request="handleFileUpload"
        :file-list="fileList"
        accept=".json,.csv,.xlsx"
        :limit="1"
        @change="onFileChange"
      >
        <template #upload-button>
          <div class="arco-upload-drag-wrapper">
            <div class="arco-upload-drag">
              <div class="arco-upload-drag-icon">
                <icon-upload />
              </div>
              <div class="arco-upload-drag-text">点击或拖拽文件到此处上传</div>
              <div class="arco-upload-drag-tip">支持 JSON、CSV、Excel 格式</div>
            </div>
          </div>
        </template>
      </a-upload>
      <div style="margin-top: 16px;">
        <a-typography-text type="secondary">
          支持格式：JSON、CSV、Excel。请确保文件格式正确。
        </a-typography-text>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconPlus,
  IconDelete,
  IconImport,
  IconExport,
  IconSearch,
  IconRefresh,
  IconUpload,
  IconEdit,
  IconEye
} from '@arco-design/web-vue/es/icon'
import { assetApi, type Asset, type AssetQueryParams } from '@/api/asset'
import { userApi, type User } from '@/api/user'

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const assets = ref<Asset[]>([])
const users = ref<User[]>([])
const userMap = ref<Record<number, User>>({})
const selectedRowKeys = ref<number[]>([])
const modalVisible = ref(false)
const importModalVisible = ref(false)
const viewModalVisible = ref(false)
const editingAsset = ref<Asset | null>(null)
const viewingAsset = ref<Asset | null>(null)
const fileList = ref([])
const userOptions = ref<{label: string, value: number}[]>([])

// 搜索表单
const searchForm = reactive<AssetQueryParams>({
  name: '',
  type: '',
  status: '',
  importance: '',
  page: 0,
  size: 10,
  sortBy: 'createdTime',
  sortDir: 'desc'
})

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// 表单数据
const formData = reactive<Partial<Asset>>({
  name: '',
  description: '',
  type: 'SERVER',
  status: 'ACTIVE',
  ipAddress: '',
  domain: '',
  port: undefined,
  protocol: '',
  service: '',
  version: '',
  operatingSystem: '',
  importance: 'MEDIUM',

  ownerId: null,
  location: '',
  vendor: '',
  tags: '',
  notes: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入资产名称' }],
  type: [{ required: true, message: '请选择资产类型' }],
  ipAddress: [
    { required: true, message: '请输入IP地址' },
    { 
      validator: (value: string, cb: any) => {
        if (!value) return cb('IP地址不能为空');
        const ipPattern = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
        if (!ipPattern.test(value)) {
          return cb('IP地址格式不正确');
        }
        return cb();
      }
    }
  ],
  port: [
    {
      validator: (value: number, cb: any) => {
        if (!value) return cb(); // 允许为空
        if (value < 1 || value > 65535) {
          return cb('端口号必须在1-65535之间');
        }
        return cb();
      }
    }
  ]
}

// 表单引用
const formRef = ref()

// 计算属性
const modalTitle = computed(() => editingAsset.value ? '编辑资产' : '新增资产')

// 表格列定义
const columns = [
  { title: '资产名称', dataIndex: 'name', ellipsis: true, tooltip: true, width: 200, minWidth: 150 },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 140 },
  { title: '类型', dataIndex: 'type', slotName: 'type', width: 110 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 90 },
  { title: '重要性', dataIndex: 'importance', slotName: 'importance', width: 90 },
  { title: '负责人', dataIndex: 'ownerId', slotName: 'owner', width: 120 },
  { title: '漏洞数量', dataIndex: 'vulnerabilityCount', slotName: 'vulnerabilityCount', width: 90 },
  { title: '更新时间', dataIndex: 'updatedTime', width: 160 },
  { title: '操作', slotName: 'operations', width: 160, fixed: 'right' }
]

// 方法定义
const loadAssets = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined,
      importance: searchForm.importance || undefined,
      ipAddress: searchForm.ipAddress || undefined,
      domain: searchForm.domain || undefined,
      keyword: searchForm.keyword || undefined,
      sortBy: searchForm.sortBy || 'createdTime',
      sortDir: searchForm.sortDir || 'desc',
      _t: new Date().getTime() // 添加时间戳防止缓存
    }
    
    // 先尝试获取所有资产，确保数据完整性
    let allAssets = []
    try {
      allAssets = await assetApi.getAllAssets()
    } catch (e) {
      // 忽略错误，继续使用分页查询
    }
    
    // 执行分页查询
    const result = await assetApi.getAssets(params)
    
    // 更新数据
    assets.value = result.content
    pagination.total = result.totalElements
    
    // 加载用户列表，用于显示负责人信息
    await loadUsers()
  } catch (error) {
    Message.error('加载资产列表失败')
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  try {
    const allUsers = await userApi.getAllUsers()
    
    // 保存用户列表
    users.value = allUsers
    
    // 构建用户映射
    userMap.value = {}
    allUsers.forEach(user => {
      userMap.value[user.id] = user
    })
    
    // 构建用户选项
    userOptions.value = allUsers.map(user => ({
      label: user.fullName || user.username,
      value: user.id
    }))
  } catch (error) {
    Message.error('加载用户列表失败')
  }
}

// 事件处理
const handleAdd = () => {
  editingAsset.value = null
  resetFormData()
  modalVisible.value = true
}

const handleEdit = (asset) => {
  editingAsset.value = asset
  
  // 复制资产数据到表单
  formData.name = asset.name
  formData.description = asset.description
  formData.type = asset.type
  formData.status = asset.status
  formData.ipAddress = asset.ipAddress
  formData.domain = asset.domain
  formData.port = asset.port
  formData.protocol = asset.protocol
  formData.service = asset.service
  formData.version = asset.version
  formData.operatingSystem = asset.operatingSystem
  formData.importance = asset.importance

  
  // 确保ownerId是数字类型或null
  formData.ownerId = asset.ownerId ? Number(asset.ownerId) : null
  
  formData.location = asset.location
  formData.vendor = asset.vendor
  formData.tags = asset.tags
  formData.notes = asset.notes
  
  modalVisible.value = true
}

const handleView = (asset: Asset) => {
  viewingAsset.value = asset
  viewModalVisible.value = true
}

const handleDelete = (asset: Asset) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除资产"${asset.name}"吗？`,
    onOk: async () => {
      try {
        await assetApi.deleteAsset(asset.id!)
        Message.success('删除成功')
        loadAssets()
      } catch (error) {
        Message.error('删除失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个资产吗？`,
    onOk: async () => {
      try {
        await assetApi.batchDeleteAssets(selectedRowKeys.value)
        Message.success('批量删除成功')
        selectedRowKeys.value = []
        loadAssets()
      } catch (error) {
        Message.error('批量删除失败')
      }
    }
  })
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    // 创建提交数据的副本，移除undefined值
    const submitData = { ...formData }
    
    // 特殊处理ownerId字段，确保它是数字或null，而不是undefined或空字符串
    if (submitData.ownerId === undefined || submitData.ownerId === '' || submitData.ownerId === 0) {
      submitData.ownerId = null;
    } else if (typeof submitData.ownerId === 'string') {
      // 如果是字符串，转换为数字
      submitData.ownerId = parseInt(submitData.ownerId, 10);
    }
    
    // 确保ownerId字段始终存在于请求中，即使是null
    if (!('ownerId' in submitData)) {
      submitData.ownerId = null;
    }
    
    // 移除其他undefined或空字符串值
    Object.keys(submitData).forEach(key => {
      if (submitData[key] === undefined || (submitData[key] === '' && key !== 'ownerId')) {
        delete submitData[key]
      }
    })
    
    // 确保必填字段存在
    if (!submitData.name) {
      Message.error('资产名称不能为空');
      return;
    }
    
    if (!submitData.type) {
      Message.error('资产类型不能为空');
      return;
    }
    
    if (!submitData.ipAddress) {
      Message.error('IP地址不能为空');
      return;
    }
    

    
    if (editingAsset.value) {
      await assetApi.updateAsset(editingAsset.value.id!, submitData)
      Message.success('更新成功')
      modalVisible.value = false
    } else {
      const createdAsset = await assetApi.createAsset(submitData as Omit<Asset, 'id'>)
      Message.success('创建成功')
      modalVisible.value = false
    }
    
    // 重置搜索条件并跳转到第一页
    Object.assign(searchForm, {
      name: '',
      type: '',
      status: '',
      importance: '',
      ipAddress: '',
      domain: '',
      keyword: '',
      page: 0,
      size: 10,
      sortBy: 'createdTime',
      sortDir: 'desc'
    });
    
    pagination.current = 1
    
    // 强制刷新资产列表
    loading.value = true
    
    // 先清空当前列表，确保视觉上有刷新效果
    assets.value = []
    
    // 延迟加载，确保后端数据已更新
    setTimeout(async () => {
      try {
        // 重新加载资产列表
        const params = {
          page: 0,
          size: pagination.pageSize,
          sortBy: 'createdTime',
          sortDir: 'desc',
          _t: new Date().getTime()
        }
        const result = await assetApi.getAssets(params)
        assets.value = result.content
        pagination.total = result.totalElements
      } catch (error) {
        // 错误处理
      } finally {
        loading.value = false
      }
    }, 1000); // 延长延迟时间到1秒
  } catch (error) {
    let errorMessage = '操作失败';
    
    if (error.response) {
      // 服务器响应错误
      if (error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response.status === 400) {
        errorMessage = '请求参数错误，请检查输入';
      } else if (error.response.status === 500) {
        errorMessage = '服务器内部错误';
      }
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    Message.error(editingAsset.value ? `更新失败: ${errorMessage}` : `创建失败: ${errorMessage}`)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  resetFormData()
}

const handleSearch = () => {
  pagination.current = 1
  loadAssets()
}

const handleReset = () => {
  // 完全重置所有搜索条件
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: '',
    importance: '',
    ipAddress: '',
    domain: '',
    keyword: '',
    page: 0,
    size: 50, // 增大分页大小
    sortBy: 'createdTime',
    sortDir: 'desc'
  })
  pagination.current = 1
  
  // 强制刷新
  loading.value = true
  assets.value = [] // 先清空列表
  
  // 延迟加载以确保视觉上有刷新效果
  setTimeout(() => {
    loadAssets()
  }, 100)
}

const handleImport = () => {
  importModalVisible.value = true
  fileList.value = []
}

const handleExport = async () => {
  try {
    const assets = await assetApi.exportAssets(selectedRowKeys.value.length > 0 ? selectedRowKeys.value : undefined)
    
    // 创建并下载文件
    const dataStr = JSON.stringify(assets, null, 2)
    const blob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `assets_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    Message.success('导出成功')
  } catch (error) {
    Message.error('导出失败')
  }
}

const handleFileUpload = (option: any) => {
  // 自定义上传逻辑
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ status: 'done' })
    }, 1000)
  })
}

const onFileChange = (fileList: any[]) => {
  // 处理文件变化
}

const handleImportConfirm = async () => {
  if (fileList.value.length === 0) {
    Message.warning('请选择要导入的文件')
    return
  }
  
  try {
    // TODO: 实现文件解析和导入逻辑
    Message.success('导入成功')
    importModalVisible.value = false
    loadAssets()
  } catch (error) {
    Message.error('导入失败')
  }
}

const onSelectionChange = (selectedKeys: number[]) => {
  selectedRowKeys.value = selectedKeys
}

const onPageChange = (page: number) => {
  pagination.current = page
  loadAssets()
}

const onPageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadAssets()
}

const resetFormData = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    type: 'SERVER',
    status: 'ACTIVE',
    ipAddress: '',
    domain: '',
    port: undefined,
    protocol: '',
    service: '',
    version: '',
    operatingSystem: '',
    importance: 'MEDIUM',

    ownerId: null,
    location: '',
    vendor: '',
    tags: '',
    notes: ''
  })
}

// 样式辅助函数
const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    SERVER: 'blue',
    WORKSTATION: 'green',
    NETWORK_DEVICE: 'orange',
    DATABASE: 'purple',
    WEB_APPLICATION: 'cyan',
    MOBILE_APPLICATION: 'lime',
    IOT_DEVICE: 'gold',
    CLOUD_SERVICE: 'geekblue',
    OTHER: 'gray'
  }
  return colors[type] || 'gray'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    SERVER: '服务器',
    WORKSTATION: '工作站',
    NETWORK_DEVICE: '网络设备',
    DATABASE: '数据库',
    WEB_APPLICATION: 'Web应用',
    MOBILE_APPLICATION: '移动应用',
    IOT_DEVICE: '物联网设备',
    CLOUD_SERVICE: '云服务',
    OTHER: '其他'
  }
  return texts[type] || type
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    ACTIVE: 'green',
    INACTIVE: 'gray',
    MAINTENANCE: 'orange',
    DECOMMISSIONED: 'red'
  }
  return colors[status] || 'gray'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    ACTIVE: '活跃',
    INACTIVE: '非活跃',
    MAINTENANCE: '维护中',
    DECOMMISSIONED: '已退役'
  }
  return texts[status] || status
}

const getImportanceColor = (importance: string) => {
  const colors: Record<string, string> = {
    LOW: 'gray',
    MEDIUM: 'blue',
    HIGH: 'orange',
    CRITICAL: 'red'
  }
  return colors[importance] || 'gray'
}

const getImportanceText = (importance: string) => {
  const texts: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '关键'
  }
  return texts[importance] || importance
}

const getUserName = (userId: number) => {
  if (!userId) return '未分配'
  const user = userMap.value[userId]
  return user ? (user.fullName || user.username) : '未知用户'
}

// 跳转到漏洞页面
const handleViewVulnerabilities = (asset: Asset) => {
  router.push({
    path: '/vulnerabilities',
    query: {
      assetId: asset.id
    }
  })
}

// 从查看界面切换到编辑界面
const handleEditFromView = () => {
  if (viewingAsset.value) {
    editingAsset.value = viewingAsset.value
    Object.assign(formData, viewingAsset.value)
    viewModalVisible.value = false
    modalVisible.value = true
  }
}

// 生命周期钩子
onMounted(async () => {
  // 先加载用户数据
  await loadUsers()
  
  // 再加载资产数据
  await loadAssets()
})
</script>

<style scoped>
.asset-view {
  padding: 0;
}

.search-bar {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
}

:deep(.arco-table) {
  border-radius: 6px;
}

:deep(.arco-table-th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.arco-table-tbody) {
  font-size: 14px;
}

:deep(.arco-table-td) {
  border-bottom: 1px solid #f2f3f5;
}

:deep(.arco-table-container) {
  border-radius: 6px;
}

:deep(.arco-upload-drag) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  text-align: center;
  padding: 20px;
}

:deep(.arco-upload-drag:hover) {
  border-color: #1890ff;
}

/* 确保表格充满容器 */
:deep(.arco-table-container) {
  width: 100%;
}

:deep(.arco-table-element) {
  width: 100%;
  table-layout: auto;
}

.asset-detail {
  padding: 0;
}

.content-box {
  padding: 8px;
  background: #f7f8fa;
  border-radius: 4px;
  min-height: 32px;
  word-break: break-word;
  white-space: pre-wrap;
  max-height: 100px;
  overflow-y: auto;
}
</style> 