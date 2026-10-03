/**
 * 患者主档字段文案 —— 全前端唯一口径来源
 *
 * 与 `lib/patientGender.js`（性别）、`lib/patientType.js`（参保性质）同一套做法：
 * 码值 → 文案的映射只在这里写一份，页面不许再各写一套。
 *
 * 为什么必须收口：同一个字段以前在三处各写一遍，且已经写出分歧 ——
 *   - Header.vue / PatientsView.vue 写 `maritalStatus === 1 ? '已婚' : maritalStatus === 0 ? '未婚' : '-'`
 *     → 「离异(2)」「丧偶(3)」被吞成一个「-」，看着像没采集，实际是页面不认识这两个码值。
 *   - DoctorWorkstationView.vue 写 `['未婚','已婚','离异','丧偶'][maritalStatus]`
 *     → 这个反而是全的。同一语义两种写法，改一处必漏另一处。
 */

/* ==================== 婚姻状况 ==================== */

/**
 * 口径判定依据（三方证据一致，实测 2026-09-20）：
 *   1) 列注释：`biz_patient.marital_status` tinyint
 *      「婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）」—— 见 source/back_end/sql/4-HIS患者管理模块.sql:30
 *   2) 字典表：`sys_dict_data.dict_type='his_marital_status'` → 0未婚 / 1已婚 / 2离异 / 3丧偶
 *   3) 真实数据分布：0→76 条、1→15 条（无 2/3，但 2/3 是合法码值，必须能渲染）
 *
 * ⚠ 反例（别照抄）：`BizPatient.java:53` 的实体注释写「1-未婚 2-已婚 3-离异 4-丧偶」，
 *   与上述三方都不符，是错的。曾有一版渲染实现照它写，会把已婚患者显示成未婚。
 */
export const MARITAL_STATUS_OPTIONS = [
  { label: '未婚', value: 0 },
  { label: '已婚', value: 1 },
  { label: '离异', value: 2 },
  { label: '丧偶', value: 3 },
]

/**
 * 婚姻状况码值 → 文案
 * @param {number|string|null|undefined} v
 * @returns {string} 未婚 / 已婚 / 离异 / 丧偶 / 未知(n) / —
 */
export function maritalStatusText(v) {
  if (v === null || v === undefined || v === '') return '—'
  const hit = MARITAL_STATUS_OPTIONS.find(o => o.value === Number(v))
  return hit ? hit.label : `未知(${v})`
}

/* ==================== 证件（卡片）类型 ==================== */

/**
 * 口径判定依据：
 *   1) 列注释：`biz_patient.card_type` tinyint「卡片类型（1-就诊卡 2-身份证 3-医保卡）」
 *   2) 字典表：`sys_dict_data.dict_type='his_card_type'` → 1就诊卡 / 2身份证 / 3医保卡
 *   3) 真实数据分布：1→79 条、2→6 条、3→6 条
 */
export const CARD_TYPE_OPTIONS = [
  { label: '就诊卡', value: 1 },
  { label: '身份证', value: 2 },
  { label: '医保卡', value: 3 },
]

/**
 * 证件类型码值 → 文案
 * @param {number|string|null|undefined} v
 * @returns {string} 就诊卡 / 身份证 / 医保卡 / 未知(n) / —
 */
export function cardTypeText(v) {
  if (v === null || v === undefined || v === '') return '—'
  const hit = CARD_TYPE_OPTIONS.find(o => o.value === Number(v))
  return hit ? hit.label : `未知(${v})`
}

/* ==================== 档案状态 ==================== */

/**
 * 口径判定依据：列注释 `biz_patient.status` tinyint「状态（0-停用 1-启用）」，
 * 真实数据当前 91 条全是 1。未知码值不回落成「启用」—— 回落会让停用档案看着像能继续用。
 * @param {number|string|null|undefined} v
 */
export function patientStatusText(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (n === 1) return '启用'
  if (n === 0) return '停用'
  return `未知(${v})`
}

/* ==================== 自由文本字段 ==================== */

/**
 * 民族 / 职业 / 联系人关系 这类字段在库里是 **varchar 自由文本**，不是码值：
 *   - `nation` varchar(20)「民族」实测存「汉族」「满族」…（16 条汉族、1 条满族），
 *     但混进了 `"2"`(3 条) / `"28"`(1 条) 这类脏数据（列注释没给码值，录入侧曾按码值写过）
 *   - `contact_relation` varchar(20)「联系人关系（父母、配偶、子女等）」实测存「配偶」「父亲」「儿子」，
 *     同样混了 `"2"`(2 条) / `"14"`(1 条)
 *
 * 所以这两个字段**不能走码值字典映射** —— 拿字典去查「汉族」必然查不到，
 * 无论返回空串还是「未知(汉族)」都是把好数据改坏。
 * 直接显示原值，让脏数据（"2"、"28"）自己暴露出来，这也符合本项目的既有口径：
 * 码值/数据不对时不许回落成看起来合法的值。
 *
 * @param {string|null|undefined} v
 * @returns {string} 原值 / —
 */
export function freeText(v) {
  if (v === null || v === undefined) return '—'
  const s = String(v).trim()
  return s === '' ? '—' : s
}

/**
 * 金额文案：null / undefined 给「—」，不要给「¥0.00」——
 * 「没这项费用」和「这项费用是 0」是两回事。
 * @param {number|string|null|undefined} v
 */
export function moneyText(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  return Number.isNaN(n) ? '—' : `¥${n.toFixed(2)}`
}

/**
 * 时间戳截到分钟（秒对临床视图没意义，还占宽度）
 * @param {string|null|undefined} t
 */
export function timeToMinute(t) {
  return t ? String(t).substring(0, 16) : '—'
}

/**
 * 时间戳截到日期
 * @param {string|null|undefined} t
 */
export function timeToDate(t) {
  return t ? String(t).substring(0, 10) : '—'
}

/* ==================== 隐私字段脱敏：不在前端做 ====================
 * 手机号 / 身份证 / 证件号 / 医保卡号的打码一律在**后端出参**时完成
 * （`his-common/support/SensitiveMaskUtils`；列表走 `maskListSensitiveFields`，
 * 档案详情走 `maskDetailSensitiveFields`），页面只渲染后端给的 `xxxMasked` 字段。
 *
 * 这里原先有一份 maskMiddle/maskPhone/maskIdCard：前端遮等于没遮 —— 明文仍在响应体里，
 * 抓包、日志采集、接口复用到第二个页面（忘记调 mask 的那个）都会漏，而且两份口径会各自漂移。
 *
 * 唯一保留明文的场景是**编辑回显**（表单数据源，如 `/patient/getById`）：
 * 表单是「回填 → 整对象 upsert」，存进去的星号会把真号洗掉。
 */
