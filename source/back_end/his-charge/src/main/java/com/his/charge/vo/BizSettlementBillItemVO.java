package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单行快照出参（L2）：项目名/价格/医保拆分抄自出账那一刻的字典与价格，不随其后变更漂移。
 */
@Data
public class BizSettlementBillItemVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号（快照）
     */
    private String billNo;

    /**
     * 来源记账行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊类型
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 费用归属科室（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 项目类型
     */
    private Integer itemType;

    /**
     * 项目编码（快照）
     */
    private String itemCode;

    /**
     * 项目名称（快照）
     */
    private String itemName;

    /**
     * 规格（快照）
     */
    private String specification;

    /**
     * 单位（快照）
     */
    private String unit;

    /**
     * 单价（快照）
     */
    private BigDecimal price;

    /**
     * 数量（快照）
     */
    private BigDecimal quantity;

    /**
     * 应收金额
     */
    private BigDecimal amount;

    /**
     * 行级分摊优惠
     */
    private BigDecimal discountAmount;

    /**
     * 行级医保统筹
     */
    private BigDecimal poolAmount;

    /**
     * 行级医保个账
     */
    private BigDecimal accountAmount;

    /**
     * 行级个人自付
     */
    private BigDecimal selfAmount;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
     */
    private Integer catalogType;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
