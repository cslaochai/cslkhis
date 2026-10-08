package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.operation.entity.BizAnesthesiaVital;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 麻醉生命体征出参（额外带上"是否异常"的机器判定）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaVitalVO extends BizAnesthesiaVital {

    private String abnormalText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime sampleTimeLabel;
}
