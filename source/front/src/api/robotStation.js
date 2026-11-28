import request from './request'

// ============ 物流机器人收发站点主数据（设备科维护） ============

// 站点分页：{keyword, stationType, enabled, pageNum, pageSize}
export function robotStationListPage(data) {
  return request.post('/iot/robotStation/listPage', data)
}

// 站点详情
export function robotStationDetail(id) {
  return request.get('/iot/robotStation/getDetailById', {params: {id}})
}

// 新增/修改（id 为空走新增）
export function robotStationUpsert(data) {
  return request.post('/iot/robotStation/upsert', data)
}

// 删除（配置类表物理删）
export function robotStationDelete(id) {
  return request.delete('/iot/robotStation/deleteById', {params: {id}})
}

// 叫车表单候选：{stations, stationTypes, goodsTypes, priorities}（登录即可，不挂维护码）
export function robotStationOptions() {
  return request.get('/iot/robotStation/options')
}
