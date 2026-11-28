import request from './request'

// ============ 物流机器人调度（HIS 侧配送任务单 + 车队状态） ============
// 车队全院共享：站点/科室发起配送单，设备科调度选空闲机器人派车，指令经 MQTT 下行到仿真机器人。

// 车队状态卡片：台账 + 最新遥测 + 当前执行任务
export function robotDispatchFleet() {
  return request.get('/iot/robotDispatch/fleet')
}

// 任务单分页：{status, priority, sourceType, robotNo, keyword, pageNum, pageSize}
export function robotDispatchListPage(data) {
  return request.post('/iot/robotDispatch/listPage', data)
}

// 任务单详情
export function robotDispatchDetail(id) {
  return request.get('/iot/robotDispatch/getDetailById', {params: {id}})
}

// 新建配送任务单：{sourceType, sourceDeptName, goodsType, goodsName, fromStation, toFloor, toStation,
//                  priority, boxCount, loadKg, autoAssign, robotNo, remark}
export function robotDispatchCreate(data) {
  return request.post('/iot/robotDispatch/create', data)
}

// 派车：{id, robotNo?}（robotNo 为空自动选空闲车）
export function robotDispatchAssign(data) {
  return request.post('/iot/robotDispatch/assign', data)
}

// 取消任务单：{id, cancelReason}
export function robotDispatchCancel(data) {
  return request.post('/iot/robotDispatch/cancel', data)
}
