package com.his.appoint.vo;

import lombok.Data;

/**
 * 队列统计VO
 */
@Data
public class QueueStatsVO {

    /**
     * 今日总人次
     */
    private Long total;

    /**
     * 待签到
     */
    private Long unchecked;

    /**
     * 候诊中
     */
    private Long waiting;

    /**
     * 就诊中
     */
    private Long consulting;

    /**
     * 已完成
     */
    private Long completed;

    /**
     * 已过号
     */
    private Long overdue;

    /**
     * 未核验分级（今日候诊中且 triage_status=0）。
     * <p>签到即给 4 级默认等级，所以这个数**不代表**「叫不出号」——
     * 它只表示护士还没看过这几个人（没量体征、没定级）。页面文案必须是「未核验」，不能写成「未分诊不能接诊」。
     */
    private Long unTriage;

    /**
     * 危重待接诊（今日候诊中且 triage_level ≤ 2，即 1 级危重 / 2 级急症）。
     * <p>这是队列里唯一还有硬约束的场景：{@code callSpecific} 会拒绝越过他们去叫普通患者，
     * 所以必须在统计条上醒目给出，护士和医生第一时间能看见。
     */
    private Long criticalWaiting;

    /**
     * 候诊超时（候诊中且已等候超过 30 分钟）。
     * 查询时现算，不落状态列 —— 落列就得靠定时任务刷，任务没跑的窗口期会显示错的旧值。
     */
    private Long timeout;

    /**
     * 坐诊医生数（从Redis获取）
     */
    private Integer doctorCount;
}
