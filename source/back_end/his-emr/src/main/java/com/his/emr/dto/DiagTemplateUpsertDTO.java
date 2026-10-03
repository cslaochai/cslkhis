package com.his.emr.dto;

import lombok.Data;

import java.util.List;

/**
 * 常用诊断模板批量保存入参
 */
@Data
public class DiagTemplateUpsertDTO {
    /**
     * 常用诊断模板列表
     */
    private List<BizDiagTemplateUpsertDTO> templates;
}
