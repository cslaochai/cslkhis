package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 法定传染病目录（报卡病种）：国标清单节选，报卡时限挂在病种行上。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_infectious_disease")
public class SysInfectiousDisease extends BaseEntity {

    /** 病种编码（FD001~） */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 传染病类别（1甲类/2乙类/3丙类） */
    private Integer infectiousClass;

    /** 报卡时限（小时，甲类2/乙丙24） */
    private Integer deadlineHours;

    /** 参考 ICD-10 编码 */
    private String icd10;

    /** 状态（1启用/0停用） */
    private Integer status;
}
