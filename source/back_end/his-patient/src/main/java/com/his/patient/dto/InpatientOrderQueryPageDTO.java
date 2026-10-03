package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院医嘱分页查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientOrderQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    /**
     * 医嘱状态：1-待校对 2-已校对 3-执行中 4-已完成 5-已停止 6-已作废 7-已退回
     */
    private Integer orderStatus;

    /**
     * 医嘱类别
     */
    private Integer orderClass;

    /**
     * 组套号
     */
    private String orderGroup;

    /**
     * 关键字（医嘱号 / 项目名称）
     */
    private String keyword;

    /**
     * 只看待校对：1-是（护士待校对列表用）
     */
    private Integer pendingVerifyOnly;
}
