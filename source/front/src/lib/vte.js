/**
 * VTE 防控口径（sql/167）—— 与后端 VteRules / VteServiceImpl **逐字对齐**，
 * 页面一律从这里取枚举文案与推荐矩阵，禁止在页面里写第二份映射。
 *
 * ⚠ 风险等级、落实率、发生率全部由服务端算；本文件的 codesOfRisk 只用于
 *   「按等级高亮推荐措施」的渲染提示，不是判定依据。
 */
import { RISK_LEVEL_TEXT } from './nursingAssessment'

/** 措施码（一次住院一条的固定三项） */
export const MEASURE_CODE = {
  BASIC: 'BASIC',
  PHYSICAL: 'PHYSICAL',
  DRUG: 'DRUG',
}

export const MEASURE_DEFS = [
  { code: 'BASIC', type: 1, name: '基础预防', desc: '健康教育、早期活动/踝泵运动、避免脱水、慎用止血药' },
  { code: 'PHYSICAL', type: 2, name: '物理预防', desc: '梯度压力袜（GCS）/间歇充气加压装置（IPC）/足底静脉泵（VFP）' },
  { code: 'DRUG', type: 3, name: '药物预防', desc: '低分子肝素/普通肝素/利伐沙班等；有活动性出血等禁忌者禁用' },
]

export const MEASURE_CODE_TEXT = {
  BASIC: '基础预防',
  PHYSICAL: '物理预防',
  DRUG: '药物预防',
}

export const EXECUTE_STATUS_TEXT = {
  0: '待落实',
  1: '已落实',
  2: '禁忌未用',
  3: '患者拒绝',
}

/** 落实状态 → tag 色；禁忌/拒绝是"有原因的未落实"，用 warning 区分于"忘了做" */
export const EXECUTE_STATUS_TAG = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'warning',
}

export const EVENT_TYPE_TEXT = {
  1: '深静脉血栓（DVT）',
  2: '肺栓塞（PE）',
  3: '预防相关出血',
}

export const ONSET_TYPE_TEXT = {
  1: '院内发生',
  2: '入院时已存在',
}

export const BASIS_TEXT = {
  1: '超声',
  2: 'CT 肺动脉造影',
  3: '静脉造影',
  4: '临床诊断',
  5: '其他',
}

export const OUTCOME_TEXT = {
  1: '好转',
  2: '未愈',
  3: '死亡',
  4: '未知',
}

export const PREVENT_STATUS_TEXT = {
  0: '未落实',
  1: '部分落实',
  2: '已落实',
}

export const PREVENT_STATUS_TAG = {
  0: 'danger',
  1: 'warning',
  2: 'success',
}

/**
 * 按风险等级推荐应落实的措施码（与后端 VteRules.codesOf 同口径）：
 * 低危 → 基础；中危 → 基础 + 物理；高危/极高危 → 基础 + 物理 + 药物。
 */
export function codesOfRisk(riskLevel) {
  if (!riskLevel || riskLevel <= 1) return [MEASURE_CODE.BASIC]
  if (riskLevel === 2) return [MEASURE_CODE.BASIC, MEASURE_CODE.PHYSICAL]
  return [MEASURE_CODE.BASIC, MEASURE_CODE.PHYSICAL, MEASURE_CODE.DRUG]
}

export function riskLevelText(level) {
  return RISK_LEVEL_TEXT[level] || '未评'
}

export function riskLevelTag(level) {
  return { 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }[level] || 'info'
}
