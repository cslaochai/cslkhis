import request from './request'

/**
 * 门诊治疗站（G19，菜单 206 / 路径 /treatment-station）
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. **一次治疗 = 一行流水 = 一条收费明细**。开单时后端按 total_times + interval_days 一次性
 *    把排期行全部落库（biz_treatment_record），页面不要自己按天数循环"算出"应该有几次。
 * 2. **进度、逾期、能否打卡全在后端算**：行上的 `canExecute` / `cannotExecuteReason` /
 *    `overdue` / `canRetryCharge` 与写接口的校验是同一个方法出来的，前端只照渲染，
 *    禁止自己再判一次 `planDate <= today`（两边口径必然漂移，表现为"按钮能点、保存报错"）。
 * 3. **必须按次序打卡**：同一疗程里存在更早的待执行次时后面那次打不了，原因由后端给。
 * 4. **打卡与记账分事务**：`execExecute` 返回 `code=200` 也可能带「已打卡，但记账失败…」的
 *    message，那是**有意允许的中间态**（行状态 已执行 + 计费失败），用 `execRetryCharge` 补记，
 *    不要在前端把它当成失败回滚或重复打卡。
 * 5. **筛选条件不参与写**：治疗台的 `planDate` 只决定"看哪天"，打卡写的是被点那一行的
 *    `recordId`；切日期不会把某次执行挪到另一天。
 * 6. ID 全是雪花 ID 字符串，**不要 Number()**；日期入参 `yyyy-MM-dd`（后端 pattern 只认这个）。
 */

// ---------- 疗程（治疗申请单） ----------

export function getTreatmentApplyListPage(params) {
  return request.post('/emr/treatment/applyListPage', params)
}

// 疗程详情（含全部按次流水）
export function getTreatmentApplyDetail(applyId) {
  return request.get('/emr/treatment/getDetailById', { params: { applyId } })
}

// 治疗项目下拉（keyword / limit）
export function getTreatmentItemSelectList(params) {
  return request.get('/emr/treatment/item/selectList', { params })
}

// 开单 / 重排疗程（applyId 为空=新开；仅一次卡都没打过时允许重排）
export function treatmentApplyUpsert(data) {
  return request.post('/emr/treatment/applyUpsert', data)
}

// 取消疗程（reason 必填，连带取消未执行次数；已计费的账不在这退）
export function treatmentApplyCancel(data) {
  return request.post('/emr/treatment/cancelApply', data)
}

export function deleteTreatmentApply(applyId) {
  return request.delete('/emr/treatment/deleteById', { params: { applyId } })
}

// ---------- 按次流水 ----------

export function getTreatmentExecListPage(params) {
  return request.post('/emr/treatment/execListPage', params)
}

// 状态分布（与分页同口径，后端 group by；被统计的那一维不带上）
export function getTreatmentExecStatusCount(params) {
  return request.post('/emr/treatment/execStatusCount', params)
}

export function getTreatmentStats() {
  return request.get('/emr/treatment/stats')
}

// 单次改期（只挪未执行的那一次，reason 必填）
export function treatmentExecReschedule(data) {
  return request.post('/emr/treatment/execReschedule', data)
}

// 按次打卡（后端随后按次计费，返回值带 chargeNotice 一句话）
export function treatmentExecExecute(data) {
  return request.post('/emr/treatment/execExecute', data)
}

// 计费补记（已执行但没记上账的行重试一次）
export function treatmentExecRetryCharge(data) {
  return request.post('/emr/treatment/execRetryCharge', data)
}
