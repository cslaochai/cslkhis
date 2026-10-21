package com.his.charge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 医保目录报销规则批量导入（按 rule_no 幂等，存在即更新）。
 */
@Data
public class YbCatalogRuleImportBatchDTO {

    /**
     * 导入明细
     */
    @NotEmpty(message = "导入明细不能为空")
    @Valid
    private List<YbCatalogRuleImportDTO> items;
}