<template>
  <a-modal
    v-model:visible="modalVisible"
    :title="isEdit ? '编辑扫描任务' : '创建扫描任务'"
    width="800px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="loading"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      layout="vertical"
      @submit="handleSubmit"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="任务名称" field="name">
            <a-input 
              v-model="formData.name" 
              placeholder="请输入任务名称"
              :max-length="100"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="所属项目" field="projectId">
            <a-select 
              v-model="formData.projectId" 
              placeholder="请选择项目"
              allow-clear
              :loading="projectsLoading"
            >
              <a-option 
                v-for="project in projects" 
                :key="project.id" 
                :value="project.id"
              >
                {{ project.name }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="任务描述" field="description">
        <a-textarea 
          v-model="formData.description" 
          placeholder="请输入任务描述"
          :max-length="1000"
          :auto-size="{ minRows: 3, maxRows: 5 }"
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="8">
          <a-form-item label="扫描类型" field="scanType">
            <a-select 
              v-model="formData.scanType" 
              placeholder="请选择扫描类型"
              @change="handleScanTypeChange"
            >
              <a-option value="PORT_SCAN">端口扫描</a-option>
              <a-option value="WEB_SCAN">Web应用扫描</a-option>
              <a-option value="SYSTEM_SCAN">系统漏洞扫描</a-option>
              <a-option value="COMPREHENSIVE_SCAN">综合扫描</a-option>
              <a-option value="CUSTOM_SCAN">自定义扫描</a-option>
            </a-select>
          </a-form-item>
        </a-col>
        
        <a-col :span="8">
          <a-form-item label="扫描引擎" field="scanEngine">
            <a-select 
              v-model="formData.scanEngine" 
              placeholder="请选择扫描引擎"
            >
              <a-option value="NESSUS">Nessus</a-option>
              <a-option value="OPENVAS">OpenVAS</a-option>
              <a-option value="AWVS">AWVS</a-option>
              <a-option value="NUCLEI">Nuclei</a-option>
              <a-option value="NMAP">Nmap</a-option>
              <a-option value="INTERNAL">内置引擎</a-option>
            </a-select>
          </a-form-item>
        </a-col>
        
        <a-col :span="8">
          <a-form-item label="扫描模板" field="scanTemplate">
            <a-select 
              v-model="formData.scanTemplate" 
              placeholder="请选择扫描模板"
              allow-clear
            >
              <a-option value="QUICK_SCAN">快速扫描</a-option>
              <a-option value="FULL_SCAN">全面扫描</a-option>
              <a-option value="WEB_APP_SCAN">Web应用扫描</a-option>
              <a-option value="NETWORK_SCAN">网络扫描</a-option>
              <a-option value="COMPLIANCE_SCAN">合规扫描</a-option>
              <a-option value="CUSTOM">自定义模板</a-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="目标资产" field="targetAssets">
        <a-textarea 
          v-model="formData.targetAssets" 
          placeholder="请输入目标资产，支持IP地址、域名、IP段等，每行一个"
          :auto-size="{ minRows: 4, maxRows: 8 }"
        />
        <div class="form-help">
          支持格式：192.168.1.1、192.168.1.1-100、192.168.1.0/24、example.com
        </div>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="计划开始时间" field="scheduledStartTime">
            <a-date-picker 
              v-model="formData.scheduledStartTime"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择计划开始时间"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="预估执行时间" field="estimatedDuration">
            <a-input-number 
              v-model="formData.estimatedDuration"
              placeholder="请输入预估执行时间"
              :min="1"
              :max="1440"
              suffix="分钟"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 高级配置 -->
      <a-collapse>
        <a-collapse-item header="高级配置" key="advanced">
          <a-form-item label="扫描参数" field="scanParameters">
            <a-textarea 
              v-model="formData.scanParameters" 
              placeholder="请输入扫描参数（JSON格式）"
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
            <div class="form-help">
              JSON格式，例如：{"timeout": 300, "threads": 10, "retry": 3}
            </div>
          </a-form-item>

          <a-form-item label="扫描选项" field="scanOptions">
            <a-textarea 
              v-model="formData.scanOptions" 
              placeholder="请输入扫描选项（JSON格式）"
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
            <div class="form-help">
              JSON格式，例如：{"skipPing": false, "detectOS": true, "aggressive": false}
            </div>
          </a-form-item>
        </a-collapse-item>
      </a-collapse>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { createScanTask, updateScanTask, type ScanTask, type ScanTaskRequest } from '@/api/scan'
import { getAllProjects, type Project } from '@/api/project'

// Props
interface Props {
  visible: boolean
  taskData?: ScanTask | null
}

const props = withDefaults(defineProps<Props>(), {
  taskData: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const loading = ref(false)
const projectsLoading = ref(false)
const projects = ref<Project[]>([])
const formRef = ref()

// 计算属性
const modalVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.taskData?.id)

// 表单数据
const formData = reactive<ScanTaskRequest>({
  name: '',
  description: '',
  scanType: 'PORT_SCAN',
  scanEngine: 'INTERNAL',
  scanTemplate: undefined,
  projectId: undefined,
  targetAssets: '',
  scanParameters: '',
  scanOptions: '',
  scheduledStartTime: undefined,
  estimatedDuration: undefined,
  scanConfigId: undefined
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入任务名称' },
    { max: 100, message: '任务名称长度不能超过100个字符' }
  ],
  scanType: [
    { required: true, message: '请选择扫描类型' }
  ],
  scanEngine: [
    { required: true, message: '请选择扫描引擎' }
  ],
  targetAssets: [
    { required: true, message: '请输入目标资产' }
  ],
  scanParameters: [
    { 
      validator: (value: string, callback: (error?: string) => void) => {
        if (value && value.trim()) {
          try {
            JSON.parse(value)
            callback()
          } catch (error) {
            callback('扫描参数必须是有效的JSON格式')
          }
        } else {
          callback()
        }
      }
    }
  ],
  scanOptions: [
    { 
      validator: (value: string, callback: (error?: string) => void) => {
        if (value && value.trim()) {
          try {
            JSON.parse(value)
            callback()
          } catch (error) {
            callback('扫描选项必须是有效的JSON格式')
          }
        } else {
          callback()
        }
      }
    }
  ]
}

// 方法
const loadProjects = async () => {
  try {
    projectsLoading.value = true
    const response = await getAllProjects()
    if (response.code === 200) {
      projects.value = response.data || []
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    projectsLoading.value = false
  }
}

const handleScanTypeChange = (scanType: string) => {
  // 根据扫描类型自动设置推荐的扫描引擎和模板
  switch (scanType) {
    case 'PORT_SCAN':
      formData.scanEngine = 'NMAP'
      formData.scanTemplate = 'NETWORK_SCAN'
      break
    case 'WEB_SCAN':
      formData.scanEngine = 'AWVS'
      formData.scanTemplate = 'WEB_APP_SCAN'
      break
    case 'SYSTEM_SCAN':
      formData.scanEngine = 'NESSUS'
      formData.scanTemplate = 'FULL_SCAN'
      break
    case 'COMPREHENSIVE_SCAN':
      formData.scanEngine = 'NESSUS'
      formData.scanTemplate = 'FULL_SCAN'
      break
    default:
      formData.scanEngine = 'INTERNAL'
      formData.scanTemplate = 'CUSTOM'
  }
}

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    scanType: 'PORT_SCAN',
    scanEngine: 'INTERNAL',
    scanTemplate: undefined,
    projectId: undefined,
    targetAssets: '',
    scanParameters: '',
    scanOptions: '',
    scheduledStartTime: undefined,
    estimatedDuration: undefined,
    scanConfigId: undefined
  })
  formRef.value?.clearValidate()
}

const fillFormData = (task: ScanTask) => {
  Object.assign(formData, {
    name: task.name,
    description: task.description || '',
    scanType: task.scanType,
    scanEngine: task.scanEngine,
    scanTemplate: task.scanTemplate,
    projectId: task.projectId,
    targetAssets: task.targetAssets || '',
    scanParameters: task.scanParameters || '',
    scanOptions: task.scanOptions || '',
    scheduledStartTime: task.scheduledStartTime,
    estimatedDuration: task.estimatedDuration,
    scanConfigId: undefined
  })
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return

    loading.value = true

    let response
    if (isEdit.value && props.taskData?.id) {
      response = await updateScanTask(props.taskData.id, formData)
    } else {
      response = await createScanTask(formData)
    }

    if (response.code === 200) {
      Message.success(isEdit.value ? '扫描任务更新成功' : '扫描任务创建成功')
      emit('success')
    } else {
      Message.error(response.message || (isEdit.value ? '更新扫描任务失败' : '创建扫描任务失败'))
    }
  } catch (error) {
    console.error('提交扫描任务失败:', error)
    Message.error(isEdit.value ? '更新扫描任务失败' : '创建扫描任务失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    if (props.taskData) {
      fillFormData(props.taskData)
    } else {
      resetForm()
    }
  }
})

// 生命周期
onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.form-help {
  font-size: 12px;
  color: #86909c;
  margin-top: 4px;
}
</style>
