package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录命中的小程序用户行
 */
@Data
public class MiniUserRowVO implements Serializable {

    /**
     * 用户ID（sys_user 主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录名
     */
    private String userName;

    /**
     * 真实姓名（可空，为空时登录返回用户名）
     */
    private String realName;

    /**
     * 用户类型（3-患者端账号；其他类型不支持患者端登录）
     */
    private Integer userType;

    /**
     * 关联患者ID（患者端账号必填）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 账号状态（0-停用 1-正常）
     */
    private Integer status;
}
