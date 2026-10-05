package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 开方前越权自检入参（医生站批量勾选药品）。
 */
@Data
public class AntibioticAuthCheckQueryDTO {

    /**
     * 药品ID集合
     */
    @NotEmpty(message = "药品ID不能为空")
    private List<Long> drugIds;
}
