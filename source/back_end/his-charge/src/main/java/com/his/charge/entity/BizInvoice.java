package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体（L4 票据）。
 *
 * <p>四层口径下一张票对一张结算账单（{@code billId}），票面金额 = 开票时该账单的净已收。
 * {@code chargeId} 是旧模型遗留，只有历史票有值，新票一律为空。
 */
@Data
@TableName("biz_invoice")
public class BizInvoice {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发票号
     */
    private String invoiceNo;
    /**
     * 发票类型（1-普通发票 2-电子发票 3-数电发票）
     */
    private Integer invoiceType;

    /**
     * 旧收费单ID（历史票遗留，四层新票为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chargeId;
    /**
     * 收费单号
     */
    private String chargeNo;

    /**
     * 结算账单ID（L4：票据与对账层对齐到 L2 账单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;
    /**
     * 结算账单号（快照）
     */
    private String billNo;

    /**
     * 红冲链：本票冲销的原发票ID（换开时新票指向作废票）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origInvoiceId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 发票金额
     */
    private BigDecimal totalAmount;
    /**
     * 发票状态（1-已开具 2-已打印 3-已作废 4-已红冲换开）
     */
    private Integer invoiceStatus;
    /**
     * 开票时间
     */
    private LocalDateTime invoiceTime;
    /**
     * 打印时间
     */
    private LocalDateTime printTime;
    /**
     * 作废时间
     */
    private LocalDateTime voidTime;
    /**
     * 作废原因
     */
    private String voidReason;
    /**
     * 电子发票地址
     */
    private String electronicUrl;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
