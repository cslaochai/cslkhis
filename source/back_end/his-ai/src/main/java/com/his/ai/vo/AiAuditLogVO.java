package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 调用审计记录。
 */
@Data
@Schema(description = "AI 调用审计记录")
public class AiAuditLogVO {

    @Schema(description = "记录ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 能力标识
     */
    @Schema(description = "能力标识")
    private String capabilityKey;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 提供方标识
     */
    @Schema(description = "服务提供方")
    private String provider;

    /**
     * 模型名
     */
    @Schema(description = "实际使用的模型")
    private String model;

    /**
     * 提示词版本号
     */
    @Schema(description = "提示词版本")
    private String promptVersion;

    /**
     * 输入摘要
     */
    @Schema(description = "输入摘要（已脱敏、已截断）")
    private String inputDigest;

    /**
     * 输出摘要
     */
    @Schema(description = "输出摘要（已脱敏、已截断）")
    private String outputDigest;

    /**
     * 输入 token 数
     */
    @Schema(description = "输入 token 数")
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    @Schema(description = "输出 token 数")
    private Integer completionTokens;

    /**
     * 调用耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private Integer latencyMs;

    @Schema(description = "状态码")
    private Integer status;

    /**
     * 状态文本
     */
    @Schema(description = "状态文本")
    private String statusText;

    /**
     * 失败原因
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 调用人
     */
    @Schema(description = "操作人")
    private String operator;

    /**
     * 调用时间
     */
    @Schema(description = "发生时间")
    private LocalDateTime createTime;
}
