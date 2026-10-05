package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班次字典实体（医院标准班次，排班/周模板「班次」下拉的唯一数据源）
 * <p>
 * 设计说明：
 * - 起止时间用 String「HH:mm」，排班/模板的时间段一律由这里带出；
 * 跨零点班次（18:00~次日 08:00 这类）只有全院值守册与全院通用册能建；
 * - duration_minutes 保存时按起止时间重算（服务端强制，前端传值不认）；
 * - dept_id 为 NULL 表示全院通用，指定科室则仅该科室可见；
 * - schedule_type（班别 1-上午 2-下午 3-全天 4-凌晨 5-夜班）是本班次的属性：
 * 排班信息 / 排班周模板表上没有这一列，展示侧按 shift_id 回字典取，
 * 建班次时显式指定，不从时间猜（「全天门诊 08:00~17:00」从时间推不出「全天」）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_shift")
public class BizShift extends BaseEntity {

    /**
     * 班次名称（如上午门诊）
     */
    private String shiftName;

    /**
     * 开始时间 HH:mm（如 08:00）
     */
    private String startTime;

    /**
     * 结束时间 HH:mm（如 12:00；跨零点班次指次日，如 18:00~08:00 的 08:00 是第二天早上）
     */
    private String endTime;

    /**
     * 跨零点标记（1-跨零点，结束时间属次日；0-同日起止）
     */
    private Integer crossDay;

    /**
     * 时长（分钟），保存时按起止时间重算校验
     */
    private Integer durationMinutes;

    /**
     * 适用科室ID（NULL=全院通用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 班次类型（1-上午 2-下午 3-全天，选中时同步排班；NULL=不联动）
     */
    private Integer scheduleType;

    /**
     * 适用域（ShiftUseScopeEnum：1-门诊排班 2-病区护理，sql/166）。
     * 两册班次互不串门：门诊排班只认 1，护理排班只认 2。
     */
    private Integer useScope;

    /**
     * 适用岗位类别（StaffTypeEnum 码值，NULL=全部岗位通用）
     */
    private Integer applyStaffType;

    /**
     * 是否夜班（1-夜班 0-白班，sql/207）。
     *
     * <p><b>这是班次自己声明的属性，不是从起止时间推出来的结论</b>：
     * 「急诊后夜班」00:00 起不跨天，按跨天判会漏；「晚间门诊」18:00 起按时刻判会误伤（那是延时门诊不是值班）。
     * 连续夜班上限、岗后最短休息这两条规则能不能判，全看这一列。
     */
    private Integer isNight;

    /**
     * 下这个班之后的最短休息小时数（0-不限制，sql/207）。
     *
     * <p>夜班通例取 16（近乎「下夜班次日不排早班」），但不是铁律 —— 具体由科室在班次字典里定。
     */
    private java.math.BigDecimal needRestHours;

    /**
     * 迟到宽限（分钟，sql/214）：签到晚于「班次开始 + 这个数」才算迟到。
     *
     * <p><b>为什么要放在班次字典上</b>：迟到宽限是<b>判定参数</b>。
     * 写死 15 分钟在 SQL 视图和 Java 两边各存一份，迟早会对不上 ——
     * 于是同一个人在面板上是"迟到"，到了报表里变成"正常"。
     * 放在这里，视图（sql/214）和签到判定读的是同一个数，
     * 急诊可以在这儿给 5 分钟、门诊给 15，各按各的规矩。
     */
    private Integer lateGraceMinutes;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
