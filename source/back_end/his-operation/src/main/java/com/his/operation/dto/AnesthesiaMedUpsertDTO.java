package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 追加一条麻醉用药记录。
 */
@Data
public class AnesthesiaMedUpsertDTO implements Serializable {

    /**
     * 麻醉记录ID
     */
    @NotNull(message = "麻醉记录单ID不能为空")
    private Long recordId;

    /**
     * 给药时刻
     */
    @NotNull(message = "给药时刻不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime medTime;

    /**
     * 用药阶段（1-诱导 2-维持 3-苏醒）
     */
    @Min(value = 1, message = "用药阶段取值不合法（应为 1-诱导 2-维持 3-苏醒）")
    @Max(value = 3, message = "用药阶段取值不合法（应为 1-诱导 2-维持 3-苏醒）")
    private Integer medPhase;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    private BigDecimal dose;

    /**
     * 单位
     */
    private String unit;

    /**
     * 给药途径：1-静脉推注 2-静脉泵注 3-静脉滴注 4-吸入 5-肌注 6-椎管内 7-局麻浸润 8-其他
     */
    private Integer route;

    /**
     * 备注
     */
    private String remark;
}
