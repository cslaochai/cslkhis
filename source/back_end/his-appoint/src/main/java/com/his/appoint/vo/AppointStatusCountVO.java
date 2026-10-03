package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 挂号状态统计 VO —— 六格状态卡的**唯一**取数出口。
 *
 * <p>为什么要一个专门接口：页面上那六个数原本是前端发 6 次 {@code /appoint/listPage}
 * （每次 {@code pageSize=1} 只为读合计）算出来的，加上列表自己那次共 7 次。
 * 那是「用分页接口当 count 接口」，代价是 6 次全量 COUNT + 6 次分页包装。
 *
 * <p><b>口径</b>：分组条件必须与 {@code AppointQueryDTO} 完全一致（患者/科室/医生/就诊日/时间区间），
 * 统计范围就是**当前筛选条件**下的挂号记录，不叠加任何隐式范围。
 * 前端筛选条件变了，这六个数必须跟着变 —— 否则"筛选后列表 3 条、卡片还是全院 4000"。
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
