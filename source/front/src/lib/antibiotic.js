// 抗菌药物管理口径（sql/161）：码值 → 文案唯一口径。
// 后端 AntibioticServiceImpl / AntibioticMonitorServiceImpl 用同一套文案，
// 前端不自己翻译码值（翻译错就是把"限制级"显示成"非限制级"）。

/** 抗菌药物分级（sys_drug.antibiotic_level，字典 his_antibiotic_level） */
export const ANTIBIOTIC_LEVEL = {
  0: '非抗菌药物',
  1: '非限制使用级',
  2: '限制使用级',
  3: '特殊使用级'
}

/** 授权状态（biz_antibiotic_auth.status，字典 his_antibiotic_auth_status） */
export const ANTIBIOTIC_AUTH_STATUS = {
  1: '有效',
  2: '暂停',
  3: '取消'
}

export const ANTIBIOTIC_STATUS_TAG = {
  1: 'success',
  2: 'warning',
  3: 'danger'
}

/** 围手术期给药时机（字典 his_antibiotic_timing） */
export const ANTIBIOTIC_TIMING = {
  1: '术前0.5~1小时',
  2: '术前>1小时',
  3: '术前<0.5小时',
  4: '术中追加',
  5: '术后才开始',
  6: '未使用'
}

/** I 类切口预防用药问题码（字典 his_antibiotic_incision_problem，41~48） */
export const INCISION_PROBLEM = {
  41: '无预防用药指征',
  42: '品种选择不合理',
  43: '给药时机不合理',
  44: '疗程过长',
  45: '无指征联合用药',
  46: '剂量不合理',
  47: '特殊使用级无会诊',
  48: '术后用药起点不明'
}

export function antibioticLevelText(level) {
  return ANTIBIOTIC_LEVEL[level] ?? (level === null || level === undefined ? '—' : `未知(${level})`)
}

export function authStatusText(status) {
  return ANTIBIOTIC_AUTH_STATUS[status] ?? (status === null || status === undefined ? '—' : `未知(${status})`)
}

export function timingText(timing) {
  return ANTIBIOTIC_TIMING[timing] ?? (timing === null || timing === undefined ? '—' : `未知(${timing})`)
}

export function incisionProblemText(codes) {
  if (!codes) return '—'
  return String(codes).split(',').map(c => INCISION_PROBLEM[c.trim()] || c.trim()).join('、')
}
