package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 用户名（唯一）
     */
    private String userName;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 关联员工ID
     * 使用ToStringSerializer序列化，防止前端接收Long类型时精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    /**
     * 用户类型（1-院内用户 2-院外用户 3-患者 4-其他）
     */
    private Integer userType;

    /**
     * 关联患者ID（user_type=3 患者时关联患者基本信息的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 微信openid（小程序订阅消息发送用，user_type=3）
     */
    private String openid;

    /**
     * 头像地址
     */
    private String avatar;

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
     * 启用状态 0-禁用 1-启用
     */
    private Integer status;
}
