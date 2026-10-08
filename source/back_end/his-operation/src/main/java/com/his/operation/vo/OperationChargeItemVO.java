package com.his.operation.vo;

import com.his.operation.entity.BizOperationChargeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术麻醉计费明细出参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationChargeItemVO extends BizOperationChargeItem {

    private String chargeStatusText;

    private String sourceTypeText;
}
