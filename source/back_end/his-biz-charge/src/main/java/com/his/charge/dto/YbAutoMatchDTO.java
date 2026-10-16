package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import lombok.Data;

/**
 * 自动对照入参（itemType 空 = 全部类型；名称精确匹配且唯一命中才落对照）。
 */
@Data
public class YbAutoMatchDTO {

    /**
     * 项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8"}, message = "项目类型不合法")
    private Integer itemType;
}
