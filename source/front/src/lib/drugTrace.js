/**
 * 药品追溯码语义判定单点（sql/156）
 *
 * 后端字段是状态码，文案/可不可以操作一律走这里改，不在页面里 if 硬写：
 * 状态改一档要同时改台账、采集窗口、发药窗口三处，散着写必漏。
 */

/** 码状态（biz_drug_trace.status） */
export const TRACE_STATUS = {
  IN_STOCK: 1,   // 在库：已采集待发
  DISPENSED: 2,  // 已发药核销
  VOID: 3,       // 已作废：退药/报损/召回（发出过的药不得再销售）
}

/** 上传状态（biz_drug_trace.upload_status） */
export const TRACE_UPLOAD_STATUS = {
  PENDING: 0,
  DONE: 1,
  FAILED: 2,
}

/** 采集来源 */
export const TRACE_SOURCE_TYPE = {
  INBOUND: 1,   // 入库采集
  RESTOCK: 2,   // 存量补采（政策前已入库的库存盘点补扫）
}

/** 码制 */
export const TRACE_CODE_TYPE = {
  GS1: 1,
  CN20: 2,
  OTHER: 3,
}

export function traceStatusTagType(status) {
  switch (Number(status)) {
    case TRACE_STATUS.IN_STOCK: return 'success'
    case TRACE_STATUS.DISPENSED: return 'primary'
    case TRACE_STATUS.VOID: return 'danger'
    default: return 'info'
  }
}

export function uploadStatusTagType(status) {
  switch (Number(status)) {
    case TRACE_UPLOAD_STATUS.PENDING: return 'warning'
    case TRACE_UPLOAD_STATUS.DONE: return 'success'
    case TRACE_UPLOAD_STATUS.FAILED: return 'danger'
    default: return 'info'
  }
}

/** 只有未作废的码可以作废（已核销的码允许退药作废，这是唯一合法路径） */
export function canVoidTrace(row) {
  return !!row && Number(row.status) !== TRACE_STATUS.VOID
}

/**
 * 只有「在库 + 未上传」的误采记录可删。
 * 已核销/已上传的码是医保数据，删了就等于追溯链断一节，后端同样会拒绝。
 */
export function canDeleteTrace(row) {
  return !!row
    && Number(row.status) === TRACE_STATUS.IN_STOCK
    && Number(row.uploadStatus) === TRACE_UPLOAD_STATUS.PENDING
}

/** 待上传（含上传失败，失败也要能再传一次） */
export function isUploadPending(row) {
  const s = Number(row && row.uploadStatus)
  return s === TRACE_UPLOAD_STATUS.PENDING || s === TRACE_UPLOAD_STATUS.FAILED
}

export function codeTypeText(codeType) {
  switch (Number(codeType)) {
    case TRACE_CODE_TYPE.GS1: return 'GS1 码'
    case TRACE_CODE_TYPE.CN20: return '中国药品追溯码20位'
    default: return '其他/未识别'
  }
}
