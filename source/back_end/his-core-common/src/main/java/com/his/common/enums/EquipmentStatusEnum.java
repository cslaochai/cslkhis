package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备状态（sys_equipment.status）：档案的生命周期状态。
 * <p>
 * 码值与文案是字典 his_equipment_status 的权威来源，改码值必须同步字典段与列注释。
 * 只有「在用」的设备会同步启用物联网接入台账，停用/维修中/报废一律停接。
 */
@Getter
public enum EquipmentStatusEnum {

    IN_SERVICE(1, "在用"),
    STOPPED(2, "停用"),
    UNDER_REPAIR(3, "维修中"),
    SCRAPPED(4, "报废");

    private final int code;
    private final String label;

    EquipmentStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EquipmentStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EquipmentStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示：合法→label；null/脏值→空串，绝不返回 null
     */
    public static String getLabel(Integer code) {
        EquipmentStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /**
     * 调用方自带缺省文案
     */
    public static String getLabel(Integer code, String defaultValue) {
        EquipmentStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /**
     * 异常/审计/合规：null/脏值→未知(n)，保留原始码值便于排查，不喂前端展示
     */
    public static String getLabelOrUnknown(Integer code) {
        EquipmentStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 是否在用（只有在用的设备才允许接入并参与业务）
     */
    public static boolean isInService(Integer code) {
        return fromCode(code) == IN_SERVICE;
    }
}
