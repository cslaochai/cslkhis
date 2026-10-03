package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药房盘点单出参（列表与详情同形状，详情多带 items/logs）
 */
@Data
public class StocktakeVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 盘点单号 */
    private String stocktakeNo;

    /** 盘点主题 */
    private String stocktakeTitle;

    /** 范围-药品类型（1-西药 2-中成药 3-中药饮片） */
    private Integer scopeDrugType;

    /** 范围-药品名称关键字 */
    private String scopeKeyword;

    /** 范围的人读描述 */
    private String scopeDesc;

    /** 账面快照时点 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime snapshotTime;

    /** 状态（1-盘点中 2-待复核 3-已过账 4-已关单） */
    private Integer status;

    /** 状态文案（后端给，前端不自己翻译） */
    private String statusText;

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

    /** 净差金额（元） */
    private BigDecimal diffAmount;

    /** 提交人 */
    private String submitBy;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /** 复核人 */
    private String auditBy;

    /** 复核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 复核意见 / 退回原因 */
    private String auditRemark;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 明细（仅详情返回） */
    private List<StocktakeItemVO> items;

    /** 本次过账落下的库存流水（仅详情返回，账实差异的可追溯证据） */
    private List<BizDrugStockLogVO> logs;
}
