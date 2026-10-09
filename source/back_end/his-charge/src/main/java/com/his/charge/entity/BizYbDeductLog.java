package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 扣款处理留痕（只增表：物理上不删不改，审计要件）。
 */
@Data
@TableName("biz_yb_deduct_log")
public class BizYbDeductLog implements Serializable {
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 自增主键
     */
    @TableId(type = IdType.ASSIGN_ID)
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
