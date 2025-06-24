import request from './index'

// 检测结果类型定义
export interface AssetDetection {
  id: number
  assetId: number
  type: DetectionType
  status: DetectionStatus
  result?: DetectionResult
  target: string
  port?: number
  responseTime?: number
  errorMessage?: string
  details?: string
  httpStatusCode?: number
  banner?: string
  startTime: string
  endTime?: string
  createdTime: string
}

export enum DetectionType {
  PING = 'PING',
  TCP_PORT = 'TCP_PORT',
  HTTP_SERVICE = 'HTTP_SERVICE',
  HTTPS_SERVICE = 'HTTPS_SERVICE',
  SSH_SERVICE = 'SSH_SERVICE',
  DATABASE_SERVICE = 'DATABASE_SERVICE',
  CUSTOM_PORT = 'CUSTOM_PORT'
}

export enum DetectionStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  TIMEOUT = 'TIMEOUT',
  CANCELLED = 'CANCELLED'
}

export enum DetectionResult {
  ONLINE = 'ONLINE',
  OFFLINE = 'OFFLINE',
  TIMEOUT = 'TIMEOUT',
  UNREACHABLE = 'UNREACHABLE',
  FILTERED = 'FILTERED',
  UNKNOWN = 'UNKNOWN'
}

// 指纹识别类型定义
export interface AssetFingerprint {
  id: number
  assetId: number
  type: FingerprintType
  name: string
  version?: string
  vendor?: string
  confidence: number
  method?: IdentificationMethod
  signature?: string
  port?: number
  protocol?: string
  banner?: string
  httpHeaders?: string
  pageTitle?: string
  errorPage?: string
  signatureFile?: string
  extraInfo?: string
  active: boolean
  createdTime: string
  updatedTime: string
}

export enum FingerprintType {
  WEB_SERVER = 'WEB_SERVER',
  APPLICATION_SERVER = 'APPLICATION_SERVER',
  DATABASE = 'DATABASE',
  OPERATING_SYSTEM = 'OPERATING_SYSTEM',
  WEB_FRAMEWORK = 'WEB_FRAMEWORK',
  CMS = 'CMS',
  PROGRAMMING_LANGUAGE = 'PROGRAMMING_LANGUAGE',
  MIDDLEWARE = 'MIDDLEWARE',
  LOAD_BALANCER = 'LOAD_BALANCER',
  WAF = 'WAF',
  CDN = 'CDN',
  CACHE = 'CACHE',
  MONITORING = 'MONITORING',
  SECURITY_PRODUCT = 'SECURITY_PRODUCT',
  NETWORK_DEVICE = 'NETWORK_DEVICE',
  IOT_DEVICE = 'IOT_DEVICE',
  OTHER = 'OTHER'
}

export enum IdentificationMethod {
  HTTP_HEADER = 'HTTP_HEADER',
  SERVER_BANNER = 'SERVER_BANNER',
  ERROR_PAGE = 'ERROR_PAGE',
  SIGNATURE_FILE = 'SIGNATURE_FILE',
  PAGE_CONTENT = 'PAGE_CONTENT',
  COOKIE = 'COOKIE',
  REDIRECT = 'REDIRECT',
  SSL_CERTIFICATE = 'SSL_CERTIFICATE',
  PORT_SERVICE = 'PORT_SERVICE',
  FAVICON = 'FAVICON',
  ROBOTS_TXT = 'ROBOTS_TXT',
  SITEMAP = 'SITEMAP',
  META_TAG = 'META_TAG',
  JAVASCRIPT = 'JAVASCRIPT',
  CSS = 'CSS',
  CUSTOM_RULE = 'CUSTOM_RULE'
}

// 检测结果类型
export interface DetectionResult {
  assetId: number
  detections: AssetDetection[]
  fingerprints: AssetFingerprint[]
  success: boolean
  errorMessage?: string
}

// 统计信息类型
export interface DetectionStatistics {
  totalDetections: number
  onlineAssets: number
  offlineAssets: number
  recentDetections: number
  averageResponseTime: number
  resultDistribution: Record<string, number>
}

export interface FingerprintStatistics {
  totalFingerprints: number
  typeDistribution: Record<string, number>
  technologyUsage: Record<string, number>
  vendorDistribution: Record<string, number>
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

// 资产检测API
export const assetDetectionApi = {
  // 检测单个资产
  detectAsset: (assetId: number, includeFingerprint: boolean = true): Promise<DetectionResult> => {
    return request.post(`/asset-detection/detect/${assetId}`, null, {
      params: { includeFingerprint }
    })
  },

  // 批量检测资产
  detectAssets: (assetIds: number[], includeFingerprint: boolean = true): Promise<DetectionResult[]> => {
    return request.post('/asset-detection/detect/batch', assetIds, {
      params: { includeFingerprint }
    })
  },

  // 检测项目下的所有资产
  detectProjectAssets: (projectId: number, includeFingerprint: boolean = true): Promise<DetectionResult[]> => {
    return request.post(`/asset-detection/detect/project/${projectId}`, null, {
      params: { includeFingerprint }
    })
  },

  // 异步检测单个资产（立即返回）
  detectAssetAsync: (assetId: number, includeFingerprint: boolean = true): Promise<string> => {
    return request.post(`/asset-detection/detect-async/${assetId}`, null, {
      params: { includeFingerprint }
    })
  },

  // 异步批量检测资产（立即返回）
  detectAssetsAsync: (assetIds: number[], includeFingerprint: boolean = true): Promise<string> => {
    return request.post('/asset-detection/detect-async/batch', assetIds, {
      params: { includeFingerprint }
    })
  },

  // 异步检测项目资产（立即返回）
  detectProjectAssetsAsync: (projectId: number, includeFingerprint: boolean = true): Promise<string> => {
    return request.post(`/asset-detection/detect-async/project/${projectId}`, null, {
      params: { includeFingerprint }
    })
  },

  // 获取资产检测历史
  getDetectionHistory: (assetId: number, page: number = 0, size: number = 20): Promise<Page<AssetDetection>> => {
    return request.get(`/asset-detection/history/${assetId}`, {
      params: { page, size }
    })
  },

  // 获取资产最新检测状态
  getLatestDetections: (assetId: number): Promise<AssetDetection[]> => {
    return request.get(`/asset-detection/latest/${assetId}`)
  },

  // 获取资产指纹信息
  getFingerprints: (assetId: number): Promise<AssetFingerprint[]> => {
    return request.get(`/asset-detection/fingerprints/${assetId}`)
  },

  // 获取检测统计信息
  getDetectionStatistics: (): Promise<DetectionStatistics> => {
    return request.get('/asset-detection/statistics/detection')
  },

  // 获取指纹统计信息
  getFingerprintStatistics: (): Promise<FingerprintStatistics> => {
    return request.get('/asset-detection/statistics/fingerprint')
  }
}

// 工具函数
export const detectionUtils = {
  // 获取检测类型标签
  getDetectionTypeLabel: (type: DetectionType) => {
    const labels = {
      [DetectionType.PING]: 'PING检测',
      [DetectionType.TCP_PORT]: 'TCP端口检测',
      [DetectionType.HTTP_SERVICE]: 'HTTP服务检测',
      [DetectionType.HTTPS_SERVICE]: 'HTTPS服务检测',
      [DetectionType.SSH_SERVICE]: 'SSH服务检测',
      [DetectionType.DATABASE_SERVICE]: '数据库服务检测',
      [DetectionType.CUSTOM_PORT]: '自定义端口检测'
    }
    return labels[type] || type
  },

  // 获取检测状态标签
  getDetectionStatusLabel: (status: DetectionStatus) => {
    const labels = {
      [DetectionStatus.PENDING]: '等待中',
      [DetectionStatus.RUNNING]: '检测中',
      [DetectionStatus.COMPLETED]: '已完成',
      [DetectionStatus.FAILED]: '检测失败',
      [DetectionStatus.TIMEOUT]: '检测超时',
      [DetectionStatus.CANCELLED]: '已取消'
    }
    return labels[status] || status
  },

  // 获取检测结果标签
  getDetectionResultLabel: (result: DetectionResult) => {
    const labels = {
      [DetectionResult.ONLINE]: '在线',
      [DetectionResult.OFFLINE]: '离线',
      [DetectionResult.TIMEOUT]: '超时',
      [DetectionResult.UNREACHABLE]: '不可达',
      [DetectionResult.FILTERED]: '被过滤',
      [DetectionResult.UNKNOWN]: '未知'
    }
    return labels[result] || result
  },

  // 获取检测结果颜色
  getDetectionResultColor: (result: DetectionResult) => {
    const colors = {
      [DetectionResult.ONLINE]: 'green',
      [DetectionResult.OFFLINE]: 'red',
      [DetectionResult.TIMEOUT]: 'orange',
      [DetectionResult.UNREACHABLE]: 'red',
      [DetectionResult.FILTERED]: 'orange',
      [DetectionResult.UNKNOWN]: 'gray'
    }
    return colors[result] || 'gray'
  },

  // 获取指纹类型标签
  getFingerprintTypeLabel: (type: FingerprintType) => {
    const labels = {
      [FingerprintType.WEB_SERVER]: 'Web服务器',
      [FingerprintType.APPLICATION_SERVER]: '应用服务器',
      [FingerprintType.DATABASE]: '数据库',
      [FingerprintType.OPERATING_SYSTEM]: '操作系统',
      [FingerprintType.WEB_FRAMEWORK]: 'Web框架',
      [FingerprintType.CMS]: '内容管理系统',
      [FingerprintType.PROGRAMMING_LANGUAGE]: '编程语言',
      [FingerprintType.MIDDLEWARE]: '中间件',
      [FingerprintType.LOAD_BALANCER]: '负载均衡器',
      [FingerprintType.WAF]: 'Web应用防火墙',
      [FingerprintType.CDN]: '内容分发网络',
      [FingerprintType.CACHE]: '缓存系统',
      [FingerprintType.MONITORING]: '监控系统',
      [FingerprintType.SECURITY_PRODUCT]: '安全产品',
      [FingerprintType.NETWORK_DEVICE]: '网络设备',
      [FingerprintType.IOT_DEVICE]: '物联网设备',
      [FingerprintType.OTHER]: '其他'
    }
    return labels[type] || type
  },

  // 获取置信度颜色
  getConfidenceColor: (confidence: number) => {
    if (confidence >= 80) return 'green'
    if (confidence >= 60) return 'blue'
    if (confidence >= 40) return 'orange'
    return 'red'
  },

  // 格式化响应时间
  formatResponseTime: (responseTime?: number) => {
    if (!responseTime) return '-'
    if (responseTime < 1000) return `${responseTime}ms`
    return `${(responseTime / 1000).toFixed(2)}s`
  },

  // 格式化指纹显示名称
  formatFingerprintName: (fingerprint: AssetFingerprint) => {
    let name = fingerprint.name
    if (fingerprint.version) {
      name += ` ${fingerprint.version}`
    }
    if (fingerprint.vendor) {
      name += ` (${fingerprint.vendor})`
    }
    return name
  }
}

export default assetDetectionApi
