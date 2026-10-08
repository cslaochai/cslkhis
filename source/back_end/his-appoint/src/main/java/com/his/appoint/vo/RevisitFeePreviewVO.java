package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 复诊费用预估出参
 */
@Data
public class RevisitFeePreviewVO {

    /**
     * 拟就诊日期（选了排班取排班日；当日回诊取今天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 挂号费（已按策略减免后的应收）
     */
    private BigDecimal registFee;

    /**
     * 诊查费（已按策略减免后的应收）
     */
    private BigDecimal diagnosisFee;

    /**
     * 合计应收
     */
    private BigDecimal totalFee;

    /**
     * 是否整单免收（合计为 0，挂号后无需缴费即可签到）
     */
    private Boolean waived;

    /**
     * 命中的收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）
     */
    private Integer chargeMode;

    /**
     * 命中的策略名称（未命中任何策略时为空，表示按全额收费兜底）
     */
    private String policyName;

    /**
     * 判定说明
     */
    private String reason;
}
