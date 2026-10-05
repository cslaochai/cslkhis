package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 三阶段清点登记入参（phase：1-术前 2-关体前 3-关体后）。
 *
 * <p><b>数量必须逐项给全</b>：漏项的阶段不允许提交 ——
 * "止血钳没数但纱布数了"的清点等于没数，缺哪一项都必须补而不是跳过。
 *
 * <p>服务端会把每个明细与术前基数比对，对不上的项会被记进差异里；
 * 这些差异最终决定 {@code OperationApplyServiceImpl#finish()} 是不是被锁死。
 */
@Data
public class CountPhaseDTO implements Serializable {

    @NotNull(message = "清点单ID不能为空")
    private Long countId;

    /**
     * 阶段：1-术前 2-关体前 3-关体后
     */
    @NotNull(message = "清点阶段不能为空")
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
