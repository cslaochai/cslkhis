package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发药核销入参（发药窗口扫追溯码 → 绑定到已发药记录）
 */
@Data
public class DrugTraceDispenseDTO {

    /** 追溯码原文 */
    @NotBlank(message = "追溯码不能为空")
    private String traceCode;

    /** 发药单ID（药品发药记录主键，跨模块快照） */
    @NotNull(message = "必须选择发药记录")
    private Long dispensingId;
}
