import request from './request'

// 查询临床规则校验列表
export function getRuleCheckList(params) {
  return request.post('/charge/ruleCheck/listPage', params)
}

// 获取校验详情
export function getRuleCheckDetail(id) {
  return request.get('/charge/ruleCheck/getById', { params: { id } })
}

// 执行临床规则校验
export function executeRuleCheck(params) {
  return request.post('/charge/ruleCheck/executeCheck', params)
}

// 处理校验问题
export function handleRuleCheck(id, params) {
  return request.post('/charge/ruleCheck/handleCheck', { id, ...params })
}
