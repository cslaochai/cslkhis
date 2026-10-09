package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * DRG 分组与权重表（接入医保局的接口面）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drg_group")
public class SysDrgGroup extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * DRG 组编码（如 FR29）
     */
    private String drgCode;

    /**
     * DRG 组名称
     */
    private String drgName;

    /**
     * MDC 主要诊断大类
     */
    private String mdcCode;

    /**
     * ADRG 编码
     */
    private String adrgCode;

    /**
     * 权重（相对权重 RW）
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元）
     */
    private BigDecimal payStandard;

    /**
     * 来源：国家CHS-DRG/省/市版本名称
     */
    private String source;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 分组类型（1-外科 2-操作 3-内科）
     */
    private Integer groupType;

    /**
     * 并发症合并症标志（0-无 1-伴CC 2-伴MCC）
     */
    private Integer ccMccFlag;

    /**
     * 性别限定（0-不限 1-男 2-女）
     */
    private Integer genderLimit;

    /**
     * 年龄分层（0-不限 1-≤6岁 2-≥70岁 3-新生儿）
     */
    private Integer ageTier;

    /**
     * 先期分组标志（0-否 1-是）
     */
    private Integer preGroupFlag;

    /**
     * 手术属性（0-普通 1-单双侧 2-机器人 3-联合）
     */
    private Integer surgeryAttr;

    /**
     * 基层病种标志（0-否 1-是）
     */
    private Integer baseDiseaseFlag;

    /**
     * 主诊断匹配键（ICD-10 亚目前缀，逗号分隔）
     */
    private String diagMatch;

    /**
     * 主手术匹配键（ICD-9-CM-3 前缀，逗号分隔）
     */
    private String operMatch;
}
