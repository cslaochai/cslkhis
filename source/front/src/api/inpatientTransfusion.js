import request from './request'

// ==================== 住院输血闭环（P4.4：申请 → 配血 → 发血 → 双人核对输注 → 完成 → 反应上报） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 九条必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围，后端统一按字符串出参，
//    前端不要 Number() 转换，否则会静默丢精度（回头查详情必然「输血申请单不存在」）。
// 2. 「能否配血 / 发血 / 输注 / 完成 / 取消 / 上报反应」由后端给的 canCrossmatch / canIssue /
//    canStart / canFinish / canCancel / canReportReaction 决定，不要自己按 transfusionStatus 码值 switch。
// 3. 状态机：0-待配血 → 1-已配血 → 2-已发血 → 3-输注中 → 4-已完成；0/1/2 → 5-已取消。
//    未配血不可发血、未发血不可输注、**输注中与已完成不可取消**（血已进入患者体内）。
// 4. **配血状态（crossmatchStatus）与流程状态（transfusionStatus）是两个东西，都要显示**：
//    0-待配血 1-配血中（未配齐）2-全部相合 3-存在配血不合。
//    配血不合时流程会停在「待配血」，如果不显示 crossmatchStatusText，
//    就把"配了、但不合"看成了"还没配"。
// 5. **配血是按「袋」做的**：crossmatch 接口传 bags 数组，可以分多批提交（血站分批到货），
//    累计不超过申请袋数 bagCount。
// 6. **ABO / Rh 不相容会被整批拒绝**（这是致死性差错的入口拦截，直接展示后端文案，不要改写）。
//    但「交叉配血结论不合」是正常业务结果：数据会落库、接口仍返回 200，
//    消息里会写明"N 袋不合，本单不能发血" —— 这两种情况的处理方式不同，注意区分。
//    【易错】配血结论在**响应 message**里（res.message），不在 data 里：
//    后端 crossmatch 的 data 恒为 null，结论文案是给调用方直接展示的。
//    写成 res.data 会让"全部相合 / N 袋不合 / 只配了 x/n 袋"全变成兜底文案。
// 7. **输注前必须双人核对**：两个核对护士不能是同一个人，1~6 项必核项一项不能缺。
// 8. **完成才回写**：一次事务里写 record_type=11 输血记录病历 + 病案首页 is_transfusion=1，
//    病历ID回填到本单（recordId）。所以「已完成」必然有 recordNo 可看，没有就是链断了。
// 9. 有反应不要改历史：输血反应在**完成之后**用 reportReaction 补登记，
//    后端会拒绝在输注中/未完成状态上报。

// 输血申请分页（admissionId / patientId / applyDeptId / transfusionStatus / crossmatchStatus /
//             bloodComponent / patientAbo / hasReaction / unfinishedOnly /
//             applyDateFrom / applyDateTo / keyword）
export function getTransfusionApplyListPage(params) {
    return request.get('/patient/inpatient/transfusionApply/listPage', {params})
}

// 输血申请详情（含血袋明细 bags）
export function getTransfusionApplyDetail(applyId) {
    return request.get('/patient/inpatient/transfusionApply/getDetailById', {params: {applyId}})
}

// 某次住院的全部输血申请（按发生顺序升序）
export function getTransfusionApplyListByAdmission(admissionId) {
    return request.get('/patient/inpatient/transfusionApply/listByAdmission', {params: {admissionId}})
}

// 发起/修改输血申请（返回输血单号；修改仅限「待配血」）
export function saveTransfusionApply(data) {
    return request.post('/patient/inpatient/transfusionApply/save', data)
}

// 配血（逐袋录入交叉配血结果；ABO/Rh 不相容整批拒绝，"配血不合"则落库并提示不可发血）
// data: { applyId, bags: [{ bagNo, donorNo, bagAbo, bagRh, bloodComponent, spec, amount,
//                           amountUnit, sourceBank, collectDate, expireDate,
//                           crossmatchMain, crossmatchSide, crossmatchResult }], crossmatchNote }
export function crossmatchTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/crossmatch', data)
}

// 发血（已配血且全部相合 → 已发血）
export function issueTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/issue', data)
}

// 开始输注（含双人核对；核对护士 1/2 不能相同，1~6 项必核）
export function startTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/startInfusion', data)
}

// 输血完成（回写输血记录病历 + 病案首页是否输血标志）
export function finishTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/finish', data)
}

// 输血反应上报（仅「已完成」且尚未上报）
export function reportTransfusionReaction(data) {
    return request.post('/patient/inpatient/transfusionApply/reportReaction', data)
}

// 取消用血（仅「待配血/已配血/已发血」）
export function cancelTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/cancel', data)
}

// 未完成输血数（工作台角标）
export function getTransfusionUnfinishedCount(params) {
    return request.get('/patient/inpatient/transfusionApply/countUnfinished', {params})
}

// 血液品种字典（下拉候选）
export function getBloodComponentList() {
    return request.get('/patient/inpatient/transfusionApply/component/selectList')
}

// 输血前核对要点字典（渲染勾选框；含 required 标识）
export function getTransfusionCheckItems() {
    return request.get('/patient/inpatient/transfusionApply/checkItemList')
}

// 输血反应类型字典（受控字典，只能选不能填）
export function getTransfusionReactionTypes() {
    return request.get('/patient/inpatient/transfusionApply/reactionTypeList')
}

// ---------------- 用血分级审批（sql/93） ----------------

// 用血审批（通过/驳回；驳回必填原因；急诊补审同走此口）
export function approveTransfusion(data) {
    return request.post('/patient/inpatient/transfusionApply/approve', data)
}

// 某单的审批流水（逐级链，时间正序）
export function getTransfusionApproveList(applyId) {
    return request.get('/patient/inpatient/transfusionApply/approveListByApply', {params: {applyId}})
}

// 审批统计（按状态 + 按级别）
export function getTransfusionApproveStats() {
    return request.get('/patient/inpatient/transfusionApply/approveStats')
}
