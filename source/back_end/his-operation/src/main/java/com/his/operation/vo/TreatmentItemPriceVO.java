package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 治疗项目字典取价行（{@code BizOperationChargeItemMapper#selectTreatmentItem} 的返回）。
 *
 * <p>对应 SQL：跨模块只读 {@code sys_treatment_item} 的 {@code item_name / price} 两列，
 * 给手术麻醉计费同步取单价。
 *
 * <p><b>只有两列是故意的</b>：治疗项目字典真实列只有 {@code item_code / item_name /
 * item_type / dept_id / price / duration / usage_method / status}，
 * <b>没有单位与规格两列</b>。裸 SQL 里写上去编译不报错、运行时整条 SQL 报
 * Unknown column → 被 GlobalExceptionHandler 兜成 500，现象是「点提交就 500」，
 * 看不出是取价挂了。所以单位由调用方兜底成「次」、规格就是 null。
 */
@Data
public class TreatmentItemPriceVO implements Serializable {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 单价
     */
    private BigDecimal price;
}
