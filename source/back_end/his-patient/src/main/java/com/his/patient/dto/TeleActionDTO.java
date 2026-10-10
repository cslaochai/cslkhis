package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 远程会诊动作入参（完成 / 取消 / 线上退诊）。
 */
@Data
public class TeleActionDTO implements Serializable {

    @NotNull(message = "会诊单ID不能为空")
    private Long id;

    /**
     * 会诊意见（完成）/ 取消原因（取消）/ 退诊原因（退诊）——三个动作接口都要留字，一律必填
     */
    @NotBlank(message = "会诊意见/取消原因/退诊原因必填")
    private String content;
}
