package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药房盘点单
 * <p>差异以建单时的账面快照为基准，过账把差量加减到当前批次余额上（不是覆盖），
 * 见 sql/127 头注。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stocktake")
public class BizStocktake extends BaseEntity {
    /** 盘点单号 */
    private String stocktakeNo;

    /** 盘点主题 */
    private String stocktakeTitle;

    /** 范围-药品类型（NULL=全部，1-西药 2-中成药 3-中药饮片） */
    private Integer scopeDrugType;

    /** 范围-药品名称/编码/批号关键字（NULL=不限） */
    private String scopeKeyword;

    /** 范围的人读描述（服务端拼） */
    private String scopeDesc;

    /** 账面快照时点 */
    private LocalDateTime snapshotTime;

    /** 状态（1-盘点中 2-待复核 3-已过账 4-已关单） */
    private Integer status;

    /** 参与盘点批次数 */
    private Integer totalItems;

    /** 已录入实盘数批次数 */
    private Integer countedItems;

    /** 有差异批次数 */
    private Integer diffItems;

    /** 盘盈批次数 */
    private Integer profitItems;

    /** 盘亏批次数 */
    private Integer lossItems;

    /** 净差数量（盈正亏负） */
    private BigDecimal diffQuantity;

    /** 净差金额（按成本价，元） */
    private BigDecimal diffAmount;

    /** 提交人 */
    private String submitBy;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 复核人（即过账操作人） */
    private String auditBy;

    /** 复核时间 */
    private LocalDateTime auditTime;

    /** 复核意见 / 退回原因 */
    private String auditRemark;
}
