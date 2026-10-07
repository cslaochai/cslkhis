package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录命中的小程序用户行，对应 {@code MiniappSysUserMapper#selectByOpenid}。
 *
 * <p>只回答「这个 openid 绑的是谁、是不是患者账号、还能不能用」，
 * 不含密码、身份证等与登录判定无关的列。
 *
 * <p>本 VO 只在服务层内部流转（最终出参是 {@link WxLoginVO}），BIGINT 主键仍按全库铁律
 * 加 {@code ToStringSerializer}，以防后续被直接序列化时在 JS 端丢精度。
 */
@Data
public class MiniappUserRowVO implements Serializable {

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
