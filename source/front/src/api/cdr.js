import request from './request'

/**
 * 患者全景时间轴（CDR / P5.2）
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. 时间轴按**就诊次**组织（门诊 / 住院 / 急诊 / 跨就诊），不是按表组织。
 * 2. 查询口径已含 EMPI 归并：被并档案的历史数据会一起出现，窗口顶部会说明归并了几份档案。
 * 3. 状态、文书类型、医嘱类别等**码值翻译全部由后端给**（未知码值后端报 `未知(n)`，前端不兜底）。
 * 4. `gaps` 是**病历完整性缺口**（该有的没有），不是错误提示，不要用红色渲染。
 * 5. `unresolvedEvents` 是归属不到就诊次的记录（锚点指向的挂号/入院不存在或不属于该患者），
 *    必须显示出来 —— 藏起来等于数据丢了。
 * 6. ID 是字符串，不要 Number()。
 */

// 患者全景时间轴（patientId 必填；startDate/endDate/eventType 用于过滤）
export function getPatientCdr(params) {
  return request.get('/report/cdr/getDetailById', { params })
}

// 事件类型字典（含归属的就诊形态、副码标签）
export function getCdrEventDict() {
  return request.get('/report/cdr/eventDict')
}

// 患者临床摘要（画像条）：过敏旗 / 慢病标签 / 危急值史 / 异常检验（未判定≠正常）/ 30天就诊与重复检查。
// 与时间轴同口径（EMPI 归并）；P6 用药安全等 CDSS 能力共用这套取数口径。
export function getClinicalSummary(params) {
  return request.get('/report/cdr/clinicalSummary', { params })
}
