package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 变异登记入参（追加式台账，仅路径执行中可登记）。
 */
@Data
public class VarianceUpsertDTO implements Serializable {

    /**
     * 入径记录ID
     */
    @NotNull(message = "入径记录不能为空")
    private Long enrollId;

    /**
     * 发生路径日
     */
    @NotNull(message = "路径日不能为空")
    private Integer dayNo;

    /**
     * 变异类型（1-医嘱变动 2-检查检验变动 3-手术操作变动 4-用药变动 5-出院延期 6-其他）
     */
    @NotNull(message = "变异类型不能为空")
    private Integer varianceType;

    // 不设 @Size：超长文本由服务端统一截到列宽落库（AGENTS 铁律），入参层别把它挡成 400
    /**
     * 变异原因
     */
    @NotBlank(message = "变异原因不能为空")
    private String varianceReason;

    /**
     * 处理措施
     */
    private String handling;

    /**
     * 变异发生日期
     */
    @NotNull(message = "发生日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate occurredDate;
}
