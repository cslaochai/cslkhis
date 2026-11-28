package com.his.iot.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车队单车状态卡片：台账归属 + 最新遥测 + 当前执行任务。
 */
@Data
public class RobotFleetVO {

    private String robotNo;
    private String deviceName;
    private String location;

    /** 在线状态：true 最近有遥测上报 */
    private Boolean online;

    /** 运行状态：IDLE 空闲 / DELIVERING 配送中 / CHARGING 充电 / OFFLINE 离线 */
    private String state;
    private String stateLabel;

    /** 电量(%) */
    private Double battery;

    /** 当前载重(kg) */
    private Double loadKg;

    /** 当前箱数 */
    private Integer boxCount;

    /** 当前位置文本 */
    private String locationText;

    /** 当前执行任务单号（空闲为空） */
    private String currentOrderNo;
    private String currentGoods;
    private String currentToStation;
    private Integer currentPriority;

    private LocalDateTime ts;
}
