package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDrugQueryPageDTO extends PageParam {

    /**
     * 药品名称，模糊匹配
     */
    private String drugName;

    /**
     * 药品类型：1-西药 2-中成药 3-中药饮片
     */
    private Integer drugType;

    /**
     * 特殊管理分类过滤：0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品
     * <p>
     * 为空=不过滤；`1` 只查麻醉药品。药房盘点麻精目录靠它。
     * 注意：**不能用 `specialFlag != 0` 表达"只看管制药品"** —— 那需要另一个布尔入参，
     * 用分类值精确过滤语义更清楚。
     */
    private Integer specialFlag;
}
