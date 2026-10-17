package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 飞检批次行（列表用，附带名下扣款单数与金额合计，便于「这个批次要处理多少事」一眼看清）。
 */
@Data
public class YbInspectionListVO {

    /**
     * 主键（雪花ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 批次号
     */
    private String inspectNo;

    /**
     * 检查类型（1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）
     */
    private Integer inspectType;

    /**
     * 统筹区/医保局名称
     */
    private String fundOrg;

    /**
     * 审核目标期间起
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectStartDate;

    /**
     * 审核目标期间止
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inspectEndDate;

    /**
     * 检查组进驻/通知日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 状态（字典 his_yb_inspect_status）
     */
    private Integer status;

    /**
     * 结项结论
     */
    private String conclusion;

    /**
     * 结项时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 名下扣款通知单数（作废闸门依据：>0 时禁作废）
     */
    private Integer deductCount;

    /**
     * 名下扣款金额合计
     */
    private BigDecimal deductAmountSum;
}
