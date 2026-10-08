package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟采出入参（sql/173）：设备对接就位前的联调/演示入口。
 */
@Data
public class EcgSimulateDTO {

    @NotNull(message = "缺少检查记录")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 节律（字典 his_ecg_rhythm：1-窦性心律 2-窦速 3-窦缓 4-房颤 5-室早）
     */
    @NotNull(message = "请选择节律")
    @Min(value = 1, message = "节律取值 1~5")
    @Max(value = 5, message = "节律取值 1~5")
    private Integer rhythmCode;
}
