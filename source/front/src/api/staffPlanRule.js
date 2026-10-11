import request from './request'

/**
 * 人力配置标准
 */

/** 分页查询人力标准 */
export function getStaffPlanRuleListPage(data) {
  return request.post('/system/staffPlanRule/listPage', data)
}

/** 新增/修改人力标准（同单元同班次同岗位只允许一条） */
export function staffPlanRuleUpsert(data) {
  return request.post('/system/staffPlanRule/staffPlanRuleUpsert', data)
}

/** 删除人力标准（物理删） */
export function deleteStaffPlanRule(id) {
  return request.delete('/system/staffPlanRule/deleteById', { params: { id } })
}
