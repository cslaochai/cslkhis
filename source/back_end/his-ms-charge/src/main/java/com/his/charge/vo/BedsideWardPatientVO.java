package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 床旁结算病区榜行（在院患者 + 钱况 + 结算单状态）。
 *
 * <p>金额口径与欠费榜逐项一致（同一个患者两个页面两个欠费额就等于没有口径）：
 * 已发生 = L1 应收净额；已收 = 净预交 + 账单上直接收的钱（排除余额抵扣）；
 * 欠费 = max(0, 已发生 − 已收)。账单列取最新一张未作废的出院结算单。
 */
@Data
public class BedsideWardPatientVO {

    @Schema(description = "入院ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @Schema(description = "入院单号")
    private String admissionNo;

    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @Schema(description = "患者号")
    private String patientNo;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "性别（字典 his_gender）")
    private Integer gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "床号")
    private String bedNo;

    @Schema(description = "诊断")
    private String diagnosis;

    @Schema(description = "入院时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    @Schema(description = "预交金余额（住院资金账户净额）")
    private BigDecimal prepayBalance;

    @Schema(description = "累计费用（L1 应收净额）")
    private BigDecimal chargedAmount;

    @Schema(description = "欠费额（已发生 − 已收，下限 0）")
    private BigDecimal arrearsAmount;

    @Schema(description = "出院结算账单ID（未出账为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    @Schema(description = "出院结算账单号（未出账为 null）")
    private String billNo;

    @Schema(description = "账单应缴（未出账为 null）")
    private BigDecimal billPayable;

    @Schema(description = "账单已收（未出账为 null）")
    private BigDecimal billPaid;

    @Schema(description = "账单未付清（未出账为 null）")
    private BigDecimal billUnpaid;

    @Schema(description = "账单状态（字典 his_bill_status；未出账为 null）")
    private Integer billStatus;

    @Schema(description = "是否已出账（有未作废的出院结算单）")
    private Boolean settled;
}
