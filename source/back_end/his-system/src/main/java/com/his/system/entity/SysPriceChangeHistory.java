package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格变更历史表
 *
 * <p>药品、耗材、检查、检验、治疗五类价表的调价留痕统一写这张表，
 * 原来的药品价格变动史只覆盖药品，保留不动。</p>
 */
@Data
@TableName("sys_price_change_history")
public class SysPriceChangeHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗
     */
    private String itemType;

    /**
     * 项目ID
     */
    private Long itemId;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称（冗余）
     */
    private String itemName;

    /**
     * 原价
     */
    private BigDecimal oldPrice;

    /**
     * 新价
     */
    private BigDecimal newPrice;

    /**
     * 调价原因
     */
    private String changeReason;

    /**
     * 操作人ID（员工ID）
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 调价时间
     */
    private LocalDateTime changeTime;
}
