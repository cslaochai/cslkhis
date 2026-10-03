package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 单条检验结果入参
 */
@Data
public class LabResultItemSaveDTO {
    /**
     * 检验项目ID
     */
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验结果值
     */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 参考区间
     */
    private String referenceRange;

    /** 异常标志（0-正常 1-偏高 2-偏低 3-异常） */
    private Integer abnormalFlag;

    /**
     * 异常描述
     */
    private String abnormalDesc;
}
