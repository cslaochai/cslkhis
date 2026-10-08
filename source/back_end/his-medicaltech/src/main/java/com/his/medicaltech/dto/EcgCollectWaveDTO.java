package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 波形采收入参（设备推送路径，sql/173）。
 */
@Data
public class EcgCollectWaveDTO {

    /**
     * 检查记录ID
     */
    @NotNull(message = "缺少检查记录")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 心电类型（字典 his_ecg_type：1-常规静息心电图 2-24小时动态心电图）
     */
    private Integer ecgType;

    /**
     * 波形数据 JSON（必填；为空请走 /simulateWave）
     */
    private String waveData;

    /**
     * 采集设备号（可空）
     */
    private String deviceNo;
}
