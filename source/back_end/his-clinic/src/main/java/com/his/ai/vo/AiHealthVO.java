package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 运行时状态（只读配置快照，不产生模型调用费用）。
 */
@Data
@Schema(description = "AI 运行时状态")
public class AiHealthVO {

    /** AI 能力总开关 */
    @Schema(description = "总开关，来自 application.yml: ai.enabled")
    private boolean enabled;

    /** 是否具备发起调用的完整条件（开关、地址、密钥、模型齐备） */
    @Schema(description = "是否具备发起调用的完整条件（开关+地址+密钥+模型）")
    private boolean ready;

    /** 未就绪原因，就绪时为空 */
    @Schema(description = "未就绪原因，就绪时为空")
    private String notReadyReason;

    /** 提供方标识 */
    @Schema(description = "协议标识")
    private String provider;

    /** 模型服务地址 */
    @Schema(description = "服务地址")
    private String baseUrl;

    /** 主模型名称 */
    @Schema(description = "主模型")
    private String model;

    /** 轻量模型名称 */
    @Schema(description = "轻量模型")
    private String modelLite;

    /** 密钥是否已配置（不返回密钥本身） */
    @Schema(description = "密钥是否已配置（不返回密钥本身）")
    private boolean apiKeyConfigured;

    /** 密钥来源（env-环境变量 yml-配置文件） */
    @Schema(description = "密钥来源，env-环境变量 HIS_AI_API_KEY / yml-application.yml")
    private String apiKeySource;

    /** 全局调用超时（毫秒） */
    @Schema(description = "全局超时（毫秒）")
    private int timeoutMs;

    /** RAG 召回条数上限 */
    @Schema(description = "RAG 召回上限")
    private int retrieveTopN;

    /** 各能力开关的配置启用状态 */
    @Schema(description = "各能力配置开关（来自 application.yml: ai.features.*）。true 只代表配置允许，能否真用还要看 ready")
    private Map<String, Boolean> features = new LinkedHashMap<>();

    /** 各能力超时的配置覆盖（毫秒） */
    @Schema(description = "各能力超时覆盖（毫秒）")
    private Map<String, Integer> timeouts = new LinkedHashMap<>();

    /** 各能力实际生效的模型名称 */
    @Schema(description = "各能力实际生效的模型（含按能力覆盖，未覆盖的回落主/轻量模型）")
    private Map<String, String> models = new LinkedHashMap<>();

    /** 各能力当前是否处于熔断状态 */
    @Schema(description = "各能力当前是否处于熔断状态")
    private Map<String, Boolean> circuitOpen = new LinkedHashMap<>();

    /** 各能力累计降级次数 */
    @Schema(description = "各能力累计降级次数（进程内计数，重启清零）")
    private Map<String, Long> degradedCount = new LinkedHashMap<>();

    /** 各能力最近一次失败原因 */
    @Schema(description = "各能力最近一次失败原因（调用成功后清除）")
    private Map<String, String> lastFailureReason = new LinkedHashMap<>();
}
