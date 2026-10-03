package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品入库单出参
 */
@Data
public class DrugInboundVO {

    /** 入库单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 入库单号（唯一） */
    private String inboundNo;

    /** 入库类型（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库） */
    private Integer inboundType;

    /** 来源采购订单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long purchaseOrderId;

    /** 来源采购订单号 */
    private String purchaseOrderNo;

    /** 供应商（文本快照） */
    private String supplier;

    /** 总金额 = Σ明细金额 */
    private BigDecimal totalAmount;

    /** 总数量 = Σ明细数量 */
    private BigDecimal totalQuantity;

    /** 入库状态（1-待审核 2-已审核 3-已入库 4-已取消） */
    private Integer inboundStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 入库人 */
    private String inboundBy;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inboundTime;

    /** 取消人 */
    private String cancelBy;

    /** 取消时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /** 取消原因 */
    private String cancelReason;

    /** 备注 */
    private String remark;

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

    /** 有效明细条数（列表带出） */
    private Integer itemCount;

    /** 入库明细（仅详情接口填充） */
    private List<DrugInboundDetailVO> items;
}
