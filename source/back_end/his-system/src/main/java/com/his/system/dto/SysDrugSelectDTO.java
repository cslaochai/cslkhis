package com.his.system.dto;

import lombok.Data;

/**
 * 药品下拉选择查询入参
 */
@Data
public class SysDrugSelectDTO {

    /**
     * 药品类型：1-西药 2-中成药 3-中药饮片
     */
    private Integer drugType;
}
