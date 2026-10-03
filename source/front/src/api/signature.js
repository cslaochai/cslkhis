import request from './request'

/**
 * 电子签名与时间戳（P5.5）
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. **签名是证据，不是状态**。一条签名落库后只增不改不删；作废是"追加作废信息"，
 *    历史行、历史的签名值、历史的被签内容快照全部保留。所以列表接口默认会带出已作废的签名，
 *    `signStatus` 是筛选条件而不是"过滤掉作废"的开关。
 * 2. **验签结论拆成两个断言**：`signatureValid`（签名值本身能不能验通 —— false 表示证据被换过）
 *    与 `contentMatched`（当前内容摘要 == 签名时摘要 —— false 表示签名后内容被改过）。
 *    绝对不要合成一个"通过/不通过"布尔值：两者的性质与处理方式完全不同。
 * 3. **时间戳来源必须自报**。本条签名的时间戳是"本机时钟 / 院内授时 / 第三方 TSA"，
 *    由 `timeSource` / `timeSourceText` / `timeSourceNote` 如实给出，**不得**对外表述为可信时间。
 * 4. **证书是院内托管的**，不是 CA 签发的。`certTrustNote` 说明信任级别，
 *    `issuedMode`=2 表示自动签发，信任级别低于人工签发，页面上要能看出来。
 * 5. 所有 ID 都是字符串（雪花ID），**不要 Number()**。
 */

// ---------------- 签名记录 ----------------

// 签名记录分页（bizType / signScene / signStatus / verifyStatus / timeSource / bizId / signerId / bizNo / keyword / beginTime / endTime 可空）
export function getSignatureList(params) {
  return request.get('/emr/signature/listPage', { params })
}

// 签名详情（含被签内容快照全文）
export function getSignatureDetail(id) {
  return request.get('/emr/signature/getById', { params: { id } })
}

// 某对象的签名链（含已作废的，按 chain_no 升序）
export function listSignatureByBiz(bizType, bizId) {
  return request.get('/emr/signature/listByBiz', { params: { bizType, bizId } })
}

// 某对象的签名情况：当前锚点状态 / 签名链 / 能否补签及不能的原因
export function getObjectSignatureStatus(bizType, bizId) {
  return request.get('/emr/signature/objectStatus', { params: { bizType, bizId } })
}

// 签名概览（有效/作废/未校验/验签失败 + 各类型覆盖率 + 时间来源与证书信任说明）
export function getSignatureSummary() {
  return request.get('/emr/signature/summary')
}

// 下拉选项（对象类型 / 场景 / 签名状态 / 验签状态 / 时间来源 / 证书状态 / 签发方式）
export function getSignatureQueryOptions() {
  return request.get('/emr/signature/queryOptions')
}

// 按 ID 验签（写库：会回写核查结果）
export function verifySignature(signId) {
  return request.post('/emr/signature/verify', { signId })
}

// 按对象验签（含已作废签名）
export function verifySignatureByBiz(bizType, bizId) {
  return request.post('/emr/signature/verifyByBiz', { bizType, bizId })
}

// 补签（管理员发起；签名人一律取当前登录用户，必须写明原因）
export function makeUpSignature(params) {
  return request.post('/emr/signature/sign', params)
}

// 作废签名（必须写理由；不改历史行，只追加作废信息并解除内容锁定）
export function invalidateSignature(signId, reason) {
  return request.post('/emr/signature/invalidate', { signId, reason })
}

// ---------------- 签名证书 ----------------

// 证书分页
export function getSignCertList(params) {
  return request.get('/emr/signCert/listPage', { params })
}

// 证书详情（含公钥 PEM 与指纹）
export function getSignCertDetail(id) {
  return request.get('/emr/signCert/getById', { params: { id } })
}

// 有效证书下拉
export function listSignCertSelect(keyword) {
  return request.get('/emr/signCert/selectList', { params: keyword ? { keyword } : {} })
}

// 人工签发证书
export function issueSignCert(params) {
  return request.post('/emr/signCert/issue', params)
}

// 吊销证书（必须写理由；吊销不删行）
export function revokeSignCert(certId, reason) {
  return request.post('/emr/signCert/revoke', { certId, reason })
}

// ---------------- 可信时间戳 TSA（G6） ----------------

/**
 * 口径：available（适配器在线）/ configTimeSource（配置值）/ effectiveTimeSource（生效值）
 * 三个必须一起读 —— 配置了 3 但适配器不在线时生效的是 1（本机时钟），只看配置值就是谎报。
 * 本期接入的是本地内置 TSA（演示信任根，令牌结构真实但非第三方 CA/TSA）。
 */

// TSA 服务状态（纯查询，无副作用）
export function getTsaStatus() {
  return request.get('/emr/tsa/status')
}

// 时间戳令牌台账分页（serial 精确 / keyword 摘要模糊 可空）
export function getTsaTokenList(params) {
  return request.get('/emr/tsa/tokenListPage', { params })
}

// ---------------- 可信时间戳 TSA 运维（G6b） ----------------

// TSA 服务启停（0 停用 / 1 启用）。停用=不再签发新令牌，签名自动降级本机时钟；历史令牌仍可验。
// 返回操作后的最新状态（available 立即反映）。
export function updateTsaStatus(tsaStatus) {
  return request.post('/emr/tsa/updateStatus', { tsaStatus })
}

// 切换签名时间来源（1 本机时钟 / 3 可信时间戳；2 院内授时未实现会被拒绝）。
// 返回操作后的最新状态（configTimeSource / effectiveTimeSource 同屏）。
export function updateTsaTimeSource(timeSource) {
  return request.post('/emr/tsa/timeSource', { timeSource })
}

// 台账令牌复验（只读，不落留痕）。valid=false 时 failReason 给人话原因。
export function verifyTsaToken(id) {
  return request.post('/emr/tsa/verifyToken', { id })
}
