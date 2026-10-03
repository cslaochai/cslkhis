/**
 * 临床路径口径单点 —— 状态标签/颜色与动作显隐，页面禁写本地副本。
 * 文案走字典 his_pathway_*（dict-cache），这里只负责「码值 → tag 类型/语义」的渲染口径。
 */

/** 模板状态：1草稿 2使用中 3已停用 */
export const PATHWAY_STATUS = {
  DRAFT: 1,
  ACTIVE: 2,
  DEPRECATED: 3,
}

/** 入径状态：1在径 2已完成 3已退径 */
export const ENROLL_STATUS = {
  ENROLLED: 1,
  FINISHED: 2,
  ABORTED: 3,
}

/** 模板状态 → tag 类型 */
export const pathwayStatusTag = (status) => {
  switch (Number(status)) {
    case PATHWAY_STATUS.DRAFT: return 'warning'
    case PATHWAY_STATUS.ACTIVE: return 'success'
    case PATHWAY_STATUS.DEPRECATED: return 'info'
    default: return 'info'
  }
}

/** 入径状态 → tag 类型 */
export const enrollStatusTag = (status) => {
  switch (Number(status)) {
    case ENROLL_STATUS.ENROLLED: return 'primary'
    case ENROLL_STATUS.FINISHED: return 'success'
    case ENROLL_STATUS.ABORTED: return 'info'
    default: return 'info'
  }
}

/** 仅草稿可编辑/发布 */
export const canEditPathway = (status) => Number(status) === PATHWAY_STATUS.DRAFT
export const canPublish = (status) => Number(status) === PATHWAY_STATUS.DRAFT
export const canDeprecate = (status) => Number(status) === PATHWAY_STATUS.ACTIVE

/** 仅在径可登记变异/完成/退径 */
export const canVariance = (status) => Number(status) === ENROLL_STATUS.ENROLLED
export const canFinish = (status) => Number(status) === ENROLL_STATUS.ENROLLED
export const canAbort = (status) => Number(status) === ENROLL_STATUS.ENROLLED
