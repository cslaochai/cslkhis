package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 药品追溯码采集入参（入库验收扫码 / 存量补采）
 */
@Data
public class DrugTraceCollectDTO {

    /** 追溯码原文 */
    @NotBlank(message = "追溯码不能为空")
    private String traceCode;

    /** 药品ID（解析未命中时必填，人工指定） */
    private Long drugId;

    /** 挂靠库存批次ID（药品批次库存主键） */
    @NotNull(message = "必须选择挂靠批次")
    private Long stockId;

    /** 来源入库单ID（入库采集时带上，便于按单核对） */
    private Long inboundId;

    /** 采集来源（1-入库采集 2-存量补采），默认 1 */
    private Integer sourceType = 1;

    /** 备注 */
    private String remark;
}
