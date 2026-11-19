package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 值班类型（值班线）出参。
 */
@Data
public class DutyTypeVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 归口科室ID（这条线由哪个部门排班）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long manageDeptId;

    /**
     * 归口科室名称
     */
    private String manageDeptName;

    /**
     * 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）
     */
    private Integer requiredStaffType;

    /**
     * 应到岗位类别文案
     */
    private String requiredStaffTypeText;

    /**
     * 允许的职务码值（逗号分隔，空-不限）
     */
    private String requiredPositions;

    /**
     * 允许的职务文案（码值翻译后逗号连接）
     */
    private String requiredPositionsText;

    /**
     * 是否需要院领导带班层（1-是 0-否）
     */
    private Integer leaderLayer;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 当前登录人能否排这条线（归口科室=当前岗位科室，或系统管理员）
     */
    private Boolean manageable;
}
