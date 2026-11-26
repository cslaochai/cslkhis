package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 签名笔迹维护入参：只更新员工档案上的签名笔迹（base64 PNG），不动其他字段。
 */
@Data
public class EmployeeSignUpsertDTO {

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 签名笔迹（base64 PNG；空串 = 清除已维护笔迹）
     */
    private String signImage;
}
