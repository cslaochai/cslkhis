import request from './request'

/**
 * 检查预约中心（G21，菜单 412）
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. **号源格子是事实，计数是派生值**：`used_source` 由占号/退号增量维护，
 *    页面显示的「已占 x/y」直接取后端返回值，禁止前端拿 board 的 rows 自己数出来的数
 *    当库存用。计数与预约单不一致时用 `examSlotRecalc` 以预约单为事实复算。
 * 2. **占号成功后申请单会变**：`book` 会把 `biz_inspection_apply.apply_status` 推到 3-已预约
 *    并写 `appointment_time`；取消/爽约回退到占号前的状态。所以「待预约申请」列表
 *    是**派生视图**（已缴费或急诊提交 + 无在办预约），不要在前端拼两个列表自己筛。
 * 3. **取消只在「已预约」上做**：`已到检` 之后要在检查工作站走完成流程，
 *    在预约中心强行取消会造成「号退了、申请单停在检查中」的双向不一致，后端直接拒。
 * 4. **改约 = 终结旧单 + 重新占号**，旧单转 4-已取消并在 remark 留「改约自 单号」痕迹，
 *    不在原单上就地改时刻（否则设备冲突检测会漏掉旧占号）。
 * 5. 所有 ID 都是雪花 ID 字符串，**不要 Number()**；日期入参 `yyyy-MM-dd`，
 *    时段是 `HH:mm` 字符串（对应 char(5) 列），不要传 ISO `T` 分隔或时间戳。
 * 6. `recommend` 会**顺手补齐目标日期的号源格子**（有写副作用，故带事务），
 *    不是纯查询；只要「今天还有没有空」就调它，不要前端遍历 board 找空档。
 */

// ---------- 设备档位 ----------

// 设备分页（keyword / deviceType / deptId / status 可空）
export function getExamDeviceListPage(params) {
  return request.post('/medicaltech/examDevice/listPage', params)
}

// 设备下拉：传 itemId 时只返回「已配置该项目且开放预约」的设备
export function getExamDeviceSelectList(params) {
  return request.get('/medicaltech/examDevice/selectList', { params })
}

// 设备台账候选（sys_equipment 只读挂接，G22 域的档案）
export function getExamEquipmentOptions() {
  return request.get('/medicaltech/examDevice/equipment/selectList')
}

// 检查项目候选（配可开展项目用）
export function getExamItemCandidates(params) {
  return request.get('/medicaltech/examDevice/item/selectList', { params })
}

// 设备详情（含可开展项目）
export function getExamDeviceDetail(deviceId) {
  return request.get('/medicaltech/examDevice/getDetailById', { params: { deviceId } })
}

export function examDeviceUpsert(data) {
  return request.post('/medicaltech/examDevice/deviceUpsert', data)
}

export function deleteExamDevice(deviceId) {
  return request.delete('/medicaltech/examDevice/deleteById', { params: { deviceId } })
}

// ---------- 设备可开展项目 ----------

export function getExamDeviceItems(deviceId) {
  return request.get('/medicaltech/examDevice/itemList', { params: { deviceId } })
}

// 覆盖式保存：items 里没带的映射视为取消
export function examDeviceItemSave(data) {
  return request.post('/medicaltech/examDevice/itemSave', data)
}

// ---------- 分时段号源 ----------

// 生成（补齐）号源：deviceId + startDate + days（1~31，受设备 ahead_days 约束）
export function examSlotGenerate(data) {
  return request.post('/medicaltech/examDevice/slotGenerate', data)
}

// 号源看板（单日全部格子 + 占用者单号/患者名）
export function getExamSlotBoard(data) {
  return request.post('/medicaltech/examDevice/slotBoard', data)
}

// 锁号 status=0 / 放号 status=1；格内有已占号源时后端拒绝锁号
export function examSlotToggle(data) {
  return request.post('/medicaltech/examDevice/slotToggle', data)
}

// 对账：以预约单为事实复算 used_source，返回漂移清单
export function examSlotRecalc(data) {
  return request.post('/medicaltech/examDevice/slotRecalc', data)
}

// ---------- 预约工作台 ----------

// 待预约申请分页（已缴费/已提交急诊且无在办预约）
export function getExamPendingListPage(params) {
  return request.post('/medicaltech/examAppoint/pendingListPage', params)
}

// 预约台账分页
export function getExamAppointListPage(params) {
  return request.post('/medicaltech/examAppoint/listPage', params)
}

// 台账状态分布（与分页同口径，后端 group by）
export function getExamAppointStatusCount(params) {
  return request.post('/medicaltech/examAppoint/statusCount', params)
}

// 预约中心统计（今日各状态 / 待预约数 / 设备开关数）
export function getExamAppointStats() {
  return request.get('/medicaltech/examAppoint/stats')
}

export function getExamAppointDetail(apptId) {
  return request.get('/medicaltech/examAppoint/getDetailById', { params: { apptId } })
}

// 占号预约（设备/患者/时长/流程四道冲突检测，失败信息里会点名占用者）
export function examAppointBook(data) {
  return request.post('/medicaltech/examAppoint/book', data)
}

// 改约（reason 必填）
export function examAppointReschedule(data) {
  return request.post('/medicaltech/examAppoint/reschedule', data)
}

// 取消预约（cancelReason 必填，仅限「已预约」）
export function examAppointCancel(data) {
  return request.post('/medicaltech/examAppoint/cancel', data)
}

// 到检签到 / 完成检查
export function examAppointArrive(apptId) {
  return request.post('/medicaltech/examAppoint/arrive', { apptId })
}

export function examAppointFinish(apptId) {
  return request.post('/medicaltech/examAppoint/finish', { apptId })
}

// 可选时段推荐（会补格子，带事务）
export function examAppointRecommend(data) {
  return request.post('/medicaltech/examAppoint/recommend', data)
}

// 手工补跑爽约扫描（定时任务漏跑时的运维入口）
export function examAppointAutoNoShow() {
  return request.post('/medicaltech/examAppoint/autoNoShow')
}
