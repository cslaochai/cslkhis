package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分组规则引用的码集合一行（集合编号 → 精确 ICD 码，规则里的匹配键事实）。
 */
@Data
public class DrgSetRowVO implements Serializable {

    /**
     * 集合编号（规则原文 in 右侧引用的名字，含 CC/MCC 排除组 EX_ 前缀）
     */
    private String setCode;

    /**
     * ICD 编码（落库前已按官方贯标写法归一比较）
     */
    private String icdCode;
}
