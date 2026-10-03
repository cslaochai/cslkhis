package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者端科室候选行。
 *
 * <p>出参键名逐字保持改造前跨模块取数的下划线形状（小程序端直接按键取值，改名即空白）。
 */
@Data
public class DeptSelectListVO implements Serializable {

    /** 科室ID（字符串化防 BIGINT 精度丢失） */
    private String id;

    /** 科室名称 */
    @JsonProperty("dept_name")
    private String deptName;

    /** 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他） */
    @JsonProperty("dept_type")
    private Integer deptType;

    /** 科室简介 */
    @JsonProperty("dept_desc")
    private String deptDesc;
}
