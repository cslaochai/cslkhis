package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * DRG 排除表查询结果（sys_drg_exclusion 一行）。
 */
@Data
public class DrgExclusionRowVO implements Serializable {

    /**
     * 主诊断编码（ICD-10）
     */
    private String mainDiagCode;

    /**
     * 被排除的 CC/MCC 诊断编码
     */
    private String excludedCode;
}
