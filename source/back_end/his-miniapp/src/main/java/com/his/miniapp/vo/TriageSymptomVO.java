package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能导诊常见症状（患者端快捷标签）。
 */
@Data
public class TriageSymptomVO implements Serializable {

    /**
     * 症状编码
     */
    private String symptomCode;

    /**
     * 症状名称
     */
    private String symptomName;
}
