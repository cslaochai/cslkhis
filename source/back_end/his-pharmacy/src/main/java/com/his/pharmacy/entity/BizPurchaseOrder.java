package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单
 *
 * ⚠ 主键列名是 `order_id` 而不是 `id`，理由同 SysSupplier —— 不继承 BaseEntity，审计字段自声明。
 *
 * ⚠ 本表**不存入库状态**：
 *   采购只决定「买什么」；是否到货、验收是否合格，由**药品入库单**记录。
 *   曾短暂加过订单级入库标记(0/1)，已移除 —— 它与药品入库单的入库状态
 *   （走字典 his_inbound_status 的 1-待审核/2-已审核/3-已入库/4-已取消）**同名但码值域完全不同**，
 *   两张表各挂一个「入库状态」必然被前端/报表混用，而字典只能映射一个。
 *   订单「是否已收货」由入库单**派生**（存在已入库〔状态3〕的入库单即已收货），不冗余存储。
 */
@Data
@TableName("biz_purchase_order")
public class BizPurchaseOrder implements Serializable {

    /** 采购订单ID */
    @TableId(value = "order_id", type = IdType.ASSIGN_ID)
    private Long orderId;

    /** 采购订单号（CG+yyyyMMdd+4位序号，唯一） */
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /** 订单总金额（= Σ明细金额，服务端重算，不信前端传值） */
    private BigDecimal totalAmount;

    /** 审批状态（0-待审批 1-已通过 2-已驳回） */
    private Integer approvalStatus;

    /** 审批人ID（工号） */
    private Long approverId;

    /** 备注 */
    private String remark;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableLogic
    private Integer delFlag;
}
