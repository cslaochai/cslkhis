import request from './request'

/**
 * 人力配置标准（sql/200，biz_staff_plan_rule）。
 *
 * 一个单元 × 一个班次 × 一个岗位类别该配多少人。它是排班保存时的闸门依据，不是报表：
 * 低于最低在岗会拦住保存（不然一个班没人值班，出事才发现），高于上限只提示。
 * 这张表由护理那套 biz_nurse_schedule_rule 泛化而来，所以现在也管医生/技师/收费窗口。
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
