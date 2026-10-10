package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 并发症合并症（CC/MCC）目录一行（分组方案快照加载用）。
 */
@Data
public class CcMccRowVO implements Serializable {

    /**
     * 诊断编码
     */
    private String icdCode;

    /**
     * 级别（MCC-严重并发症合并症 CC-并发症合并症）
     */
    private String ccLevel;

    /**
     * 排除组编号：主诊断落进该组时，本条诊断不再算并发症合并症
     */
    private String exclGroup;
}
