package com.his.drgdip.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * DRG 分组方案版本枚举（值 = 库内 version 字符串，如 chs-drg-2.0 / 3.0）。
 */
@Getter
public enum DrgVersionEnum {

    /**
     * CHS-DRG 2.0 官方方案
     */
    CHS_DRG_2_0("chs-drg-2.0", "CHS-DRG 2.0"),
    /**
     * CHS-DRG 3.0 官方方案
     */
    DRG_3_0("3.0", "CHS-DRG 3.0");

    private final String code;
    private final String label;

    DrgVersionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 校验并透传库内版本字符串；仅支持枚举内定义的版本，非法值直接抛错。
     */
    public static String requireValid(String version) {
        String v = version == null ? "" : version.trim();
        for (DrgVersionEnum e : values()) {
            if (e.code.equals(v)) {
                return v;
            }
        }
        throw new IllegalStateException(
                "DRG 分组方案版本仅支持 " + Arrays.stream(values()).map(e -> e.code).collect(Collectors.joining(" / "))
                        + "，当前配置为: [" + version + "]");
    }
}
