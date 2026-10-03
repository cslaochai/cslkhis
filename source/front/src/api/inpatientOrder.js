import request from './request'

// ==================== 住院医嘱（P1：医嘱 → 校对 → 执行 → 计费） ====================
// 约定：查询一律 GET（与后端 @GetMapping 对齐），写操作一律 POST。
//
// 三个必须记住的口径：
// 1. 所有 ID 都是**字符串**：雪花ID 超过 JS 的 Number 安全整数范围（2^53），
//    后端统一按字符串出参，前端不要再 Number() 转换，否则会静默丢精度
//    （2100945558440022018 → ...0022000，回头查详情必然「医嘱不存在」）。
// 2. 「能否校对 / 能否停止 / 能否作废」由后端给的 canVerify / canStop / canCancel 决定，
//    前端不要自己按 orderStatus 码值 switch：一旦后端新增码值，switch 会静默渲染成
//    「看着正常」的错按钮（同「未知码值一律渲染成未知(码值)」的道理）。
// 3. /execPendingList 是 GET **但会写库**：它会为"该有今天这次执行"的长期医嘱补当天计划行（幂等）。
//    刻意不引入定时任务，避免"服务停机那天全院长期医嘱计划集体缺失"。

// ---------- 医嘱 ----------

// 医嘱分页（admissionId / patientId / orderType / orderStatus / orderClass / keyword / pendingVerifyOnly）
export function getInpatientOrderListPage(params) {
    return request.get('/patient/inpatient/order/listPage', {params})
}

// 开立/修改医嘱（一次提交 = 一个组套），返回组套号
export function saveInpatientOrder(data) {
    return request.post('/patient/inpatient/order/save', data)
}

// 护士医嘱校对（批量）：未校对不可执行，校对通过才生成执行计划
export function verifyInpatientOrder(data) {
    return request.post('/patient/inpatient/order/verify', data)
}

// 停止医嘱（同组套整组停）
export function stopInpatientOrder(data) {
    return request.post('/patient/inpatient/order/stop', data)
}

// 作废医嘱（仅「待校对」；属组套的整组作废）
export function cancelInpatientOrder(data) {
    return request.post('/patient/inpatient/order/cancel', data)
}

// 待校对医嘱数（护士站卡片）
export function getInpatientOrderPendingVerifyCount(admissionId) {
    return request.get('/patient/inpatient/order/countPendingVerify', {params: {admissionId}})
}

// 待执行医嘱数（护士站卡片）
export function getInpatientOrderPendingExecCount(admissionId) {
    return request.get('/patient/inpatient/order/countPendingExec', {params: {admissionId}})
}

// ---------- 执行 ----------

// 护士待执行队列（加急优先、按计划时间升序；会补当天长期医嘱计划）
export function getOrderExecPendingList(params) {
    return request.get('/patient/inpatient/order/execPendingList', {params})
}

// 医嘱执行（批量）：execStatus=2 已执行并计费 / 3 已跳过且 execNote 必填
export function completeOrderExec(data) {
    return request.post('/patient/inpatient/order/exec/complete', data)
}

// 执行记录查询（含已执行 / 已跳过，留痕不删除）
export function getOrderExecList(params) {
    return request.get('/patient/inpatient/order/execList', {params})
}

// ---------- 输液执行闭环（G14） ----------
// 闭环挂在「执行行」上（长期医嘱一天多袋，每袋独立闭环）；
// 仅静脉类给药（静滴/静注/静推/泵入）有闭环，判定口径在后端 InpatientInfusionServiceImpl.isInfusionRoute，
// 前端用 exec.infusion 布尔（后端算好）决定入口显隐，**不要在前端自己写 route 关键词匹配**。

// 开始输注（执行行需已执行；记录滴速）
export function startInfusion(data) {
    return request.post('/patient/inpatient/infusion/start', data)
}

// 巡视（开始后、结束前；滴速/余量/备注）
export function addInfusionRound(data) {
    return request.post('/patient/inpatient/infusion/round', data)
}

// 结束输注（adverseFlag=1 时 adverseNote 必填）
export function finishInfusion(data) {
    return request.post('/patient/inpatient/infusion/finish', data)
}

// 某执行行的巡视记录（时间升序）
export function getInfusionRounds(execId) {
    return request.get('/patient/inpatient/infusion/rounds', {params: {execId}})
}

// ---------- 医嘱模板（医生个人模板，sql/103） ----------
// 模板不落库到医嘱表：套用只是把明细回填到开立表单，医生改完仍走 /order/save（source=2）。
// 所以模板接口不碰医嘱的双签、组套同起同停、欠费管控与计费快照。
// 权限复用 ipd:order:*；后端一律按当前登录医生过滤，前端传别人的模板 id 只会得到「模板不存在」。

// 模板下拉候选（开立弹窗「套用模板」：id/名称/类型/条数）
export function getOrderTemplateSelectList() {
    return request.get('/patient/inpatient/order/template/selectList')
}

// 模板分页（模板管理弹窗：keyword / orderType / pageNum / pageSize）
export function getOrderTemplateListPage(params) {
    return request.get('/patient/inpatient/order/template/listPage', {params})
}

// 模板明细（含 items，套用时用它回填表单）
export function getOrderTemplateById(id) {
    return request.get('/patient/inpatient/order/template/getById', {params: {id}})
}

// 新增/修改模板（一次提交 = 全量明细），返回模板ID
export function upsertOrderTemplate(data) {
    return request.post('/patient/inpatient/order/template/upsert', data)
}

// 删除模板（只删模板，不影响已按它开出的医嘱）
export function deleteOrderTemplateById(id) {
    return request.delete('/patient/inpatient/order/template/deleteById', {params: {id}})
}

// ---------- 医嘱基础字典（途径 / 频次 / 剂量单位，sql/142） ----------
// 为什么不是 /system/dict/*：通用字典接口要 system:dict:add，而这页挂的是 ipd:orderDict:*。
// 为了在这页能保存就把系统字典写权限发给医生，等于把「患者性别」「收费项目类别」一起交出去，
// 所以后端口子只认三种 dictType（his_order_route / his_order_freq / his_dose_unit）。
//
// 两条硬规则（服务端收口，前端照做即可）：
//  1. 字典值不可改 —— 存量医嘱行里存的就是这串值，改了历史医嘱会渲染成「未知(xxx)」。
//     要换值：停用旧的 + 新增一条。
//  2. 写完后端会刷 sys:dict:* 缓存，医生站下拉当次就能看到新值（不刷要等 24 小时）。

// 字典分页（dictType 必填：his_order_route / his_order_freq / his_dose_unit）
export function getOrderDictListPage(params) {
    return request.get('/patient/inpatient/order/dict/listPage', {params})
}

// 启用的字典项（下拉用）
export function getOrderDictSelectList(dictType) {
    return request.get('/patient/inpatient/order/dict/selectList', {params: {dictType}})
}

// 新增/修改字典项（新增和修改同一接口、同一个 :add 权限码）
export function upsertOrderDict(data) {
    return request.post('/patient/inpatient/order/dict/upsert', data)
}

// 删除字典项（逻辑删；id 与 dictType 都要传，后端按类型校验归属）
export function deleteOrderDictById(id, dictType) {
    return request.delete('/patient/inpatient/order/dict/deleteById', {params: {id, dictType}})
}

// ---------- 医嘱组套模板（个人 / 科室 / 全院，sql/142） ----------
// 与个人模板共用一张表，差别只在 scope：1-个人 2-科室 3-全院。
// 可见范围由服务端按「全院 ∪ 本科室 ∪ 本人」切，前端传任何 id 都越不出去；
// 列表行带 editable —— 按钮能不能点由它决定，不做「点了才报错」。

// 组套下拉候选（开立弹窗「套用组套」：可见的全院 + 本科室 + 自己的）
export function getOrderSetSelectList() {
    return request.get('/patient/inpatient/order/set/selectList')
}

// 组套分页（管理页：keyword / scope / orderType / pageNum / pageSize）
export function getOrderSetListPage(params) {
    return request.get('/patient/inpatient/order/set/listPage', {params})
}

// 组套明细（含 items，编辑回显与预览共用）
export function getOrderSetDetailById(id) {
    return request.get('/patient/inpatient/order/set/getDetailById', {params: {id}})
}

// 新增/修改组套（一次提交 = 全量明细），返回组套ID
export function upsertOrderSet(data) {
    return request.post('/patient/inpatient/order/set/upsert', data)
}

// 删除组套（只删模板，不影响已按它开出的医嘱）
export function deleteOrderSetById(id) {
    return request.delete('/patient/inpatient/order/set/deleteById', {params: {id}})
}
