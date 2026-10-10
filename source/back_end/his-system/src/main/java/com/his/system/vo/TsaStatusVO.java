package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TSA 服务状态（签名中心「时间戳」区块与运维排障用）。
 */
@Data
public class TsaStatusVO {

    /**
     * 是否有就绪的 TSA 适配器
     */
    private Boolean available;

    /**
     * 适配器名称
     */
    private String tsaName;

    /**
     * TSA 公钥指纹（分组展示，人工核对用）
     */
    private String keyFingerprintGroups;

    /**
     * 配置的时间来源（系统参数 sign.time_source 原始值，未配置取默认 1）
     */
    private Integer configTimeSource;

    /**
     * 实际生效的时间来源（降级后的真值）
     */
    private Integer effectiveTimeSource;

    /**
     * 实际生效的时间来源文案
     */
    private String effectiveTimeSourceText;

    /**
     * 令牌台账总条数
     */
    private Long tokenCount;

    /**
     * 最近一次盖章时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTokenTime;

    /**
     * 信任根说明（如实标注"本地内置信任根，非第三方"）
     */
    private String trustNote;
}
