package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 药品字典 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drug")
public class SysDrug extends BaseEntity {
    /**
     * 药品编码（唯一）
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 药品类型（1-西药 2-中成药 3-中药饮片）
     */
    private Integer drugType;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 商品名
     */
    private String tradeName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型（片剂、胶囊、注射剂等）
     */
    private String dosageForm;

    /**
     * 单位（片、粒、支等）
     */
    private String unit;

    /**
     * 每最小库存单位含多少克（sql/139 中药饮片换算率：散装 kg=1000、10g/袋包装=10）。
     * <p>为空 = 该药不按克开方（西药/中成药），处方数量即库存数量。
     * 换算一律走 {@code TcmGramUnits}，不许去 parse 规格文本。
     */
    private BigDecimal gramPerUnit;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 批准文号
     */
    private String approvalNumber;

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 药品分类ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    /**
     * 药品分类名称
     */
    private String categoryName;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 是否医保药品（0-否 1-是）
     */
    private Integer isMedicalInsurance;

    /**
     * 医保编码
     */
    private String medicalInsuranceCode;

    /**
     * 储存条件
     */
    private String storageCondition;

    /**
     * 有效期（月）
     */
    private Integer shelfLife;

    /**
     * 是否需要皮试（0-否 1-是）
     */
    private Integer isSkinTest;

    /**
     * 是否冷链药品（0-否 1-是）
     */
    private Integer isColdChain;

    /**
     * 特殊管理分类（0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品）
     * <p>
     * 字典 {@code his_drug_special_flag}。**必须是数据而不是代码常量** ——
     * 管制目录会调整（如 2024-07-01 咪达唑仑原料药与注射剂由第二类升为第一类），
     * 写死在枚举里意味着每次调整都要改代码发版。
     * 该字段决定：处方限量档位、是否需专册登记、是否需双人复核、是否需空安瓿回收。
     */
    private Integer specialFlag;

    /**
     * 禁忌症
     */
    private String contraindication;

    /**
     * 不良反应
     */
    private String adverseReaction;

    /**
     * 用法用量
     */
    private String usageDosage;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
