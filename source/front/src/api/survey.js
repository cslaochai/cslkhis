import request from './request'

/**
 * 满意度评价（sql/164）—— 问卷模板 → 发放/回收 → 答卷 → 看板 四件套。
 *
 * 三条必须记住的口径：
 * 1. **回收率的分母是发放行，不是答卷行**：未回收的也是数据（待推送/待回收/已过期/已拒答），
 *    所以看板数字一律走 `/survey/stat` 服务端聚合，前端不数当前页。
 * 2. **按钮可用性由后端给**（canFill / canPush / canRefuse / canEdit / canVoid），
 *    前端不按 dispatch_status 码值 switch。
 * 3. 手机号列表只出 `phoneMasked`；明文仅 `dispatch/getById`（回填表单）返回，
 *    且那个接口要 `qc:survey:edit`。前端不许再遮一遍。
 *
 * 所有 ID 都是字符串（雪花 ID 超出 JS 安全整数），不要 Number() 转换。
 */

// ---------------- 问卷模板 ----------------

/** 模板分页（keyword/scene/status/pageNum/pageSize） */
export function listTemplatePage(data) {
  return request({ url: '/survey/template/listPage', method: 'post', data })
}

/** 模板下拉（scene 可选；只回启用的） */
export function selectTemplateList(scene) {
  return request({ url: '/survey/template/selectList', method: 'get', params: { scene } })
}

/** 模板详情（含题目清单） */
export function getTemplateDetail(id) {
  return request({ url: '/survey/template/getDetailById', method: 'get', params: { id } })
}

/** 模板新增/修改（题目整卷覆盖；有发放记录后不可改题） */
export function templateUpsert(data) {
  return request({ url: '/survey/template/templateUpsert', method: 'post', data })
}

/** 删除模板 */
export function deleteTemplate(id) {
  return request({ url: '/survey/template/deleteById', method: 'delete', params: { id } })
}

// ---------------- 发放与回收 ----------------

/** 发放台账分页 */
export function listDispatchPage(data) {
  return request({ url: '/survey/dispatch/listPage', method: 'post', data })
}

/** 发放单详情（含明文手机号，供代填回显） */
export function getDispatchDetail(id) {
  return request({ url: '/survey/dispatch/getById', method: 'get', params: { id } })
}

/** 按随访任务手工发卷（同一任务 + 同一模板幂等） */
export function issueDispatch(data) {
  return request({ url: '/survey/dispatch/issue', method: 'post', data })
}

/** 标记推送状态：action 1-已推送 2-已拒答（「已回收」只能由答卷写入） */
export function markDispatch(data) {
  return request({ url: '/survey/dispatch/mark', method: 'post', data })
}

// ---------------- 答卷 ----------------

/** 答卷分页 */
export function listAnswerPage(data) {
  return request({ url: '/survey/answer/listPage', method: 'post', data })
}

/** 答卷详情（含逐题答案） */
export function getAnswerDetail(id) {
  return request({ url: '/survey/answer/getById', method: 'get', params: { id } })
}

/** 提交答卷（回收；低分会在后端自动登记投诉） */
export function submitAnswer(data) {
  return request({ url: '/survey/answer/submit', method: 'post', data })
}

/** 作废答卷（原因必填，作废后不进统计） */
export function voidAnswer(data) {
  return request({ url: '/survey/answer/voidAnswer', method: 'post', data })
}

// ---------------- 看板 ----------------

/** 满意度看板聚合（templateId/scene/dateFrom/dateTo 全可选） */
export function getSurveyStat(params) {
  return request({ url: '/survey/stat', method: 'get', params })
}
