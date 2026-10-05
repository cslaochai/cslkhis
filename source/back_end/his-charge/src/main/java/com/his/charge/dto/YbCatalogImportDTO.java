package com.his.charge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 医保目录批量导入（按 yb_code 幂等，存在即更新）。
 */
@Data
public class YbCatalogImportDTO {

    /**
     * 导入明细
     */
    @NotEmpty(message = "导入明细不能为空")
    @Valid
    private List<YbCatalogUpsertDTO> items;
}
