package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 麻醉记录单分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaRecordQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 手术申请单ID
     */
    private Long applyId;

    /**
     * 麻醉医师ID（员工ID）
     */
    private Long anesthetistId;

    /**
     * 记录状态（0-记录中 1-已提交 2-已审核）
     */
    private Integer recordStatus;

    /**
     * 关键字（麻醉单号 / 申请单号 / 患者姓名 / 术式）
     */
    private String keyword;

    /**
     * 只看尚未计费：手术室每天对账用（有账没计上的必须能筛出来）
     */
    private Integer unchargedOnly;
}
