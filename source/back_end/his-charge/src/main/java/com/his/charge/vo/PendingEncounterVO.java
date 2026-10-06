package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收费台首屏的一行 = <b>一次就诊</b>（不是一个患者、也不是一张收费单）。
 *
 * <p>四层之后"欠多少"由两层各自回答，所以这里两列并存、各说各的：
 * {@code pendingFeeAmount} 是 L1 里还没出账的应收净额（开完单没结算的部分），
 * {@code unpaidBillAmount} 是 L2 里已出账但钱没收齐的账单差额。
 * 合成一个数字就会看不出"该先出账还是该收钱"。
 */
@Data
public class PendingEncounterVO {

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 就诊类型（1-门诊 2-住院）
     */
    private Integer encounterType;

    private String encounterNo;

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
     * 待出账的记账行数
     */
    private Long pendingFeeCount;

    /**
     * 待出账应收净额（含红冲负行）
     */
    private BigDecimal pendingFeeAmount;

    /**
     * 已出账但未收齐的账单张数
     */
    private Long unpaidBillCount;

    /**
     * 已出账但未收齐的应缴差额
     */
    private BigDecimal unpaidBillAmount;

    /**
     * 最近一次记账/出账时间（排序用，SQL 里已格式化，避免各语言再解释一遍日期）
     */
    private String lastTime;
}
