package com.his.appoint.enums;

import lombok.Getter;

/**
 * 门诊分诊等级（区别于急诊 I~IV 级）。
 *
 * <p>数字越小越优先，{@code callNext} 按 {@code triage_level ASC, sequence_no ASC} 取号。
 * <p><b>默认 4（非急）</b>：患者签到入队时后端就写入 4 级，因此「未分诊」不再阻塞接诊。
 * 分诊台的职责是把 1/2/3 级<b>提上来</b>，而不是给 4 级患者放行 —— 唯一保留的硬约束是
 * 「队列里还有 1/2 级未接诊时，{@code callSpecific} 不允许跳过他们」。
 */
@Getter
public enum TriageLevelEnum {

    CRITICAL(1, "1级·危重"),
    EMERGENCY(2, "2级·急症"),
    URGENT(3, "3级·亚急"),
    NON_URGENT(4, "4级·非急");

    private final Integer code;
    private final String desc;

    TriageLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TriageLevelEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (TriageLevelEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return parse(code) != null;
    }
}
