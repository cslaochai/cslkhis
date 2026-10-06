package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊慢特病病种目录（参照数据：启用/禁用走 status，不删）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_chronic_catalog")
public class BizYbChronicCatalog extends BaseEntity {

    /**
     * 病种编码（MZ/MT+序号）
     */
    private String diseaseCode;

    /**
     * 病种名称
     */
    private String diseaseName;

    /**
     * 类别（字典 his_chronic_disease_type：1-慢性病 2-特殊病）
     */
    private Integer diseaseType;

    /**
     * 对应 ICD-10 主码
     */
    private String icdCode;

    /**
     * 默认有效期月数（NULL=长期）
     */
    private Integer defaultValidMonths;

    /**
     * 启用状态（1-启用 0-停用）
     */
    private Integer status;
}
