package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 门诊号源按日汇总行（{@code BizScheduleMapper#summaryByDay} 的返回行）。
 *
 * <p>对应 SQL：{@code biz_schedule} 按 {@code schedule_date} 的 group by ——
 * 班次数、号源总数、已挂数、停诊班次数。排班周总览（总览驾驶舱）据此铺一周矩阵。
 *
 * <p>{@code scheduleDate} 直接给 {@link LocalDate}：本列就是 DATE 类型，
 * 早前为了绕开裸 Map 取值才 {@code DATE_FORMAT} 成字符串，换 VO 后由 MyBatis 直接映射。
 */
@Data
public class ScheduleDaySummaryRowVO implements Serializable {

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 出诊班次数
     */
    private Long shiftCount;

    /**
     * 总号源
     */
    private BigDecimal totalSource;

    /**
     * 已挂号源
     */
    private BigDecimal usedSource;

    /**
     * 停诊班次数（status=0）
     */
    private Long stoppedCount;
}
