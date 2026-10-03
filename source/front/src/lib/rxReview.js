/**
 * 处方点评码值单点定义（菜单 510/511，sql/160）。
 *
 * 口径依据《医院处方点评管理规范（试行）》（卫医管发〔2010〕28号）：
 * 不合理处方 = 不规范处方 + 用药不适宜处方 + 超常处方；问题码三组与结论分组一一对应。
 */

/** 点评结论（biz_rx_review_item.review_result） */
export const REVIEW_RESULT = {
  1: '合理处方',
  2: '不规范处方',
  3: '用药不适宜处方',
  4: '超常处方'
}

/** 结论 → Element Plus tag 类型 */
export const RESULT_TAG_TYPE = { 1: 'success', 2: 'warning', 3: 'warning', 4: 'danger' }

/** 不合理结论集合（2/3/4） */
export const UNREASONABLE_RESULTS = [2, 3, 4]

/** 问题码分组（key=结论）：勾选问题项必须与结论同组 */
export const PROBLEM_GROUPS = {
  2: [
    { code: '11', label: '处方前记/正文/后记缺项或书写不规范' },
    { code: '12', label: '未使用药品规范名称' },
    { code: '13', label: '药品剂量、规格、数量书写不规范' },
    { code: '14', label: '单张门急诊处方超过 5 种药品未注明原因' },
    { code: '15', label: '处方修改未签名并注明修改日期' }
  ],
  3: [
    { code: '21', label: '适应证不适宜' },
    { code: '22', label: '遴选药品不适宜' },
    { code: '23', label: '药品剂型或给药途径不适宜' },
    { code: '24', label: '用法用量不适宜' },
    { code: '25', label: '联合用药不适宜' },
    { code: '26', label: '重复给药' },
    { code: '27', label: '配伍禁忌或不良相互作用' }
  ],
  4: [
    { code: '31', label: '无适应证用药' },
    { code: '32', label: '无正当理由开具高价药' },
    { code: '33', label: '无正当理由超说明书用药' },
    { code: '34', label: '无正当理由为同一患者同时开具 2 种以上药理作用相同药物' }
  ]
}

/** 点评状态 */
export const REVIEW_STATUS = { 0: '待点评', 1: '已点评' }

/** 公示状态 */
export const PUBLICITY_STATUS = { 0: '未公示', 1: '已公示' }

/** 批次状态 */
export const BATCH_STATUS = { 1: '进行中', 2: '已完成' }

/** 点评类型 */
export const REVIEW_TYPE = { 1: '常规点评', 2: '专项点评' }

/** 处方类型（biz_prescription.prescription_type） */
export const RX_TYPE = { 1: '西药处方', 2: '中成药处方', 3: '中药饮片处方' }

/** 处方来源（biz_prescription.prescription_source） */
export const RX_SOURCE = { 1: '门诊', 2: '急诊', 3: '住院' }

/** 约谈类型（biz_rx_doctor_talk.talk_type）：对应规范「警告 → 限制处方权 → 取消处方权」递进 */
export const TALK_TYPE = { 1: '首次约谈', 2: '警告约谈', 3: '限制处方权', 4: '取消处方权', 5: '恢复处方权' }

/** 整改状态 */
export const RECTIFY_STATUS = { 1: '待整改', 2: '已整改' }

/** 结论文案（未知码值不回落，显示 未知(n)） */
export function resultText(result) {
  if (result == null) return '未点评'
  return REVIEW_RESULT[result] || `未知(${result})`
}

/** 问题码 "21,24" → 「适应证不适宜、用法用量不适宜」 */
export function problemTypesText(problemTypes) {
  if (!problemTypes) return ''
  const map = {}
  for (const g of Object.values(PROBLEM_GROUPS)) {
    for (const p of g) map[p.code] = p.label
  }
  return problemTypes.split(',').map(c => map[c] || `未知(${c})`).join('、')
}
