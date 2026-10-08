package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 麻醉随访分页查询入参（命名遵循 AGENTS.md：分页查询用 xxxQueryPageDTO）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaFollowupQueryPageDTO extends PageParam implements Serializable {

    /**
     * 麻醉记录ID（麻醉记录页签里看这台手术的全部随访）
     */
    private Long recordId;

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 状态（0-草稿 1-已完成）
     */
    private Integer followupStatus;

    /**
     * 关键字（随访单号 / 麻醉记录单号 / 患者姓名）
     */
    private String keyword;
}
