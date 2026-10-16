package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import com.his.common.enums.DrugStockChangeTypeEnum;
import com.his.common.enums.StockRoomEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 药品库存流水列表VO（联表药品字典带出药品名称）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizDrugStockLogVO extends BaseEntity {
    /** 备注 */
    private String remark;


    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;


    /**
     * 库存批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 批号
     */
    private String batchNo;

    /**
     * 变动类型（1-入库 2-发药出库 3-退药回库 4-其他出库 5-盘盈 6-盘亏 7-调拨出库 8-调拨入库 9-退货出库）
     */
    private Integer changeType;

    /**
     * 变动类型文字（服务端按 DrugStockChangeTypeEnum 算）
     */
    private String changeTypeText;

    /**
     * 批次所在库存地点（联表药品批次库存带出：1-药库 2-药房，sql/154）
     */
    private Integer stockRoom;

    /**
     * 库存地点文字
     */
    private String stockRoomText;

    /**
     * 变动数量（正=入负=出）
     */
    private BigDecimal changeQuantity;

    /**
     * 变动前批次数量
     */
    private BigDecimal quantityBefore;

    /**
     * 变动后批次数量
     */
    private BigDecimal quantityAfter;

    /**
     * 来源类型（dispensing-发药 dispenseReturn-退药 manual-手工）
     */
    private String sourceType;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 流水文案统一在服务端补齐（变动类型 + 库位）：流水台账分页与各单据详情抽屉共用这一份口径，
     * 前端再抄一次 1/2→药库/药房迟早和枚举漂移。
     */
    public BizDrugStockLogVO fillTexts() {
        this.changeTypeText = DrugStockChangeTypeEnum.getText(this.changeType);
        this.stockRoomText = StockRoomEnum.getText(this.stockRoom);
        return this;
    }
}
