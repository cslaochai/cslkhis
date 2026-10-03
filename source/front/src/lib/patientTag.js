/**
 * 患者标签 chip 的显示口径（单点定义）
 *
 * 标签 chip 在三个地方渲染 —— 患者列表页（PatientsView）、
 * 选患者下拉（PatientSelect）、患者详情弹窗头部（PatientDetailDialog）。
 * 三处各写一遍看着无害，但已经出过两次分歧：
 * ① 弹窗头部曾渲染 `shortName`（单字缩写），另外两处渲染全名 —— 同一个患者
 *    列表里写「糖尿病」、弹窗里写「糖」，医生读不出「糖」是什么，当时收口为「一律全名」。
 * ② 2026-09-22 老王拍板改回「简写优先」：列表页标签挪到姓名下方做小 chip，
 *    全名太占宽度。口径改为：**chip 文本 = short_name，缺失时回退 tagName**。
 *    （「读不出」由悬停 title 提示全名兜底。）
 *
 * 所以文本口径**仍然只能有一份**：改这里一处，三处同步生效。
 */

/** chip 上显示的文本：简写优先（short_name），没有简写回退全名 */
export function tagChipText(tag) {
    return tag?.shortName || tag?.tagName || ''
}

/** chip 悬停提示：始终给全名，弥补简写读不出的情况 */
export function tagChipTitle(tag) {
    return tag?.tagName || ''
}

/**
 * 患者详情弹窗头部的折叠阈值。
 * 弹窗头部是固定高度区（下面的概览卡与自适应区会跟着头部高度跳），
 * 8 个之外折叠成「+N」、悬停才铺出剩余，是为了让头部高度稳定。
 */
export const TAG_VISIBLE_LIMIT = 8

/**
 * 患者列表页（PatientsView）姓名下方小 chip 的折叠阈值（2026-09-22 老王定 5）：
 * 超过 5 个折叠成「+N」，点击展开 / 收起。
 */
export const TAG_LIST_VISIBLE_LIMIT = 5
