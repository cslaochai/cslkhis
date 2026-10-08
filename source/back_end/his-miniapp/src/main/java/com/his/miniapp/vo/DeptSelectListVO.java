package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者端科室候选行。
 */
@Data
public class DeptSelectListVO implements Serializable {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室名称
     */
    @JsonProperty("dept_name")
    private String deptName;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）
     */
    @JsonProperty("dept_type")
    private Integer deptType;

    /**
     * 科室简介
     */
    @JsonProperty("dept_desc")
    private String deptDesc;
}
