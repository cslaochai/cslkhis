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
