package com.his.pharmacy.vo;

import lombok.Data;

/**
 * 追溯码对账统计VO（"采集与核对"这一页的全部结论都从这里出）
 *
 * <p>三组口径：
 * <ol>
 *   <li><b>码状态分布</b>：在库 / 已核销 / 已作废 —— 与库存数量对不上就是采集漏了。</li>
 *   <li><b>上传分布</b>：待上传 / 已上传 / 上传失败 —— 待上传长期不下去就是没跑上传任务。</li>
 *   <li><b>稽核风险</b>：近 30 天已发药但未核销追溯码的行数（其中必须采集品种的行数单独出）——
 *       这是医保局真正会查的那一项，"发药没扫码"。历史发药（政策前）不计入。</li>
 * </ol>
 */
@Data
public class DrugTraceReconcileVO {

    /** 采集总量 */
    private Long total;
    /** 在库（已采集待发） */
    private Long inStock;
    /** 已发药核销 */
    private Long dispensed;
    /** 已作废 */
    private Long voided;

    /** 待上传 */
    private Long pendingUpload;
    /** 已上传 */
    private Long uploaded;
    /** 上传失败 */
    private Long uploadFailed;

    /** 近30天已发药但未核销追溯码的发药行数 */
    private Long unTracedDispense;
    /** 其中「必须采集」品种的行数（麻精/集采/医保谈判） */
    private Long requiredUnTracedDispense;
    /** 近30天已发药总行数（分母） */
    private Long recentDispenseTotal;

    /** 已采集码覆盖到的品种数 */
    private Long drugKinds;
}
