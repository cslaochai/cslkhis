package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 开放科室下拉行（患者端挂号页），对应 {@code MiniappDirectoryMapper#selectOpenDepartments}。
 *
 * <p>Mapper 直接返回本 VO（不再经中间 Map 转换）：出参键名逐字保持改造前的下划线形状
 * （小程序端直接按键取值，改名即空白），SQL 别名与字段名的对应靠 {@code @JsonProperty} 固定。
 */
@Data
public class OpenDeptRowVO implements Serializable {

    /**
     * 科室ID（SQL 已 CAST 成字符串，BIGINT 直出会在 JS 端丢精度）
     */
    @JsonProperty("id")
    private String id;

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
