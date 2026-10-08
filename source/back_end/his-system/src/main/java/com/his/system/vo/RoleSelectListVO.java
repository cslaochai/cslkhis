package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色下拉出参：配岗位/配工作台时只用来认「哪个角色」。
 */
@Data
@Schema(name = "RoleSelectListVO", description = "角色下拉出参")
public class RoleSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;
}
