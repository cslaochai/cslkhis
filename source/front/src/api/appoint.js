import request from './request'

// ========== 挂号管理 ==========

// 查询预约列表
export function getRegistrationList(params) {
    return request.get('/appoint/listPage', {params})
}

/**
 * 六格状态卡取数：一次请求拿「总数 + 五个状态」的条数。
 *
 * 为什么不再复用 getRegistrationList：那六个数原本是前端发 6 次 listPage、每次 pageSize=1
 * 只为读 total 算出来的（用分页接口当 count 接口）；加上列表自己那次，首屏共 7 次。
 * 统计口径与 listPage 完全一致（同一套筛选条件），所以**筛选条件变了这六个数要跟着变**。
 */
export function getAppointStatusCount(params) {
    return request.get('/appoint/statusCount', {params})
}

/**
 * 预约看板取数：一次拿整段区间（日视图 1 天 / 周视图 7 天）的**全部**挂号，不分页。
 *
 * 为什么不复用 getRegistrationList：看板要按「日期 × 班次 × 医生」铺格子，每格的「已挂 N / 总号源」
 * 和「还有 M 人」都要求拿到整段全量，某一页没有意义 —— 按天拆 7 次分页查询会变成 7×页数 个请求，
 * 且任一分页被截断时看板只是「就这么几张」，**不报错**。
 */
export function getBoardRegistList(data) {
    return request.post('/appoint/boardList', data)
}

// 新增预约/挂号
export function createRegistration(data) {
    return request.post('/appoint/appointUpsert', data)
}

// 取消预约/退号
export function cancelRegistration(id, reason) {
    return request.post('/appoint/cancelRegist', {registId: id, reason})
}

// 修改挂号
export function updateRegistration(data) {
    return request.post('/appoint/appointUpsert', data)
}

/**
 * 复诊「原病历」候选列表（按就诊日倒序）。
 *
 * 为什么不走 getEmrRecordList：那个接口挂在 EmrController 的**类级**
 * hasAnyAuthority('opd:doctorWorkstation:list', ...) 下，收费员/前台导诊调它是 403，
 * 而「挂复诊要选哪一次就诊」正是挂号窗口每天在做的事。
 */
export function getRevisitRecordSelectList(patientId) {
    return request.get('/appoint/revisitRecordSelectList', {params: {patientId}})
}

/**
 * 复诊费用预估：提交挂号前问一次「这张号收多少钱、按哪条策略」。
 * 判定与后端实收共用同一个 decide，所以不会出现「预览 0 元、窗口要交 12 元」。
 */
export function previewRevisitFee(data) {
    return request.post('/appoint/revisitFeePreview', data)
}

/**
 * 医生站建复诊号（来源 1-当日回诊 / 2-医嘱复诊预约）。
 * 后端要的是 opd:doctorWorkstation:add；不要用 createRegistration —— 那个接口要挂号页的
 * opd:appointments:add，医生角色（sql/120 起）已经拿不到，按钮点了会 403。
 */
export function createRevisitRegistration(data) {
    return request.post('/appoint/revisitUpsert', data)
}

// ========== 排班管理 ==========

// 查询排班列表
export function getScheduleList(params) {
    return request.post('/schedule/list', params)
}

// 查询可挂号源（doctorId 可选：已收费号改约只认同医生的号源，取数时就收口）
export function getAvailableSchedule(deptId, visitDate, doctorId) {
    return request.post('/schedule/selectList', {deptId, visitDate, doctorId})
}

/**
 * 查询排班时间片段（半小时一档，biz_schedule_slot）。
 * 号源与占用的事实都在段上：挂号/预约选段后带 slotId 提交，后端按段扣号源；
 * 段可能被排班重建物理删除，重新拉一次即可。
 */
export function getScheduleSlots(scheduleId) {
    return request.get('/schedule/slotList', {params: {scheduleId}})
}

/**
 * 批量查询多条排班的时间片段（日视图「医生 × 半小时段」看板一次拉全）。
 * 一天几十条排班逐条调 slotList 会打出一串请求，且整屏要等最慢那一格。
 * @param scheduleIds 排班ID数组
 */
export function getScheduleSlotsBatch(scheduleIds) {
    return request.post('/schedule/slotListBatch', {scheduleIds: scheduleIds || []})
}

/**
 * 今日在岗（sql/196）—— 排班的下游出口。
 * 排班表是「计划」，业务真正要问的是「此刻这个科室谁在班」。全岗位混排，看 staffType 分流。
 *
 * @param params {deptId?, staffType?, date?, moment?, onDutyOnly?}
 *   moment 传 "HH:mm" 可按指定时刻判定（跨零点夜班 20:00-08:00 的判定要可复现）；
 *   onDutyOnly=false 返回当天全部排班并逐条带 onDutyNow。
 */
export function getOnDutyStaff(params) {
    return request.post('/schedule/onDuty', params)
}

// 批量新增排班：整批（人×日）一次提交，展开与事务全部在后端完成，前端禁止循环调单条接口
export function createScheduleBatch(data) {
    return request.post('/schedule/batchUpsert', data)
}

// 复刻排班：把源区间内正常排班整体后移 dayOffset 天，单事务落库
export function replicateSchedule(data) {
    return request.post('/schedule/replicate', data)
}

// 删除排班
export function deleteSchedule(id) {
    return request.delete('/schedule/deleteById', {params: {scheduleId: id}})
}

// ========== 排班发布态流转（G-15：草稿→待审核→已发布→调班中→已变更）==========

// 提交审核（草稿→待审核）
export function scheduleSubmit(scheduleId) {
    return request.post('/schedule/submit', null, {params: {scheduleId}})
}

// 审核发布（待审核→已发布，同时出号）
export function scheduleAuditPublish(scheduleId) {
    return request.post('/schedule/auditPublish', null, {params: {scheduleId}})
}

// 撤回（任意态→草稿，已发布的号源一并撤下）
export function scheduleRecall(scheduleId) {
    return request.post('/schedule/recall', null, {params: {scheduleId}})
}

// 进入调班（已发布/已变更→调班中，锁定并冻结号源）
export function scheduleAdjustStart(scheduleId) {
    return request.post('/schedule/adjustStart', null, {params: {scheduleId}})
}

// 整周提交审核：区间内本科室草稿→待审核，body 传 {deptId, startDate, endDate, staffType}，返回提交条数
export function scheduleSubmitWeek(data) {
    return request.post('/schedule/submitWeek', data)
}

// 整周审核发布：区间内本科室待审核→已发布并出号，body 同上，返回发布条数
export function scheduleAuditWeek(data) {
    return request.post('/schedule/auditWeek', data)
}

// 门诊排班「待我审批」分页：管理员/门诊办看全部待审核，普通审核人只看主档指定的本科室
export function schedulePendingApproval(data) {
    return request.post('/schedule/pendingApproval', data)
}

// 驳回：待审核→草稿（审核人不通过，退回排班人修改），body 传 {scheduleId, reason}
export function scheduleReject(data) {
    return request.post('/schedule/reject', data)
}

// ========== 班次字典（biz_shift：医院标准班次，排班/模板「标准班次」下拉的数据源） ==========

// 标准班次列表（deptId 传了 = 该科室适用 + 全院通用；status=1 只取启用）
export function getShiftSelectList(params) {
    return request.get('/shift/selectList', {params})
}

// 标准班次分页（关键词=名称模糊，后端 like，前端不切片）
export function getShiftListPage(data) {
    return request.post('/shift/listPage', data)
}

// 新增/修改标准班次（完整建条；时长按起止重算、同名同科室防重、跨零点拦截都在后端）
export function createShift(data) {
    return request.post('/shift/shiftUpsert', data)
}

// 标准班次改名（维护界面唯一入口：后端只更新名称列，时间/科室/类型不可改）
export function renameShift(id, shiftName) {
    return request.post('/shift/rename', null, {params: {id, shiftName}})
}

// 标准班次启用/停用（后端只更新状态列）
export function updateShiftStatus(id, status) {
    return request.post('/shift/updateStatus', null, {params: {id, status}})
}

// ========== 分诊站台 ==========
// 查询队列列表
export function getTodayQueueList(params) {
    // return request.get('/queue/getTodayQueueList', {params})
    return request.get('/queue/getTodayQueueList', {})
}

// 分页查询队列列表
export function getQueueListPage(params) {
    return request.get('/queue/listPage', {params})
}

// 队列统计
export function getQueueStats(params) {
    return request.get('/queue/stats', {params})
}

// ========== 门诊日志（跨科室查询分析） ==========
// 与 /queue/listPage 刻意分开：listPage 是分诊台口径（被当前登录用户科室强制收窄），
// 这两个端点的科室范围完全由筛选条件决定，且筛选条件全部下推后端。
// 前端禁止再对返回结果做 filter —— 那样翻页后结果会静默变少。

// 门诊日志分页
export function getOpdLogListPage(params) {
    return request.get('/queue/opdLogListPage', {params})
}

// 门诊日志统计条（与分页同一套筛选条件）
export function getOpdLogStats(params) {
    return request.get('/queue/opdLogStats', {params})
}

/**
 * 日终结转（未签到→爽约 / 已签到未就诊→未就诊 / 队列行→已失效）。
 * 不传 settleDate = 补跑「最早遗留日 ~ 昨天」；dryRun=true 只试算。
 * 后端还有两条触发路径（每晚 00:10 定时、进门诊页面顺手补跑），这里是手工重放。
 */
export function runDayEndSettle(data = {}) {
    return request.post('/appoint/dayEndSettle/run', data)
}

// 叫下一位
export function callNextQueue(data) {
    // silent：医生工作站需要按后端文案分流（「没有候诊患者」改成行动指引），错误提示由该页 catch 自行处理
    return request.post('/queue/callNext', data, {silent: true})
}

// 按挂号记录签到
export function checkInByRegist(registId) {
    return request.post('/queue/checkInByRegist', {registId})
}

// 排班停诊/启用（只更新状态列）
export function updateScheduleStatus(scheduleId, status) {
    return request.post('/schedule/updateStatus', {scheduleId, status})
}

// ========== 排班模板（周模板，按星期几配置固定班次） ==========

// 模板分页查询（关键词=科室/医生/诊室/备注；deptId/weekDay/status 精确过滤）
export function getScheduleTemplateListPage(data) {
    return request.post('/scheduleTemplate/listPage', data)
}

// 新增/修改模板
export function saveScheduleTemplate(data) {
    return request.post('/scheduleTemplate/save', data)
}

// 删除模板
export function deleteScheduleTemplate(id) {
    return request.delete('/scheduleTemplate/delete', {params: {id}})
}

// 模板启停
export function updateScheduleTemplateStatus(id, status) {
    return request.post('/scheduleTemplate/updateStatus', null, {params: {id, status}})
}

// 按模板生成目标周排班（weekOffset：0=本周 1=下周；staffType 岗位类别，空=全部岗位）
export function generateScheduleFromTemplate(weekOffset, deptId, staffType) {
    return request.post('/scheduleTemplate/generate', {weekOffset, deptId, staffType})
}

// 生成预览（dryRun 不落库）：将新增/跳过明细 + 涉及人员名单（staffType 岗位类别，空=全部岗位）
export function previewScheduleTemplate(weekOffset, deptId, staffType) {
    return request.get('/scheduleTemplate/preview', {params: {weekOffset, deptId, staffType}})
}

// 停诊影响名单（该班次在挂患者）
export function getStopImpact(scheduleId) {
    return request.get('/schedule/stopImpact', {params: {scheduleId}})
}

// 停诊批量退号（scheduleId 可空：空则只退号、不落排班变更日志）
export function batchCancelRegist(scheduleId, registIds, reason) {
    return request.post('/schedule/batchCancel', {scheduleId, registIds, reason})
}

// 门诊排班变更留痕分页（审计：谁在何时对哪个班次做了什么、为什么）
export function getClinicSourceLogPage(data) {
    return request.post('/schedule/logPage', data)
}

// 加号（号源总数/剩余同步增加并留痕）
export function addScheduleSource(scheduleId, addNum, reason) {
    return request.post('/schedule/addSource', {scheduleId, addNum, reason})
}

// 过号处理
export function overdueQueue(id, reason) {
    return request.post('/queue/overdueQueue', {id, reason})
}

// 医保费用预估
export function estimateInsurance(data) {
    return request.post('/queue/estimate', data)
}

// 呼叫患者
export function callPatient(id) {
    return request.post('/queue/callPatient', null, {params: {queueId: id}})
}

// 重呼患者
export function recallPatient(id) {
    return request.post('/queue/recallPatient', null, {params: {queueId: id}})
}

// 重新入队：mode=1 置为优先（候诊中插队首）/ mode=2 过号回队（排到队尾），返回新顺序号
export function rejoinQueue(id, mode = 1) {
    return request.post('/queue/rejoinQueue', null, {params: {queueId: id, mode}})
}

// 调号：候诊中患者相对当前位置前移/后移 N 位（step 正=后移，负=前移），返回新顺序号
export function moveQueue(queueId, step) {
    return request.post('/queue/moveQueue', null, {params: {queueId, step}})
}

// 改派：候诊中患者改到目标医生/诊室，返回新排队号与顺序号
export function reassignQueue(queueId, doctorId, roomId) {
    return request.post('/queue/reassignQueue', null, {params: {queueId, doctorId, roomId}})
}

// 读取当前登录医生的接诊状态（0 空闲 / 1 接诊中 / 2 暂离），医生 id 由服务端取，前端不传
export function getCurrentDoctorStatus() {
    return request.get('/queue/doctorStatus/current')
}

// 设置当前登录医生的接诊状态：0 恢复接诊 / 2 暂离
export function setDoctorStatus(status) {
    return request.post('/queue/doctorStatus/set', {status})
}

// ========== 分诊台统计接口 ==========

// 获取当前就诊中的患者
export function getConsultingPatients() {
    return request.get('/queue/consultingPatients')
}

// 获取复诊等候超时患者
export function getRevisitTimeoutPatients() {
    return request.get('/queue/revisitTimeout')
}

// ========== 门诊分诊（分诊卡） ==========
// 分诊记录只增不改：同一个患者重测/重定级是追加一条，历史留痕保留。
// 后端会回写队列上的「当前生效值」（triage_status/triage_level/room_*）。

// 保存分诊
export function saveTriage(data) {
    return request.post('/queue/triage/save', data)
}

// 分诊卡回显（当前生效值 + 历史）
export function getTriageByQueueId(queueId) {
    return request.get('/queue/triage/getByQueueId', {params: {queueId}})
}

// 已缴费未签到（分诊台「待签到」抽屉）。口径与 /queue/stats 的 unchecked 完全一致 ——
// 统计说 5 个、点开只有 3 个，比统计错更糟。
export function getUncheckedList(params) {
    return request.get('/queue/uncheckedList', {params})
}
