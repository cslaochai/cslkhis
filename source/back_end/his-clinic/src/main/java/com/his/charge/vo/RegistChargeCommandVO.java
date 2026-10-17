package com.his.charge.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 挂号费记账出账命令
 */
@Data
@NoArgsConstructor
public class RegistChargeCommandVO {
    private Long registId;
    private String registNo;
    private Long patientId;
    private String patientNo;
    private String patientName;
    private Long deptId;
    private String deptName;
    private Long doctorId;
    private String doctorName;
    private BigDecimal registFee;
    private BigDecimal diagnosisFee;
    private boolean waived;
    private String waiveReason;

    /**
     * 本单结算方式（与收费账单 settlement_mode 同口径）：1-全自费 2-医保联网。
     * 由挂号单 settlement_type 透传，允许医保患者本单改自费；不传时收费侧按患者费别/医保号推断。
     */
    private Integer settlementMode;
}