package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 一台监护仪最近一段波形窗。曲线只在内存缓冲，不落时序库；数值（心率/血压/血氧等）
 * 仍走最新遥测接口，本对象只带画图所需的三通道采样点与最新判读。
 */
@Data
@Schema(description = "监护波形窗")
public class IotWaveformVO implements Serializable {

    @Schema(description = "设备编号", example = "MON-01")
    private String deviceNo;

    @Schema(description = "心律判读码", example = "SINUS")
    private String rhythm;

    @Schema(description = "最新心率", example = "72")
    private Integer hr;

    @Schema(description = "最新呼吸率", example = "16")
    private Integer rr;

    @Schema(description = "最近一帧时间")
    private String ts;

    @Schema(description = "II 导联心电采样点（最近 4 秒，100Hz）")
    private List<Double> ecg;

    @Schema(description = "血氧脉搏波采样点（最近 4 秒，100Hz）")
    private List<Double> pleth;

    @Schema(description = "胸阻抗呼吸波采样点（最近 8 秒，20Hz）")
    private List<Double> resp;

    @Schema(description = "心电/脉搏波采样率 Hz", example = "100")
    private Integer ecgHz;

    @Schema(description = "呼吸波采样率 Hz", example = "20")
    private Integer waveHz;
}
