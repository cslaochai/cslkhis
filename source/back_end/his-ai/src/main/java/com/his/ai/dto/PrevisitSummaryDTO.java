package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预问诊病史摘要入参（患者端，按挂号取已提交问卷生成摘要）
 */
@Data
public class PrevisitSummaryDTO {

    /**
     * 挂号ID
     */
    @NotNull(message = "挂号ID不能为空")
    private Long registId;
}
