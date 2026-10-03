/**
 * 营养膳食口径（sql/168）—— 与后端 NutritionRules / ConsultationLabels **逐字对齐**，
 * 页面一律从这里取枚举与目标值，禁止在页面里写第二份映射。
 *
 * ⚠ 只有「渲染」在这里：NRS2002 总分、有无营养风险、BMI、复筛日期、风险等级、
 *   订餐状态机推进是否合法，全部由服务端判定（前端算的一律视为不可信输入）。
 */

/** 量表：1-NRS2002 2-PG-SGA 3-MNA */
export const SCREEN_TYPE_TEXT = {
  1: 'NRS2002 营养风险筛查',
  2: 'PG-SGA 主观整体评估',
  3: 'MNA 老年微型营养评估',
}

/** 筛查时机 */
export const SCREEN_SOURCE_TEXT = {
  1: '入院48小时内',
  2: '病情变化复筛',
  3: '术后复筛',
  4: '定期复筛',
}

/** 判定：0-无营养风险 1-有营养风险（NRS2002 总分 ≥3） */
export const RISK_FLAG_TEXT = {
  0: '无营养风险',
  1: '有营养风险',
}

export const RISK_FLAG_TAG = {
  0: 'success',
  1: 'danger',
}

/** NRS2002 判风险阈值（与后端 NutritionRules.NRS_RISK_CUTOFF 一致，只用于表单提示） */
export const NRS_RISK_CUTOFF = 3
/** 阴性复筛间隔（天） */
export const RE_SCREEN_DAYS = 7

/** 饮食类别 */
export const DIET_CATEGORY_TEXT = {
  1: '基本饮食',
  2: '治疗饮食',
  3: '诊断试验饮食',
  4: '营养支持',
}

/** 给食途径：只有口服进食堂订餐 */
export const DIET_ROUTE_TEXT = {
  1: '口服',
  2: '管饲',
  3: '静脉（肠外）',
}

/** 订餐餐次 */
export const MEAL_TYPE_TEXT = {
  1: '早餐',
  2: '午餐',
  3: '晚餐',
  4: '加餐',
}

/** 订餐状态：0 待配餐 → 1 已配餐 → 2 已配送 → 3 已签收；4 已取消是旁路 */
export const MEAL_STATUS_TEXT = {
  0: '待配餐',
  1: '已配餐',
  2: '已配送',
  3: '已签收',
  4: '已取消',
}

export const MEAL_STATUS_TAG = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger',
}

/** 状态机下一步（后端 NutritionRules.mealNextStatus 同口径；页面只按它显示按钮文案） */
export const MEAL_NEXT_STATUS = {
  0: 1,
  1: 2,
  2: 3,
}

/** 膳食方案状态 */
export const PLAN_STATUS_TEXT = {
  1: '执行中',
  2: '已停止',
  3: '已作废',
}

export const PLAN_STATUS_TAG = {
  1: 'success',
  2: 'info',
  3: 'danger',
}

/** 营养科接收状态：执行率分子只数 1-已接收 */
export const CONFIRM_STATUS_TEXT = {
  0: '待接收',
  1: '已接收',
  2: '已退回',
}

export const CONFIRM_STATUS_TAG = {
  0: 'warning',
  1: 'success',
  2: 'danger',
}

/** 方案来源 */
export const PLAN_SOURCE_TEXT = {
  1: '医嘱校对派生',
  2: '营养师登记',
}

/** 会诊类别（后端 ConsultationLabels.categoryText 同口径） */
export const CONSULT_CATEGORY_TEXT = {
  1: '普通科间会诊',
  2: '营养会诊',
  3: '药学会诊',
  4: '其他专科会诊',
}

/** 营养会诊类别码（发起时固定提交给后端，前端不选） */
export const CONSULT_CATEGORY_NUTRITION = 2

/** 指标目标值（等级评审常用阈值，只作提示不判定；与 NutritionRules.TARGET_* 一致） */
export const STATS_TARGETS = {
  screenRate: 90,
  dietConfirmRate: 95,
  consultOnTimeRate: 90,
  mealSignRate: 95,
}

/** 分项评分下拉：受损 / 严重度 0~3，年龄项 0~1（由服务端按患者年龄算，表单只读展示） */
export const SCORE_OPTIONS_0_3 = [
  { value: 0, label: '0 分' },
  { value: 1, label: '1 分' },
  { value: 2, label: '2 分' },
  { value: 3, label: '3 分' },
]

/** 提示用：NRS2002 三个分项现算总分（不参与判定，判定永远以后端返回的 totalScore/riskFlag 为准） */
export function nrsLocalTotal(impair, severity, ageScore) {
  return (Number(impair) || 0) + (Number(severity) || 0) + (Number(ageScore) || 0)
}

/** 提示用：本地按 ≥3 预判风险，仅用于表单里"预计将有营养风险"的黄条提示 */
export function nrsLocalRisk(total) {
  return total >= NRS_RISK_CUTOFF
}
