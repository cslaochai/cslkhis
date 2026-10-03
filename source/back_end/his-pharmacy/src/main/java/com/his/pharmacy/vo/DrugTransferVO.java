package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品调拨单出参（列表与详情同形状，详情多带 items/logs）
 */
@Data
public class DrugTransferVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 调拨单号 */
    private String transferNo;

    /** 方向（1-药库下拨药房 2-药房退回药库） */
    private Integer transferType;

    /** 方向文案（后端给，前端不抄映射） */
    private String transferTypeText;

    /** 发出库位（1-药库 2-药房） */
    private Integer fromRoom;

    private String fromRoomText;

    /** 接收库位 */
    private Integer toRoom;

    private String toRoomText;

    /** 事由 */
    private String reason;

    /** 状态（1-待发出 2-待接收 3-已完成 4-已作废） */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 批次数 */
    private Integer totalItems;

    /** 申请合计数量 */
    private BigDecimal totalQuantity;

    /** 已发出合计数量 */
    private BigDecimal outQuantity;

    /** 已接收合计数量（=outQuantity 即账平） */
    private BigDecimal inQuantity;

    /** 合计金额（按批次成本价） */
    private BigDecimal totalAmount;

    /** 发出人 */
    private String outBy;

    /** 发出时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime;

    /** 接收人 */
    private String inBy;

    /** 接收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime;

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

    /** 明细（仅详情返回） */
    private List<DrugTransferItemVO> items;

    /**
     * 本单落下的库存流水（仅详情返回）
     * <p>调拨一张单出两行（7 为负 / 8 为正），合计为 0 就说明「搬出去又搬进来」闭环了；
     * 只有一行就是还在途，这一眼是这张单最需要的对账证据。
     */
    private List<BizDrugStockLogVO> logs;
}
