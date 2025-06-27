<template>
  <div class="user-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>用户管理</h1>
        <p>管理系统用户账户和权限</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <template #icon><icon-plus /></template>
            新建用户
          </a-button>
          <a-button @click="showImportModal">
            <template #icon><icon-upload /></template>
            批量导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><icon-download /></template>
            导出用户
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="[24, 16]" class="stats-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <a-statistic
            title="总用户数"
            :value="stats.total"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <a-statistic
            title="活跃用户"
            :value="stats.active"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <a-statistic
            title="管理员"
            :value="stats.admin"
            :value-style="{ color: '#fa8c16' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <a-statistic
            title="非活跃用户"
            :value="stats.inactive"
            :value-style="{ color: '#f5222d' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 搜索表单 -->
    <a-card class="search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="用户名">
          <a-input v-model="searchForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model="searchForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model="searchForm.role" placeholder="请选择角色" allow-clear>
            <a-option v-for="option in roleOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="searchForm.status" placeholder="请选择状态" allow-clear>
            <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>





    <!-- 用户表格 -->
    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data="users"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #avatar="{ record }">
          <a-avatar v-if="record.avatarUrl" :src="record.avatarUrl" />
          <a-avatar v-else>{{ record.fullName?.charAt(0) || record.username.charAt(0) }}</a-avatar>
        </template>
        
        <template #role="{ record }">
          <a-tag :color="getRoleColor(record.role)">
            {{ getRoleLabel(record.role) }}
          </a-tag>
        </template>
        
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusLabel(record.status) }}
          </a-tag>
        </template>
        
        <template #createdTime="{ record }">
          {{ formatDateTime(record.createdTime, 'YYYY-MM-DD HH:mm') }}
        </template>
        
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="showDetailModal(record)">查看</a-button>
            <a-button type="text" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-button type="text" size="small" @click="showResetPasswordModal(record)">重置密码</a-button>
            <a-button
              type="text"
              size="small"
              :status="record.status === 'ACTIVE' ? 'warning' : 'normal'"
              @click="toggleUserStatus(record)"
              :disabled="record.id === currentUserId"
            >
              {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
            </a-button>
            <a-button
              type="text"
              size="small"
              status="danger"
              @click="handleDelete(record)"
              :disabled="record.id === currentUserId"
            >
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 创建用户模态框 -->
    <a-modal
      v-model:visible="createModalVisible"
      title="新建用户"
      width="800px"
      @ok="handleCreateUser"
      @cancel="createModalVisible = false"
    >
      <a-form ref="createFormRef" :model="createForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="用户名"
              field="username"
              :rules="[
                { required: true, message: '请输入用户名' },
                { minLength: 3, message: '用户名至少3位' },
                { maxLength: 20, message: '用户名不能超过20位' }
              ]"
            >
              <a-input v-model="createForm.username" placeholder="请输入用户名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="邮箱"
              field="email"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' }
              ]"
            >
              <a-input v-model="createForm.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="姓名"
              field="fullName"
              :rules="[{ required: true, message: '请输入姓名' }]"
            >
              <a-input v-model="createForm.fullName" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电话" field="phone">
              <a-input v-model="createForm.phone" placeholder="请输入电话号码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="角色"
              field="role"
              :rules="[{ required: true, message: '请选择角色' }]"
            >
              <a-select v-model="createForm.role" placeholder="请选择角色">
                <a-option v-for="option in roleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" field="status">
              <a-select v-model="createForm.status" placeholder="请选择状态">
                <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="部门" field="department">
              <a-input v-model="createForm.department" placeholder="请输入部门" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职位" field="position">
              <a-input v-model="createForm.position" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item
          label="密码"
          field="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        >
          <a-input-password
            v-model="createForm.password"
            placeholder="请输入密码"
            autocomplete="new-password"
            @input="checkPassword(createForm.password, true)"
          >
            <template #suffix>
              <a-button
                type="text"
                size="mini"
                @click="generatePassword(true)"
                title="生成随机密码"
              >
                <icon-refresh />
              </a-button>
            </template>
          </a-input-password>

          <!-- 密码强度指示器 -->
          <div v-if="passwordStrength" class="password-strength" style="margin-top: 8px;">
            <div class="strength-bar">
              <div
                class="strength-fill"
                :style="{
                  width: passwordStrength.percentage + '%',
                  backgroundColor: passwordStrength.color
                }"
              ></div>
            </div>
            <div class="strength-text" :style="{ color: passwordStrength.color }">
              强度: {{ getPasswordStrengthText(passwordStrength.strength) }}
            </div>
            <div v-if="passwordStrength.feedback.length > 0" class="strength-feedback">
              <div v-for="tip in passwordStrength.feedback" :key="tip" class="feedback-item">
                {{ tip }}
              </div>
            </div>
          </div>
        </a-form-item>

        <a-form-item
          label="确认密码"
          field="confirmPassword"
          :rules="[{ required: true, message: '请确认密码' }]"
        >
          <a-input-password
            v-model="createForm.confirmPassword"
            placeholder="请再次输入密码"
            autocomplete="new-password"
          />
        </a-form-item>

        <a-form-item label="备注" field="notes">
          <a-textarea
            v-model="createForm.notes"
            placeholder="请输入备注信息"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑用户模态框 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑用户"
      width="800px"
      @ok="handleEditUser"
      @cancel="editModalVisible = false"
    >
      <a-form ref="editFormRef" :model="editForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名">
              <a-input :value="currentUser?.username" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="邮箱"
              field="email"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' }
              ]"
            >
              <a-input v-model="editForm.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="姓名"
              field="fullName"
              :rules="[{ required: true, message: '请输入姓名' }]"
            >
              <a-input v-model="editForm.fullName" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电话" field="phone">
              <a-input v-model="editForm.phone" placeholder="请输入电话号码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="角色"
              field="role"
              :rules="[{ required: true, message: '请选择角色' }]"
            >
              <a-select v-model="editForm.role" placeholder="请选择角色">
                <a-option v-for="option in roleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="状态"
              field="status"
              :rules="[{ required: true, message: '请选择状态' }]"
            >
              <a-select v-model="editForm.status" placeholder="请选择状态">
                <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="部门" field="department">
              <a-input v-model="editForm.department" placeholder="请输入部门" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职位" field="position">
              <a-input v-model="editForm.position" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注" field="notes">
          <a-textarea
            v-model="editForm.notes"
            placeholder="请输入备注信息"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 用户详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="用户详情"
      width="800px"
      :footer="false"
    >
      <div v-if="currentUser" class="user-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="用户名">{{ currentUser.username }}</a-descriptions-item>
          <a-descriptions-item label="姓名">{{ currentUser.fullName }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ currentUser.email }}</a-descriptions-item>
          <a-descriptions-item label="电话">{{ currentUser.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag :color="getRoleColor(currentUser.role)">
              {{ getRoleLabel(currentUser.role) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentUser.status)">
              {{ getStatusLabel(currentUser.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="部门">{{ currentUser.department || '-' }}</a-descriptions-item>
          <a-descriptions-item label="职位">{{ currentUser.position || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatDateTime(currentUser.createdTime) }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ formatDateTime(currentUser.updatedTime) }}</a-descriptions-item>
          <a-descriptions-item label="最后登录">{{ formatDateTime(currentUser.lastLoginTime) }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ currentUser.notes || '-' }}</a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 重置密码模态框 -->
    <a-modal
      v-model:visible="resetPasswordModalVisible"
      title="重置密码"
      width="500px"
      @ok="handleResetPassword"
      @cancel="resetPasswordModalVisible = false"
    >
      <a-form ref="resetPasswordFormRef" :model="resetPasswordForm" layout="vertical">
        <a-form-item
          label="新密码"
          field="newPassword"
          :rules="[{ required: true, message: '请输入新密码' }]"
        >
          <a-input-password
            v-model="resetPasswordForm.newPassword"
            placeholder="请输入新密码"
            autocomplete="new-password"
            @input="checkPassword(resetPasswordForm.newPassword, false)"
          >
            <template #suffix>
              <a-button
                type="text"
                size="mini"
                @click="generatePassword(false)"
                title="生成随机密码"
              >
                <icon-refresh />
              </a-button>
            </template>
          </a-input-password>

          <!-- 密码强度指示器 -->
          <div v-if="passwordStrength" class="password-strength" style="margin-top: 8px;">
            <div class="strength-bar">
              <div
                class="strength-fill"
                :style="{
                  width: passwordStrength.percentage + '%',
                  backgroundColor: passwordStrength.color
                }"
              ></div>
            </div>
            <div class="strength-text" :style="{ color: passwordStrength.color }">
              强度: {{ getPasswordStrengthText(passwordStrength.strength) }}
            </div>
          </div>
        </a-form-item>

        <a-form-item
          label="确认密码"
          field="confirmPassword"
          :rules="[{ required: true, message: '请确认密码' }]"
        >
          <a-input-password
            v-model="resetPasswordForm.confirmPassword"
            placeholder="请再次输入密码"
            autocomplete="new-password"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconPlus, IconUpload, IconDownload, IconEye, IconEyeInvisible, IconRefresh } from '@arco-design/web-vue/es/icon'
import { formatDateTime } from '@/utils/date'
import { useAuthStore } from '@/stores/auth'
import { userApi, type User, type CreateUserRequest, type UpdateUserRequest, type ResetPasswordRequest, type UserQueryRequest } from '@/api/user'
import { checkPasswordStrength, validatePassword, validateConfirmPassword, generateRandomPassword, getPasswordStrengthText } from '@/utils/password'

// 模态框状态
const createModalVisible = ref(false)
const editModalVisible = ref(false)
const detailModalVisible = ref(false)
const resetPasswordModalVisible = ref(false)
const importModalVisible = ref(false)

// 表单引用
const createFormRef = ref()
const editFormRef = ref()
const resetPasswordFormRef = ref()

// 当前操作的用户
const currentUser = ref<User | null>(null)
const selectedUsers = ref<number[]>([])

// 密码相关状态
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordStrength = ref<any>(null)

const authStore = useAuthStore()
const currentUserId = computed(() => authStore.user?.id)
const isDev = import.meta.env.DEV

// 响应式数据
const loading = ref(false)
const users = ref<User[]>([])
const stats = ref<UserStats>({
  total: 0,
  active: 0,
  inactive: 0,
  admin: 0,
  manager: 0,
  user: 0
})

// 搜索表单
const searchForm = reactive<UserQueryRequest>({
  username: '',
  email: '',
  fullName: '',
  role: undefined,
  status: undefined,
  department: '',
  page: 0,
  size: 10
})

// 创建用户表单
const createForm = reactive<CreateUserRequest>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  fullName: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE',
  department: '',
  position: '',
  notes: ''
})

// 编辑用户表单
const editForm = reactive<UpdateUserRequest>({
  email: '',
  fullName: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE',
  department: '',
  position: '',
  notes: ''
})

// 重置密码表单
const resetPasswordForm = reactive<ResetPasswordRequest>({
  newPassword: '',
  confirmPassword: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: true,
  pageSizeOptions: ['10', '20', '50', '100']
})

// 选项配置
const roleOptions = [
  { value: 'ADMIN', label: '管理员', color: 'red' },
  { value: 'MANAGER', label: '经理', color: 'orange' },
  { value: 'ANALYST', label: '分析师', color: 'blue' },
  { value: 'VIEWER', label: '查看者', color: 'green' },
  { value: 'USER', label: '普通用户', color: 'gray' }
]

const statusOptions = [
  { value: 'ACTIVE', label: '活跃', color: 'green' },
  { value: 'INACTIVE', label: '非活跃', color: 'gray' },
  { value: 'LOCKED', label: '锁定', color: 'red' }
]

// 表格列配置
const columns = [
  { title: '头像', slotName: 'avatar', width: 80 },
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '姓名', dataIndex: 'fullName', width: 120 },
  { title: '邮箱', dataIndex: 'email', width: 200 },
  { title: '角色', slotName: 'role', width: 100 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '部门', dataIndex: 'department', width: 120 },
  { title: '职位', dataIndex: 'position', width: 120 },
  { title: '创建时间', slotName: 'createdTime', width: 160 },
  { title: '操作', slotName: 'actions', width: 280, fixed: 'right' }
]

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    // 构建请求参数，过滤掉空值
    const params: any = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }

    // 只添加非空的搜索条件
    if (searchForm.username?.trim()) params.username = searchForm.username.trim()
    if (searchForm.email?.trim()) params.email = searchForm.email.trim()
    if (searchForm.fullName?.trim()) params.fullName = searchForm.fullName.trim()
    if (searchForm.role) params.role = searchForm.role
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.department?.trim()) params.department = searchForm.department.trim()

    const response = await userApi.getList(params)
    // 处理响应数据
    if (response?.data?.content) {
      users.value = response.data.content
      pagination.total = response.data.totalElements || 0
    } else {
      users.value = []
      pagination.total = 0
    }

  } catch (error) {
    console.error('Load users error:', error)
    Message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await userApi.getStats()
    if (response && response.data) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    email: '',
    role: undefined,
    status: undefined
  })
  pagination.current = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadUsers()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadUsers()
}

// 显示创建用户模态框
const showCreateModal = () => {
  resetCreateForm()
  createModalVisible.value = true
}

// 显示编辑用户模态框
const showEditModal = (user: User) => {
  currentUser.value = user
  Object.assign(editForm, {
    email: user.email,
    fullName: user.fullName,
    phone: user.phone || '',
    role: user.role,
    status: user.status,
    department: user.department || '',
    position: user.position || '',
    notes: user.notes || ''
  })
  editModalVisible.value = true
}

// 显示用户详情模态框
const showDetailModal = (user: User) => {
  currentUser.value = user
  detailModalVisible.value = true
}

// 显示重置密码模态框
const showResetPasswordModal = (user: User) => {
  currentUser.value = user
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  passwordStrength.value = null
  resetPasswordModalVisible.value = true
}

// 显示导入模态框
const showImportModal = () => {
  importModalVisible.value = true
}

// 重置创建表单
const resetCreateForm = () => {
  Object.assign(createForm, {
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    fullName: '',
    phone: '',
    role: 'USER',
    status: 'ACTIVE',
    department: '',
    position: '',
    notes: ''
  })
  passwordStrength.value = null
  createFormRef.value?.resetFields()
}

// 辅助方法
const getRoleColor = (role: string) => {
  const option = roleOptions.find(opt => opt.value === role)
  return option?.color || 'gray'
}

const getRoleLabel = (role: string) => {
  const option = roleOptions.find(opt => opt.value === role)
  return option?.label || role
}

const getStatusColor = (status: string) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option?.color || 'gray'
}

const getStatusLabel = (status: string) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option?.label || status
}

// 密码强度检查
const checkPassword = (password: string, isCreate: boolean = true) => {
  if (!password) {
    passwordStrength.value = null
    return
  }

  passwordStrength.value = checkPasswordStrength(password)
}

// 生成随机密码
const generatePassword = (isCreate: boolean = true) => {
  const newPassword = generateRandomPassword(12, true)
  if (isCreate) {
    createForm.password = newPassword
    createForm.confirmPassword = newPassword
    checkPassword(newPassword, true)
  } else {
    resetPasswordForm.newPassword = newPassword
    resetPasswordForm.confirmPassword = newPassword
    checkPassword(newPassword, false)
  }
}

// 导出用户数据
const handleExport = async () => {
  try {
    const response = await userApi.export(searchForm)

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `users_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    Message.success('导出成功')
  } catch (error: any) {
    Message.error(error.message || '导出失败')
  }
}

// 下载导入模板
const downloadTemplate = async () => {
  try {
    const response = await userApi.downloadTemplate()

    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'user_import_template.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    Message.success('模板下载成功')
  } catch (error: any) {
    Message.error(error.message || '模板下载失败')
  }
}

// 处理文件导入
const handleFileImport = async (file: File) => {
  try {
    const response = await userApi.importFile(file)

    if (response.success > 0) {
      Message.success(`成功导入 ${response.success} 个用户`)
      loadUsers()
      loadStats()
    }

    if (response.failed > 0) {
      Message.warning(`${response.failed} 个用户导入失败`)
      console.error('导入错误:', response.errors)
    }

    importModalVisible.value = false
  } catch (error: any) {
    Message.error(error.message || '导入失败')
  }
}

// 切换用户状态
const toggleUserStatus = async (user: User) => {
  const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

  try {
    await userApi.updateStatus(user.id, newStatus)
    Message.success(`用户状态已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`)
    loadUsers()
    loadStats()
  } catch (error: any) {
    Message.error(error.message || '状态更新失败')
  }
}

// 创建用户
const handleCreateUser = async () => {
  try {
    await createFormRef.value?.validate()

    // 验证密码
    const passwordValidation = validatePassword(createForm.password)
    if (!passwordValidation.valid) {
      Message.error(passwordValidation.errors[0])
      return
    }

    // 验证确认密码
    const confirmValidation = validateConfirmPassword(createForm.password, createForm.confirmPassword)
    if (!confirmValidation.valid) {
      Message.error(confirmValidation.error!)
      return
    }

    // 清理空字符串字段，转换为null
    const cleanedForm = { ...createForm }
    Object.keys(cleanedForm).forEach(key => {
      if (cleanedForm[key] === '') {
        cleanedForm[key] = null
      }
    })

    await userApi.create(cleanedForm)
    Message.success('用户创建成功')
    createModalVisible.value = false
    loadUsers()
    loadStats()
  } catch (error: any) {
    Message.error(error.message || '创建用户失败')
  }
}

// 编辑用户
const handleEditUser = async () => {
  try {
    await editFormRef.value?.validate()

    if (!currentUser.value) return

    await userApi.update(currentUser.value.id, editForm)
    Message.success('用户信息更新成功')
    editModalVisible.value = false
    loadUsers()
    loadStats()
  } catch (error: any) {
    Message.error(error.message || '更新用户失败')
  }
}

// 删除用户
const handleDelete = (user: User) => {
  if (user.id === currentUserId.value) {
    Message.warning('不能删除当前登录用户')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户"${user.username}"吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        await userApi.delete(user.id)
        Message.success('删除成功')
        loadUsers()
        loadStats()
      } catch (error: any) {
        Message.error(error.message || '删除失败')
      }
    }
  })
}

// 批量删除用户
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    Message.warning('请选择要删除的用户')
    return
  }

  // 检查是否包含当前用户
  if (selectedUsers.value.includes(currentUserId.value!)) {
    Message.warning('不能删除当前登录用户')
    return
  }

  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        await userApi.batchDelete(selectedUsers.value)
        Message.success('批量删除成功')
        selectedUsers.value = []
        loadUsers()
        loadStats()
      } catch (error: any) {
        Message.error(error.message || '批量删除失败')
      }
    }
  })
}

// 重置密码
const handleResetPassword = async () => {
  try {
    await resetPasswordFormRef.value?.validate()

    if (!currentUser.value) return

    // 验证密码
    const passwordValidation = validatePassword(resetPasswordForm.newPassword)
    if (!passwordValidation.valid) {
      Message.error(passwordValidation.errors[0])
      return
    }

    // 验证确认密码
    const confirmValidation = validateConfirmPassword(resetPasswordForm.newPassword, resetPasswordForm.confirmPassword)
    if (!confirmValidation.valid) {
      Message.error(confirmValidation.error!)
      return
    }

    await userApi.resetPassword(currentUser.value.id, resetPasswordForm)
    Message.success('密码重置成功')
    resetPasswordModalVisible.value = false
  } catch (error: any) {
    Message.error(error.message || '密码重置失败')
  }
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadStats()
})
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-left p {
  margin: 5px 0 0 0;
  color: #666;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.user-detail {
  padding: 16px 0;
}

.password-strength {
  margin-top: 8px;
}

.strength-bar {
  width: 100%;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 4px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
}

.strength-feedback {
  font-size: 12px;
  color: #666;
}

.feedback-item {
  margin-bottom: 2px;
}

.feedback-item:before {
  content: '• ';
  color: #999;
}
</style>
