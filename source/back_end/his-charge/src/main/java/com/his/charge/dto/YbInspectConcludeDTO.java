package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 飞检批次结项（结论必填，结项后内容不可再改）。
 */
@Data
public class YbInspectConcludeDTO {

    @NotNull(message = "批次ID不能为空")
    private Long id;

    /**
     * 审核结论
     */
    @NotBlank(message = "结项结论不能为空")
    private String conclusion;
}
