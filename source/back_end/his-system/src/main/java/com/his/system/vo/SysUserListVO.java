package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO（包含员工信息）
 */
@Data
public class SysUserListVO {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /** 用户类型（1-系统用户 2-外部用户） */
    private Integer userType;

    /**
     * 最后登录时间
     * 按 yyyy-MM-dd HH:mm:ss 格式序列化
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 密码更新时间
     * 按 yyyy-MM-dd HH:mm:ss 格式序列化
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordUpdateTime;

    /**
     * 启用状态（his_enable_status：0-禁用 1-启用；无「锁定」态，原 his_user_status 字典已删除）
     */
    private Integer status;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
