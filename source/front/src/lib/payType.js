/**
 * 费别（参保类型）口径 —— 全前端唯一口径来源
 *
 * 患者主档只存两列：
 *   - medical_insurance_type：费别，字典 medical_insurance_type，值即中文标准名
 *   - medical_insurance_no  ：医保号（费别为医保类时填写）
 * 已无独立的 patient_type 字段。
 *
 * 标准费别：自费 / 城镇职工医保 / 城乡居民医保 / 公费医疗 / 商业医疗保险。
 * 本单怎么结（全自费 / 医保联网）是挂号单上的 settlement_type（两档），
 * 与费别是两回事：费别是患者属性，结算方式是这一单的选择（医保患者也可本单改自费）。
 */

/**
 * 该费别是否走医保统筹（与后端 MedicalInsuranceTypeEnum.isPoolCovered 对齐）：
 * 职工 / 居民（含新农合）/ 公费走统筹；自费、商业保险、空值都不走。
 * @param {string} medicalInsuranceType 费别中文名
 * @returns {boolean}
 */
export function isInsuranceFeeType(medicalInsuranceType) {
  const t = String(medicalInsuranceType || '')
  if (!t) return false
  if (t.includes('自费') || t.includes('商业')) return false
  return t.includes('职工')
      || t.includes('居民')
      || t.includes('农村')
      || t.includes('新农合')
      || t.includes('公费')
}

/**
 * 费别短标签（角标用）：自费返回空串（默认态不打标），其余压缩成两到四字。
 * @param {string} medicalInsuranceType
 * @returns {string}
 */
export function feeShortLabel(medicalInsuranceType) {
  const t = String(medicalInsuranceType || '')
  if (!t || t.includes('自费')) return ''
  if (t.includes('职工')) return '职工医保'
  if (t.includes('居民') || t.includes('农村') || t.includes('新农合')) return '居民医保'
  if (t.includes('公费')) return '公费医疗'
  if (t.includes('商业')) return '商业保险'
  return t
}
