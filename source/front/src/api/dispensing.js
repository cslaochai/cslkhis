import request from './request'

// ========== 药房发药（活表 biz_drug_dispensing；后端含审方闸门/FEFO扣库存/处方状态联动） ==========

// 查询发药列表（patientName/prescriptionNo 模糊 + dispensingStatus 过滤）
export function getDispensingList(params) {
  return request.post('/charge/dispensing/listPage', params)
}

// 获取发药详情
export function getDispensingDetail(id) {
  return request.get('/charge/dispensing/getById', { params: { id } })
}

// 发药（单行）
// checkerId：麻精双人复核的复核药师ID（麻醉药品/第一类精神药品必填，姓名由后端反查，不得与发药人同人）
// overLimitReason：第二类精神药品超 7 日的医师理由（《处方管理办法》第24条；其余情形不生效）
export function dispense(data) {
  return request.post('/charge/dispensing/dispense', {
    id: data.id,
    checkerId: data.checkerId,
    overLimitReason: data.overLimitReason,
  })
}

// 按处方整单发药
export function dispenseByPrescription(prescriptionId, checkerId, overLimitReason) {
  return request.post('/charge/dispensing/dispenseByPrescription', {
    prescriptionId,
    checkerId,
    overLimitReason,
  })
}

// 退药（回库存 + 处方状态联动）
export function returnDrug(id, reason) {
  return request.post('/charge/dispensing/returnDrug', { id, reason })
}
