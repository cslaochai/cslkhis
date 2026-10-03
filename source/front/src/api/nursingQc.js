import request from './request'

// 护理质控（sql/168，菜单 330 护理管理 / 334 护理质控）
// 后端控制器：/nursing/qc（his-patient）
//
// 与病案质控（biz_record_qc_*，评单份病历书写）不是一张账：这里评的是「病区 × 月」的护理质量。
// 四条指标分两类，分母来源完全不同（NursingIndicatorEnum）：
//   BASIC_NURSING / NURSING_DOC —— 检查表评分，分母=抽查例数，事实来自 biz_nursing_qc_check
//   FALL_RATE / UPPR_RATE      —— 千床日率，分母=实际占用床日，分子=biz_adverse_event
// 所以看板的每一个数字都是后端算好的，前端只做展示与连线，禁止自己平均或求和。

// ========== 参照数据（跨岗位下拉，后端只要求登录） ==========

// 病区下拉：只含当前岗位可见科室下的启用病区，附开放床位与占用床位数
export function getQcWardSelectList(params) {
  return request.get('/nursing/qc/wardSelectList', { params })
}

// 检查人下拉：该病区所属科室的在职人员（护士长 / 护理部质控组）
export function getQcInspectorSelectList(params) {
  return request.get('/nursing/qc/inspectorSelectList', { params })
}

// 检查项标准目录：category 空=全部启用项；新检查单的表单行来源
export function getQcItemSelectList(params) {
  return request.get('/nursing/qc/itemSelectList', { params })
}

// ========== 检查表 ==========

// 检查单分页（病区 × 月 × 类别）
export function listQcCheckPage(data) {
  return request.post('/nursing/qc/checkListPage', data)
}

// 检查单详情：主表 + 已录明细 + 本类别目录 + 漏查项数
export function getQcCheckDetailById(id) {
  return request.get('/nursing/qc/getDetailById', { params: { id } })
}

// 保存检查单（新增与修改同一个 upsert；主表六个汇总数字由明细求和，接口不接收前端传值）
export function upsertQcCheck(data) {
  return request.post('/nursing/qc/checkUpsert', data)
}

// 确认(2)/退回草稿(1)：确认后明细冻结，要改必须先退回
export function updateQcCheckStatus(data) {
  return request.post('/nursing/qc/checkStatus', data)
}

// 删除检查单（连同明细物理删：唯一键不含 del_flag）
export function deleteQcCheckById(id) {
  return request.delete('/nursing/qc/checkDeleteById', { params: { id } })
}

// ========== 护理部视角：看板 / 趋势 / 对比 ==========

// 月度 KPI（四条指标永远都在，没台账的返回空值=未重算）
export function getQcMonthMetrics(data) {
  return request.post('/nursing/qc/monthMetrics', data)
}

// 指标趋势：一条指标按月一个点，wardId 空=可见范围全院合并
export function getQcTrend(data) {
  return request.post('/nursing/qc/trend', data)
}

// 病区对比：某月某指标各病区落点（后端已按指标值倒序）
export function getQcWardCompare(data) {
  return request.post('/nursing/qc/wardCompare', data)
}

// ========== 月度台账 ==========

// 台账分页（每行都带分子分母与来源备注，护理部要能追问「这个数怎么来的」）
export function listQcLedgerPage(data) {
  return request.post('/nursing/qc/ledgerListPage', data)
}

// 重算台账（已上报的行由 SQL 侧闸门跳过，不会静默改历史数字）
export function recalcQcLedger(data) {
  return request.post('/nursing/qc/recalc', data)
}

// 上报(2)锁定 / 退回(1)：退回之后重算才会覆盖该月
export function reportQcLedger(data) {
  return request.post('/nursing/qc/report', data)
}

// 删除台账行（物理删，删掉再重算即可）
export function deleteQcLedgerById(id) {
  return request.delete('/nursing/qc/ledgerDeleteById', { params: { id } })
}
