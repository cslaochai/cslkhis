package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室医生名册行（患者端挂号选医生），对应 {@code MiniappDirectoryMapper#selectDoctorsByDept}。
 *
 * <p>出参键名逐字保持改造前的下划线形状（小程序端直接按键取值，改名即空白）。
 */
@Data
public class DoctorRowVO implements Serializable {

    /**
     * 员工ID（SQL 已 CAST 成字符串，BIGINT 直出会在 JS 端丢精度）
     */
    @JsonProperty("id")
    private String id;

    /**
     * 员工姓名
     */
    @JsonProperty("emp_name")
    private String empName;

    /**
     * 所属科室ID
     */
    @JsonProperty("dept_id")
    private Long deptId;

    /**
     * 所属科室名称
     */
    @JsonProperty("dept_name")
    private String deptName;

    /**
     * 职称
     */
    @JsonProperty("title")
    private String title;

    /**
     * 擅长
     */
    @JsonProperty("specialty")
    private String specialty;

    /**
     * 是否专家（1-是 0-否）
     */
    @JsonProperty("is_expert")
    private Integer isExpert;

    /**
     * 专家费（元）
     */
    @JsonProperty("expert_price")
    private BigDecimal expertPrice;
}
