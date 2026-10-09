package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 单病种质控病种目录（M4）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_single_disease")
public class SysSingleDisease extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 病种编码
     */
    private String diseaseCode;

    /**
     * 病种名称
     */
    private String diseaseName;

    /**
     * 纳入 ICD-10 前缀（逗号分隔，如 I21,I22）
     */
    private String icd10Prefix;
}
