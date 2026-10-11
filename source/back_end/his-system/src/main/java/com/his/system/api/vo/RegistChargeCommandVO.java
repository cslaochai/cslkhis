package com.his.system.api.vo;

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
}