package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 入径动作入参（完成不需要原因；退径 reason 必填且服务端截到列宽）。
 */
@Data
public class EnrollActionDTO implements Serializable {

    @NotNull(message = "入径记录ID不能为空")
    private Long id;

    // 不设 @Size：超长由服务端截到列宽落库
    /**
     * 原因
     */
    private String reason;
}
