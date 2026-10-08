package com.his.pharmacy.vo;

import lombok.Data;

/**
 * 追溯码对账统计VO（"采集与核对"这一页的全部结论都从这里出）
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
