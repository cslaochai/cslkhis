package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 就诊人绑定表（就诊人绑定）：一个登录账号（user_type=3）可绑多个就诊人。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_guardian")
public class BizPatientGuardian extends BaseEntity {

    /** 登录账号ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /** 就诊人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 与账号所有人关系（码值，字典患者关系字典：1-本人 2-配偶 3-父亲 … 99-其他）
     */
    private Integer relation;

    /** 是否默认就诊人（0-否 1-是） */
    private Integer isDefault;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
