package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收费单信息VO（含明细及处方明细）
 */
@Data
public class ChargeInfoVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 收费单号
     */
    private String chargeNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 收费类型：1-挂号费 2-药品费 3-检查费 4-检验费 5-治疗费 6-综合收费
     */
    private Integer chargeType;

    /**
     * 收费状态：1-待收费 2-已收费 3-已退费 4-部分退费 5-已取消
     */
    private Integer chargeStatus;

    /**
     * 支付方式：1-现金 2-微信 3-支付宝 4-银行卡 5-医保
     */
    private Integer paymentMethod;

    /**
     * 应收总金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 实收金额，单位：元
     */
    private BigDecimal actualAmount;

    /**
     * 退费金额，单位：元
     */
    private BigDecimal refundAmount;

    /**
     * 收费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chargeTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 收费明细列表
     */
    private List<ChargeDetailVO> details;

    /**
     * 收费明细VO
     */
    @Data
    public static class ChargeDetailVO {

        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 项目类型：1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗
         */
        private Integer itemType;

        /**
         * 项目编码
         */
        private String itemCode;

        /**
         * 项目名称
         */
        private String itemName;

        /**
         * 规格
         */
        private String specification;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private BigDecimal quantity;

        /**
         * 单价，单位：元
         */
        private BigDecimal price;

        /**
         * 金额，单位：元
         */
        private BigDecimal amount;

        /**
         * 实收金额，单位：元
         */
        private BigDecimal actualAmount;

        /**
         * 缴费状态：0-未缴费 1-已缴费 2-已退费
         */
        private Integer paymentStatus;

        /**
         * 来源业务ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long sourceId;

        /**
         * 来源业务单号
         */
        private String sourceNo;

        /**
         * 处方类型明细的药品列表
         */
        private List<PrescriptionDetailVO> prescriptionDetails;
    }

    /**
     * 处方药品明细VO
     */
    @Data
    public static class PrescriptionDetailVO {

        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 药品编码
         */
        private String drugCode;

        /**
         * 药品名称
         */
        private String drugName;

        /**
         * 规格
         */
        private String specification;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private BigDecimal quantity;

        /**
         * 单价，单位：元
         */
        private BigDecimal price;

        /**
         * 金额，单位：元
         */
        private BigDecimal amount;

        /**
         * 单次用量
         */
        private String singleDosage;

        /**
         * 频次
         */
        private String frequency;

        /**
         * 给药途径
         */
        private String route;
    }
}
