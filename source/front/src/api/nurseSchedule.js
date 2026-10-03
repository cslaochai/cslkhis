import request from './request'

// 病区护理排班（sql/166，菜单 330 护理管理 / 331 病区护理排班）
// 后端控制器：/nursing/schedule（his-patient）
//
// 与门诊医生排班（/schedule + /shift，权限码 org.schedule.*）是两套班次册：
// biz_shift.use_scope 1-门诊/急诊 2-病区护理。本页所有班次都来自矩阵接口回传的 shifts
// （后端已按 use_scope=2 + status=1 收口），前端不自己拉全院班次。

// ========== 参照数据（跨岗位下拉，后端只要求登录） ==========

// 病区下拉：只含当前岗位可见科室下的启用病区，附在册护士数（旧接口，只列病区）
export function getNurseWardSelectList(params) {
  return request.get('/nursing/schedule/wardSelectList', { params })
}

// 护理排班单元下拉（sql/209）：病区 + 有护理编制的门诊科室，附在册护士数
// unitType 1-病区 2-门诊科室；两种单元的 id 不在同一个空间，请求必须带着类型一起走
export function getNurseUnitSelectList(params) {
  return request.get('/nursing/schedule/unitSelectList', { params })
}

// 护士下拉：该排班单元所属科室的在册护士（护士/护师）
export function getNurseSelectList(params) {
  return request.get('/nursing/schedule/nurseSelectList', { params })
}

// ========== 周矩阵排班 ==========

// 周矩阵：行=在册护士 列=周一至周日，一次带回 cells + 本周告警 + 每日每班次在岗人数
export function getNurseWeekMatrix(data) {
  return request.post('/nursing/schedule/weekMatrix', data)
}

// 点格排班/改格（一人一天一条；非上班状态由服务端清空班次与工时）
export function upsertNurseScheduleCell(data) {
  return request.post('/nursing/schedule/upsert', data)
}

// 删除一格（后端物理删：唯一键不含 del_flag，软删会让「同一人同一天重排」撞键）
export function deleteNurseScheduleCell(id) {
  return request.delete('/nursing/schedule/deleteById', { params: { id } })
}

// 复制上周：只填目标周空缺格，已排的不覆盖
export function copyNurseScheduleWeek(data) {
  return request.post('/nursing/schedule/copyWeek', data)
}

// ========== 规则校验 / 工时 / 台账 ==========

// 区间规则校验（告警明细，不阻断保存）
export function checkNurseSchedule(data) {
  return request.post('/nursing/schedule/check', data)
}

// 月度工时统计（含整段未排班的人）
export function getNurseMonthWorkload(data) {
  return request.post('/nursing/schedule/monthWorkload', data)
}

// 排班台账分页（跨病区回看）
export function listNurseSchedulePage(data) {
  return request.post('/nursing/schedule/listPage', data)
}

// ========== 人力配置标准 ==========

// 标准列表（含停用行，看得见某条规则为什么不生效）
export function getNurseScheduleRuleList(unitType, unitId) {
  return request.get('/nursing/schedule/ruleList', { params: { unitType, unitId } })
}

// 保存标准（shiftId=0 是病区级行，周工时/连班上限只在这一行有效）
export function upsertNurseScheduleRule(data) {
  return request.post('/nursing/schedule/ruleUpsert', data)
}

// 删除标准（物理删）
export function deleteNurseScheduleRule(id) {
  return request.delete('/nursing/schedule/ruleDeleteById', { params: { id } })
}
