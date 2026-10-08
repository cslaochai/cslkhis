package com.his.operation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 三阶段清点登记入参（phase：1-术前 2-关体前 3-关体后）。
 */
@Data
public class CountPhaseDTO implements Serializable {

    @NotNull(message = "清点单ID不能为空")
    private Long countId;

    /**
     * 阶段：1-术前 2-关体前 3-关体后
     */
    @NotNull(message = "清点阶段不能为空")
    @Min(value = 1, message = "清点阶段取值不合法（应为 1-术前 2-关体前 3-关体后）")
    @Max(value = 3, message = "清点阶段取值不合法（应为 1-术前 2-关体前 3-关体后）")
    private Integer phase;

    /**
     * 核对人（员工ID，姓名服务端查）——出事后要能回答"当时是谁数的"
     */
    @NotNull(message = "核对人不能为空（清点必须留名）")
    private Long nurseId;

    /**
     * 各项数量；key = 明细ID，value = 该阶段数量
     */
    private List<CountQtyDTO> quantities;

    /**
     * 差异说明（任一项对不上时必填）
     */
    private String diffNote;

    /**
     * 备注
     */
    private String remark;
}
