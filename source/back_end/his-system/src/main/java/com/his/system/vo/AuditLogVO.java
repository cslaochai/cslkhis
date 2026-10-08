package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志行（审计日志：业务模块显式调用 SysAuditLogService 写的那本账，
 */
@Data
public class AuditLogVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 操作人姓名
     */
    private String userName;
    /**
     * 模块名称
     */
    private String module;
    /**
     * 操作类型
     */
    private String operation;
    /**
     * 操作对象ID
     */
    private String targetId;
    /**
     * 操作对象类型
     */
    private String targetType;
    /**
     * 操作内容
     */
    private String content;
    /**
     * IP地址
     */
    private String ip;

    /**
     * 1-成功 0-失败（注意：与操作日志的 0-正常 1-异常相反，别混用）
     */
    private Integer status;
    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
