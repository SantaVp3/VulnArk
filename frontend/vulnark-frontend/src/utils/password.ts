// 密码强度等级
export enum PasswordStrength {
  WEAK = 'weak',
  MEDIUM = 'medium',
  STRONG = 'strong',
  VERY_STRONG = 'very_strong'
}

// 密码强度检查结果
export interface PasswordStrengthResult {
  strength: PasswordStrength
  score: number
  feedback: string[]
  color: string
  percentage: number
}

// 密码验证规则
export const passwordRules = {
  minLength: 8,
  maxLength: 128,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,
  specialChars: '!@#$%^&*()_+-=[]{}|;:,.<>?'
}

/**
 * 检查密码强度
 * @param password 密码
 * @returns 密码强度结果
 */
export function checkPasswordStrength(password: string): PasswordStrengthResult {
  const feedback: string[] = []
  let score = 0

  // 长度检查
  if (password.length < passwordRules.minLength) {
    feedback.push(`密码长度至少${passwordRules.minLength}位`)
  } else if (password.length >= passwordRules.minLength) {
    score += 1
  }

  if (password.length >= 12) {
    score += 1
  }

  // 大写字母检查
  if (!/[A-Z]/.test(password)) {
    feedback.push('密码应包含大写字母')
  } else {
    score += 1
  }

  // 小写字母检查
  if (!/[a-z]/.test(password)) {
    feedback.push('密码应包含小写字母')
  } else {
    score += 1
  }

  // 数字检查
  if (!/\d/.test(password)) {
    feedback.push('密码应包含数字')
  } else {
    score += 1
  }

  // 特殊字符检查
  const specialCharRegex = new RegExp(`[${passwordRules.specialChars.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}]`)
  if (!specialCharRegex.test(password)) {
    feedback.push('密码应包含特殊字符')
  } else {
    score += 1
  }

  // 连续字符检查
  if (/(.)\1{2,}/.test(password)) {
    feedback.push('避免使用连续相同字符')
    score -= 1
  }

  // 常见密码检查
  const commonPasswords = ['password', '123456', 'admin', 'qwerty', 'abc123']
  if (commonPasswords.some(common => password.toLowerCase().includes(common))) {
    feedback.push('避免使用常见密码')
    score -= 1
  }

  // 确定强度等级
  let strength: PasswordStrength
  let color: string
  let percentage: number

  if (score <= 2) {
    strength = PasswordStrength.WEAK
    color = '#f5222d'
    percentage = 25
  } else if (score <= 4) {
    strength = PasswordStrength.MEDIUM
    color = '#fa8c16'
    percentage = 50
  } else if (score <= 5) {
    strength = PasswordStrength.STRONG
    color = '#52c41a'
    percentage = 75
  } else {
    strength = PasswordStrength.VERY_STRONG
    color = '#1890ff'
    percentage = 100
  }

  return {
    strength,
    score: Math.max(0, score),
    feedback,
    color,
    percentage
  }
}

/**
 * 获取密码强度文本
 * @param strength 密码强度
 * @returns 强度文本
 */
export function getPasswordStrengthText(strength: PasswordStrength): string {
  const texts = {
    [PasswordStrength.WEAK]: '弱',
    [PasswordStrength.MEDIUM]: '中等',
    [PasswordStrength.STRONG]: '强',
    [PasswordStrength.VERY_STRONG]: '很强'
  }
  return texts[strength]
}

/**
 * 生成随机密码
 * @param length 密码长度
 * @param includeSpecialChars 是否包含特殊字符
 * @returns 生成的密码
 */
export function generateRandomPassword(length: number = 12, includeSpecialChars: boolean = true): string {
  const lowercase = 'abcdefghijklmnopqrstuvwxyz'
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const numbers = '0123456789'
  const specialChars = '!@#$%^&*()_+-='

  let charset = lowercase + uppercase + numbers
  if (includeSpecialChars) {
    charset += specialChars
  }

  let password = ''
  
  // 确保至少包含每种类型的字符
  password += lowercase[Math.floor(Math.random() * lowercase.length)]
  password += uppercase[Math.floor(Math.random() * uppercase.length)]
  password += numbers[Math.floor(Math.random() * numbers.length)]
  
  if (includeSpecialChars) {
    password += specialChars[Math.floor(Math.random() * specialChars.length)]
  }

  // 填充剩余长度
  for (let i = password.length; i < length; i++) {
    password += charset[Math.floor(Math.random() * charset.length)]
  }

  // 打乱密码字符顺序
  return password.split('').sort(() => Math.random() - 0.5).join('')
}

/**
 * 验证密码是否符合要求
 * @param password 密码
 * @returns 验证结果
 */
export function validatePassword(password: string): { valid: boolean; errors: string[] } {
  const errors: string[] = []

  if (!password) {
    errors.push('密码不能为空')
    return { valid: false, errors }
  }

  if (password.length < passwordRules.minLength) {
    errors.push(`密码长度至少${passwordRules.minLength}位`)
  }

  if (password.length > passwordRules.maxLength) {
    errors.push(`密码长度不能超过${passwordRules.maxLength}位`)
  }

  if (passwordRules.requireUppercase && !/[A-Z]/.test(password)) {
    errors.push('密码必须包含大写字母')
  }

  if (passwordRules.requireLowercase && !/[a-z]/.test(password)) {
    errors.push('密码必须包含小写字母')
  }

  if (passwordRules.requireNumbers && !/\d/.test(password)) {
    errors.push('密码必须包含数字')
  }

  if (passwordRules.requireSpecialChars) {
    const specialCharRegex = new RegExp(`[${passwordRules.specialChars.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}]`)
    if (!specialCharRegex.test(password)) {
      errors.push('密码必须包含特殊字符')
    }
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 验证确认密码
 * @param password 原密码
 * @param confirmPassword 确认密码
 * @returns 验证结果
 */
export function validateConfirmPassword(password: string, confirmPassword: string): { valid: boolean; error?: string } {
  if (!confirmPassword) {
    return { valid: false, error: '请确认密码' }
  }

  if (password !== confirmPassword) {
    return { valid: false, error: '两次输入的密码不一致' }
  }

  return { valid: true }
}
