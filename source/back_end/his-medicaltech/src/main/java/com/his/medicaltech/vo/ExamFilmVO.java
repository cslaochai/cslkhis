package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 胶片用量出参（sql/138）。
 */
@Data
public class ExamFilmVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 胶片单号
     */
    private String filmNo;

    /**
     * 检查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查记录号
     */
    private String recordNo;

    /**
     * 检查申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

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
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 检查项目编码
     */
    private String itemCode;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 影像模态（，快照）
     */
    private Integer modality;

    private String modalityText;

    /**
     * 胶片规格ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long specId;

    /**
     * 规格编码
     */
    private String specCode;

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 计价单位
     */
    private String unit;

    /**
     * 胶片张数
     */
    private Integer quantity;

    /**
     * 金额（服务端单价×张数现算）
     */
    private BigDecimal amount;

    /**
     * 胶片状态（1-已登记 2-已打印 3-已发放 4-已作废）
     */
    private Integer filmStatus;

    private String filmStatusText;

    /**
     * 是否已记账（0-未记账 1-已记账）
     */
    private Integer chargeFlag;

    /**
     * 记账流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeId;

    /**
     * 记账流水号
     */
    private String feeNo;

    /**
     * 打印人
     */
    private String printBy;

    /**
     * 打印时间
     */
    private LocalDateTime printTime;

    /**
     * 发放人
     */
    private String deliverBy;

    /**
     * 发放时间
     */
    private LocalDateTime deliverTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 顶部统计卡（只有 stats 接口会填这几列）
     */
    @Data
    public static class FilmStats {
        private Integer rowCount;
        private Integer totalQuantity;
        /**
         * 合计金额
         */
        private BigDecimal totalAmount;
        private BigDecimal chargedAmount;
    }
}
