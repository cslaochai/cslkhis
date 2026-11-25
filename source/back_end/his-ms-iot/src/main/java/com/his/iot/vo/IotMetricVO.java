package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 单个指标值。设备类型不同、指标集合不同，前端按 {@code key} 取，所以这里用「键值对清单」而不是
 * 一个覆盖 11 类设备的大宽表 VO——后者每加一类设备就得改一次接口契约。
 */
@Data
@AllArgsConstructor
@Schema(description = "遥测指标")
public class IotMetricVO implements Serializable {

    @Schema(description = "指标键（与报文 metrics 的键一致）", example = "rate")
    private String key;

    @Schema(description = "指标中文名", example = "滴速")
    private String label;

    @Schema(description = "单位", example = "滴/分")
    private String unit;

    @Schema(description = "值（统一转成文本，布尔/字符串指标也能原样展示）", example = "62")
    private String value;

    @Schema(description = "数值形态，非数值指标为 null；趋势图直接用它", example = "62.0")
    private Double numeric;

    public static IotMetricVO text(String key, String label, String unit, String value) {
        return new IotMetricVO(key, label, unit, value, null);
    }

    public static IotMetricVO number(String key, String label, String unit, Double numeric) {
        return new IotMetricVO(key, label, unit, numeric == null ? null : trim(numeric), numeric);
    }

    private static String trim(Double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf(v.longValue());
        }
        return String.valueOf(v);
    }
}
