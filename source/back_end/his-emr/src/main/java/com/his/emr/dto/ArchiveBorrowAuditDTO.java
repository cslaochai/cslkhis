package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病案借阅/复印审核入参
 */
@Data
public class ArchiveBorrowAuditDTO {

    /**
     * 单据ID
     */
    @NotNull(message = "单据ID不能为空")
    private Long id;

    /**
     * true 通过 / false 拒绝
     */
    @NotNull(message = "请选择审核结论")
    private Boolean approve;

    /**
     * 审核意见（拒绝必填）
     */
    private String remark;
}
