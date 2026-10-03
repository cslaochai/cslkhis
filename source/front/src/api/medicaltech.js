import request from './request'

// ========== 检查管理 ==========

// 查询检查记录列表
export function getInspectionRecordList(params) {
  return request.post('/medicaltech/inspection/list', params)
}
// 分页查询检查记录列表
export function getInspectionRecordListPage(params) {
  return request.post('/medicaltech/inspection/listPage', params)
}

// 获取检查记录详情
export function getInspectionDetail(recordId) {
  return request.get('/medicaltech/inspection/getDetailById', { params: { recordId } })
}

// 检查签到
export function checkIn(recordId) {
  return request.post('/medicaltech/inspection/checkIn', null, { params: { recordId } })
}

// 开始检查
export function startInspection(recordId) {
  return request.post('/medicaltech/inspection/start', null, { params: { recordId } })
}

// 执行检查
export function executeInspection(recordId, data) {
  return request.post('/medicaltech/inspection/execute', { ...data, recordId })
}

// 拍片完成（sql/138：放射项目专用，技师岗终点 —— 不建报告不签名，诊断归放射诊断工作站）
export function finishShoot(recordId) {
  return request.post('/medicaltech/inspection/finishShoot', null, { params: { recordId } })
}

// 审核检查报告
export function auditInspection(recordId, auditBy) {
  return request.post('/medicaltech/inspection/audit', { recordId, auditBy })
}

// ========== 检验管理 ==========

// 查询检验记录列表
export function getLaboratoryRecordList(params) {
  return request.post('/medicaltech/laboratory/list', params)
}
// 分页查询检验记录列表
export function getLaboratoryRecordListPage(params) {
  return request.post('/medicaltech/laboratory/listPage', params)
}

// 获取检验记录详情
export function getLaboratoryDetail(recordId) {
  return request.get('/medicaltech/laboratory/getDetailById', { params: { recordId } })
}

// 接收标本
export function receiveSpecimen(recordId, receiveBy) {
  return request.post('/medicaltech/laboratory/receive', { recordId, receiveBy })
}

// 录入检验结果
export function inputLabResult(recordId, data) {
  return request.post('/medicaltech/laboratory/inputResult', { ...data, recordId })
}

// 审核检验报告
export function auditLaboratory(recordId, auditBy) {
  return request.post('/medicaltech/laboratory/audit', { recordId, auditBy })
}

// ========== 标本管理 ==========

// 查询标本列表
export function getSpecimenList(params) {
  return request.post('/medicaltech/specimen/list', params)
}

// 标本统计
export function getSpecimenStats() {
  return request.get('/medicaltech/specimen/stats')
}

// 分配标本条码
export function assignBarcode(recordId, specimenNo) {
  return request.post('/medicaltech/specimen/barcode', { recordId, specimenNo })
}

// 标本采集确认
export function sampleSpecimen(recordId, sampleBy) {
  return request.post('/medicaltech/specimen/sample', { recordId, sampleBy })
}

// 标本退回
export function rejectSpecimen(recordId, reason) {
  return request.post('/medicaltech/specimen/reject', { recordId, reason })
}

// ========== 报告管理 ==========

// 查询报告列表
export function getReportList(params) {
  return request.post('/medicaltech/report/list', params)
}

// 获取报告详情
export function getReportDetail(reportId) {
  return request.get('/medicaltech/report/getById', { params: { reportId } })
}

// 发布报告
export function publishReport(reportId, publishBy) {
  return request.post('/medicaltech/report/publish', { reportId, publishBy })
}

// ========== 危急值管理 ==========
//
// 危急值由后台在录入检验结果时按下述硬规则自动识别（不经过模型），
// 上报的同时会给开单医生发站内信。这里只做查询与闭环流转。

// 分页查询危急值
// params: { pageNum, pageSize, keyword?, status?, criticalType?, patientId?,
//           startDate?, endDate?, overdueOnly? }
// status: 1-待接收 2-已接收 3-已处置 4-已作废
// criticalType: 1-偏低 2-偏高
// overdueOnly: 只看「已超过处置时限且尚未处置」
export function getCriticalValueListPage(params) {
  return request.post('/medicaltech/criticalValue/listPage', params)
}

// 危急值详情（含处置轨迹与超时计算）
export function getCriticalValueDetail(criticalValueId) {
  return request.get('/medicaltech/criticalValue/getById', { params: { criticalValueId } })
}

// 危急值统计（本月总数/待接收/已接收/已处置/超时/及时处置率）
export function getCriticalValueStats() {
  return request.get('/medicaltech/criticalValue/stats')
}

// 确认接收
export function receiveCriticalValue(criticalValueId, receiveBy) {
  return request.post('/medicaltech/criticalValue/receive', { criticalValueId, receiveBy })
}

// 记录处置措施（未接收直接处置是允许的，后台会自动补上接收人）
export function handleCriticalValue(criticalValueId, handleMeasure, handleBy) {
  return request.post('/medicaltech/criticalValue/handle', { criticalValueId, handleMeasure, handleBy })
}

// ========== G17 病理亚专业 ==========

// 分页查询病理单 params: { pageNum, pageSize, orderNo?, patientName?, examType?, status?, startDate?, endDate? }
export function getPathologyListPage(params) {
  return request.post('/medicaltech/pathology/listPage', params)
}
// 病理统计
export function getPathologyStats() {
  return request.get('/medicaltech/pathology/stats')
}
// 病理单详情（含蜡块明细）
export function getPathologyDetail(orderId) {
  return request.get('/medicaltech/pathology/getDetailById', { params: { orderId } })
}
// 新增/修改病理单
export function pathologyUpsert(data) {
  return request.post('/medicaltech/pathology/orderUpsert', data)
}
// 标本接收
export function pathologyReceive(data) {
  return request.post('/medicaltech/pathology/receive', data)
}
// 主单流程推进
export function pathologyProcess(data) {
  return request.post('/medicaltech/pathology/process', data)
}
// 蜡块登记
export function pathologyBlockUpsert(data) {
  return request.post('/medicaltech/pathology/blockUpsert', data)
}
// 蜡块流转（1 取材 2 包埋 3 切片）
export function pathologyBlockAction(data) {
  return request.post('/medicaltech/pathology/blockAction', data)
}
// 初诊
export function pathologyReport(data) {
  return request.post('/medicaltech/pathology/report', data)
}
// 审核（服务端校验审核人 ≠ 初诊人）
export function pathologyAudit(data) {
  return request.post('/medicaltech/pathology/audit', data)
}
// 发布报告
export function pathologyPublish(orderId) {
  return request.post('/medicaltech/pathology/publish', { orderId })
}
// 取消
export function pathologyCancel(data) {
  return request.post('/medicaltech/pathology/cancel', data)
}

// ========== G17 内镜亚专业 ==========

// 分页 params: { pageNum, pageSize, recordNo?, patientName?, endoType?, status?, startDate?, endDate? }
export function getEndoscopyListPage(params) {
  return request.post('/medicaltech/endoscopy/listPage', params)
}
export function getEndoscopyStats() {
  return request.get('/medicaltech/endoscopy/stats')
}
export function getEndoscopyDetail(recordId) {
  return request.get('/medicaltech/endoscopy/getDetailById', { params: { recordId } })
}
export function endoscopyUpsert(data) {
  return request.post('/medicaltech/endoscopy/recordUpsert', data)
}
export function endoscopyCheckIn(recordId) {
  return request.post('/medicaltech/endoscopy/checkIn', { recordId })
}
export function endoscopyExecute(data) {
  return request.post('/medicaltech/endoscopy/execute', data)
}
export function endoscopySendBiopsy(data) {
  return request.post('/medicaltech/endoscopy/sendBiopsy', data)
}
export function endoscopyReport(data) {
  return request.post('/medicaltech/endoscopy/report', data)
}
export function endoscopyAudit(data) {
  return request.post('/medicaltech/endoscopy/audit', data)
}
export function endoscopyPublish(recordId) {
  return request.post('/medicaltech/endoscopy/publish', { recordId })
}
export function endoscopyCancel(data) {
  return request.post('/medicaltech/endoscopy/cancel', data)
}

// ========== G17 超声亚专业 ==========

// 分页 params: { pageNum, pageSize, recordNo?, patientName?, usType?, status?, startDate?, endDate? }
export function getUltrasoundListPage(params) {
  return request.post('/medicaltech/ultrasound/listPage', params)
}
export function getUltrasoundStats() {
  return request.get('/medicaltech/ultrasound/stats')
}
// 详情（含结构化测量值列表）
export function getUltrasoundDetail(recordId) {
  return request.get('/medicaltech/ultrasound/getDetailById', { params: { recordId } })
}
export function ultrasoundUpsert(data) {
  return request.post('/medicaltech/ultrasound/recordUpsert', data)
}
export function ultrasoundCheckIn(recordId) {
  return request.post('/medicaltech/ultrasound/checkIn', { recordId })
}
export function ultrasoundExecute(data) {
  return request.post('/medicaltech/ultrasound/execute', data)
}
// 保存结构化测量值（整单覆盖，异常标志服务端判定）
export function ultrasoundSaveMeasures(data) {
  return request.post('/medicaltech/ultrasound/saveMeasures', data)
}
export function ultrasoundReport(data) {
  return request.post('/medicaltech/ultrasound/report', data)
}
export function ultrasoundAudit(data) {
  return request.post('/medicaltech/ultrasound/audit', data)
}
export function ultrasoundPublish(recordId) {
  return request.post('/medicaltech/ultrasound/publish', { recordId })
}
export function ultrasoundCancel(data) {
  return request.post('/medicaltech/ultrasound/cancel', data)
}

// ========== G17 LIS 室内质控 ==========

// 质控计划分页 params: { pageNum, pageSize, itemName?, instrumentName?, status? }
export function getQcPlanListPage(params) {
  return request.post('/medicaltech/lisQc/planListPage', params)
}
export function qcPlanUpsert(data) {
  return request.post('/medicaltech/lisQc/planUpsert', data)
}
export function qcPlanToggle(planId, status) {
  return request.post('/medicaltech/lisQc/planToggle', { planId, status })
}
// 录入质控结果（服务端 Westgard 判定，前端禁止自判）
export function qcInputResult(data) {
  return request.post('/medicaltech/lisQc/inputResult', data)
}
// 质控记录分页 params: { pageNum, pageSize, planId?, itemName?, status?, handleStatus?, startDate?, endDate? }
export function getQcRecordListPage(params) {
  return request.post('/medicaltech/lisQc/recordListPage', params)
}
export function qcHandle(data) {
  return request.post('/medicaltech/lisQc/handle', data)
}
export function qcReview(recordId) {
  return request.post('/medicaltech/lisQc/review', { recordId })
}
export function getQcStats() {
  return request.get('/medicaltech/lisQc/stats')
}

// ========== 室间质评 EQA（菜单 416 / sql/172）==========
// ⚠ SDI / 偏倚 / PT 得分 / 互差全部服务端算，页面只展示，不得自行判断合格与否。

// 批次分页 params: { pageNum, pageSize, planYear?, batchNo?, orgName?, status? }
export function getEqaPlanListPage(params) {
  return request.post('/medicaltech/lisEqa/planListPage', params)
}
export function eqaPlanUpsert(data) {
  return request.post('/medicaltech/lisEqa/planUpsert', data)
}
export function eqaPlanArchive(planId) {
  return request.post('/medicaltech/lisEqa/planArchive', { planId })
}
// 盲样台账分页 params: { pageNum, pageSize, planId?, sampleNo?, itemName?, instrumentName?, status?, resultStatus?, handleStatus? }
export function getEqaSampleListPage(params) {
  return request.post('/medicaltech/lisEqa/sampleListPage', params)
}
// 批量生成盲样骨架（样品序号 × 项目 × 仪器）
export function eqaSampleGenerate(data) {
  return request.post('/medicaltech/lisEqa/sampleGenerate', data)
}
export function eqaSampleUpsert(data) {
  return request.post('/medicaltech/lisEqa/sampleUpsert', data)
}
export function eqaSampleDeleteById(sampleId) {
  return request.delete('/medicaltech/lisEqa/sampleDeleteById', { params: { sampleId } })
}
export function eqaSampleTest(data) {
  return request.post('/medicaltech/lisEqa/sampleTest', data)
}
export function eqaSampleReport(data) {
  return request.post('/medicaltech/lisEqa/sampleReport', data)
}
// 成绩回报（靶值/SD/TEa）→ 服务端判定 + 重算 PT 与互差
export function eqaReturnScore(data) {
  return request.post('/medicaltech/lisEqa/returnScore', data)
}
// 仪器间比对（室间差）分页 params: { pageNum, pageSize, planId?, itemName?, status? }
export function getEqaCompareListPage(params) {
  return request.post('/medicaltech/lisEqa/compareListPage', params)
}
export function eqaRectify(data) {
  return request.post('/medicaltech/lisEqa/rectify', data)
}
export function eqaRectifyReview(sampleId) {
  return request.post('/medicaltech/lisEqa/rectifyReview', { sampleId })
}
export function getEqaStats() {
  return request.get('/medicaltech/lisEqa/stats')
}

// ========== G17 血库储血台账 ==========

// 血袋入库
export function bloodInbound(data) {
  return request.post('/medicaltech/bloodBank/inbound', data)
}
// 库存台账分页 params: { pageNum, pageSize, bagNo?, bloodType?, rhType?, componentType?, status?, expireWithinDays? }
export function getBloodInventoryPage(params) {
  return request.post('/medicaltech/bloodBank/inventoryListPage', params)
}
export function getBloodInventoryStats() {
  return request.get('/medicaltech/bloodBank/inventoryStats')
}
export function bloodReserve(data) {
  return request.post('/medicaltech/bloodBank/reserve', data)
}
export function bloodCancelReserve(data) {
  return request.post('/medicaltech/bloodBank/cancelReserve', data)
}
export function bloodIssue(data) {
  return request.post('/medicaltech/bloodBank/issue', data)
}
export function bloodScrap(data) {
  return request.post('/medicaltech/bloodBank/scrap', data)
}
export function bloodReturn(data) {
  return request.post('/medicaltech/bloodBank/returnBag', data)
}
// 新建配血单
export function crossmatchCreate(data) {
  return request.post('/medicaltech/bloodBank/crossmatchCreate', data)
}
// 配血单分页 params: { pageNum, pageSize, matchNo?, bagNo?, patientName?, status?, result? }
export function getCrossmatchPage(params) {
  return request.post('/medicaltech/bloodBank/crossmatchListPage', params)
}
export function crossmatchExecute(data) {
  return request.post('/medicaltech/bloodBank/crossmatchExecute', data)
}
export function crossmatchVerify(matchId) {
  return request.post('/medicaltech/bloodBank/crossmatchVerify', { matchId })
}
export function crossmatchVoid(matchId) {
  return request.post('/medicaltech/bloodBank/crossmatchVoid', { matchId })
}
// 出入库流水分页
export function getBloodLogPage(params) {
  return request.post('/medicaltech/bloodBank/logListPage', params)
}

// ========== 放射诊断工作站（sql/138，菜单 414）==========
//
// 拍片（技师，检查工作站 401）→ 诊断（医师，本页）→ 胶片（技师，菜单 415）三岗分离。
// 本页只做「下诊断结论」这一件事：待书写 / 待审核 / 已审核 / 已发布四栏。

// 报告工作台分页。params: { pageNum, pageSize, keyword?, reportStatus?, onlyUnwritten?, positiveFlag?, startDate?, endDate? }
// 一行 = 一次检查 + 它那份报告（可能还没有）；reportId 为空 = 待书写
export function getRadioReportListPage(params) {
  return request.post('/medicaltech/radiology/report/listPage', params)
}
// 详情（按检查记录；还没写报告时也能取，用来打开书写台）
export function getRadioReportDetailByRecordId(recordId) {
  return request.get('/medicaltech/radiology/report/getDetailByRecordId', { params: { recordId } })
}
// 详情（按报告）
export function getRadioReportDetailByReportId(reportId) {
  return request.get('/medicaltech/radiology/report/getDetailByReportId', { params: { reportId } })
}
// 保存草稿
export function saveRadioReportDraft(data) {
  return request.post('/medicaltech/radiology/report/saveDraft', data)
}
// 提交审核（同时完成报告医师签名）
export function submitRadioReport(data) {
  return request.post('/medicaltech/radiology/report/submit', data)
}
// 审核通过
export function auditRadioReport(data) {
  return request.post('/medicaltech/radiology/report/audit', data)
}
// 退回重写（reason 必填）
export function rejectRadioReport(data) {
  return request.post('/medicaltech/radiology/report/reject', data)
}
// 发布
export function publishRadioReport(reportId) {
  return request.post('/medicaltech/radiology/report/publish', null, { params: { reportId } })
}
// 报告模板下拉（modality 可空，空=全部）
export function getRadioTemplateSelectList(modality) {
  return request.get('/medicaltech/radiology/template/selectList', { params: { modality } })
}
// 模板列表（维护用，含停用）
export function getRadioTemplateList() {
  return request.get('/medicaltech/radiology/template/list')
}
export function radioTemplateUpsert(data) {
  return request.post('/medicaltech/radiology/template/upsert', data)
}
export function radioTemplateDelete(id) {
  return request.delete('/medicaltech/radiology/template/deleteById', { params: { id } })
}

// ========== 检查胶片量方与发放（sql/138，菜单 415）==========

// 胶片用量分页。params: { pageNum, pageSize, keyword?, recordId?, filmStatus?, chargeFlag?, startDate?, endDate? }
export function getExamFilmListPage(params) {
  return request.post('/medicaltech/examFilm/listPage', params)
}
// 某一次检查的全部胶片
export function getExamFilmByRecordId(recordId) {
  return request.get('/medicaltech/examFilm/listByRecordId', { params: { recordId } })
}
// 汇总：张数 / 金额 / 已记账金额（不传日期=今天）
export function getExamFilmStats(startDate, endDate) {
  return request.get('/medicaltech/examFilm/stats', { params: { startDate, endDate } })
}
// 登记用量（金额服务端按 单价×张数 现算，前端不传金额）
export function examFilmUpsert(data) {
  return request.post('/medicaltech/examFilm/upsert', data)
}
// 生成记账（幂等；收费模块缺席时后端会明确报错）
export function examFilmCharge(filmId) {
  return request.post('/medicaltech/examFilm/charge', null, { params: { filmId } })
}
export function examFilmMarkPrinted(filmId) {
  return request.post('/medicaltech/examFilm/markPrinted', null, { params: { filmId } })
}
export function examFilmDeliver(filmId) {
  return request.post('/medicaltech/examFilm/deliver', null, { params: { filmId } })
}
// 作废（已记账的后端拒绝）
export function examFilmDelete(filmId, reason) {
  return request.delete('/medicaltech/examFilm/deleteById', { params: { filmId, reason } })
}
// 胶片规格下拉（带单价）
export function getFilmSpecSelectList() {
  return request.get('/medicaltech/examFilm/specSelectList')
}
export function filmSpecUpsert(data) {
  return request.post('/medicaltech/examFilm/specUpsert', data)
}
export function filmSpecDelete(id) {
  return request.delete('/medicaltech/examFilm/specDeleteById', { params: { id } })
}

// ========== 心电工作站（sql/173，菜单 417）==========
//
// 超声/内镜/放射/病理都有独立工作站，心电在此补齐：签到 → 波形采集（12 导联）→
// 测量参数 / Holter 分析 → 报告书写（草稿/提交/审核/退回/发布）。
// 闸门在服务端：提交必须有波形；ecg_type=2（Holter）必须已有分析；不能自审。

// 工作台分页。params: { pageNum, pageSize, keyword?, collectPending?, onlyUnwritten?, reportStatus?, startDate?, endDate? }
// 一行 = 一次检查 + 波形/测量/Holter/报告（可能都没有）；collectPending=1~3 待采集
export function getEcgListPage(params) {
  return request.post('/medicaltech/ecg/listPage', params)
}
// 详情（按检查记录；含 waveData 波形 JSON，未采集时也能取）
export function getEcgDetailByRecordId(recordId) {
  return request.get('/medicaltech/ecg/getDetailByRecordId', { params: { recordId } })
}
// 签到（已登记 → 已签到）
export function ecgCheckIn(recordId) {
  return request.post('/medicaltech/ecg/checkIn', null, { params: { recordId } })
}
// 波形采集（设备推送路径）：waveData 为 12 导联 JSON 字符串
export function ecgCollectWave(data) {
  return request.post('/medicaltech/ecg/collectWave', data)
}
// 模拟采集（演示/联调）：rhythmCode 1窦性 2窦速 3窦缓 4房颤 5室早
export function ecgSimulateWave(data) {
  return request.post('/medicaltech/ecg/simulateWave', data)
}
// 保存测量参数（心率/PR/QRS/QT/QTc/电轴/节律，upsert）
export function ecgSaveMeasure(data) {
  return request.post('/medicaltech/ecg/saveMeasure', data)
}
// 保存 Holter 分析（心搏统计/心律失常事件/小时心率，upsert）
export function ecgSaveHolter(data) {
  return request.post('/medicaltech/ecg/saveHolter', data)
}
// 保存报告草稿
export function ecgSaveDraft(data) {
  return request.post('/medicaltech/ecg/saveDraft', data)
}
// 提交审核（同时完成报告医师签名）
export function ecgSubmit(data) {
  return request.post('/medicaltech/ecg/submit', data)
}
// 审核通过
export function ecgAudit(data) {
  return request.post('/medicaltech/ecg/audit', data)
}
// 退回重写（reason 必填）
export function ecgReject(data) {
  return request.post('/medicaltech/ecg/reject', data)
}
// 发布
export function ecgPublish(reportId) {
  return request.post('/medicaltech/ecg/publish', null, { params: { reportId } })
}
// 报告模板下拉（ecgType 可空，空=全部，通用模板始终可选）
export function getEcgTemplateSelectList(ecgType) {
  return request.get('/medicaltech/ecg/template/selectList', { params: { ecgType } })
}
// 模板列表（维护用，含停用）
export function getEcgTemplateList() {
  return request.get('/medicaltech/ecg/template/list')
}
export function ecgTemplateUpsert(data) {
  return request.post('/medicaltech/ecg/template/upsert', data)
}
export function ecgTemplateDelete(id) {
  return request.delete('/medicaltech/ecg/template/deleteById', { params: { id } })
}
