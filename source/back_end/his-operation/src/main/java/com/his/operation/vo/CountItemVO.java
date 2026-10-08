package com.his.operation.vo;

import com.his.operation.entity.BizOperationCountItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 清点明细出参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CountItemVO extends BizOperationCountItem {

    private String itemCategoryText;

    /**
     * 关体后 vs 术前是否一致（未登记某个阶段时为 null）
     */
    private Boolean consistent;

    /**
     * 差值（关体后 - 术前；未登记时为 null）
     */
    private Integer diffQty;
}
