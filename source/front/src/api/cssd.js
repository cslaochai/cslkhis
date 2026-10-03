import request from './request'

// ============ CSSD 消毒供应追溯（G22）============
// 状态机：1已回收 → 2清洗中 → 3已打包 → 4灭菌中 → 5待发放 → 6已发放；
// 灭菌完成判不合格自动退回清洗，追溯节点只增不改。

// 回收登记（新建器械包并记录回收节点）
export function cssdReceive(data) {
  return request.post('/cssd/pack/receive', data)
}

// 流转到下一追溯节点
export function cssdAdvance(data) {
  return request.post('/cssd/pack/advance', data)
}

// 器械包分页查询
export function cssdListPage(data) {
  return request.post('/cssd/pack/listPage', data)
}

// 器械包详情（含全量追溯链）
export function getCssdDetail(packId) {
  return request.get('/cssd/pack/getDetailById', { params: { packId } })
}

// ============ CSSD 器械包模板目录（sql/99）============

// 模板下拉（仅启用）——回收登记选包数据源
export function cssdTemplateSelectList() {
  return request.get('/cssd/template/selectList')
}

// 模板分页查询
export function cssdTemplateListPage(data) {
  return request.post('/cssd/template/listPage', data)
}

// 模板详情（含组成明细）
export function getCssdTemplateDetail(templateId) {
  return request.get('/cssd/template/getDetailById', { params: { templateId } })
}

// 器械名称下拉（启用模板明细去重汇总，带规格单位）
export function cssdTemplateItemSelectList() {
  return request.get('/cssd/template/item/selectList')
}

// 模板新增/修改（明细整删重插）
export function cssdTemplateUpsert(data) {
  return request.post('/cssd/template/templateUpsert', data)
}

// 模板删除（软删）
export function cssdTemplateDelete(templateId) {
  return request.delete('/cssd/template/deleteById', { params: { templateId } })
}
