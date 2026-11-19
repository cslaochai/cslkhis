package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 值班类型新增/修改入参。
 */
@Data
public class DutyTypeUpsertDTO implements Serializable {

    /**
     * 主键（空=新增）
     */
    private Long id;

    /**
     * 类型编码（唯一，大写字母/下划线）
     */
    @NotBlank(message = "类型编码不能为空")
    @Size(max = 32, message = "类型编码不能超过 32 个字符")
    private String typeCode;

    /**
     * 类型名称
     */
    @NotBlank(message = "类型名称不能为空")
    @Size(max = 64, message = "类型名称不能超过 64 个字符")
    private String typeName;

    /**
     * 归口科室ID：这条线由哪个部门排班（权限锚点）
     */
    @NotNull(message = "归口科室不能为空")
    private Long manageDeptId;

    /**
     * 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6"}, message = "应到岗位类别取值不合法")
    private Integer requiredStaffType;

    /**
     * 允许的职务码值（字典 hospital_position，逗号分隔，空-不限）
     */
    @Size(max = 200, message = "职务码值串不能超过 200 个字符")
    private String requiredPositions;

    /**
     * 是否需要院领导带班层（1-是 0-否）
     */
    @AllowedValues(values = {"0", "1"}, message = "带班层标志取值不合法")
    private Integer leaderLayer;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 状态（1-启用 0-停用）
     */
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注不能超过 200 个字符")
    private String remark;
}
