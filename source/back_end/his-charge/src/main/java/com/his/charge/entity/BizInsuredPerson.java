package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 参保台账（医保局参保库镜像）：结算前按医保卡号核验参保状态，未参保/停保拒按医保结算。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insured_person")
public class BizInsuredPerson extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 身份证号 */
    private String idCard;

    /** 医保卡号 */
    private String insuranceNo;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 险种（城镇职工医保/城乡居民医保/公费医疗） */
    private String insuranceType;

    /** 参保状态（1-正常参保 0-停保） */
    private Integer personStatus;

    /** 个账余额（元） */
    private BigDecimal accountBalance;

    /** 参保地区 */
    private String region;
}
