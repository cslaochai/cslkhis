package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收费政策价定义（诊查费等政策类项目）。
 *
 * <p>价格权威在价表：政策级属性（院内费用行编码/名称/支付分类）在本表，
 * 档位价格在 {@link SysChargePolicyPrice}。运行时取价走
 * {@code ChargeFacadeService.policyPrice}，调价走价格管理页「政策价」页签。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_charge_policy")
public class SysChargePolicy extends BaseEntity {

    /** 政策编码（OPD_CONSULT-门诊诊查费 EMG_CONSULT-急诊诊查费），业务唯一键 */
    private String policyCode;

    /** 政策项目名称（物价目录名） */
    private String policyName;

    /** 院内费用行项目编码（biz_fee_record.item_code，如 ZCF/JZCF） */
    private String itemCode;

    /** 院内费用行项目名称 */
    private String itemName;

    /** 计价单位 */
    private String unit;

    /** 支付分类（FeeCatalogTypeEnum：3-丙类，湘医保发〔2025〕58号诊查类全自费） */
    private Integer catalogType;

    /** 国家医保编码说明（档位子码见价目行 yb_code） */
    private String ybNote;

    /** 状态：0-停用 1-启用（停用后运行时按缺行降级） */
    private Integer status;

    /** 备注 */
    private String remark;
}
