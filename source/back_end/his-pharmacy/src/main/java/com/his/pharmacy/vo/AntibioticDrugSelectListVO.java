package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 抗菌药物下拉选项（selectList 口径，与接口末段同名） */
@Data
public class AntibioticDrugSelectListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 抗菌药物分级（1/2/3） */
    private Integer antibioticLevel;

    private String antibioticLevelText;
}
