package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）。
 *
 * <p>麻醉方式属于手术侧口径，刻意与给药途径等码表保持独立，不要图省事复用别的码表。
 *
 * <p>每个码值带一个 {@code chargeItemCode}（该方式对应的麻醉费项目编码）：
 * 计费项目是由麻醉方式唯一决定的收费口径，与码值同生共死，
 * 所以挂在枚举上而不是留在计费类里当一张码值→编码的映射表。
 */
@Getter
public enum OperationAnesthesiaMethodEnum {

    GENERAL(1, "全身麻醉", "AN001"),
    NEURAXIAL(2, "椎管内麻醉", "AN002"),
    NERVE_BLOCK(3, "神经阻滞麻醉", "AN003"),
    LOCAL(4, "局部麻醉", "AN004"),
    OTHER(5, "其他", "AN005");

    private final int code;
    private final String label;
    /** 该麻醉方式对应的麻醉费项目编码 */
    private final String chargeItemCode;

    OperationAnesthesiaMethodEnum(int code, String label, String chargeItemCode) {
        this.code = code;
        this.label = label;
        this.chargeItemCode = chargeItemCode;
    }

    public static OperationAnesthesiaMethodEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationAnesthesiaMethodEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值 → 展示文案（本枚举文案唯一出口）。
     *
     * <p><b>本枚举声明的缺省展示文案是「—」</b>：null（未填写）渲染为「—」；
     * 合法码值取 label；脏值（不在枚举内的越界码值）返回空串 {@code ""}，
     * 绝不回落合法文案，也绝不返回 null。
     * 异常 / 审计场景需保留原始码值时用 {@link #labelOrUnknown(Integer)}。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        OperationAnesthesiaMethodEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 渲染「未知」，脏值渲染「未知(n)」保留原始码值；
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        OperationAnesthesiaMethodEnum e = code == null ? null : fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
