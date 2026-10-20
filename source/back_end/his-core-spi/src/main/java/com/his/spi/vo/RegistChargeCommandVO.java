package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 挂号费出账命令：挂号成功后交给收费域写记账行的事实载体。
 */
@Data
@NoArgsConstructor
public class RegistChargeCommandVO {
    /**
     * 挂号单 id（记账行的临床来源单据）
     */
    private Long registId;
    /**
     * 挂号单号
     */
    private String registNo;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者档案编号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 就诊科室 id
     */
    private Long deptId;
    /**
     * 就诊科室名称
     */
    private String deptName;
    /**
     * 出诊医生 id
     */
    private Long doctorId;
    /**
     * 出诊医生姓名
     */
    private String doctorName;
    /**
     * 挂号费金额（元）
     */
    private BigDecimal registFee;
    /**
     * 职级加收档位（1-普通 2-副主任医师 3-主任医师 4-知名专家）：决定加收子项的院内码与名称。
     * null/1 = 只出主项门诊诊查费，不出加收子项。
     */
    private Integer diagnosisTier;
    /**
     * 主项门诊诊查费金额（普通档全额，如 9；儿童加收后 = 9×1.3）。
     */
    private BigDecimal baseFee;
    /**
     * 职级加收项金额（普通档为 null/0；专家档 = 对应加收额，儿童加收后同步 ×1.3）。
     * 0/null 表示不出加收子项。
     */
    private BigDecimal surchargeFee;
    /**
     * 本单免收（挂号侧已豁免收费）
     */
    private boolean waived;
    /**
     * 免收原因（免收时必填，供对账与追责回看）
     */
    private String waiveReason;

    /**
     * 本单结算方式（与收费账单 settlement_mode 同口径）：1-全自费 2-医保联网。
     * 由挂号单 settlement_type 透传，允许医保患者本单改自费；不传时收费侧按患者费别/医保号推断。
     */
    private Integer settlementMode;
}