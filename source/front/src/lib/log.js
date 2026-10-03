/**
 * 日志审计的枚举文案（sql/158）。
 *
 * 这些码值后端没建字典（就 7 个值、且是审计口径，不适合让管理员在字典里改），
 * 所以前端单点定义在这里 —— 页面不要各写一份。
 * 后端 VO 里同时带了 xxxText 字段，优先用后端的；这里的文本只用于下拉选项与兜底。
 */

// 操作日志业务类型（sys_oper_log.business_type）
export const OPER_BUSINESS_TYPE = [
  { value: 0, label: '其他' },
  { value: 1, label: '新增' },
  { value: 2, label: '修改' },
  { value: 3, label: '删除' },
  { value: 4, label: '授权' },
  { value: 5, label: '导出' },
  { value: 6, label: '导入' },
  { value: 7, label: '清空' },
]

// 操作日志状态（0-正常 1-异常）
export const OPER_STATUS = [
  { value: 0, label: '正常' },
  { value: 1, label: '异常' },
]

// 登录日志状态（0-成功 1-失败）
export const LOGIN_STATUS = [
  { value: 0, label: '成功' },
  { value: 1, label: '失败' },
]

// 审计日志状态（注意与操作日志相反：1-成功 0-失败）
export const AUDIT_STATUS = [
  { value: 1, label: '成功' },
  { value: 0, label: '失败' },
]

// 字段级修改日志：对象类型（sys_field_change_log.biz_type）
// 新增对象类型时这里必须同步补 —— 后端 bizTypeText 认不出会原样返回码值，界面上会露出原始码值。
export const FIELD_CHANGE_BIZ_TYPE = [
  { value: 'PATIENT', label: '患者主档' },
  { value: 'USER', label: '系统用户' },
  { value: 'EMPLOYEE', label: '员工档案' },
  { value: 'MEDICAL_RECORD', label: '门诊病历' },
  { value: 'INPATIENT_RECORD', label: '住院文书' },
]

// 变更类型：INSERT-建档（只有新值）UPDATE-修改（新旧都有）ACTION-操作留痕（无字段级值）
export const FIELD_CHANGE_TYPE = [
  { value: 'INSERT', label: '建档' },
  { value: 'UPDATE', label: '修改' },
  { value: 'ACTION', label: '操作' },
]

export const LOG_TYPE = { OPER: 1, LOGIN: 2, AUDIT: 3, FIELD_CHANGE: 4 }

export const bizTypeText = (v) => {
  const hit = FIELD_CHANGE_BIZ_TYPE.find((i) => i.value === v)
  return hit ? hit.label : v || '未知'
}

export const changeTypeTagType = (v) => {
  if (v === 'INSERT') return 'success'
  if (v === 'UPDATE') return 'warning'
  return 'info'
}

export const operStatusTagType = (v) => (v === 1 ? 'danger' : 'success')
export const loginStatusTagType = (v) => (v === 1 ? 'danger' : 'success')
export const auditStatusTagType = (v) => (v === 1 ? 'success' : 'danger')

export const businessTypeText = (v) => {
  const hit = OPER_BUSINESS_TYPE.find((i) => i.value === v)
  return hit ? hit.label : `未知(${v})`
}
