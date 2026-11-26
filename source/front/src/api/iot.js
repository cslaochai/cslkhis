import request from './request'

// ============ 物联网设备遥测（his-ms-iot → TDengine 时序库） ============
// 数据不在 MySQL：最新值/历史值都实时查时序库，故没有分页，只有设备类型与时间范围两个维度。

// 设备类型下拉（后端 DeviceTypeSchema 唯一注册表）
export function iotSelectDeviceTypes() {
  return request.get('/iot/telemetry/selectDeviceTypes')
}

// 设备最新遥测（不传 deviceType 返回全部类型）
export function iotLatestList(data) {
  return request.post('/iot/telemetry/latestList', data)
}

// 单台设备历史遥测（时间格式 yyyy-MM-dd HH:mm:ss）
export function iotHistoryList(data) {
  return request.post('/iot/telemetry/historyList', data)
}

// 冷链温湿度 GSP 记录（5 分钟一轮，含最低/最高）：{deviceNo,startTime,endTime}
export function iotColdChainRecords(data) {
  return request.post('/iot/telemetry/coldChainRecords', data)
}

// 冷链近 N 小时汇总（默认 24 小时）：返回每台柜的温度区间与归档窗口数
export function iotColdChainSummary(hours = 24) {
  return request.get('/iot/telemetry/coldChainSummary', {params: {hours}})
}

// ============ 设备—患者绑定（设备共享调配的底层关系） ============
// 绑定对象是住院就诊（admissionId），泵跟着患者走，转床不用重绑。
// 绑定/解绑只由设备共享调配的发放/验收调用；监控页只读。

// 当前在用绑定清单
export function iotActiveBinds(data) {
  return request.post('/iot/bind/listActiveBinds', data)
}

// ============ 设备档案（台账只读：名称/归属/位置） ============

// 设备清单：{deviceType, deviceNo}
export function iotDeviceSelectList(data) {
  return request.post('/iot/device/selectList', data)
}

// ============ 设备事件（告警闭环，落 biz_iot_event） ============
// 事件来自 MQTT .../event 主题，QoS1 重发按 (设备,seq,ts) 唯一键去重。

// 事件分页：{pageNum,pageSize,deviceType,handledFlag,startTime,endTime}
export function iotEventListPage(data) {
  return request.post('/iot/event/listPage', data)
}

// 处理事件：{id, handledFlag, remark}（1-已处理 2-已忽略）
export function iotEventHandle(data) {
  return request.post('/iot/event/handle', data)
}
