package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 病案借阅/复印申请入参
 */
@Data
public class ArchiveBorrowApplyDTO {

    /** 归档记录病历归档的ID */
    @NotNull(message = "归档记录不能为空")
    private Long archiveId;

    /** 类型（1-借阅 2-复印） */
    @NotNull(message = "请选择借阅或复印")
    private Integer borrowType;

    /** 借阅/复印用途（病历讨论/医保核查/司法取证/科研等） */
    @NotBlank(message = "请填写用途")
    private String purpose;

    /** 应归还日期（借阅必填） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectReturnDate;
}
