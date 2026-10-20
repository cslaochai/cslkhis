package com.his.operation.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 手术间利用率统计行（排台总表配套看板：某区间各手术间用了多少、空了多少）。
 *
 * <p>口径：可用时长 = 单间每日运营窗口 × 区间天数（运营窗口当前按 08:00~18:00 常量，
 * 如需按院方实际上下班配置，改为从字典读取）。占用时长按排台计划起止分钟计，
 * 跨天台不做截断（务实口径，注释声明）。利用率 = 占用时长 / 可用时长，保留 1 位小数百分比。
 */
@Data
public class RoomUtilizationVO {

    /** 手术间主数据 id */
    private Long roomId;

    /** 手术间编码 */
    private String roomCode;

    /** 手术间名称 */
    private String roomName;

    /** 位置 */
    private String location;

    /** 区间内已排台数（状态 1-已排期/2-术前核对/3-已登记完成） */
    private Integer scheduledCount;

    /** 其中急诊台数 */
    private Integer emergencyCount;

    /** 占用分钟数 */
    private Long occupiedMinutes;

    /** 可用分钟数 */
    private Long capacityMinutes;

    /** 利用率（百分比，1 位小数） */
    private BigDecimal utilizationRate;
}
