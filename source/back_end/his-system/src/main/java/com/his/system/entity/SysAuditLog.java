package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审计日志 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity {

    /** 操作人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 操作人姓名 */
    private String userName;
    /** 模块名称 */
    private String module;
    /** 操作类型 */
    private String operation;
    /** 操作对象ID */
    private String targetId;
    /** 操作对象类型 */
    private String targetType;
    /** 操作内容 */
    private String content;
    /** IP地址 */
    private String ip;
    /** 状态（1-成功 0-失败） */
    private Integer status;
    /** 错误信息 */
    private String errorMsg;
}
