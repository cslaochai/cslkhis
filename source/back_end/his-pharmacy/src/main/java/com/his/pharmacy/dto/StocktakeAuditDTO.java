package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 盘点复核入参
 * <p>pass=true → 差异过账（落库存流水，改批次余额），单据转「已过账」；
 * <br>pass=false → 退回「盘点中」重录实盘数，过账不发生。
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
