package com.his.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * DIP 病种库版本枚举（值 = biz_dip_disease.version 列字符串，如 2.0 / 3.0）。
 */
@Getter
public enum DipVersionEnum {

    /**
     * 国家 DIP 病种库 2.0（2024-07 发布）
     */
    DIP_2_0("2.0", "国家 DIP 病种库 2.0"),
    /**
     * 国家 DIP 病种库 3.0（核心病种精简，覆盖病例数提升）
     */
    DIP_3_0("3.0", "国家 DIP 病种库 3.0");

    private final String code;
    private final String label;

    DipVersionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 校验并透传库内版本字符串；仅支持枚举内定义的版本，非法值直接抛错。
     * 统筹区在 2.0/3.0 基础上的本地化版本号（如「本地 2026 版」）请先扩枚举再配置。
     */
    public static String requireValid(String version) {
        String v = version == null ? "" : version.trim();
        for (DipVersionEnum e : values()) {
            if (e.code.equals(v)) {
                return v;
            }
        }
        throw new IllegalStateException(
                "DIP 病种库版本仅支持 " + Arrays.stream(values()).map(e -> e.code).collect(Collectors.joining(" / "))
                        + "，当前配置为: [" + version + "]");
    }
}
