package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 值守点位出参。
 */
@Data
public class DutyPostVO {

    /** 值守点位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 点位编码 */
    private String postCode;
    /** 点位名称 */
    private String postName;

    /** 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室） */
    private Integer dutyScope;
    /** 责任范围文案（由枚举 label 渲染） */
    private String dutyScopeText;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;
    /** 排班单元类型文案（由枚举 label 渲染） */
    private String orgTypeText;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称：点位归属的科室/病区名 */
    private String orgName;

    /** 班内角色（1-主班 2-副班） */
    private Integer roleType;
    /** 班内角色文案（由枚举 label 渲染） */
    private String roleTypeText;

    /**
     * 值班层级（0-不适用 1-一线 2-二线 3-三线）
     */
    private Integer dutyLevel;
    /** 值班层级文案（由枚举 label 渲染） */
    private String dutyLevelText;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;
    /** 响应形态文案（由枚举 label 渲染） */
    private String attendModeText;

    /** 标准班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 标准班次名称 */
    private String shiftName;

    /**
     * 班次开始时间 HH:mm
     */
    private String startTime;

    /**
     * 班次结束时间 HH:mm
     */
    private String endTime;

    /** 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工护理辅助，空-不限） */
    private Integer requiredStaffType;
    /** 应到岗位类别文案（由枚举 label 渲染） */
    private String requiredStaffTypeName;

    /** 点位值班电话 */
    private String phone;
    /** 排序号：点位列表展示顺序，小的在前 */
    private Integer sortNo;
    /** 状态（1-启用 0-停用） */
    private Integer status;
    /** 备注 */
    private String remark;
}
