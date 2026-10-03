package com.his.emr.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 处方模板保存入参
 */
@Data
public class BizRxTemplateUpsertDTO {
    /**
     * 模板ID，新增时为空
     */
    private Long id;

    /**
     * 所属医生ID（后端以当前登录用户覆盖）
     */
    private Long doctorId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 模板合计金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 处方模板明细列表
     */
    private List<BizRxTemplateDetailUpsertDTO> details;
}
