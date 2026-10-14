package com.mss.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户新增/修改入参（id 为空走新增，必填密码；id 非空走修改，密码不传则不变）
 */
@Data
public class UserUpsertDTO implements Serializable {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private Long empId;

    /** 用户类型（1-系统用户 2-外部用户） */
    private Integer userType;

    /** 状态（0-停用 1-启用 2-锁定） */
    private Integer status;

    private String remark;
}
