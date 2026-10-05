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
// 分工：哪些项目异常、是否达危急值、趋势是升是降 —— 全部由后端代码算好。
// 模型只在异常项 ≥3（组合异常）时参与做连贯解读（data.source='model'）；
// 异常项少时默认走规则结论（source='rule'，设计内路径，不是降级）；
// data.degraded=true 仅表示「组合异常需要模型但调用失败」，此时 items / trends /
// conclusion 依然有值（来自规则层），界面照常可用且警示条必显。
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

// ========== 语音口述转写（G-14，P5，医生工作站） ==========

// ASR 语音转写：把医生口述录音转成文本（确定性转换，失败如实报错，不造文本）
// params: FormData { file: 音频Blob, durationSeconds? }
// 返回 { text, durationSeconds, model, elapsedMs }；无 degraded 语义 —— 失败就是失败。
// 转写原文不落库，审计只落音频元数据 digest。
export function transcribeVoice(formData) {
  // request.js 实例默认 Content-Type: application/json，axios 1.x 对「FormData + JSON 头」
  // 会走 formDataToJSON 把文件序列化成 JSON 体，后端直接 MultipartException ——
  // 请求级显式声明 multipart，axios 才会让浏览器自己带 boundary 发原始表单
  return request.post('/ai/emrText/transcribe', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ========== AI 运营问数（NL2SQL） ==========

// 自然语言问数（白名单表受控 SELECT，只读）
// params: { question, withSummary? }
//
// 铁律（后端强制，前端不要绕过）：
//   1. data.degraded=true 时 rows 一定为空 —— 降级原因必须展示，不要拿空表格糊弄管理者；
//   2. data.sql 是实际执行的 SELECT，界面要能展开查看（透明可查）；
//   3. 行数据 cells 与 columns 按下标对应，渲染前要按列拼成对象。
export function askOperationQa(params) {
  return request.post('/ai/operationQa/ask', params)
}

// 可查询的数据域（白名单表清单，纯配置展示，不调用模型）
export function getOperationSchema() {
  return request.get('/ai/operationQa/schema')
}

// ========== 知识库问答（RAG） ==========

// 知识库问答：检索增强生成，只科普不判定（危急值/用药禁忌/分诊级别不在范围内）
// params: { question }
// data.degraded=true 时 answer 来自检索原文片段（模型未参与），degradeReason 必须展示。
export function askKnowledge(params) {
  return request.post('/ai/knowledge/ask', params)
}

// ========== 知识库维护（ai:knowledge:manage，AI 管理台维护签页） ==========

// 录入/更新知识文档（自动切块建索引）
// params: { title, category?, content, sourceType? }  sourceType: 2-手工录入 3-文件导入
export function ingestKnowledgeDoc(params) {
  return request.post('/ai/knowledge/ingest', params)
}

// 知识文档分页列表
// params: { pageNum, pageSize, title?, category? }
export function getKnowledgeDocPage(params) {
  return request.post('/ai/knowledge/listPage', params)
}

// 知识文档详情（含原文）
// params: { id }  id 全程字符串
export function getKnowledgeDocById(params) {
  return request.post('/ai/knowledge/getById', params)
}

// 删除知识文档（同步删除切块与索引）
// params: { id }  id 全程字符串
export function deleteKnowledgeDocById(params) {
  return request.post('/ai/knowledge/deleteById', params)
}

// 重建向量索引（从 chunk 表全量重载；向量库为进程内存，重启后丢失，靠它恢复）
export function rebuildKnowledgeIndex() {
  return request.post('/ai/knowledge/rebuild')
}

// 灌入内置示例语料（仅库为空时生效，返回灌入条数）
export function seedKnowledgeCorpus() {
  return request.post('/ai/knowledge/seed')
}

// ========== 随访话术草拟（G-06，医护端） ==========

// AI 拟随访话术草稿（产物只是输入框初稿，医生终审后才随任务下发）
// params: { patientId, followupType, diagnosis? }
// data.degraded=true 时 content 来自类型模板（模型不可用或文案越界被硬闸拦下），degradeReason 必须展示。
export function composeFollowup(params) {
  return request.post('/ai/followup/compose', params)
}

// ========== 草稿留痕（G-10，AI 管理台） ==========

// 病历草稿 vs 终稿 diff 分页（表在 his-emr，权限复用 ai:admin:list）
// params: { pageNum, pageSize, patientName?, doctorName?, changed? }
// row.diffJson 是分段数组 JSON：[{type:0|1|2, text}]，0-相同 1-删 2-增；渲染用模板插值，禁止 v-html。
export function listDraftDiffPage(params) {
  return request.post('/emr/draftDiff/listPage', params)
}

// ========== 医保审核证据判定（G-07，P3） ==========

// 对一条合规审核记录的命中项逐条判「证据支持/证据反驳/证据不足」；只读计算，不写库不改规则结论
// params: { auditId }
// data.degraded=true 表示模型未产出判定（judgments 为空），规则自身的判定依据与整改建议照常在页面上，
// 前端必须展示警示，禁止把「无判定」渲染成「无风险」。
export function judgeInsuranceEvidence(params) {
  return request.post('/ai/insurance/evidence', params)
}

// ========== 危重预警（G-12，P4，护士工作站/床位管理共用） ==========

// 病区扫描：MEWS+SpO2 纯代码评分，无模型调用、无审计行、只提示不写库
// params: { wardId }
// data: [{ admissionId, patientName, bedNo, measureTime, items, totalScore, alertLevel, alertText }]
// alertLevel: 0-无 1-关注 2-高危；评分口径单点在后端评分规则里，前端不要复算。
export function scanWardDeterioration(params) {
  return request.get('/ai/deterioration/wardScan', { params })
}

// 单患者预警解释：仅 alertLevel≥1 的患者才调模型给观察建议；advice 是建议不是医嘱
// params: { admissionId }
// data.degraded=true 时 advice 为空 —— 没有建议就如实说没有，界面警示条必显，
// 禁止拿模板文字糊弄护士。
export function explainDeterioration(params) {
  return request.get('/ai/deterioration/explain', { params })
}

// ========== AI 护理交接班（G-13，P4，护士工作站） ==========

// AI 拟 SBAR 交接班摘要：后端只聚合病区×班次事实，模型只拟文，护士编辑终审
// params: { wardId, shift(1-白班 2-小夜 3-大夜), shiftDate }
// data.source: 1-模型 2-规则模板；source=2 或 degraded=true 时警示必显。
// 产物不写库 —— 护士确认后的正文由护士自己贴进交接班记录。
export function composeNursingHandover(params) {
  return request.post('/ai/nursingHandover/compose', params)
}
