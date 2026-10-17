package com.his.api.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户契约（患者端 / 系统操作人）。
 *
 * <p>his-api 对外暴露的最小用户视图：miniapp 只依赖这个类型，不依赖 his-system 的 {@code CurrentUser}。
 * his-system 的 {@code AuthContextApi} 实现负责把院内 {@code CurrentUser} 映射为本契约。
 */
@Data
public class CurrentUser implements Serializable {

    /**
     * 用户ID（账号维度）
     */
    private Long userId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 关联的就诊人ID（患者端登录态必带）
     */
    private Long patientId;

    /**
     * 用户类型（见 his-common 的 UserTypeEnum：3=患者）
     */
    private Integer userType;
}
