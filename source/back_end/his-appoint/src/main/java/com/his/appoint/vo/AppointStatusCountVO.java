package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 挂号状态统计 VO —— 六格状态卡的**唯一**取数出口。
 */
@Data
public class AppointStatusCountVO {

    /**
     * 当前筛选条件下的挂号总数
     */
    private Long total;

    /**
     * 待就诊（regist_status=1 已挂号）
     */
    private Long waiting;

    /**
     * 已签到（regist_status=2）
     */
    private Long checkedIn;

    /**
     * 已就诊（regist_status=4）
     */
    private Long completed;

    /**
     * 已退号（regist_status=5）
     */
    private Long refunded;

    /**
     * 已过号（regist_status=6）
     */
    private Long overdue;

    /**
     * 统计口径的筛选范围描述，供页面显示「本次统计范围」
     */
    private String scopeLabel;

    /**
     * 统计取数时间（页面上要能看出这是什么时候的数）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statsTime;
}
