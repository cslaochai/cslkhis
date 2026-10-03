/**
 * 血液净化（透析）口径单点 —— 状态标签/颜色与动作显隐，页面禁写本地副本。
 * 文案走字典 his_dialysis_*（dict-cache），这里只负责「码值 → tag 类型/语义」。
 */

/** 透析档案状态：1在透 2暂停 3退出 */
export const ARCHIVE_STATUS = {
  ON: 1,
  PAUSED: 2,
  EXITED: 3,
}

/** 透析单状态：1已排班 2透析中 3已完成 4已取消 */
export const SESSION_STATUS = {
  SCHEDULED: 1,
  ON_MACHINE: 2,
  DONE: 3,
  CANCELLED: 4,
}

/** 机位状态：1可用 2维修 3停用 */
export const MACHINE_STATUS = {
  USABLE: 1,
  REPAIR: 2,
  OFF: 3,
}

/** 处方状态：1有效 2已停用 */
export const PRESCRIPTION_STATUS = {
  ACTIVE: 1,
  STOPPED: 2,
}

export const archiveStatusTag = (status) => {
  switch (Number(status)) {
    case ARCHIVE_STATUS.ON: return 'success'
    case ARCHIVE_STATUS.PAUSED: return 'warning'
    case ARCHIVE_STATUS.EXITED: return 'info'
    default: return 'info'
  }
}

export const sessionStatusTag = (status) => {
  switch (Number(status)) {
    case SESSION_STATUS.SCHEDULED: return 'warning'
    case SESSION_STATUS.ON_MACHINE: return 'primary'
    case SESSION_STATUS.DONE: return 'success'
    case SESSION_STATUS.CANCELLED: return 'info'
    default: return 'info'
  }
}

export const machineStatusTag = (status) => {
  switch (Number(status)) {
    case MACHINE_STATUS.USABLE: return 'success'
    case MACHINE_STATUS.REPAIR: return 'warning'
    case MACHINE_STATUS.OFF: return 'info'
    default: return 'info'
  }
}

export const prescriptionStatusTag = (status) => {
  switch (Number(status)) {
    case PRESCRIPTION_STATUS.ACTIVE: return 'success'
    case PRESCRIPTION_STATUS.STOPPED: return 'info'
    default: return 'info'
  }
}

/** 暂停/退出都要填原因；已退档是终态，任何状态变更都不再允许 */
export const canChangeArchive = (status) => Number(status) !== ARCHIVE_STATUS.EXITED
/** 在透才能开处方 */
export const canWritePrescription = (status) => Number(status) !== ARCHIVE_STATUS.EXITED
export const canStopPrescription = (status) => Number(status) === PRESCRIPTION_STATUS.ACTIVE
/** 只有已排班能上机/改期/取消 */
export const canStart = (status) => Number(status) === SESSION_STATUS.SCHEDULED
export const canReschedule = (status) => Number(status) === SESSION_STATUS.SCHEDULED
export const canCancel = (status) => Number(status) === SESSION_STATUS.SCHEDULED
/** 透析中才能下机；透析中与已完成都能补登不良反应 */
export const canFinish = (status) => Number(status) === SESSION_STATUS.ON_MACHINE
export const canRecordAdverse = (status) =>
  Number(status) === SESSION_STATUS.ON_MACHINE || Number(status) === SESSION_STATUS.DONE

/** 机位看板里一格能否排班（空闲格 + 可用机位） */
export const canScheduleCell = (cell) =>
  !!cell && !cell.sessionId && Number(cell.machineStatus) === MACHINE_STATUS.USABLE
