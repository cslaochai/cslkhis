/**
 * 身份证校验（抄 HIS lib/patientGender.js 的员工表单所需两个函数）
 */

/** 18 位身份证格式（末位可为 X） */
export function isIdCardFormatLegal(idCard) {
  return /^[0-9]{17}[0-9Xx]$/.test((idCard || '').trim())
}

/** 身份证第 7-14 位的出生日期是否真实存在 */
export function isIdCardBirthDateLegal(idCard) {
  const s = (idCard || '').trim()
  if (!isIdCardFormatLegal(s)) return false
  const y = Number(s.slice(6, 10))
  const m = Number(s.slice(10, 12))
  const d = Number(s.slice(12, 14))
  const dt = new Date(y, m - 1, d)
  return dt.getFullYear() === y && dt.getMonth() === m - 1 && dt.getDate() === d
}
