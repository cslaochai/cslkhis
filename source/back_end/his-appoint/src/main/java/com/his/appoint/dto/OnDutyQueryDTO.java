package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 「今日在岗」查询入参。
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
