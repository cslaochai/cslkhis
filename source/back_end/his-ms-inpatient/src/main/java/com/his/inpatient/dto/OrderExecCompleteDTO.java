package com.his.inpatient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotEmpty(message = "请选择要处理的执行记录")
    private List<Long> execIds;

    /**
     * 执行状态（本端点只接受 2-已执行 3-已跳过；1-待执行 4-已退回由其他动作推进）
     * 必填：不允许为空，更不能在后端兜底成「已执行」——否则漏传会静默触发计费
     */
    @NotNull(message = "请选择执行结果（已执行 / 已跳过）")
    @AllowedValues(values = { "2", "3"}, message = "执行结果取值不合法")
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
