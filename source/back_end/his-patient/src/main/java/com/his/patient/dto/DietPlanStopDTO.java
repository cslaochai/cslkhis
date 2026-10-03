package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 手工停止膳食方案（营养师侧）。
 *
 * <p>医嘱链上的停嘱会自动停方案，这里服务的是"医嘱没停但临床上不吃了"
 * （患者拒食、转出病区、临时检查禁食）。停方案不会自动退已经生成的餐 ——
 * 已送出去的饭是既成事实，退订要走订餐状态机并写原因。
 */
@Data
public class DietPlanStopDTO {

    @NotNull(message = "膳食方案ID不能为空")
    private Long id;

    /** 停止时间，为空取当前时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopTime;

    /** 停止原因（必填，落 remark） */
    @NotBlank(message = "停餐必须填写原因（拒食/检查禁食/转出病区等，无原因的停餐在病历上说不通）")
    private String reason;
}
