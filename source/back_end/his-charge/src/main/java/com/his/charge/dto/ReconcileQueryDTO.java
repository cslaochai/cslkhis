package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 医保报盘日对账入参
 */
@Data
public class ReconcileQueryDTO {

    /**
     * 账期日（对账口径 = 报文发出日）。只认 yyyy-MM-dd，前端不要传 ISO T 分隔
     */
    @NotNull(message = "账期日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;
}
