/**
 * ICU 专科监护口径单点 —— 状态标签/颜色与动作显隐，页面禁写本地副本。
 * 文案走字典 his_icu_*（dict-cache），这里只负责「码值 → tag 类型/语义」。
 */

/** 在科状态：1在科 2已出科 */
export const STAY_STATUS = {
  IN: 1,
  OUT: 2,
}

/** 转出去向：1普通病房 2专科病房 3手术室 4转院 5死亡 6自动离院 */
export const OUT_DEST = {
  GENERAL_WARD: 1,
  SPECIAL_WARD: 2,
  OR: 3,
  TRANSFER: 4,
  DEATH: 5,
  SELF: 6,
}

/** 需要写转归说明的去向（与后端 IcuServiceImpl 同一口径） */
export const REQUIRES_REASON_DEST = [OUT_DEST.TRANSFER, OUT_DEST.DEATH, OUT_DEST.SELF]

export const stayStatusTag = (status) => {
  switch (Number(status)) {
    case STAY_STATUS.IN: return 'primary'
    case STAY_STATUS.OUT: return 'success'
    default: return 'info'
  }
}

export const careLevelTag = (level) => {
  switch (Number(level)) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'info'
    default: return 'info'
  }
}

/** 仅在科可登记监护记录、可修改、可出科 */
export const canWriteMonitor = (status) => Number(status) === STAY_STATUS.IN
export const canEditStay = (status) => Number(status) === STAY_STATUS.IN
export const canOutStay = (status) => Number(status) === STAY_STATUS.IN
export const outNeedsReason = (dest) => REQUIRES_REASON_DEST.includes(Number(dest))

/**
 * GCS 三项必须同时填写（后端也校验，这里只挡提交）；总分前端只做展示，落库值由服务端求和。
 */
export const gcsTotalOf = (form) => {
  const { gcsEye, gcsVerbal, gcsMotor } = form
  if (gcsEye == null || gcsVerbal == null || gcsMotor == null) return null
  return Number(gcsEye) + Number(gcsVerbal) + Number(gcsMotor)
}

export const gcsIncomplete = (form) => {
  const filled = [form.gcsEye, form.gcsVerbal, form.gcsMotor].filter((v) => v != null && v !== '').length
  return filled > 0 && filled < 3
}

/** 液体平衡 = 入量 - 出量（展示用，落库值服务端回算） */
export const balanceOf = (intake, output) => {
  if (intake == null && output == null) return null
  return (Number(intake) || 0) - (Number(output) || 0)
}

/** 血压展示合并，避免表格两列窄列 */
export const bpText = (row) => (row.sbp == null && row.dbp == null ? '—' : `${row.sbp ?? '—'}/${row.dbp ?? '—'}`)
