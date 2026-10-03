import request from './request'

// ==================== 住院手术闭环（P4.3：申请 → 排台 → 术前核对 → 完成 → 回写首页手术明细） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 八条必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围，后端统一按字符串出参，
//    前端不要 Number() 转换，否则会静默丢精度（回头查详情必然「手术申请单不存在」）。
// 2. 「能否排台 / 核对 / 完成 / 取消 / 修改」由后端给的 canSchedule / canPreopCheck /
//    canFinish / canCancel / canEdit 决定，前端不要自己按 operationStatus 码值 switch。
// 3. 状态机：0-待排期 → 1-已排期 → 2-术前核对完成 → 3-已完成；0/1 → 4-已取消。
//    未排期不可核对、未核对不可完成、**核对完成后不可取消**（患者已进手术区流程）。
// 4. 排台会被后端校验「同手术间时段重叠」，撞台时返回的文案会点明和哪一台撞了 ——
//    直接展示即可，不要自己改写成「排台失败」。
// 5. **完成才回写**：一次事务里写 ①病案首页手术明细 ②record_type=5 手术记录病历，
//    两个ID回填到本单（operationId / recordId）。所以「已完成」必然有 recordNo 可看。
// 6. 首页记的是**实际做的**手术（actualOperationName），不是拟施 ——
//    与拟施不一致时以实际为准，这是防「只做探查却编切除术」的关键。
// 7. 病案首页表单**不能删除/覆盖**由本闭环回写的手术明细行（后端按 apply_id 区分），
//    要在首页改这台手术，得回到本页面改手术单。
// 8. 日期筛选传 `yyyy-MM-dd` 即可（后端会放宽到整天）；传 `yyyy-MM-dd HH:mm:ss` 则按精确时刻。

// 手术申请分页（admissionId / applyDeptId / surgeonId / operationStatus / operationRoom /
//             plannedDateFrom / plannedDateTo / keyword / unfinishedOnly）
export function getOperationApplyListPage(params) {
    return request.get('/patient/inpatient/operationApply/listPage', {params})
}

// 手术申请详情
export function getOperationApplyDetail(applyId) {
    return request.get('/patient/inpatient/operationApply/getDetailById', {params: {applyId}})
}

// 某次住院的全部手术申请（按发生顺序升序）
export function getOperationApplyListByAdmission(admissionId) {
    return request.get('/patient/inpatient/operationApply/listByAdmission', {params: {admissionId}})
}

// 发起/修改手术申请（返回手术申请单号；修改仅限「待排期」）
export function saveOperationApply(data) {
    return request.post('/patient/inpatient/operationApply/save', data)
}

// 排台（待排期→已排期；已排期可改期；同手术间时段重叠会被拒）
export function scheduleOperation(data) {
    return request.post('/patient/inpatient/operationApply/schedule', data)
}

// 术前核对（已排期→术前核对完成；1/2/3/4 四项必核项缺一不可）
export function preopCheckOperation(data) {
    return request.post('/patient/inpatient/operationApply/preopCheck', data)
}

// 手术完成（回写病案首页手术明细 + 手术记录病历）
export function finishOperation(data) {
    return request.post('/patient/inpatient/operationApply/finish', data)
}

// 取消手术（仅「待排期/已排期」；已核对不可取消）
export function cancelOperation(data) {
    return request.post('/patient/inpatient/operationApply/cancel', data)
}

// 未完成手术数（工作台角标）
export function getOperationUnfinishedCount(params) {
    return request.get('/patient/inpatient/operationApply/countUnfinished', {params})
}

// 已用过的手术间（下拉候选）
export function getOperationRoomList() {
    return request.get('/patient/inpatient/operationApply/room/selectList')
}

// 术前核对要点字典（渲染勾选框；含 required 标识）
export function getOperationCheckItems() {
    return request.get('/patient/inpatient/operationApply/checkItemList')
}

// 排台总表（某天 × 手术间矩阵；date 为空按今天，格式 yyyy-MM-dd）
export function getOperationScheduleMatrix(date) {
    return request.get('/patient/inpatient/operationApply/scheduleMatrix', {params: {date}})
}

// ---------------- 手术间主数据（P134.1，排台总表的"台"） ----------------

// 全部手术间（含停用，管理页用；总表列只渲染启用中的）
export function getOperationRoomAll() {
    return request.get('/patient/inpatient/operationRoom/listAll')
}

// 启用中的手术间（下拉候选；参照数据，登录即可）
export function getOperationRoomEnabled() {
    return request.get('/patient/inpatient/operationRoom/selectList')
}

// 新增/修改手术间
export function saveOperationRoom(data) {
    return request.post('/patient/inpatient/operationRoom/upsert', data)
}

// 删除手术间（物理删；停用请改 status=0，别删）
export function deleteOperationRoom(roomId) {
    return request.delete('/patient/inpatient/operationRoom/deleteById', {params: {roomId}})
}
