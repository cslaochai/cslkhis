package com.his.ai.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答观测日志分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeQueryLogPageQueryDTO extends PageParam {

    /**
     * 问题模糊
     */
    private String question;

    /**
     * 是否降级（null-全部 0-正常 1-降级）
     */
    private Integer degraded;

    /**
     * 反馈（null-全部 0-未反馈 1-有用 2-无用）
     */
    private Integer feedback;
}
