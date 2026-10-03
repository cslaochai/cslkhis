package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 值守点位新增/修改入参。
 */
@Data
public class DutyPostUpsertDTO {

    /** 主键ID（空=新增） */
    private Long id;

    /** 点位编码 */
    @NotBlank(message = "点位编码不能为空")
    private String postCode;

    /** 点位名称 */
    @NotBlank(message = "点位名称不能为空")
    private String postName;

    /** 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室） */
    @NotNull(message = "责任范围不能为空")
    private Integer dutyScope;

    /** 排班单元类型（1-科室 2-病区 3-全院，空=全院） */
    private Integer orgType;

    /** 排班单元ID（全院级不传） */
    private Long orgId;

    /** 班内角色（1-主班 2-副班） */
    @NotNull(message = "班内角色不能为空")
    private Integer roleType;

    /** 标准班次ID */
    @NotNull(message = "请选择班次")
    private Long shiftId;

    /** 值班层级（0-不适用 1-一线 2-二线 3-三线，空=按责任范围推：临床科室默认一线，其余为不适用） */
    private Integer dutyLevel;

    /** 响应形态（1-坐班 2-听班 3-留院值班，空=按层级推：一线留院值班，二线三线听班） */
    private Integer attendMode;

    /** 应到岗位类别（空=不限） */
    private Integer requiredStaffType;

    /** 点位值班电话 */
    private String phone;

    /** 排序号 */
    private Integer sortNo;

    /** 状态（0-停用 1-启用，空=启用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
