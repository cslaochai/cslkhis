package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 全院总值班区间规则校验入参（连续值班 / 值班频次 / 主班漏排）。
 * 告警明细不阻断保存。
 */
@Data
public class DutyCheckDTO {

    /**
     * 开始日期（含）
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（含）
     */
    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 值班点位ID，空=全部点位
     */
    private Long postId;
}
