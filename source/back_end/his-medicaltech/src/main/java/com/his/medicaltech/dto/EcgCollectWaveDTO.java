package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 波形采收入参（设备推送路径，sql/173）。
 *
 * <p>{@code waveData} 是设备导出的 12 导联采样 JSON：
 * <pre>{sampleRate:250, durationSec:10, gainMmPerMv:10, paperSpeedMmPerS:25,
 *       leads:[{name:"I", samples:[...]}, ...], rhythm:{name:"II", samples:[...]}}</pre>
 * 联调/演示路径走 /simulateWave（服务端合成），两者落到同一张表、同一套模型。
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
