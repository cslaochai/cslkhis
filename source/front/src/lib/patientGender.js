/**
 * 性别文案 —— 全前端唯一口径来源（对应后端 PatientGenderText / SysGenderEnum）
 *
 * 口径（2026-09-23 统一，见 source/back_end/sql/75）：
 *   员工（sys_employee.gender）与患者（biz_patient.gender 及各快照列）同用一套码值
 *   **1-男 2-女 9-未知**（字典 sys_gender；9 取 GB/T 2261.1「未说明」档位）。
 *   原员工口径 0-女 1-男 已迁移（0→2），原患者未知 3 已迁移为 9。
 *
 *   `0` 是历史脏数据（不是"女"），刻意渲染成「未知(0)」让它暴露，不要回落到合法值。
 *
 * 为什么必须用这个函数而不是各页面写 `gender === 1 ? '男' : '女'`：
 * 二元写法会把 0 等异常码值都渲染成"女"—— 那是**编造一个性别**，而性别会流进
 * 检验参考区间与性别专属诊断判断。
 */

/** 新增 / 编辑（患者、员工通用）的性别下拉选项 */
export const PATIENT_GENDER_OPTIONS = [
  { label: '男', value: 1 },
  { label: '女', value: 2 },
  { label: '未知', value: 9 },
]

/**
 * 性别码值 → 文案
 * @param {number|string|null|undefined} gender
 * @returns {string} 男 / 女 / 未知 / 未知(n) / —
 */
export function patientGenderText(gender) {
  if (gender === null || gender === undefined || gender === '') return '—'
  const n = Number(gender)
  if (n === 1) return '男'
  if (n === 2) return '女'
  if (n === 9) return '未知'
  return `未知(${gender})`
}

/**
 * 性别是否已采集（1/2/9 之外的都算"码值异常"，不算采到了）
 * @param {number|string|null|undefined} gender
 */
export function isPatientGenderCollected(gender) {
  const n = Number(gender)
  return n === 1 || n === 2 || n === 9
}

/**
 * 性别符号（头像 / 列表上的小标记）——未知与异常码值用「?」，不要用 ♀ 兜底
 * @param {number|string|null|undefined} gender
 * @returns {string} ♂ / ♀ / ?
 */
export function patientGenderSymbol(gender) {
  const n = Number(gender)
  if (n === 1) return '♂'
  if (n === 2) return '♀'
  return '?'
}

/**
 * 头像配色档位（下拉项头像、详情弹框头像共用）
 *
 * 返回 'male' | 'female' | 'unknown'，调用方拼成 class：`.ps-avatar-{tone}`（下拉项）
 * 或 `.patient-avatar-{tone}`（详情弹框）。**色值只在 style.css 写一份**，两类选择器共用同一组声明 ——
 * 此前详情头像用统一主题色、下拉按性别区分，同一个患者两处不同色，就是"同一语义两处实现"的老毛病。
 *
 * 顺带收口：其它页面各自写 `gender === 1 ? 'bg-blue-500' : gender === 2 ? 'bg-pink-500' : 'bg-slate-400'`
 * 的写法（急诊/病历/预约等 5 处）不要再扩散，新增头像一律走本函数。
 *
 * 未知 / 脏码值（0）：中性灰 —— 用粉或蓝兜底等于替患者编一个性别。
 * @param {number|string|null|undefined} gender
 * @returns {'male'|'female'|'unknown'}
 */
export function patientAvatarTone(gender) {
  const n = Number(gender)
  if (n === 1) return 'male'
  if (n === 2) return 'female'
  return 'unknown'
}

/**
 * 年龄文案：没有年龄时给「—」，不要渲染成光秃秃的「岁」。
 *
 * age 由后端拿 birth_date 算（见 PatientController#fillBirthDateAndAge）。出生日期是选填，
 * 存量里就有一批没有 birth_date 的老档 —— 直接拼 `{{ age }}岁` 会显示成「岁」，
 * 看着像页面坏了，也分不清"没采集"和"0 岁"。
 * @param {number|string|null|undefined} age
 * @returns {string} 例：32岁 / —
 */
export function patientAgeText(age) {
  if (age === null || age === undefined || age === '') return '—'
  return `${age}岁`
}

/* ==================== 建档字段校验（与后端 PatientProfileValidator 对齐） ==================== */

/**
 * 身份证 18 位格式：前 17 位数字，末位数字或 X/x
 * @param {string} idCard
 */
export function isIdCardFormatLegal(idCard) {
  return /^[0-9]{17}[0-9Xx]$/.test((idCard || '').trim())
}

/**
 * 身份证第 7-14 位的出生日期是否真实存在。
 * 单独判断是为了提示能指到点子上：否则「19990230」这种号只会被告知"校验位不正确"。
 * @param {string} idCard
 */
export function isIdCardBirthDateLegal(idCard) {
  const s = (idCard || '').trim()
  if (!isIdCardFormatLegal(s)) return false
  const y = Number(s.slice(6, 10))
  const m = Number(s.slice(10, 12))
  const d = Number(s.slice(12, 14))
  const dt = new Date(y, m - 1, d)
  return dt.getFullYear() === y && dt.getMonth() === m - 1 && dt.getDate() === d
}

/**
 * 身份证校验位（GB 11643）。
 * 只用于新增（P5.7 开工前实测：存量 42 条有身份证的档案只有 10 条校验位成立，其余是造数时编的；
 * 修改时只验格式，否则这些老档连改电话都保存不了）。
 * @param {string} idCard
 */
export function isIdCardChecksumLegal(idCard) {
  const s = (idCard || '').trim()
  if (!isIdCardFormatLegal(s)) return false
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const codes = '10X98765432'
  let sum = 0
  for (let i = 0; i < 17; i++) sum += Number(s[i]) * weights[i]
  return codes[sum % 11] === s[17].toUpperCase()
}

/**
 * 手机号：11 位、1 开头、第 2 位 3-9。空值不算错（是否允许空由调用方决定）
 * @param {string} phone
 */
export function isPhoneLegal(phone) {
  return /^1[3-9]\d{9}$/.test((phone || '').trim())
}

/**
 * 从身份证号取出生日期（YYYY-MM-DD）。格式不合法或日期不存在时返回 ''
 *
 * 建档时身份证必填、出生日期选填 —— 让录入的人少填一格，也避免 birth_date 空着导致
 * 列表年龄为空、EMPI 的「同名+同性别+同出生日期」那一档匹配不上（后端也会补一次，双保险）。
 * @param {string} idCard
 * @returns {string}
 */
export function birthDateFromIdCard(idCard) {
  const s = (idCard || '').trim()
  if (!isIdCardBirthDateLegal(s)) return ''
  return `${s.slice(6, 10)}-${s.slice(10, 12)}-${s.slice(12, 14)}`
}
