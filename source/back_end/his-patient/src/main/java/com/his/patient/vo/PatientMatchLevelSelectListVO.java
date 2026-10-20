package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 匹配级别字典项（供前端渲染筛选项与"能不能直接合并"的判断）。
 */
@Data
public class PatientMatchLevelSelectListVO implements Serializable {

    /**
     * 级别码
     */
    private Integer code;

    /**
     * 级别文案
     */
    private String text;

    /**
     * 是否强依据
     */
    private Boolean strong;

    /**
     * 合并理由长度下限
     */
    private Integer minReasonLength;
}
