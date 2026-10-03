import request from './request'

/**
 * 数据质量报表（P5.3）
 *
 * 口径（写在这里，视图层不再各写一套）：
 * 1. **每条规则都带分母**：`checkedTotal`（在多少条里查）与 `issueCount`（命中多少条）。
 *    只有 `issueCount = 0` 而没有分母时，无法区分「查过且干净」与「根本没查」。
 * 2. **`empty = true` 必须显式提示**：说明这条规则的分母为 0，规则没生效 ——
 *    这不是「通过」，是「失效」。前端会把它单独标成告警样式。
 * 3. **`passRate` 为 null 表示没查**（不是 0 也不是 100）。后端在分母为 0 时返回 null，
 *    前端不要用 `?? 0` 之类的兜底把它变成 0。
 * 4. 维度合规率是**加权口径**（该维度所有规则的分母、分子分别求和后相除）。
 *    不同规则的分母不是同一批记录，所以它只用于横向比较维度、纵向看趋势，
 *    不代表「全库数据有 X% 是干净的」。页面上必须把这句写出来。
 * 5. 明细里的 `tableName + recordId` 是**精确定位**到行的依据，不要丢。
 *    重复类规则（如检验结果重复）命中数 == 明细行数，可以直接对账。
 * 6. ID 一律字符串，不要 Number()。
 */

// 五维度总览（含每条规则的分母与命中数）
export function getQualitySummary() {
  return request.get('/report/quality/getSummary')
}

// 规则清单（可按维度过滤）
export function getQualityRuleList(params) {
  return request.get('/report/quality/getRuleList', { params })
}

// 维度字典
export function getQualityDimensionDict() {
  return request.get('/report/quality/dimensionDict')
}

// 问题清单（分页；dimension / ruleCode / severity / keyword 可空）
export function listQualityIssuePage(params) {
  return request.get('/report/quality/listIssuePage', { params })
}
