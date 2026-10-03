package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医保飞检/专项审核批次任务头。
 *
 * <p>状态机 1-进行中 → 2-已结项 / 3-已作废；结项必填结论，名下有扣款通知时禁作废
 * （问题已入账不能蒸发）。作废是状态不是删除，本表不提供物理删。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_inspection")
public class BizYbInspection extends BaseEntity {

    /**
     * 批次号（FI+yyyyMMdd+4位）
     */
    private String inspectNo;

    /**
     * 检查类型（字典 his_yb_inspect_type：1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）
     */
    private Integer inspectType;

    /**
     * 统筹区/医保局名称
     */
    private String fundOrg;

    /**
     * 审核目标期间起
     */
    private LocalDate inspectStartDate;

    /**
     * 审核目标期间止
     */
    private LocalDate inspectEndDate;

    /**
     * 检查组进驻/通知日期
     */
    private LocalDate inspectDate;

    /**
     * 检查组/审核团队名称
     */
    private String inspectTeam;

    /**
     * 本院接待负责人
     */
    private String ourReceiver;

    /**
     * 状态（字典 his_yb_inspect_status：1-进行中 2-已结项 3-已作废）
     */
    private Integer status;

    /**
     * 结项结论（已结项必填）
     */
    private String conclusion;

    /**
     * 结项时间
     */
    private LocalDateTime concludeTime;

    /**
     * 结项经办人
     */
    private String concludeBy;

    /**
     * 作废原因（必填）
     */
    private String cancelReason;

    /**
     * 作废经办人
     */
    private String cancelBy;

    /**
     * 作废时间
     */
    private LocalDateTime cancelTime;
}
