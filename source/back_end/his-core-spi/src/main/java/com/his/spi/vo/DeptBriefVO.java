package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科室主数据摘要：跨模块只读取科室的名称、类型、启用状态与三类排班审核人。
 */
@Data
@NoArgsConstructor
public class DeptBriefVO {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室编码
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔
     */
    private String deptType;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 门诊排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long outpatientAuditorId;

    /**
     * 护理排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nursingAuditorId;

    /**
     * 全院岗位排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleAuditorId;
}
