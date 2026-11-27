package com.his.iot.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备事件（告警）出参。护士站告警列表只渲染这里的文案字段。
 */
@Data
@Schema(description = "物联网设备事件")
public class IotEventVO implements Serializable {

    @Schema(description = "事件ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "设备编号", example = "FRIDGE-01")
    private String deviceNo;

    @Schema(description = "设备类型", example = "coldchain")
    private String deviceType;

    @Schema(description = "事件码", example = "OUT_OF_AREA")
    private String eventCode;

    @Schema(description = "事件文案", example = "离开授权区域")
    private String eventCodeText;

    @Schema(description = "级别（1-提示 2-警告 3-严重）")
    private Integer level;

    @Schema(description = "级别文案")
    private String levelText;

    @Schema(description = "事件描述")
    private String detail;

    /** 设备随事件带上的结构化明细（JSON 字符串）：任务单号、货物、箱号、签收人、等梯秒数等 */
    @Schema(description = "结构化明细（JSON 字符串），无则为空")
    private String dataJson;

    @Schema(description = "事件时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ts;

    @Schema(description = "处理标志（0-未处理 1-已处理 2-已忽略）")
    private Integer handledFlag;

    @Schema(description = "处理状态文案")
    private String handledFlagText;

    @Schema(description = "处理人")
    private String handleBy;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @Schema(description = "处理说明")
    private String handleRemark;
}
