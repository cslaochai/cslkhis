package com.his.system.vo;

import lombok.Data;

/**
 * 登录结果出参
 */
@Data
public class LoginVO {

    /**
     * 登录令牌
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 当前角色编码
     */
    private String currentRole;

    /**
     * 关联患者ID（患者登录时返回，用于小程序端患者档案/挂号/报告等患者级查询）
     */
    private Long patientId;

    /**
     * 用户类型（1-院内用户 2-院外用户 3-患者 4-其他）
     */
    private Integer userType;
}
