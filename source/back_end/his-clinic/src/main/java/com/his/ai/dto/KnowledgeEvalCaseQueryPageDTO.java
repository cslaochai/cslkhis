package com.his.ai.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评测用例分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeEvalCaseQueryPageDTO extends PageParam {

    /**
     * 问题模糊
     */
    private String question;
}
