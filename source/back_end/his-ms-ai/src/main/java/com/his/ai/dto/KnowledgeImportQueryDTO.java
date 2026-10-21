package com.his.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 导入任务批量查询入参（前端轮询一组任务状态）。
 */
@Data
public class KnowledgeImportQueryDTO {

    /** 导入任务ID列表（Long 入参） */
    @NotEmpty
    private List<Long> ids;
}
