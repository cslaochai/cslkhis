package com.his.clinical.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病历更正申请台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmrCorrectionPageQueryDTO extends PageParam {

    /**
     * 状态（1-待科室审核 2-待医务审批 3-已批准待修改 4-已驳回 5-已完成 6-已撤回）
     */
    private Integer status;

    /**
     * 更正类型（1-经治医生更正 2-上级代改）
     */
    private Integer correctionType;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 关键词（病历号 / 患者姓名 / 单号）
     */
    private String keyword;
}
