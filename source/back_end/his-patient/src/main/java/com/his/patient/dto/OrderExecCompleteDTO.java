package com.his.patient.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医嘱执行入参（支持批量）。
 */
@Data
public class OrderExecCompleteDTO implements Serializable {

    /**
     * 执行记录ID列表（≥1 条）
     */
    private List<Long> execIds;

    /**
     * 执行状态（本端点只接受 2-已执行 3-已跳过；1-待执行 4-已退回由其他动作推进）
     */
    @Min(value = 2, message = "执行结果取值不合法（应为 2-已执行 3-已跳过）")
    @Max(value = 3, message = "执行结果取值不合法（应为 2-已执行 3-已跳过）")
    private Integer execStatus;

    /**
     * 执行备注 / 跳过原因（跳过时必填）
     */
    private String execNote;

    /**
     * 实际执行时间（不传取当前时间；允许护士补录几分钟前刚做完的执行）
     */
    private LocalDateTime execTime;
}
