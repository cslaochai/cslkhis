import request from './request'

// 实际出勤与工时归因（sql/214，闭环第③④步：计划 vs 实际 → 校准标准）
// 后端控制器：/system/staffAttendance（his-system）
//
// 为什么排班页要读它：页面上一个「缺 2 人」说不清是编制不够还是今天派的俩人没来。
// 把「实际来了几个、实际干了多少工时」摆到同一屏上，缺口才有了可追的答案。
//
// ⚠ 一条必须在前端也守住的口径：**查不到出勤记录不等于缺勤**。
//   后端只在护士长显式确认时才产生「缺勤」，其余一律是「未回填」（待登记）。
//   所以页面上显示未回填时，动作是「去补登」，不是「已缺勤」。

// 单元 × 日 执行汇总：计划人数/实到/缺勤/未回填/加班人数与工时差
export function getAttendanceSummary(params) {
  return request.get('/system/staffAttendance/summary', { params })
}

// 行级对照：某单元某天每个人的计划 vs 实际，含差异类型与写给人看的原因
export function getAttendanceComparison(params) {
  return request.get('/system/staffAttendance/comparison', { params })
}

// 编制校准建议：把执行结果喂回第 ① 层标准（只给建议，不自动改编制）
export function getAttendanceAdvice(params) {
  return request.get('/system/staffAttendance/advice', { params })
}

// 护士长手工登记/修正工时（没打卡的日子补账）
export function adjustAttendance(data) {
  return request.post('/system/staffAttendance/adjust', data)
}

// 确认缺勤：全系统唯一能产生「缺勤」的入口，必须人来确认
export function markAttendanceAbsent(data) {
  return request.post('/system/staffAttendance/markAbsent', data)
}

// 签到/签退（幂等：重复刷卡不改最早那次签到时间）
export function checkInAttendance(data) {
  return request.post('/system/staffAttendance/checkIn', data)
}
export function checkOutAttendance(data) {
  return request.post('/system/staffAttendance/checkOut', data)
}

// 科室确认（0-待确认 1-已确认 2-有异议）
export function confirmAttendance(params) {
  return request.post('/system/staffAttendance/confirm', null, { params })
}

// 撤销一条出勤登记
export function removeAttendance(params) {
  return request.post('/system/staffAttendance/remove', null, { params })
}
