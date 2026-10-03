package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单病种质控病种目录（M4）。
 *
 * <p>纳入规则：ICD-10 前缀（逗号分隔）——出院首页主要诊断编码命中前缀即纳入。
 * 单病种质控目录.disease_code 唯一键不含 del_flag → 删除走物理删（纯配置表无留档价值）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_single_disease")
public class SysSingleDisease extends BaseEntity {

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 纳入 ICD-10 前缀（逗号分隔，如 I21,I22） */
    private String icd10Prefix;
}
