/**
 * 病历更正申请 —— 码值唯一口径（与后端 EmrCorrectionStatusEnum / EmrCorrectionTypeEnum 同源对齐，#20）。
 *
 * 状态机：1 待科室审核 → 2 待医务审批 → 3 已批准待修改 → 5 已完成；
 * 任一级驳回 → 4 已驳回；申请人撤回在途单 → 6 已撤回。
 * 页面禁止写本地映射副本：两份码值表一定有一份先过期。
 */

export const CORRECTION_STATUS = {
  DEPT_PENDING: 1,
  MEDICAL_PENDING: 2,
  APPROVED: 3,
  REJECTED: 4,
  COMPLETED: 5,
  REVOKED: 6,
}

export const CORRECTION_TYPE = {
  DOCTOR_SELF: 1,
  SUPERIOR_ON_BEHALF: 2,
}

const STATUS_TEXT = {
  1: '待科室审核',
  2: '待医务审批',
  3: '已批准待修改',
  4: '已驳回',
  5: '已完成',
  6: '已撤回',
}

const TYPE_TEXT = {
  1: '经治医生更正',
  2: '上级代改',
}

/** 未知码值渲染「未知(码值)」，绝不回落成看似合法的值 */
export const correctionStatusText = (v) => STATUS_TEXT[Number(v)] ?? `未知(${v})`
export const correctionTypeText = (v) => TYPE_TEXT[Number(v)] ?? `未知(${v})`

/** 状态标签色：待审=蓝，待修改=橙，驳回=红，完成=绿，撤回=灰 */
export const correctionStatusTag = (v) => {
  switch (Number(v)) {
    case CORRECTION_STATUS.DEPT_PENDING:
    case CORRECTION_STATUS.MEDICAL_PENDING:
      return 'primary'
    case CORRECTION_STATUS.APPROVED:
      return 'warning'
    case CORRECTION_STATUS.REJECTED:
      return 'danger'
    case CORRECTION_STATUS.COMPLETED:
      return 'success'
    default:
      return 'info'
  }
}

export const CORRECTION_STATUS_OPTIONS = Object.entries(STATUS_TEXT).map(([value, label]) => ({
  value: Number(value),
  label,
}))

export const CORRECTION_TYPE_OPTIONS = Object.entries(TYPE_TEXT).map(([value, label]) => ({
  value: Number(value),
  label,
}))
