package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在院欠费患者榜 VO。
 */
@Data
public class ArrearsPatientVO {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    private String admissionNo;

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
     * 科室名称
     */
    private String deptName;
    /**
     * 诊断
     */
    private String diagnosis;
    /**
     * 入院时间
     */
    private LocalDateTime admitTime;

    /**
     * 住院资金账户余额（L3 账户流水净额，已扣余额抵扣与出院退差；它不等价于「已收」）
     */
    private BigDecimal prepayBalance;

    /**
     * 已发生费用（L1 记账行应收净额，红冲负行一起算）
     */
    private BigDecimal chargedAmount;

    /**
     * 欠费额 = max(0, 已发生 − 已收)，已收 = 净预交 + 账单上直接收的钱
     */
    private BigDecimal arrearsAmount;
}
