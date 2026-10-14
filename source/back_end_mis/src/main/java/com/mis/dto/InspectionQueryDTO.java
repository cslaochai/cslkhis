package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 飞检任务分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionQueryDTO extends BizPageParam {

    /** 检查类型 */
    private String inspectType;
    /** 状态（草稿/待开展/检查中/已完成） */
    private String status;
}
