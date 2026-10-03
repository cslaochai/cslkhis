package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 「今日在岗」查询入参。
 *
 * <p><b>这个接口是排班业务的下游出口</b>：排班表本身只是计划，真正被业务流程消费的是
 * 「此刻/今天这个科室谁在岗」。分院急救派单、今日在岗看板、分诊当班护士都走这里，
 * 所以入了 {@code deptId} 之外的两个可选旋钮：
 *
 * <ul>
 *   <li>{@code moment} —— 按指定时刻判定在岗，而不是只能用服务器当前时间。
 *       跨零点夜班（20:00-08:00）的判定必须是可复现的，否则没法验证、夜间也无法排查。</li>
 *   <li>{@code onDutyOnly} —— false 时返回当天全部排班（含已下班/未上班），
 *       用于「今天谁上班」这类全天视角而不是「此刻谁在」。</li>
 * </ul>
 */
@Data
public class OnDutyQueryDTO {

    /**
     * 科室ID；不传取当前登录人所属科室
     */
    private Long deptId;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他）；空=全部岗位
     */
    private Integer staffType;

    /**
     * 排班日期；不传=今天
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 判定时刻 HH:mm（24 小时制）；不传=服务器当前时间。
     * 只用于「此刻在不在班」的判定，不影响取哪一天的排班。
     */
    private String moment;

    /**
     * true=只返回此刻在岗的人（默认）；false=返回当天全部排班，并逐条标 {@code onDutyNow}
     */
    private Boolean onDutyOnly = Boolean.TRUE;
}
