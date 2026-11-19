import request from './request'

/**
 * 值班类型（值班线）维护（菜单 2983）。
 *
 * 为什么要这张表：厂商的总值班第一能力是「自定义添加值班类型」——
 * 医疗总值班 / 护理总值班 / 行政总值班 / 院领导带班只是本院当前四条线，
 * 信息中心总值、后勤总值随时会加。写死成枚举 = 每加一条线发一次版。
 */

/** 全部值班类型（含停用；每条带 manageable：当前岗位能否排这条线） */
export function getDutyTypeList() {
  return request.get('/system/dutyType/list')
}

/** 新增/修改值班类型 */
export function dutyTypeUpsert(data) {
  return request.post('/system/dutyType/dutyTypeUpsert', data)
}

/** 删除值班类型（还挂着点位/已有值班记录的类型后端会拦，只能停用） */
export function deleteDutyType(id) {
  return request.delete('/system/dutyType/deleteById', { params: { id } })
}
