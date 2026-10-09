package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 室间质评批次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_eqa_plan")
public class BizLisEqaPlan extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 质评批次号
     */
    private String planNo;

    /**
     * 质评年度
     */
    private Integer planYear;

    /**
     * 本年度第几批（1-上半年 2-下半年）
     */
    private Integer batchNo;

    /**
     * 组织方（国家/省/市临床检验中心或第三方质评机构）
     */
    private String orgName;

    /**
     * 质评计划名称
     */
    private String planName;

    /**
     * 本次参加项目数
     */
    private Integer itemCount;

    /**
     * 本次下发盲样数
     */
    private Integer sampleCount;

    /**
     * 盲样接收日期
     */
    private LocalDate receiveDate;

    /**
     * 盲样接收人
     */
    private String receiveBy;

    /**
     * 结果上报截止日（超期上报会记 overdue_flag，不拦，因为数据还是要寄出去）
     */
    private LocalDate reportDeadline;

    /**
     * 成绩回报日期
     */
    private LocalDate returnDate;

    /**
     * 批次状态（1-待收样 2-检测中 3-已上报 4-已回报 5-已归档）
     */
    private Integer status;

    /**
     * PT 得分（合格项数 / 已回报项数 × 100，服务端算）
     */
    private BigDecimal ptScore;

    /**
     * 1-合格 0-不合格（PT ≥ 80%）
     */
    private Integer passFlag;

    /**
     * 判定为「不合格」的盲样项数
     */
    private Integer failCount;

    /**
     * 归档人
     */
    private String archiveBy;

    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;
}
