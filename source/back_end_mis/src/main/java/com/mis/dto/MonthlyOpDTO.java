package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 月度操作入参（申报/审核/拨付/年清算）
 */
@Data
public class MonthlyOpDTO {

    /** 账期 yyyy-MM；申报/审核/拨付留空则默认当月 */
    @NotBlank(message = "账期 month 不能为空（yyyy-MM）")
    private String month;
}
