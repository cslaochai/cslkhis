/**
 * 患者类型（参保性质）文案 —— 全前端唯一口径来源
 *
 * 口径判定依据（实测 2026-09-20，三方证据一致）：
 *   1) 列注释：biz_patient.patient_type tinyint DEFAULT 1
 *      「患者类型（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他）」
 *   2) 字典表：sys_dict_data.dict_type='his_patient_type'（remark 明确写 biz_patient.patient_type）
 *      1=自费 2=城镇职工医保 3=城乡居民医保 4=公费 5=其他
 *   3) 真实数据：patient_type 分布 1(64条)/2(15条)/3(5条)/4(2条) —— 4=公费确有用例
 *
 * ⚠ 全库只有 biz_patient 一张表有这一列，没有快照列，所以这是**唯一**口径点。
 *
 * 被修掉的三处错口径（都曾是"看着像对的"）：
 *   - PatientSelect.vue  曾写 {1:'门诊',2:'住院',3:'急诊'} → 挂号选患者时把
 *     「城镇职工医保」的患者挂上「住院」角标，纯属编造
 *   - PatientsView.vue   曾写 {1:'普通患者',2:'医保患者',3:'公费患者'} → 2/3 都译错
 *   - PatientUpsertDTO.java 注释写「1-门诊 2-住院 3-急诊」（注释先错，渲染照着抄）
 *   「1-门诊 2-住院 3-急诊」那套是**就诊类型**的口径，跟参保性质无关，别混进来。
 *
 * 未知码值渲染「未知(n)」，不回落成合法值 —— 回落会把"参保性质没录对"洗成"自费"，
 * 而参保性质决定收费时走不走统筹。
 */

/** 患者类型下拉选项（新增/编辑/筛选共用） */
export const PATIENT_TYPE_OPTIONS = [
  { label: '自费', value: 1 },
  { label: '城镇职工医保', value: 2 },
  { label: '城乡居民医保', value: 3 },
  { label: '公费', value: 4 },
  { label: '其他', value: 5 },
]

/**
 * 患者类型码值 → 文案
 * @param {number|string|null|undefined} patientType
 * @returns {string} 自费 / 城镇职工医保 / 城乡居民医保 / 公费 / 其他 / 未知(n) / —
 */
export function patientTypeText(patientType) {
  if (patientType === null || patientType === undefined || patientType === '') return '—'
  const hit = PATIENT_TYPE_OPTIONS.find(o => o.value === Number(patientType))
  return hit ? hit.label : `未知(${patientType})`
}

/**
 * 患者类型 → 结算方式（settlement_type 同构，仅 5 有差异：患者侧"其他"、结算侧"商业保险"）。
 * 挂号时用来带出默认结算方式；「其他」回落到自费（结算方式没有"其他"这一档）。
 * @param {number|string|null|undefined} patientType
 * @returns {number} 1-自费 2-城镇职工医保 3-城乡居民医保 4-公费
 */
export function patientTypeToSettlementType(patientType) {
  const n = Number(patientType)
  return n === 2 || n === 3 || n === 4 ? n : 1
}
