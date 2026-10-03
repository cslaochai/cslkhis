package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扣款处理留痕（只增表：物理上不删不改，审计要件）。
 *
 * <p>没有 del_flag，所以不继承 BaseEntity —— 一旦带上 @TableLogic，
 * 「按单据捞全过程」会被逻辑删过滤条件悄悄截断，审计就看不到完整链条。
 */
@Data
@TableName("biz_yb_deduct_log")
public class BizYbDeductLog implements Serializable {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 扣款通知ID
     */
    private Long noticeId;

    /**
     * 动作（字典 his_yb_deduct_action：1-新建草稿 2-发起申诉 3-录入申诉结果 4-确认扣款并追责 5-录入缴回 6-作废）
     */
    private Integer action;

    /**
     * 动作详情/备注
     */
    private String detail;

    /**
     * 涉及金额（缴回/分摊时记）
     */
    private BigDecimal amount;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
}
