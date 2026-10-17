package com.his.common.enums;

import lombok.Getter;

/**
 * 门诊慢特病备案状态枚举（1-有效 2-已注销 3-已驳回）。
 * <p>
 * 只有 1-有效 参与「同患者同病种只许一条在有效期内」的判重与待遇计算；
 * 与慢病档案的认定状态是两个不同口径，不要互相套用。
 */
@Getter
public enum YbChronicRegStatusEnum {

    VALID(1, "有效"),
    CANCELLED(2, "已注销"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String label;

    YbChronicRegStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YbChronicRegStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (YbChronicRegStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        YbChronicRegStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        YbChronicRegStatusEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
