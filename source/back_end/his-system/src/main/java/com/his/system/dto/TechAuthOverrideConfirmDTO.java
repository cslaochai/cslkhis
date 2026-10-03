package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 越权登记上级确认入参（事后追认，见 sql/155 的越权授权事后登记）
 */
@Data
public class TechAuthOverrideConfirmDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "越权登记不能为空")
    private Long id;

    /**
     * 确认意见（认可该次越权的理由）
     */
    private String confirmOpinion;
}
