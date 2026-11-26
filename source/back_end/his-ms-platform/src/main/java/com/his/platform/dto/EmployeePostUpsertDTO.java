package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 岗位调配入参：只维护某员工的岗位（角色 × 科室），不动档案其他字段。
 *
 * <p>独立于 {@link EmployeeUpsertDTO}：岗位弹框整体保存时不应把病区/医疗组等档案字段洗成空。
 */
@Data
public class EmployeePostUpsertDTO {

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 岗位列表：空集合 = 显式清空该员工全部岗位
     */
    @Valid
    private List<EmployeePostDTO> posts;
}
