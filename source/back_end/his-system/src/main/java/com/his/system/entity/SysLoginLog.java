package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志（登录日志，建表见 sql/10，查看页见 sql/158）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_log")
public class SysLoginLog extends BaseEntity {

    /**
     * 用户名（登录名，失败时也能留下"谁在被试"）
     */
    private String userName;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 真实姓名（用户存在时才有）
     */
    private String realName;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录地点：内网 / 外网（没有 IP 归属库，不猜城市）
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 登录状态（0-成功 1-失败）
     */
    private Integer loginStatus;

    /**
     * 提示消息（失败原因）
     */
    private String msg;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
