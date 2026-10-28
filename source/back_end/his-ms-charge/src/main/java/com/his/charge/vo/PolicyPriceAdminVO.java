package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 政策价管理行出参（价格管理页「政策价」页签）：政策级属性与档位价格行拼平。
 */
@Data
public class PolicyPriceAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 政策主键 */
    private Long policyId;

    /** 政策编码 */
    private String policyCode;

    /** 政策项目名称（物价目录名） */
    private String policyName;

    /** 院内费用行项目编码（ZCF/JZCF） */
    private String itemCode;

    /** 院内费用行项目名称 */
    private String itemName;

    /** 计价单位 */
    private String unit;

    /** 支付分类（3-丙类） */
    private Integer catalogType;

    /** 国家医保编码说明 */
    private String ybNote;

    /** 政策状态：0-停用 1-启用 */
    private Integer policyStatus;

    /** 政策备注 */
    private String policyRemark;

    /** 价目行主键 */
    private Long priceId;

    /** 档位码（1-普通 2-副主任 3-主任 4-知名专家） */
    private Integer tierCode;

    /** 档位名称 */
    private String tierLabel;

    /** 国家医保编码 */
    private String ybCode;

    /** 全额（元） */
    private BigDecimal amount;

    /** 加收额（元），无加收为 null */
    private BigDecimal surcharge;

    /** 价目行状态：0-停用 1-启用 */
    private Integer status;

    /** 价目行备注 */
    private String remark;
}
