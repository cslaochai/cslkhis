import request from './request'

// ==================== 住院转科（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历） ====================
// 约定：查询一律 GET，写操作一律 POST。
//
// 六条必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围，后端统一按字符串出参，
//    前端不要 Number() 转换，否则会静默丢精度（回头查详情必然「转科记录不存在」）。
// 2. 「能否接收 / 取消」由后端给的 canAccept / canCancel 决定，前端不要自己按 transferStatus
//    码值 switch —— 后端加一个状态，switch 会静默渲染成"看着正常"的错按钮。
// 3. **发起 ≠ 转科生效**：save 只留下一张「待接收」的单，床位不占、科室不改、医嘱不停。
//    真正生效在 accept 那一刻（后端会重新校验目标床位是否仍空闲，所以 save 时床是空的、
//    accept 时可能已被占 —— 这是正常拦截，不是 bug）。
// 4. **接收会自动回写转科记录病历**（record_type=10，状态=已提交），病历号从 recordNo 读；
//    前端不许手工建"转科记录"型文书（后端也会拒绝）。
// 5. 转科 ≠ 换床：同科室挪床位走 `/patient/inpatient/transfer`（换床），跨科室才走本模块；
//    发起同科室转科会被后端直接拒绝。
// 6. 医嘱处置结果在 `orderRemark` 里，形如「已随转科停止长期医嘱 3 条；仍有 1 条长期医嘱未停
//    （YZ202609190001/待校对），需由原科室医生处理」——**必须展示给使用者**，
//    后端刻意不用静默跳过（停不掉的医嘱是医嘱，不是"顺手的事"）。

// 转科记录分页（admissionId / patientId / fromDeptId / toDeptId / transferStatus / transferType / keyword）
export function getTransferListPage(params) {
    return request.get('/patient/inpatient/transferRecord/listPage', {params})
}

// 转科详情
export function getTransferDetail(transferId) {
    return request.get('/patient/inpatient/transferRecord/getDetailById', {params: {transferId}})
}

// 某次住院的转科轨迹（按发生顺序升序，第一条的 fromDept 就是入院科室）
export function getTransferListByAdmission(admissionId) {
    return request.get('/patient/inpatient/transferRecord/listByAdmission', {params: {admissionId}})
}

// 发起转科（返回转科单号；此时未生效，需转入科室接收）
export function saveTransfer(data) {
    return request.post('/patient/inpatient/transferRecord/save', data)
}

// 转入科室接收（转科真正生效：停原长期医嘱 + 换科室换床 + 回写病历）
export function acceptTransfer(data) {
    return request.post('/patient/inpatient/transferRecord/accept', data)
}

// 取消转科申请（仅「待接收」；已接收的要转回去，请再发起一次转科）
export function cancelTransfer(data) {
    return request.post('/patient/inpatient/transferRecord/cancel', data)
}

// 待接收转科数（转入科室工作台角标）
export function getTransferPendingCount(params) {
    return request.get('/patient/inpatient/transferRecord/countPending', {params})
}
