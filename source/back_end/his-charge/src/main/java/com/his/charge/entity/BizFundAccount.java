package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金账户（L3）：门诊余额挂在「人」上、住院预交金挂在「这次入院」上，
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_fund_account")
public class BizFundAccount extends BaseEntity {

    /**
     * 账户主体，字典 {@code his_account_owner_type}：1-患者 2-住院就诊次
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
     * 余额。权威值永远是 SUM(资金账户流水的金额)，
     * 本列只是并发受控的缓存，两者对不上以流水为准。
     */
    private BigDecimal balance;

    /**
     * 乐观锁版本号（每次余额变动 +1）
     */
    private Long version;

    /**
     * 累计充值
     */
    private BigDecimal totalRecharge;

    /**
     * 累计扣用
     */
    private BigDecimal totalConsume;

    /**
     * 账户状态：1-正常 2-冻结（冻结后不收不抵）
     */
    private Integer accountStatus;

    /**
     * 最后一笔流水时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTxnTime;
}
