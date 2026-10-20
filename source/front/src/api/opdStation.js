import request from './request'

/**
 * 门诊公共工位字典 + 工位派岗（sql/277）。
 *
 * 回答「A 护士今天被排到总导诊/分诊台/输液室/注射室/采血室哪个位」：
 * 位先定义存在（字典），排班员把人写进位里（派岗）；派岗只挪当天已排上班的人，
 * 本身不产出出勤事实，也不进工时闭环。
 */

// ========== 工位字典 ==========

/** 工位下拉（只要求登录：派岗弹框选「排到哪个位」） */
export function getOpdStationSelectList(params) {
  return request.get('/system/opdStation/selectList', { params })
}

/** 工位字典分页 */
export function getOpdStationPage(data) {
  return request.post('/system/opdStation/stationPage', data)
}

/** 新增/修改工位字典（编码全局唯一） */
export function opdStationUpsert(data) {
  return request.post('/system/opdStation/stationUpsert', data)
}

/** 删除工位字典（物理删；已被派岗占用的会被拦下） */
export function deleteOpdStation(id) {
  return request.delete('/system/opdStation/stationDelete', { params: { id } })
}

// ========== 工位派岗 ==========

/** 派岗记录分页（日期区间 + 工位 + 借调 + 姓名关键词） */
export function getOpdAssignPage(data) {
  return request.post('/system/opdStation/assignPage', data)
}

/** 派岗候选：指定日期已发布上班、且岗位类别符合该工位要求的人（与保存校验同源，选人下拉用它） */
export function getAssignCandidates(params) {
  return request.get('/system/opdStation/assignCandidates', { params })
}

/** 排班员指定派岗（同日同工位同班同人唯一；isLoan=1 登记跨单元借调） */
export function opdAssignUpsert(data) {
  return request.post('/system/opdStation/assignUpsert', data)
}

/** 撤销派岗（物理删） */
export function deleteOpdAssign(id) {
  return request.delete('/system/opdStation/assignDelete', { params: { id } })
}
