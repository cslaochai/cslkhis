package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 药品追溯码扫码解析入参
 *
 * <p>{@code scene} 决定返回哪些校验结论：
 * 1-采集场景（入库验收/存量补采）看「能不能采」；2-核销场景（发药窗口）看「能不能销」，
 * 后者必须带 {@code dispensingId}，否则无从判断串码。
 */
@Data
public class DrugTraceScanDTO {

    /** 追溯码原文（扫码枪整串） */
    @NotBlank(message = "追溯码不能为空")
    private String traceCode;

    /** 场景（1-入库采集 2-发药核销），默认 1 */
    private Integer scene = 1;

    /** 发药单ID（scene=2 时必填，用于串码校验） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;

    /** 人工指定药品ID（码识别不出药品时由窗口选药，采集时按这个药品挂靠） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
}
