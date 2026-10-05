package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 护理交接班摘要入参（G-13）。
 */
@Data
public class NursingHandoverDTO {

    /** 病区ID */
    @NotNull(message = "病区不能为空")
    private Long wardId;

    /** 班次（1-白班 2-小夜班 3-大夜班） */
    @NotNull(message = "班次不能为空")
    @Min(value = 1, message = "班次取值不合法（1-白班 2-小夜班 3-大夜班）")
    @Max(value = 3, message = "班次取值不合法（1-白班 2-小夜班 3-大夜班）")
    private Integer shift;

    /** 交班日期（yyyy-MM-dd，缺省=今天；大夜班窗为该日 00:00-08:00） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shiftDate;
}
