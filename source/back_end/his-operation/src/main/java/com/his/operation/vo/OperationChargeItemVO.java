package com.his.operation.vo;

import com.his.operation.entity.BizOperationChargeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术麻醉计费明细出参。
 *
 * <p>刻意带上 {@code feeRecordId}：它是本系统"请付这笔钱"的最终落点，
 * 缺少这一列的记账行意味着**记账了但没落到账上** —— 必须能一眼看出来。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationChargeItemVO extends BizOperationChargeItem {

    private String chargeStatusText;

    private String sourceTypeText;
}
