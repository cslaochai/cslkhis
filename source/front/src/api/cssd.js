import request from './request'

// ============ CSSD 消毒供应追溯（G22）============
// 状态机：0待回收 → 1已回收 → 2清洗中 → 3已打包 → 4灭菌中 → 5待发放 → 6已发放 → 7已签收；
// 灭菌完成判不合格自动退回清洗，追溯节点只增不改。
// 临床科室（护士站）只做两件事：发起回收申请(apply)、收到无菌包后签收(sign)；
// 供应室在消毒供应页做回收确认(receiveConfirm)与逐环节流转(advance)。

// 回收登记（新建器械包并记录回收节点）
export function cssdReceive(data) {
  return request.post('/cssd/pack/receive', data)
}

// 回收申请（临床科室发起，落 0-待回收，等供应室确认）
export function cssdApply(data) {
  return request.post('/cssd/pack/apply', data)
}

// 回收确认（供应室 0-待回收→1-已回收）
export function cssdReceiveConfirm(data) {
  return request.post('/cssd/pack/receiveConfirm', data)
}

// 科室签收（6-已发放→7-已签收）
export function cssdSign(data) {
  return request.post('/cssd/pack/sign', data)
}

// 流转到下一追溯节点
export function cssdAdvance(data) {
  return request.post('/cssd/pack/advance', data)
}

// 器械包分页查询（供应室全量台账）
export function cssdListPage(data) {
  return request.post('/cssd/pack/listPage', data)
}

// 本科室器械包分页（按当前岗位科室收口，护士站用）
export function cssdMyListPage(data) {
  return request.post('/cssd/pack/myListPage', data)
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
