/**
 * 病历三级质控流转 —— 码值唯一口径（与后端 QcTexts.qcFlow* 同源对齐，G18）。
 *
 * 页面禁止写本地映射副本：两份码值表一定有一份先过期，
 * 然后界面把「未知码值」渲染成某个看似合法的值。
 *
 * 状态机：1 科级待审 → 2 病案室待审 → 3 医务处待审 → 4 终审通过；
 * 任一审核级可退回 → 5 整改中（整改提交后回到退回发生级）。
 */

export const FLOW_STATUS = {
  DEPT_PENDING: 1,
  ARCHIVE_PENDING: 2,
  MEDAFFAIRS_PENDING: 3,
  FINAL_APPROVED: 4,
  REWORKING: 5,
}

export const FLOW_LEVEL = { DEPT: 1, ARCHIVE: 2, MEDAFFAIRS: 3 }

export const FLOW_ACTION = { START: 1, APPROVE: 2, RETURN: 3, RESUBMIT: 4, FINAL: 5 }

export const FLOW_STATUS_TEXT = {
  1: '科级待审',
  2: '病案室待审',
  3: '医务处待审',
  4: '终审通过',
  5: '整改中',
}

export const FLOW_LEVEL_TEXT = { 1: '科级', 2: '病案室', 3: '医务处' }

export const FLOW_ACTION_TEXT = {
  1: '发起送审',
  2: '审核通过',
  3: '退回整改',
  4: '整改提交',
  5: '终审通过',
}

export const FLOW_GRADE_TEXT = { 1: '甲级', 2: '乙级', 3: '丙级' }

/** 未知码值渲染「未知(码值)」，绝不回落成看似合法的值 */
export const flowStatusText = (v) => FLOW_STATUS_TEXT[Number(v)] ?? `未知(${v})`
export const flowLevelText = (v) => FLOW_LEVEL_TEXT[Number(v)] ?? `未知(${v})`
export const flowActionText = (v) => FLOW_ACTION_TEXT[Number(v)] ?? `未知(${v})`
export const flowGradeText = (v) => (v === null || v === undefined ? '—' : FLOW_GRADE_TEXT[Number(v)] ?? `未知(${v})`)

/** 状态标签色：待审=蓝，终审通过=绿，整改中=橙 */
export const flowStatusTag = (v) => {
  switch (Number(v)) {
    case FLOW_STATUS.DEPT_PENDING:
    case FLOW_STATUS.ARCHIVE_PENDING:
    case FLOW_STATUS.MEDAFFAIRS_PENDING:
      return 'primary'
    case FLOW_STATUS.FINAL_APPROVED:
      return 'success'
    case FLOW_STATUS.REWORKING:
      return 'warning'
    default:
      return 'info'
  }
}
