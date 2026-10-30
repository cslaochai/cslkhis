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
 * 3. **签名时间取本机时钟**，由 `timeSource` / `timeSourceText` / `timeSourceNote` 如实给出，**不得**对外表述为可信时间。
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

// 作废签名（必须写理由；不改历史行，只追加作废信息并解除内容锁定）
export function invalidateSignature(signId, reason) {
  return request.post('/emr/signature/invalidate', { signId, reason })
}

// ---------------- 签名证书（CA 线下人工签发） ----------------

// 证书分页
export function getSignCertList(params) {
  return request.get('/emr/signCert/listPage', { params })
}

// 证书详情（含公钥 PEM 与指纹）
export function getSignCertDetail(id) {
  return request.get('/emr/signCert/getById', { params: { id } })
}

// 生成签名密钥对（CA 线下签发第一步）：返回「CA 签发申请单」（Subject DN + 公钥 PEM），供线下送 CA 人工签发
export function keygenSignCert(params) {
  return request.post('/emr/signCert/keygen', params)
}

// 回填 CA 线下签发的证书（CA 线下签发第二步）：校验证书公钥与本地密钥对一致后转为有效
export function bindSignCert(params) {
  return request.post('/emr/signCert/bind', params)
}

// 吊销证书（必须写理由；吊销不删行）
export function revokeSignCert(certId, reason) {
  return request.post('/emr/signCert/revoke', { certId, reason })
}

// 按员工查签名证书（员工管理「签名设置」只读弹框用；无证书返回空）
export function getSignCertByEmp(empId) {
  return request.get('/emr/signCert/getByEmp', { params: { empId } })
}
