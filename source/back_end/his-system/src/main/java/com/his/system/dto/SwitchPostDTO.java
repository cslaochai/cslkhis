package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 切换岗位入参：角色 + 科室必须成对传，不接受「只切角色」或「只切科室」。
 */
@Data
public class SwitchPostDTO {

    /**
     * 目标角色编码（角色.role_code）
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 目标科室ID（科室的ID）
     */
    @NotNull(message = "科室不能为空")
    private Long deptId;
}
