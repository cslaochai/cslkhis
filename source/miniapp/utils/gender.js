// 性别码值 → 文案（与后端 SysGenderEnum / 前端 @/lib/patientGender 同一套口径：1-男 2-女 9-未知）
// 小程序工程独立，无法复用前端 lib，故在此收口一份，消灭各页面散落的 `gender === 1 ? '男' : '女'` 三元。
// 0 是历史脏数据（不是"女"），刻意渲染成「未知」让它暴露，不要回落成"女"。
export function genderText(gender) {
  const n = Number(gender)
  if (n === 1) return '男'
  if (n === 2) return '女'
  return '未知'
}
