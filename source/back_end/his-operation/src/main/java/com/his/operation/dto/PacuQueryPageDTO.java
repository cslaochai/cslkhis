package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * PACU 复苏记录分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PacuQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 手术申请单ID
     */
    private Long applyId;

    /**
     * 麻醉记录ID
     */
    private Long recordId;

    /**
     * 状态：0-在室 1-已出室
     */
    private Integer status;

    /**
     * 关键字
     */
    private String keyword;

    private Integer unchargedOnly;
}
