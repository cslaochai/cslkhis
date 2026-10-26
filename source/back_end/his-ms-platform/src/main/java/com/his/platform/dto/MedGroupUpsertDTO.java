package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 医疗组新增/修改入参
 */
@Data
public class MedGroupUpsertDTO {

    /**
     * 医疗组ID，新增时为空，修改时必填
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 医疗组编码，新增时可不传（系统按 MG + 序号生成）
     */
    private String groupCode;

    /**
     * 医疗组名称，如「呼吸内科一组」
     */
    @NotBlank(message = "医疗组名称不能为空")
    private String groupName;

    /**
     * 所属科室ID
     */
    @NotNull(message = "所属科室不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主诊医师 / 医疗组长（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaderId;

    /**
     * 状态（0-停用 1-启用）
     */
    @AllowedValues(values = {"0", "1"}, message = "医疗组状态取值不合法")
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
