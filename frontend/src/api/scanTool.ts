import request from './request'

export interface ScanTool {
  id: number
  name: string
  displayName: string
  currentVersion?: string
  latestVersion?: string
  installPath?: string
  configPath?: string
  status: 'NOT_INSTALLED' | 'INSTALLING' | 'INSTALLED' | 'UPDATING' | 'ERROR' | 'OUTDATED'
  autoUpdate: boolean
  downloadUrl?: string
  checksum?: string
  lastCheckTime?: string
  lastUpdateTime?: string
  errorMessage?: string
  fileSize?: number
  downloadProgress: number
}

export interface ScanToolStatus {
  name: string
  displayName: string
  status: string
  currentVersion?: string
  latestVersion?: string
  downloadProgress: number
  errorMessage?: string
  needsUpdate: boolean
  isInstalled: boolean
  lastCheckTime?: string
  lastUpdateTime?: string
}

export interface ScanToolStatistics {
  totalTools: number
  installedTools: number
  needUpdateTools: number
  statusDistribution: Record<string, number>
}

/**
 * 获取所有扫描工具
 */
export const getAllTools = () => {
  return request.get('/scan-tools')
}

/**
 * 获取所有扫描工具（管理员）
 */
export function getScanTools() {
  return request({
    url: '/admin/scan-tools',
    method: 'get'
  });
}

/**
 * 添加扫描工具
 */
export function addScanTool(data: any) {
  return request({
    url: '/admin/scan-tools',
    method: 'post',
    data
  });
}

/**
 * 更新扫描工具
 */
export function updateScanTool(id: number, data: any) {
  return request({
    url: `/admin/scan-tools/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除扫描工具
 */
export function deleteScanTool(id: number) {
  return request({
    url: `/admin/scan-tools/${id}`,
    method: 'delete'
  });
}

/**
 * 获取可下载的工具列表
 */
export function getAvailableTools() {
  return request({
    url: '/admin/tools/available',
    method: 'get'
  });
}

/**
 * 获取工具下载链接
 */
export function getToolDownloadUrl(toolType: string, version?: string, platform?: string, arch?: string) {
  let url = `/admin/tools/download/${toolType}`;
  const params: Record<string, string> = {};
  
  if (version) params.version = version;
  if (platform) params.platform = platform;
  if (arch) params.arch = arch;
  
  return request({
    url,
    method: 'post',
    params
  });
}

/**
 * 检查工具安装状态
 */
export function checkToolStatus(toolType: string) {
  return request({
    url: `/admin/tools/status/${toolType}`,
    method: 'get'
  });
}

/**
 * 获取工具状态
 */
export const getToolStatus = (toolName: string) => {
  return request.get(`/scan-tools/${toolName}/status`)
}

/**
 * 安装工具
 */
export const installTool = (toolName: string) => {
  return request.post(`/scan-tools/${toolName}/install`)
}

/**
 * 更新工具
 */
export const updateTool = (toolName: string) => {
  return request.post(`/scan-tools/${toolName}/update`)
}

/**
 * 检查所有工具更新
 */
export const checkUpdates = () => {
  return request.post('/scan-tools/check-updates')
}

/**
 * 获取工具统计信息
 */
export const getStatistics = () => {
  return request.get('/scan-tools/statistics')
}

/**
 * 获取已安装的工具
 */
export const getInstalledTools = () => {
  return request.get('/scan-tools/installed')
}

/**
 * 检查工具是否可用
 */
export const checkToolAvailable = (toolName: string) => {
  return request.get(`/scan-tools/${toolName}/available`)
}

/**
 * 初始化默认工具
 */
export const initializeTools = () => {
  return request.post('/scan-tools/initialize')
}

export default {
  getAllTools,
  getToolStatus,
  installTool,
  updateTool,
  checkUpdates,
  getStatistics,
  getInstalledTools,
  checkToolAvailable,
  initializeTools
}
