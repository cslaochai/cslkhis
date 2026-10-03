package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品供应商退货单出参（列表与详情同形状，详情多带 items/logs）
 */
@Data
public class SupplierReturnVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 退货单号 */
    private String returnNo;

    /** 供应商ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 供应商名称（快照） */
    private String supplierName;

    /** 退货原因（近效期 / 质量问题 / 冷链断链 / 采购让价退货…，必填） */
    private String returnReason;

    /** 原入库单号或采购单号 */
    private String srcRefNo;

    /** 状态（1-待退货 2-已退货 3-已作废） */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 批次数 */
    private Integer totalItems;

    /** 退货合计数量 */
    private BigDecimal totalQuantity;

    /** 退货合计金额（向供应商主张退款的依据） */
    private BigDecimal totalAmount;

    /** 退货经办人 */
    private String returnBy;

    /** 退货时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /** 作废操作人 */
    private String cancelBy;

    /** 作废时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /** 作废原因 */
    private String cancelReason;

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

    /** 明细项集合 */
    private List<SupplierReturnItemVO> items;

    /** 本单落下的库存流水（type=9 退货出库，数量为负） */
    private List<BizDrugStockLogVO> logs;
}
