package com.mis.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录出参
 */
@Data
public class LoginVO implements Serializable {

    private String token;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
}
