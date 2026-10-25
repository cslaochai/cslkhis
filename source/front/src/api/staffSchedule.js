import request from './request'

/**
 * 全院岗位排班（sql/200 核心表 biz_staff_schedule）。
 *
 * 这一页回答的是「谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤」——
 * 与「门诊出诊计划」是两件事：出诊计划挂号源，核心表管出勤。同一个护士以前可以在两张表里
 * 各排一次、时间重叠都不报错，接了这个接口之后事实只有一条。
 *
 * /onDuty 与 /changeLogList 只要求登录：此刻谁在岗是分诊、急诊、收费处要拿来打电话的公共信息，
 * 按菜单权限收口等于让人半夜找不到人。
 */

/** 分页查询排班（日期区间 + 单元 + 岗位类别 + 出勤状态 + 关键词） */
export function getStaffScheduleListPage(data) {
  return request.post('/system/staffSchedule/listPage', data)
}

/** 排班周总览（只读驾驶舱：门诊号源 / 在岗人次 / 人力缺口 / 每日总值班一屏聚合） */
export function getScheduleOverviewWeek(beginDate) {
  return request.get('/schedule/overviewWeek', { params: { beginDate } })
}

/** 全岗位月度工时统计（按人员聚合当月已发布排班的出勤/工时/休息） */
export function getWorkloadMonthly(monthStart) {
  return request.get('/schedule/workloadMonthly', { params: { monthStart } })
}

/** 新增/修改排班（时间与工时由班次带出，超出人力上限只提示不拦） */
export function staffScheduleUpsert(data) {
  return request.post('/system/staffSchedule/staffScheduleUpsert', data)
}

/** 删除排班（唯一键不含 del_flag → 物理删；删后低于最低在岗后端会当场拦下并回滚） */
export function deleteStaffSchedule(id) {
  return request.delete('/system/staffSchedule/deleteById', { params: { id } })
}

/** 换班（两条同天排班互换）/ 代班（单向换人承接） */
export function staffScheduleSwap(data) {
  return request.post('/system/staffSchedule/swap', data)
}

/** 按星期整周复制排班（返回补上的行数，已排过的人自动跳过） */
export function staffScheduleCopyRange(data) {
  return request.post('/system/staffSchedule/copyRange', data)
}

/** 此刻在岗名单（跨零点班归开始日，所以名单里可能有「昨天」的夜班） */
export function getStaffOnDuty(params) {
  return request.get('/system/staffSchedule/onDuty', { params })
}

/** 某条排班的变更留痕（换班/代班/停班/加减号，最新在前） */
export function getScheduleChangeLogList(staffScheduleId) {
  return request.get('/system/staffSchedule/changeLogList', { params: { staffScheduleId } })
}

/** 变更记录全局分页（台账：跨排班单元回看，按发生时间/动作/值班人筛） */
export function getScheduleChangeLogPage(data) {
  return request.post('/system/staffSchedule/changeLogPage', data)
}

/** 区间规则校验（连续夜班/连续上班/单周工时/班次冲突/人力缺口与富余） */
export function checkStaffSchedule(data) {
  return request.post('/system/staffSchedule/check', data)
}

/** 月度工时统计（一人一行，含整段未排班的人） */
export function getStaffWorkload(data) {
  return request.post('/system/staffSchedule/monthWorkload', data)
}

/** 排班单元候选（科室/病区/全院）：页面按单元过滤、整周提交审核时选单元 */
export function getStaffScheduleUnitSelectList(params) {
  return request.get('/system/staffSchedule/unitSelectList', { params })
}

// ========== 发布态流转（排班保存只到草稿，审核发布后才计在岗） ==========
// 入参 id = 一行排班的主键（后端已 ToStringSerializer 字符串化）

// 提交审核：草稿 → 待审核（排班人自己提交，仍要审核发布才生效）
export function submitStaffSchedule(id) {
  return request.post('/system/staffSchedule/submit', null, { params: { id } })
}

// 审核发布：待审核 → 已发布（此后才计入在岗）；须命中科室指定的全院岗位排班审核人
export function auditPublishStaffSchedule(id) {
  return request.post('/system/staffSchedule/auditPublish', null, { params: { id } })
}

// 驳回：待审核 → 草稿（附驳回原因，写进变更留痕）
export function rejectStaffSchedule(data) {
  return request.post('/system/staffSchedule/reject', data)
}

// 撤回：任意态 → 草稿（撤回后立刻不计在岗）
export function recallStaffSchedule(id) {
  return request.post('/system/staffSchedule/recall', null, { params: { id } })
}

// 进入调班：已发布/已变更 → 调班中（锁定，改完保存自动转「已变更」）
export function adjustStartStaffSchedule(id) {
  return request.post('/system/staffSchedule/adjustStart', null, { params: { id } })
}

// 整周提交审核：本单元整周草稿 → 待审核（入参 { orgType, orgId, weekStart }）
export function submitWeekStaffSchedule(data) {
  return request.post('/system/staffSchedule/submitWeek', data)
}

// 整周审核发布：本单元整周待审核 → 已发布（入参 { orgType, orgId, weekStart }）
export function auditWeekStaffSchedule(data) {
  return request.post('/system/staffSchedule/auditWeek', data)
}

// 待我审批聚合：当前登录人作为指定审核人的待审核排班（入参 { orgType, orgId, pageNum, pageSize }）
export function getStaffSchedulePendingApproval(data) {
  return request.post('/system/staffSchedule/pendingApproval', data)
}

// 急诊值班公开视图：急诊内科/外科/儿科当天已发布的医生值班表（全院可查，入参 date=YYYY-MM-DD）
export function getErDutyPublic(date) {
  return request.get('/system/staffSchedule/erDutyPublic', {params: {date}})
}
