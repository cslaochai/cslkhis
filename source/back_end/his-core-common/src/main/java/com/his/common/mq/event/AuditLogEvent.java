package com.his.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 操作审计日志事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String module;

    private String operation;

    private String targetType;

    private String targetId;

    private String content;

    private String ip;
    /**
     * 1-成功 0-失败
     */
    private Integer status;

    private String errorMsg;

    /**
     * 触发本次审计的操作人 ID（由 HisMqPublisher 自动填充；与 userId 同值）。
     */
    private Long createById;

    /**
     * 触发本次审计的操作人姓名（由 HisMqPublisher 自动填充；与 userName 同值）。
     */
    private String createBy;
}
