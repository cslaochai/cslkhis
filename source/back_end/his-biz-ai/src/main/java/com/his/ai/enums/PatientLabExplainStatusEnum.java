package com.his.ai.enums;

import lombok.Getter;

/**
 * 患者端检验报告解读的单项状态（暴露在 PatientLabItemPlainVO.status，前端按此渲染箭头与文案）。
 */
@Getter
public enum PatientLabExplainStatusEnum {

    NORMAL(1, "正常"),
    HIGH(2, "偏高"),
    LOW(3, "偏低"),
    UNJUDGED(4, "待核对"),
    ABNORMAL(5, "异常");

    private final Integer code;
    private final String desc;

    PatientLabExplainStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PatientLabExplainStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PatientLabExplainStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        PatientLabExplainStatusEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        PatientLabExplainStatusEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}
