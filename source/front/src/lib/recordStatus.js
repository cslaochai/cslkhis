/**
 * 病历 / 审核状态 文案 —— 全前端唯一口径来源
 *
 * 口径判定依据（实测 2026-09-23，三方证据一致）：
 *   1) 列注释：biz_medical_record.record_status「病历状态（1-草稿 2-已提交 3-已归档 4-已作废）」
 *              biz_medical_record.review_status「审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）」
 *   2) 字典表：sys_dict_data dict_type='his_record_status'      1草稿 2已提交 3已归档 4已作废
 *              sys_dict_data dict_type='his_review_status'      0待提交 1待审核 2审核通过 3审核驳回
 *   3) 真实数据：biz_medical_record 19 行 —— (2,1)×13 / (2,2)×3 / (1,0)×2 / (3,0)×1 / (2,0)×2
 *              record_status=4 与 review_status=3 暂无用例，但两处口径都已定义，按定义渲染
 *
 * ⚠ 修掉的两处错口径：
 *   - `RecordsView.vue` 曾写三元链 `recordStatus === 3 ? '已归档' : 2 ? '已提交' : '草稿'`
 *     → **没有第 4 档**，`record_status=4（已作废）`会被渲染成「草稿」。
 *     把"病历已作废"显示成"草稿"，医生会以为还能继续写。
 *   - `MedicalRecordView.vue` 自带一份 statusMap/reviewMap，与本文件重复；
 *     两份措辞还不一样（未提交/待提交、已通过/审核通过），同一状态两个说法。
 *
 * ⚠ 不要拿 his_inpatient_record_status（住院病历，只有 1草稿 2已提交 3已归档，**无作废档**）
 *   当门诊病历状态用；也不要拿 his_inpatient_record_type 当状态 —— 那是文书类型。
 *
 * 未知码值渲染「未知(n)」，不回落成看似合法的值。
 */

/** 门诊病历状态码值 */
export const RECORD_STATUS_DRAFT = 1
export const RECORD_STATUS_SUBMITTED = 2
export const RECORD_STATUS_ARCHIVED = 3
export const RECORD_STATUS_VOIDED = 4

/** 病历状态下拉选项（筛选 + 渲染共用） */
export const RECORD_STATUS_OPTIONS = [
  { label: '草稿', value: RECORD_STATUS_DRAFT, tagType: 'info' },
  { label: '已提交', value: RECORD_STATUS_SUBMITTED, tagType: 'warning' },
  { label: '已归档', value: RECORD_STATUS_ARCHIVED, tagType: 'success' },
  { label: '已作废', value: RECORD_STATUS_VOIDED, tagType: 'danger' },
]

/** 审核状态下拉选项 */
export const REVIEW_STATUS_OPTIONS = [
  { label: '待提交', value: 0, tagType: 'info' },
  { label: '待审核', value: 1, tagType: 'warning' },
  { label: '审核通过', value: 2, tagType: 'success' },
  { label: '审核驳回', value: 3, tagType: 'danger' },
]

/**
 * 病历状态码值 → 文案
 * @param {number|string|null|undefined} value
 * @returns {string} 草稿 / 已提交 / 已归档 / 已作废 / 未知(n) / —
 */
export function recordStatusText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = RECORD_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

/**
 * 病历状态码值 → el-tag type（未命中给 info，文案侧已渲染「未知(n)」不会误导）
 * @param {number|string|null|undefined} value
 */
export function recordStatusTagType(value) {
  return RECORD_STATUS_OPTIONS.find(o => o.value === Number(value))?.tagType || 'info'
}

/**
 * 审核状态码值 → 文案
 * @param {number|string|null|undefined} value
 * @returns {string} 待提交 / 待审核 / 审核通过 / 审核驳回 / 未知(n) / —
 */
export function reviewStatusText(value) {
  if (value === null || value === undefined || value === '') return '—'
  const hit = REVIEW_STATUS_OPTIONS.find(o => o.value === Number(value))
  return hit ? hit.label : `未知(${value})`
}

/**
 * 审核状态码值 → el-tag type
 * @param {number|string|null|undefined} value
 */
export function reviewStatusTagType(value) {
  return REVIEW_STATUS_OPTIONS.find(o => o.value === Number(value))?.tagType || 'info'
}
