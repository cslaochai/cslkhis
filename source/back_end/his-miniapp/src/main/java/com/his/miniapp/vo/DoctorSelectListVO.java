package com.his.miniapp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 患者端某科室的医生名册行。
 */
@Data
public class DoctorSelectListVO implements Serializable {

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 员工姓名
     */
    @JsonProperty("emp_name")
    private String empName;

    /**
     * 所属科室ID
     */
    @JsonProperty("dept_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 所属科室名称
     */
    @JsonProperty("dept_name")
    private String deptName;

    /**
     * 职称
     */
    private String title;

    /**
     * 擅长
     */
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
