package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 抗菌药物别名维护（住院医嘱名 → 药品目录的精确匹配键） */
@Data
public class AntibioticAliasUpsertDTO {

    /** id 为空=新增；非空=改指向的药品 */
    private Long id;

    /** 药品ID */
    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    /** 别名 */
    @NotBlank(message = "别名不能为空")
    private String aliasName;

    /** 备注 */
    private String remark;
}
