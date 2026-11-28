package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配送任务发起来源：各业务站点发起，或设备科手工派单。
 */
@Getter
public enum RobotDispatchSourceEnum {

    PIVAS("PIVAS", "静配中心"),
    PHARMACY("PHARMACY", "住院药房"),
    LAB("LAB", "检验科"),
    WAREHOUSE("WAREHOUSE", "中心库房"),
    CSSD("CSSD", "消毒供应中心"),
    LAUNDRY("LAUNDRY", "洗涤中心"),
    MANUAL("MANUAL", "手工派单");

    private final String code;
    private final String label;

    RobotDispatchSourceEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RobotDispatchSourceEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (RobotDispatchSourceEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        RobotDispatchSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }
}
