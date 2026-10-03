package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调价历史查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceHistoryQueryPageDTO extends PageParam {

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗；为空查全部
     */
    private String itemType;

    /**
     * 关键字，按项目编码或名称模糊匹配
     */
    private String keyword;
}
