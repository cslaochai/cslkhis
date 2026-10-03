package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病历归档查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 归档状态：1-待归档 2-已归档 3-已封存
     */
    private Integer archiveStatus;

    /**
     * 关键词（病历号/患者姓名模糊；病案借阅申请的病案选择器用）
     */
    private String keyword;
}
