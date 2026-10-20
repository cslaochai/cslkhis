package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金账户出参（L3 台账列表）。
 */
@Data
public class FundAccountListVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账户主体（字典 his_account_owner_type：1-患者 2-住院就诊次）
     */
    private Integer ownerType;

    /**
     * 主体ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;

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
     * 余额
     */
    private BigDecimal balance;

    /**
     * 累计充值
     */
    private BigDecimal totalRecharge;

    /**
     * 累计扣用
     */
    private BigDecimal totalConsume;

    /**
     * 账户状态（1-正常 2-冻结）
     */
    private Integer accountStatus;

    /**
     * 最后一笔流水时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTxnTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
