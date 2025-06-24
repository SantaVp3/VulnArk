/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期时间
 * @param dateStr 日期字符串
 * @param format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串，如果输入无效则返回 '-'
 */
export function formatDateTime(dateStr: string | null | undefined, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!dateStr) {
    return '-'
  }

  try {
    // 处理后端返回的日期格式 "2025-06-23 14:45:51"
    let date: Date
    
    if (typeof dateStr === 'string') {
      // 如果是字符串，尝试解析
      if (dateStr.includes('T')) {
        // ISO 格式
        date = new Date(dateStr)
      } else if (dateStr.includes(' ')) {
        // "YYYY-MM-DD HH:mm:ss" 格式
        date = new Date(dateStr.replace(' ', 'T'))
      } else if (dateStr.includes('-')) {
        // "YYYY-MM-DD" 格式
        date = new Date(dateStr)
      } else {
        // 时间戳
        date = new Date(parseInt(dateStr))
      }
    } else {
      date = new Date(dateStr)
    }

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', dateStr)
      return '-'
    }

    // 根据格式返回相应的字符串
    switch (format) {
      case 'YYYY-MM-DD':
        return date.toISOString().split('T')[0]
      case 'YYYY-MM-DD HH:mm':
        return date.toISOString().slice(0, 16).replace('T', ' ')
      case 'YYYY-MM-DD HH:mm:ss':
        return date.toISOString().slice(0, 19).replace('T', ' ')
      case 'MM-DD HH:mm':
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${month}-${day} ${hours}:${minutes}`
      case 'relative':
        return getRelativeTime(date)
      default:
        return date.toISOString().slice(0, 19).replace('T', ' ')
    }
  } catch (error) {
    console.error('Date formatting error:', error, 'Input:', dateStr)
    return '-'
  }
}

/**
 * 格式化日期（只显示日期部分）
 * @param dateStr 日期字符串
 * @returns 格式化后的日期字符串
 */
export function formatDate(dateStr: string | null | undefined): string {
  return formatDateTime(dateStr, 'YYYY-MM-DD')
}

/**
 * 格式化时间（显示到分钟）
 * @param dateStr 日期字符串
 * @returns 格式化后的时间字符串
 */
export function formatTime(dateStr: string | null | undefined): string {
  return formatDateTime(dateStr, 'YYYY-MM-DD HH:mm')
}

/**
 * 获取相对时间（如：2小时前、3天前）
 * @param date 日期对象
 * @returns 相对时间字符串
 */
export function getRelativeTime(date: Date): string {
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffSeconds < 60) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return formatDate(date.toISOString())
  }
}

/**
 * 将日期字符串转换为 Date 对象
 * @param dateStr 日期字符串
 * @returns Date 对象或 null
 */
export function parseDate(dateStr: string | null | undefined): Date | null {
  if (!dateStr) {
    return null
  }

  try {
    let date: Date
    
    if (typeof dateStr === 'string') {
      if (dateStr.includes('T')) {
        date = new Date(dateStr)
      } else if (dateStr.includes(' ')) {
        date = new Date(dateStr.replace(' ', 'T'))
      } else {
        date = new Date(dateStr)
      }
    } else {
      date = new Date(dateStr)
    }

    return isNaN(date.getTime()) ? null : date
  } catch (error) {
    console.error('Date parsing error:', error, 'Input:', dateStr)
    return null
  }
}

/**
 * 检查日期是否有效
 * @param dateStr 日期字符串
 * @returns 是否有效
 */
export function isValidDate(dateStr: string | null | undefined): boolean {
  return parseDate(dateStr) !== null
}

/**
 * 格式化日期范围
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 格式化后的日期范围字符串
 */
export function formatDateRange(startDate: string | null | undefined, endDate: string | null | undefined): string {
  const start = formatDate(startDate)
  const end = formatDate(endDate)

  if (start === '-' && end === '-') {
    return '-'
  } else if (start === '-') {
    return `至 ${end}`
  } else if (end === '-') {
    return `${start} 至 未定`
  } else {
    return `${start} 至 ${end}`
  }
}

/**
 * 格式化相对时间（用于仪表盘）
 * @param dateStr 日期字符串
 * @returns 相对时间字符串
 */
export function formatRelativeTime(dateStr: string | null | undefined): string {
  const date = parseDate(dateStr)
  if (!date) return '-'
  return getRelativeTime(date)
}
