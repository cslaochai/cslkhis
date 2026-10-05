package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志行（等保三级要的正是失败登录那一半，所以成功失败同表同列表，靠 loginStatus 区分）。
 */
@Data
public class LoginLogVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 登录IP
     */
    private String loginIp;
    /**
     * 登录地点
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
     * 登录状态（0-成功 1-失败）
     */
    private Integer loginStatus;
    private String loginStatusText;

    /**
     * 提示消息（失败原因）
     */
    private String msg;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 完整 UA（列表也返回，排查异常终端靠它）
     */
    private String userAgent;
}
