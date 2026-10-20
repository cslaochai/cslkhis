package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 急诊派单方式枚举
 */
@Getter
@AllArgsConstructor
public enum EmergencyAssignTypeEnum {

    /**
     * 0-历史未记（sql/145 之前的存量行）
     */
    LEGACY(0, "历史未记"),

    /**
     * 1-登记时指定医生
     */
    MANUAL(1, "登记指定"),

    /**
     * 2-登记未填医生，按当日该科室排班自动派单
     */
    AUTO_BY_SCHEDULE(2, "系统派单"),

    /**
     * 3-当日该科室无在岗医生，进待派单池（等接诊认领或超时升级）
     */
    POOL(3, "入池待派单"),

    /**
     * 4-入池后由医生在「接诊」时认领（谁接诊谁负责，覆盖 POOL 的落点）
     */
    CLAIMED(4, "接诊认领"),

    /**
     * 5-交班时由接班人承接：交出人把「无人指派」或「自己名下还没闭环」的行点名移交。
     * 与 4 的区别在于责任是<b>换人</b>而不是<b>首次落人</b>，事后要分得开
     * "这条一直是某医生看的"和"这条是下班时被交出去的" —— 后者说明上一班没闭环。
     */
    HANDOVER_TAKE(5, "交班承接");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例，未匹配返回 {@code null}
     */
    public static EmergencyAssignTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EmergencyAssignTypeEnum type : values()) {
            if (type.code == code) {
                return type;
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
}
