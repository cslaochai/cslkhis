package com.his.emr.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量执行质控入参（病案室月末批量质控的场景）。
 */
@Data
public class QcBatchExecuteDTO {

    /**
     * 病历来源（OUTPATIENT / INPATIENT）；为空按门诊病历处理
     */
    private String recordSource;

    /**
     * 病历ID列表。前端读接口时拿到的就是字符串（雪花ID字符串化），
     * 这里用 Long 接收由 Jackson 完成字符串→长整型转换，全程不经过 JS 的 Number。
     */
    private List<Long> recordIds;

    /**
     * 质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）
     */
    private Integer qcType;
}
