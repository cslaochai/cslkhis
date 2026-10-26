package com.his.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 听班呼叫到岗应答入参（sql/297）。
 */
@Data
public class DutyCallArriveDTO {

    /** 呼叫单ID */
    @NotNull(message = "呼叫单ID不能为空")
    private Long id;

    /** 到岗说明（可空） */
    private String arriveNote;
}
