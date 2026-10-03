package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.operation.entity.BizOperationCountItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 清点明细出参。
 *
 * <p>{@code consistent} 是"关体后是否与术前一致"的机器比较结果：
 * 数值相等就一致，任一列为 null 就是<b>还没数</b>，返回 null 而不是 false ——
 * 没数 ≠ 不一致，这与"未判定 ≠ 异常"是同一条铁律。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CountItemVO extends BizOperationCountItem {

    private String itemCategoryText;

    /** 关体后 vs 术前是否一致（未登记某个阶段时为 null） */
    private Boolean consistent;

    /** 差值（关体后 - 术前；未登记时为 null） */
    private Integer diffQty;
}
