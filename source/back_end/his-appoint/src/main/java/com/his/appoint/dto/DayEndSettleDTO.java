package com.his.appoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 日终结转入参。
 */
@Data
@Schema(description = "日终结转入参")
public class DayEndSettleDTO {

    @Schema(description = "要结转的就诊日（yyyy-MM-dd）。不传=补跑从最早遗留日到昨天")
    private LocalDate settleDate;

    @Schema(description = "只试算不落库（true 时返回将要影响的条数）。默认 false")
    private Boolean dryRun;
}
