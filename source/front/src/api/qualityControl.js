import request from './request'

/**
 * 病案质控（P5.4）
 *
 * 路径从 `/charge/qualityControl` 改为 `/emr/qualityControl`：质控属于病历管理，
 * 原前缀让所有人误以为它是收费模块的功能。
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. **来源（recordSource）必须二选一**：门诊病历与住院文书是两张结构完全不同的表，
 *    归并成一张列表要 UNION，而 UNION 之后的字符串列很容易踩排序规则的坑。
 *    质控员的实际工作流本来就是"今天质控住院病案 / 今天质控门诊病历"，不混着看。
 * 2. **得分与结论是两件事**：`score` 是 100 分制扣分（甲≥90 / 乙75~89 / 丙<75），
 *    `qcResult` 只说通过与否（0-不通过 / 1-通过）。有否决项命中的病历必定不通过。
 * 3. **`score` 为 null 表示旧版质控未评分**（2026-09-19 之前那批单子），
 *    这时 `gradeText` 也是 null —— 不要用 `?? 0` 兜底成 0 分。
 * 4. **`empty = true` 必须显式提示**：这条规则从没命中过。0 命中是观测值，
 *    不等于"这条规则没问题"，更不等于"这条规则实现了"。
 * 5. 所有 ID 都是字符串（雪花ID），**不要 Number()**，否则精度直接丢失。
 * 6. AI 内涵质控（qcType=4）由 AI 模块产生，同样落在这张表里，各自留痕、互不覆盖。
 */

// 质控单分页（recordSource / qcType / qcStatus / qcResult / keyword 可空）
export function getQualityControlList(params) {
  return request.post('/emr/qualityControl/listPage', params)
}

// 质控单详情（含问题明细）
export function getQualityControlDetail(id) {
  return request.get('/emr/qualityControl/getById', { params: { id } })
}

// 质控概览（总数 / 通过数 / 平均分 / 甲级率 / 维度分布）
export function getQualityControlOverview() {
  return request.get('/emr/qualityControl/getOverview')
}

// 规则清单与命中统计（含从未命中的规则）
export function listQcRuleMetric(dimension) {
  return request.get('/emr/qualityControl/listRuleMetric', {
    params: dimension ? { dimension } : {},
  })
}

// 某质控单的问题明细
export function listQcIssues(qcId) {
  return request.get('/emr/qualityControl/listIssueByQc', { params: { qcId } })
}

// 待质控病历候选分页（必须先选 recordSource）
export function listQcCandidatePage(params) {
  return request.get('/emr/qualityControl/listCandidatePage', { params })
}

// 维度字典
export function getQcDimensionDict() {
  return request.get('/emr/qualityControl/dimensionDict')
}

// 质控类型字典（0-综合 1~3-三维度 4-AI内涵质控）
export function getQcTypeDict() {
  return request.get('/emr/qualityControl/qcTypeDict')
}

// 执行质控（单份病历）
export function executeQualityControl(params) {
  return request.post('/emr/qualityControl/executeQc', params)
}

// 执行质控（批量）
export function executeQualityControlBatch(params) {
  return request.post('/emr/qualityControl/executeQcBatch', params)
}

// 处理质控问题（整改完成 / 忽略）
export function handleQualityControl(id, params) {
  return request.post('/emr/qualityControl/handleQc', { id, ...params })
}

// ---------- 病历三级质控流转（G18） ----------
// 状态机收口在后端 RecordQcFlowServiceImpl；码值文案唯一口径 lib/recordQcFlow.js。
// 发起条件：病历存在且未作废；同一病历同时只允许一条在途（未终审通过即在途）。

// 发起流转（进入科级待审）
export function startQcFlow(data) {
  return request.post('/recordQcFlow/start', data)
}

// 当前级审核通过（科级/病案室；医务处须走 finalQcFlow）
export function approveQcFlow(data) {
  return request.post('/recordQcFlow/approve', data)
}

// 当前级退回整改（defectDetail / requirement 必填）
export function returnQcFlow(data) {
  return request.post('/recordQcFlow/return', data)
}

// 科室整改提交（回到退回发生级待审）
export function resubmitQcFlow(data) {
  return request.post('/recordQcFlow/resubmit', data)
}

// 医务处终审（grade 必填：1甲/2乙/3丙，终态）
export function finalQcFlow(data) {
  return request.post('/recordQcFlow/finalApprove', data)
}

// 流转单分页
export function getQcFlowListPage(params) {
  return request.post('/recordQcFlow/listPage', params)
}

// 流转单详情
export function getQcFlowDetail(id) {
  return request.get('/recordQcFlow/getDetailById', { params: { id } })
}

// 流转时间线（动作升序）
export function getQcFlowActions(flowId) {
  return request.get('/recordQcFlow/listActions', { params: { flowId } })
}
