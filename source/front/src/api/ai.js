import request from './request'

/**
 * AI 能力接口层
 *
 * 设计约定（与后端一致，前端不要绕过）：
 * 1. 所有接口返回 { code, message, data }，业务字段在 data 里。
 * 2. data.degraded === true 表示「本次未经过大模型」，结果来自确定性规则。
 *    界面必须把这个状态显示出来 —— 医生有权知道这条建议是谁给的。
 * 3. 审核类接口只传业务ID，不传业务内容。传内容等于给了绕过审核的口子。
 */

// ========== 运维 ==========

// AI 运行时状态（只读配置快照，不产生模型调用费用）
export function getAiHealthCheck() {
  return request.get('/ai/healthCheck')
}

// 刷新 AI 配置与 ICD 码表缓存
export function refreshAiConfig() {
  return request.post('/ai/configRefresh')
}

// ========== ICD-10 智能编码 ==========

// 推荐 ICD-10 编码
// params: { recordId?, patientId?, chiefComplaint?, presentIllness?, specialistExam?, diagnosis?, topN? }
// 说明：传 recordId 由服务端回查病历；也可直接传文本（用于未保存的草稿实时推荐）
export function predictIcd10(params) {
  return request.post('/ai/icd10/predict', params)
}

// 检索 ICD-10 编码下拉选项（纯字典查询，不调用模型）
export function selectIcd10List(params) {
  return request.post('/ai/icd10/selectList', params)
}

// ========== 处方合理性审核 ==========

// 执行处方审核
// params: { prescriptionId, saveResult? }  saveResult=false 时只审核不落库
export function executeDrugAudit(params) {
  return request.post('/ai/drugAudit/execute', params)
}

// ========== 病历内涵质控 ==========

// 执行病历内涵质控
// params: { recordId, saveResult? }
export function executeEmrQc(params) {
  return request.post('/ai/emrQc/execute', params)
}

// ========== 调用审计 ==========

// 分页查询 AI 调用审计
// params: { pageNum, pageSize, capabilityKey?, status?, bizType?, bizId?, operator?, startDate?, endDate? }
export function getAiAuditLogPage(params) {
  return request.post('/ai/auditLog/listPage', params)
}

// ========== 检验结果智能解读 ==========

// 解读一份检验报告
// params: { recordId, overwriteConclusion?, includeTrend? }
//
// 分工：哪些项目异常、是否达危急值、趋势是升是降 —— 全部由后端代码算好，
// 模型只解释这些异常组合起来意味着什么。所以 data.degraded=true 时
// items / trends / conclusion 依然有值（来自规则层），界面照常可用。
//
// overwriteConclusion 默认 false：结论是草稿，需要显式开启才会写回
// biz_laboratory_record.diagnosis / suggestions（会覆盖检验科已填内容）。
export function executeLabInterpret(params) {
  return request.post('/ai/labInterpret/execute', params)
}

// ========== 急诊智能分诊 ==========

// 分诊级别建议
// params: { emergencyId? } 或 { chiefComplaint, vitalSigns?, gender?, age? }
//
// 铁律（后端强制，前端不要绕过）：
//   1. 只升不降 —— 建议级别永远不会低于护士已选级别；
//   2. 不写库 —— 该接口没有写回端点，级别必须由人确认后手工设置；
//   3. levelBasis 说明建议来自哪里：HARD_RULE（确定性红旗征象）/ MANUAL（维持人工值）/ MODEL。
export function suggestEmergencyTriage(params) {
  return request.post('/ai/emergencyTriage/suggest', params)
}

// ========== 病历文本结构化抽取 / 草拟 ==========

// 把一段自由文本拆进病历各字段
// params: { rawText, recordId?, gender?, age? }
//
// 铁律（后端强制，前端不要绕过）：
//   1. 不写库 —— 产出只是候选值，必须由医生逐字段点「填入」才进表单；
//   2. 每条结果都带 source 与 evidence：
//        source=HARD_RULE 表示按原文标签逐字切分（未改写，优先采信）；
//        source=LLM 表示模型从自由文本里搬运，evidence 已校验存在于原文中；
//   3. rejectedCount > 0 说明有内容被判为「原文里找不到依据 / 字段不可写」而丢弃，
//      界面必须显示出来 —— 否则医生会以为原文里只有这些内容。
export function extractEmrText(params) {
  return request.post('/ai/emrText/extract', params)
}

// 病历草拟：只生成现病史草稿，不生成诊断与处理意见
// params: { recordId }
// data.draft 恒为 true：这是草稿，必须医生确认后才进病历。
// data.degraded=true 时 presentIllness 为空 —— 现病史没有「纯规则」版本可写，
// 界面不要拿模板文字糊弄医生，如实说「没有草稿可给」。
export function draftEmrText(params) {
  return request.post('/ai/emrText/draft', params)
}
