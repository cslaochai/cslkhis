package com.his.common.enums;

import lombok.Getter;

/**
 * 急诊分区（由急诊分诊级别推导：I/II 级 → 红区，III 级 → 黄区，其余 → 绿区）。
 */
@Getter
public enum EmergencyZoneEnum {

    RED("红区"),
    YELLOW("黄区"),
    GREEN("绿区");

    private final String label;

    EmergencyZoneEnum(String label) {
        this.label = label;
    }

    /**
     * 分诊级别 → 分区。未定级 / 未知级别按绿区（最低优先）处理。
     */
    public static EmergencyZoneEnum ofTriageLevel(Integer triageLevel) {
        if (triageLevel == null) {
            return GREEN;
        }
        return switch (triageLevel) {
            case 1, 2 -> RED;
            case 3 -> YELLOW;
            default -> GREEN;
        };
    }

    /**
     * 分诊级别 → 分区文案。
     */
    public static String zoneText(Integer triageLevel) {
        return ofTriageLevel(triageLevel).label;
    }
}
