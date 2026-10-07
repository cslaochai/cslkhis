package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 追溯码台账状态分布（对应 {@code BizDrugTraceMapper#selectTraceStats}）。
 *
 * <p>两组口径，都是 {@code SUM(CASE WHEN ...)} 的单值聚合：
 * <ul>
 *   <li><b>码状态</b>：在库 / 已发药核销 / 已作废 —— 与库存数量对不上就是采集漏了；</li>
 *   <li><b>上传状态</b>：待上传 / 已上传 / 上传失败 —— 待上传长期不下去就是没跑上传任务。</li>
 * </ul>
 *
 * <p><b>口径提醒</b>：{@code COUNT(DISTINCT drug_id)} 统计的是「已采集码覆盖到的品种数」，
 * 它<b>不等于</b>在库品种数（有码的品种可能已全部发完），拿它跟字典品种数比会误报缺药。
 *
 * <p>SQL 侧已用 {@code COALESCE(..., 0)} 兜底：{@code SUM} 在零行时返回 NULL，
 * 不兜底的话空台账会把「0 条」变成「null 条」，看板上两者含义完全不同。
 */
@Data
public class DrugTraceStatVO implements Serializable {

    /**
     * 采集总量（{@code COUNT(*)}，已排除逻辑删除）
     */
    private Long total;

    /**
     * 在库（已采集待发）
     */
    private Long inStock;

    /**
     * 已发药核销
     */
    private Long dispensed;

    /**
     * 已作废（退药/报损/召回）
     */
    private Long voided;

    /**
     * 待上传
     */
    private Long pendingUpload;

    /**
     * 已上传
     */
    private Long uploaded;

    /**
     * 上传失败
     */
    private Long uploadFailed;

    /**
     * 已采集码覆盖到的品种数
     */
    private Long drugKinds;
}
