package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 盘点复核入参
 */
@Data
public class StocktakeAuditDTO {

    @NotNull(message = "盘点单ID不能为空")
    private Long id;

    @NotNull(message = "复核结论不能为空")
    private Boolean pass;

    /** 复核意见（退回时就是退回原因；服务端截到列宽 500） */
    private String remark;
}
