# Arco Design Vue 图标参考

## 🎯 已使用的图标

### 基础图标
- `IconDashboard` - 仪表盘
- `IconFolder` - 文件夹/项目
- `IconDesktop` - 桌面/资产
- `IconUser` - 用户
- `IconBug` - 漏洞/错误
- `IconSafe` - 安全/盾牌 (替代 IconShield)

### 操作图标
- `IconPlus` - 添加/新建
- `IconRefresh` - 刷新
- `IconSearch` - 搜索
- `IconUpload` - 上传
- `IconDownload` - 下载
- `IconSettings` - 设置
- `IconPoweroff` - 退出登录

### 状态图标
- `IconCheckCircle` - 完成/成功
- `IconExclamation` - 警告/高优先级 (替代 IconExclamationCircle)
- `IconPlayArrow` - 播放/活跃
- `IconArrowUp` - 上升趋势
- `IconMinus` - 稳定/无变化
- `IconClockCircle` - 时间
- `IconInfoCircle` - 信息

### 图表图标
- `IconBarChart` - 柱状图/趋势图 (替代 IconLineChart)
- `IconPieChart` - 饼图
- `IconList` - 列表

### 表单图标
- `IconLock` - 密码/锁定
- `IconRight` - 右箭头/提交

## 🚫 不存在的图标 (已替换)

- ❌ `IconShield` → ✅ `IconSafe`
- ❌ `IconLineChart` → ✅ `IconBarChart`
- ❌ `IconExclamationCircle` → ✅ `IconExclamation`

## 📚 常用图标分类

### 导航类
```javascript
import {
  IconDashboard,
  IconFolder,
  IconDesktop,
  IconUser,
  IconSettings
} from '@arco-design/web-vue/es/icon'
```

### 操作类
```javascript
import {
  IconPlus,
  IconRefresh,
  IconSearch,
  IconUpload,
  IconDownload,
  IconEdit,
  IconDelete
} from '@arco-design/web-vue/es/icon'
```

### 状态类
```javascript
import {
  IconCheckCircle,
  IconExclamation,
  IconInfoCircle,
  IconCloseCircle,
  IconPlayArrow,
  IconPause
} from '@arco-design/web-vue/es/icon'
```

### 图表类
```javascript
import {
  IconBarChart,
  IconPieChart,
  IconLineChart, // 注意：这个可能不存在
  IconAreaChart
} from '@arco-design/web-vue/es/icon'
```

## 💡 使用建议

1. **图标一致性**: 在同类功能中使用相同的图标
2. **语义化**: 选择符合功能语义的图标
3. **大小统一**: 保持图标大小的一致性
4. **颜色搭配**: 图标颜色要与主题色彩协调

## 🔍 图标查找方法

1. 访问 [Arco Design 图标库](https://arco.design/vue/component/icon)
2. 在代码中先导入测试是否存在
3. 使用浏览器开发者工具检查图标是否正确渲染

## 🛠️ 自定义图标

如果需要特殊图标，可以考虑：
1. 使用 SVG 图标
2. 引入第三方图标库 (如 Heroicons, Feather Icons)
3. 创建自定义图标组件

## 📝 注意事项

- 图标名称区分大小写
- 导入时使用正确的路径：`@arco-design/web-vue/es/icon`
- 某些图标可能在不同版本中有变化
- 建议在使用前先在官方文档中确认图标是否存在
