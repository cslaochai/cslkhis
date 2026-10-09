package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CC/MCC 目录查询结果（sys_drg_ccmcc 一行）。
 */
@Data
public class CcMccRowVO implements Serializable {

    /**
     * 诊断编码（ICD-10）
     */
    private String icdCode;

    /**
     * 级别（MCC-严重并发症合并症 CC-并发症合并症 NONE-无）
     */
    private String ccLevel;

    /**
     * 分组方案版本
     */
    private String version;
}
