// 纯静态 UI 字典（不来自后端、不随数据变化）
export const INSURANCE_TYPES = ['职工医保', '居民医保', '异地就医']
export const SETTLE_TYPES = ['门诊', '住院']
export const BILL_STATUS = ['已结算', '待审核', '已退费', '异常']
export const CATALOG_CATEGORIES = ['西药', '中成药', '中草药', '诊疗项目', '医用耗材']

// ---------------- 以下为 HIS 系统管理页面抄入的静态字典 ----------------

// 用户状态
export const USER_STATUS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

// 员工类型
export const EMP_TYPE = [
  { label: '医生', value: 1 },
  { label: '护士', value: 2 },
  { label: '药剂师', value: 3 },
  { label: '收费员', value: 4 },
  { label: '管理员', value: 5 },
]

// 是否
export const YES_NO = [
  { label: '是', value: 1 },
  { label: '否', value: 0 },
]

// 菜单类型
export const MENU_TYPE = [
  { label: '目录', value: 1 },
  { label: '菜单', value: 2 },
  { label: '按钮', value: 3 },
]

/**
 * 根据值获取标签
 * @param {Array} dictList - 字典列表
 * @param {*} value - 值
 * @returns {string} 标签文本
 */
export function getDictLabel(dictList, value) {
  const item = dictList.find(item => item.value == value)
  return item ? item.label : ''
}

/**
 * 根据值获取标签（带颜色）
 * @param {Array} dictList - 字典列表
 * @param {*} value - 值
 * @returns {object} { label, type } type 用于 el-tag
 */
export function getDictTag(dictList, value) {
  const item = dictList.find(item => item.value == value)
  if (!item) return { label: '', type: 'info' }

  // 根据状态返回对应颜色
  if (value == 1 || value === 1) return { label: item.label, type: 'success' }
  if (value == 0 || value === 0) return { label: item.label, type: 'danger' }
  return { label: item.label, type: 'info' }
}
