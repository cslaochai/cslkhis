package com.his.appoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置当前登录医生接诊状态入参。
 * <p>
 * 只允许 0 空闲（恢复接诊）/ 2 暂离 —— 1 接诊中由接诊动作（callNext）自己写，
 * 不允许前端指定，避免前端把状态改成"接诊中"而队列里并没有就诊中患者。
 */
@Data
public class DoctorStatusSetDTO {

    /**
     * 接诊状态：0 恢复接诊 / 2 暂离
     */
    @NotNull(message = "接诊状态不能为空")
    private Integer status;
}
