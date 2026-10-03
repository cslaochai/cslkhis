package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 技术授权收回入参（动态调整：年度再授权未通过、事件触发、转岗）
 */
@Data
public class TechAuthRevokeDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "授权记录不能为空")
    private Long id;

    /**
     * 收回原因（必填：不留原因等于把「为什么突然不能做四级手术」这个事实丢掉）
     */
    @NotBlank(message = "收回原因不能为空")
    private String revokeReason;
}
