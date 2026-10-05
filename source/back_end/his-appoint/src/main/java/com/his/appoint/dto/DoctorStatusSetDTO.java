package com.his.appoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置当前登录医生接诊状态入参。
 */
@Data
public class DoctorStatusSetDTO {

    /**
     * 接诊状态：0 恢复接诊 / 2 暂离
     */
    @NotNull(message = "接诊状态不能为空")
    private Integer status;
}
