package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流机器人站点类型：站点是物理收发点，不是组织节点。
 * <p>
 * sourceType 用于把「谁发起的」推导成任务单来源：药房→PHARMACY、静配→PIVAS、医技→LAB、
 * 消供→CSSD、库房→WAREHOUSE、被服→LAUNDRY；护士站/充电桩/中转驿站与设备科手工单一律 MANUAL。
 */
@Getter
public enum RobotStationTypeEnum {

    NURSE_STATION(1, "护士站", "MANUAL"),
    PHARMACY(2, "药房", "PHARMACY"),
    PIVAS(3, "静配中心", "PIVAS"),
    TECH(4, "医技", "LAB"),
    CSSD(5, "消毒供应", "CSSD"),
    WAREHOUSE(6, "库房", "WAREHOUSE"),
    LAUNDRY(7, "被服洗涤", "LAUNDRY"),
    DOCK(8, "充电桩", "MANUAL"),
    HUB(9, "中转驿站", "MANUAL");

    private final int code;
    private final String label;
    /** 推导出的任务单发起来源（RobotDispatchSourceEnum 的码值） */
    private final String sourceType;

    RobotStationTypeEnum(int code, String label, String sourceType) {
        this.code = code;
        this.label = label;
        this.sourceType = sourceType;
    }

    public static RobotStationTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RobotStationTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        RobotStationTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RobotStationTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RobotStationTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** 站点 → 任务单发起来源；未知类型按手工派单处理 */
    public static String sourceTypeOf(Integer code) {
        RobotStationTypeEnum item = fromCode(code);
        return item == null ? RobotDispatchSourceEnum.MANUAL.getCode() : item.sourceType;
    }
}
