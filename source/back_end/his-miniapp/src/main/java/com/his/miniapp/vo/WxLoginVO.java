package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 微信登录结果。
 */
@Data
public class WxLoginVO {

    /** 是否已绑定患者账号：false 时前端引导账密/短信注册登录后调 bindOpenid */
    private Boolean bound;

    /** JWT（bound=true 时返回） */
    private String token;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String username;

    private String realName;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private Integer userType;
}
