package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 患者联系方式表 (患者联系方式)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_contact")
public class BizPatientContact extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 患者ID（关联患者主表）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 与患者关系（码值，字典患者关系字典：1-本人 2-配偶 3-父亲 … 99-其他）
     *
     * <p>⚠ 这里必须是 Integer，不能是 String。表列 {@code relationship} 是 {@code tinyint NOT NULL}，
     * 而患者基本信息.contact_relation 是 varchar 存**标签文本**。两者一旦用同一个 Java 类型接，
     * 建档时写进来的「配偶」会被 MySQL 按隐式转换存成 0（或严格模式下直接报 1366），
     * 而且**不抛异常、不回滚**，页面上只会看到一个莫名其妙的关系。实测库里那三条
     * {@code contact_relation='2'} 的档案就是这么来的（把码值当标签填进了主档）。
     */
    private Integer relationship;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 是否主要联系人（0-否 1-是）
     */
    private Integer isPrimary;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}