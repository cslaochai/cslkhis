package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品出参
 */
@Data
public class SysDrugVO {

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 药品编码，唯一
     */
    private String drugCode;

    /**
     * 药品名称（通用名）
     */
    private String drugName;

    /**
     * 药品类型：1-西药 2-中成药 3-中药饮片
     */
    private Integer drugType;

    /**
     * 通用名（英文名/拉丁名）
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
     * 每最小库存单位含多少克（中药饮片换算率）
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
     * 售价，单位：元
     */
    private BigDecimal price;

    /**
     * 成本价，单位：元
     */
    private BigDecimal costPrice;

    /**
     * 零售价，单位：元
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
     * 保质期（月）
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
     * 特殊管理分类：0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品
     * （字典 his_drug_special_flag）
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
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
