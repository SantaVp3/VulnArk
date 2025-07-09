<template>
  <div class="user-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">用户管理</h1>
        <p class="page-description">管理系统用户和权限</p>
      </div>
      <div class="header-right">
        <a-button type="primary" @click="openCreateModal">
          <template #icon>
            <icon-plus />
          </template>
          新建用户
        </a-button>
      </div>
    </div>

    <div class="table-section">
      <a-card>
        <a-table :data="users" :loading="loading" :pagination="false">
          <template #columns>
            <a-table-column title="ID" data-index="id" :width="80" />
            <a-table-column title="用户名" data-index="username" :width="150" />
            <a-table-column title="姓名" data-index="fullName" :width="150" />
            <a-table-column title="邮箱" data-index="email" :width="200" />
            <a-table-column title="角色" data-index="role" :width="120">
              <template #cell="{ record }">
                <a-tag :color="getRoleColor(record.role)">
                  {{ getRoleLabel(record.role) }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column title="状态" data-index="status" :width="100">
              <template #cell="{ record }">
                <a-tag :color="getStatusColor(record.status)">
                  {{ getStatusLabel(record.status) }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column title="创建时间" data-index="createdTime" :width="150">
              <template #cell="{ record }">
                {{ formatDate(record.createdTime) }}
              </template>
            </a-table-column>
            <a-table-column title="操作" :width="200" fixed="right">
              <template #cell="{ record }">
                <a-space size="small">
                  <a-button type="text" size="small" @click="editUser(record)">编辑</a-button>
                  <a-button 
                    v-if="!isDefaultAdmin(record)"
                    type="text" 
                    size="small" 
                    :status="record.status === 'ACTIVE' ? 'danger' : 'normal'"
                    @click="toggleUserStatus(record)"
                  >
                    {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
                  </a-button>
                  <a-button 
                    v-if="!isDefaultAdmin(record)"
                    type="text" 
                    size="small" 
                    status="danger" 
                    @click="deleteUser(record)"
                  >
                    删除
                  </a-button>
                  <a-tag v-if="isDefaultAdmin(record)" color="gold" size="small">系统管理员</a-tag>
                </a-space>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 创建/编辑用户模态框 -->
    <a-modal 
      v-model:visible="modalVisible" 
      :title="isEditing ? '编辑用户' : '新建用户'"
      @ok="handleSave"
      @cancel="handleCancel"
      :confirm-loading="saveLoading"
    >
      <a-form :model="formData" layout="vertical" ref="formRef">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item 
              field="username" 
              label="用户名" 
              :rules="[{ required: true, message: '请输入用户名' }]"
            >
              <a-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEditing" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              field="fullName" 
              label="姓名"
              :rules="[{ required: true, message: '请输入姓名' }]"
            >
              <a-input v-model="formData.fullName" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item 
              field="email" 
              label="邮箱"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' }
              ]"
            >
              <a-input v-model="formData.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              field="role" 
              label="角色"
              :rules="[{ required: true, message: '请选择角色' }]"
            >
              <a-select v-model="formData.role" placeholder="请选择角色">
                <a-option value="ADMIN">管理员</a-option>
                <a-option value="MANAGER">项目经理</a-option>
                <a-option value="ANALYST">安全分析师</a-option>
                <a-option value="VIEWER">查看者</a-option>
                <a-option value="USER">普通用户</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item 
              field="notes" 
              label="部门/职位"
            >
              <a-input v-model="formData.notes" placeholder="请输入部门/职位" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              field="status" 
              label="状态"
            >
              <a-select v-model="formData.status" placeholder="请选择状态">
                <a-option value="ACTIVE">活跃</a-option>
                <a-option value="INACTIVE">禁用</a-option>
                <a-option value="LOCKED">锁定</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item 
              field="phone" 
              label="电话"
            >
              <a-input v-model="formData.phone" placeholder="请输入电话" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              field="password" 
              label="密码"
              :rules="[{ required: true, message: '请输入密码' }, { minLength: 6, message: '密码长度至少6位' }]"
            >
              <a-input-password v-model="formData.password" placeholder="请输入密码" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 重置密码模态框 -->
    <a-modal 
      v-model:visible="passwordModalVisible" 
      title="重置密码"
      @ok="handleResetPassword"
      @cancel="passwordModalVisible = false"
      :confirm-loading="resetPasswordLoading"
    >
      <a-form :model="passwordFormData" layout="vertical">
        <a-form-item 
          field="password" 
          label="新密码"
          :rules="[{ required: true, message: '请输入新密码' }, { minLength: 6, message: '密码长度至少6位' }]"
        >
          <a-input-password v-model="passwordFormData.password" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item 
          field="confirmPassword" 
          label="确认密码"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validatePasswordConfirm }
          ]"
        >
          <a-input-password v-model="passwordFormData.confirmPassword" placeholder="请再次输入密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { Message, Modal } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import { userApi, type User, type UserRequest } from '../api/user'

// 响应式数据
const users = ref<User[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const passwordModalVisible = ref(false)
const saveLoading = ref(false)
const resetPasswordLoading = ref(false)
const isEditing = ref(false)
const editingUserId = ref<number | null>(null)
const resetPasswordUserId = ref<number | null>(null)

// 表单数据
const formData = reactive<UserRequest>({
  username: '',
  email: '',
  fullName: '',
  role: 'VIEWER',
  status: 'ACTIVE',
  phone: '',
  password: '',
  notes: '' // 替换department和position为notes
})

const passwordFormData = reactive({
  password: '',
  confirmPassword: ''
})

// 表单引用
const formRef = ref()

// 加载用户数据
const loadUsers = async () => {
  try {
    loading.value = true

    // 首先尝试获取所有用户
    const response = await userApi.getAllUsers()
    let userData = []

    // 处理不同的响应格式
    if (response && response.data) {
      if (response.data.code === 200) {
        userData = response.data.data || []
      } else {
        userData = response.data || []
      }
    } else if (Array.isArray(response)) {
      userData = response
    } else {
      userData = []
    }

    if (Array.isArray(userData) && userData.length > 0) {
      users.value = userData
      console.log(`成功加载 ${userData.length} 个用户`)
    } else {
      // 如果getAllUsers返回格式不对，尝试分页查询
      try {
        const fallbackResponse = await userApi.getUsers({ page: 0, size: 100 })
        let fallbackData = {}

        // 处理分页查询的响应格式
        if (fallbackResponse && fallbackResponse.data) {
          if (fallbackResponse.data.code === 200) {
            fallbackData = fallbackResponse.data.data || { content: [] }
          } else {
            fallbackData = fallbackResponse.data || { content: [] }
          }
        } else {
          fallbackData = fallbackResponse || { content: [] }
        }

        if (fallbackData.content && Array.isArray(fallbackData.content)) {
          users.value = fallbackData.content
          console.log(`通过分页查询成功加载 ${fallbackData.content.length} 个用户`)
        } else {
          users.value = []
          console.warn('用户数据为空')
        }
      } catch (fallbackError) {
        console.error('分页查询也失败:', fallbackError)
        users.value = []
      }
    }
  } catch (error: any) {
    console.error('加载用户数据失败:', error)
    users.value = []
    Message.error('加载用户数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 打开创建模态框
const openCreateModal = () => {
  isEditing.value = false
  editingUserId.value = null
  resetFormData()
  modalVisible.value = true
}

// 编辑用户
const editUser = (user: User) => {
  isEditing.value = true
  editingUserId.value = user.id
  
  // 填充表单数据
  Object.assign(formData, {
    username: user.username,
    email: user.email,
    fullName: user.fullName || '',
    role: user.role,
    status: user.status,
    phone: user.phone || '',
    password: '',
    notes: user.department || user.position || '' // 填充notes
  })
  
  modalVisible.value = true
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    username: '',
    email: '',
    fullName: '',
    role: 'VIEWER',
    status: 'ACTIVE',
    phone: '',
    password: '',
    notes: ''
  })
}

// 保存用户
const handleSave = async () => {
  try {
    // 表单验证
    const valid = await formRef.value?.validate()
    if (!valid) {
      console.log('表单验证失败');
      return;
    }

    console.log('开始保存用户，表单数据:', formData);
    saveLoading.value = true;

    if (isEditing.value && editingUserId.value) {
      // 编辑用户
      console.log('更新用户:', editingUserId.value);
      const updateData = { ...formData };
      delete updateData.password; // 编辑时不包含密码
      const result = await userApi.updateUser(editingUserId.value, updateData);
      console.log('用户更新结果:', result);
      Message.success('用户更新成功');
    } else {
      // 创建用户
      console.log('创建新用户');
      const result = await userApi.createUser(formData);
      console.log('用户创建结果:', result);
      Message.success('用户创建成功');
    }

    modalVisible.value = false;
    console.log('准备重新加载用户列表');
    await loadUsers();
    console.log('用户列表已重新加载');
  } catch (error: any) {
    console.error('保存用户失败:', error);
    if (error.response) {
      console.error('错误响应:', error.response.data);
      Message.error('保存失败: ' + (error.response.data.message || error.message || '未知错误'));
    } else {
      Message.error('保存失败: ' + (error.message || '未知错误'));
    }
  } finally {
    saveLoading.value = false;
  }
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
  resetFormData()
}

// 切换用户状态
const toggleUserStatus = (user: User) => {
  if (isDefaultAdmin(user)) {
    Message.warning('默认管理员账户不能被禁用')
    return
  }

  let newStatus: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  let actionText: string
  
  if (user.status === 'ACTIVE') {
    newStatus = 'INACTIVE'
    actionText = '禁用'
  } else {
    newStatus = 'ACTIVE'
    actionText = '启用'
  }
  
  Modal.confirm({
    title: `确认${actionText}用户`,
    content: `确定要${actionText}用户 "${user.fullName || user.username}" 吗？`,
    onOk: async () => {
      try {
        await userApi.updateUserStatus(user.id, newStatus)
        Message.success(`用户${actionText}成功`)
        await loadUsers()
      } catch (error: any) {
        Message.error(`${actionText}失败: ` + (error.message || '未知错误'))
      }
    }
  })
}

// 删除用户
const deleteUser = (user: User) => {
  if (isDefaultAdmin(user)) {
    Message.warning('默认管理员账户不能被删除')
    return
  }

  Modal.confirm({
    title: '确认删除用户',
    content: `确定要删除用户 "${user.fullName || user.username}" 吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        await userApi.deleteUser(user.id)
        Message.success('用户删除成功')
        await loadUsers()
      } catch (error: any) {
        Message.error('删除失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 重置密码
const resetPassword = (user: User) => {
  resetPasswordUserId.value = user.id
  passwordFormData.password = ''
  passwordFormData.confirmPassword = ''
  passwordModalVisible.value = true
}

// 处理重置密码
const handleResetPassword = async () => {
  try {
    if (!passwordFormData.password || passwordFormData.password !== passwordFormData.confirmPassword) {
      Message.error('密码不匹配')
      return
    }

    resetPasswordLoading.value = true
    await userApi.resetUserPassword(resetPasswordUserId.value!, passwordFormData.password)
    Message.success('密码重置成功')
    passwordModalVisible.value = false
  } catch (error: any) {
    Message.error('重置密码失败: ' + (error.message || '未知错误'))
  } finally {
    resetPasswordLoading.value = false
  }
}

// 密码确认验证
const validatePasswordConfirm = (value: string, callback: Function) => {
  if (value && value !== passwordFormData.password) {
    callback('两次输入的密码不一致')
  } else {
    callback()
  }
}

// 工具函数
const isDefaultAdmin = (user: User) => {
  return user.id === 1 && user.username === 'admin' && user.role === 'ADMIN'
}

const getRoleColor = (role: string) => {
  const colors = {
    ADMIN: 'red',
    MANAGER: 'orange',
    ANALYST: 'blue',
    VIEWER: 'green',
    USER: 'cyan'
  }
  return colors[role as keyof typeof colors] || 'gray'
}

const getRoleLabel = (role: string) => {
  const labels = {
    ADMIN: '管理员',
    MANAGER: '项目经理',
    ANALYST: '安全分析师',
    VIEWER: '查看者',
    USER: '普通用户'
  }
  return labels[role as keyof typeof labels] || role
}

const getStatusColor = (status: string) => {
  const colors = {
    ACTIVE: 'green',
    INACTIVE: 'red',
    LOCKED: 'orange'
  }
  return colors[status as keyof typeof colors] || 'gray'
}

const getStatusLabel = (status: string) => {
  const labels = {
    ACTIVE: '活跃',
    INACTIVE: '禁用',
    LOCKED: '锁定'
  }
  return labels[status as keyof typeof labels] || status
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-page {
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

.table-section {
  margin-bottom: var(--spacing-xl);
}
</style> 