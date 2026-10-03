package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 医保合规审核配置。
 * <p>
 * 落点在 {@code application.yml} 的 {@code insurance.compliance.*} 段（启动时绑定一次，改值需重启）。
 * <b>刻意不写系统参数</b>：这些是部署期阈值，属于随制品走的配置，
 * 与 AI 配置（{@code ai.*}）同一口径。
 */
@Data
@Component
@ConfigurationProperties(prefix = "insurance.compliance")
public class ComplianceProperties {

    /**
     * 审核总开关。关闭后所有规则返回「不适用」，不会假装通过
     */
    private boolean enabled = true;

    /**
     * 高倍率阈值：实际费用 / 病组支付标准超过它 → 高倍率（疑似高编高套）
     */
    private BigDecimal highCostRatio = new BigDecimal("2.0");

    /**
     * 低倍率阈值：低于它 → 低倍率（疑似低编入组 / 分解住院）
     */
    private BigDecimal lowCostRatio = new BigDecimal("0.5");

    /**
     * 最短住院天数：住院天数小于它且无手术操作 → 疑似低标准入院
     */
    private int minInpatientDays = 3;

    /**
     * 分解住院识别窗口（天）：同一患者在此窗口内再次以同一主诊断入组
     */
    private int readmitWindowDays = 15;

    /**
     * 留痕时引用的原文最大长度，防止 evidence 撑爆审计表
     */
    private int maxEvidenceQuote = 300;
}
