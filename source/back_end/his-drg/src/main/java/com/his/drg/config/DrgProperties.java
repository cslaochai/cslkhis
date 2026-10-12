package com.his.drg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DRG 分组方案配置，由 {@code config/domain/his-drg.yml} 的 his.drg.* 段绑定。
 *
 * <p>核心只有一项：结算默认启用的分组方案版本 {@link #defaultVersion}，日常维护只动这一行，
 * 填 {@code 2.0} 或 {@code 3.0} 即可（2.0 对应库内 version 字符串 chs-drg-2.0，3.0 对应 3.0）。
 * 也允许直接写库内完整版本串（含 '-'，如 chs-drg-2.0），但日常不要这么写。
 *
 * <p>非法值（既非 2.0/3.0 也非合法库内串）在 {@link #resolveDbVersion(String)} 解析时启动即 fail-fast，
 * 不让配置错误静默回落到某一个版本。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.drg")
public class DrgProperties {

    /**
     * DRG 结算默认启用的分组方案版本：2.0 或 3.0。
     */
    private String defaultVersion = "3.0";

    /**
     * 把短版本号（2.0 / 3.0 或库内完整版本串）解析成库内 version 字符串；非法值直接抛错。
     */
    public static String resolveDbVersion(String shortVersion) {
        String v = shortVersion == null ? "" : shortVersion.trim();
        return switch (v) {
            case "2.0" -> "chs-drg-2.0";
            case "3.0" -> "3.0";
            default -> {
                if (v.contains("-")) {
                    yield v;
                }
                throw new IllegalStateException(
                        "his.drg.default-version 仅支持 2.0 或 3.0（或库内完整版本串，如 chs-drg-2.0），当前配置为: ["
                                + shortVersion + "]");
            }
        };
    }
}
