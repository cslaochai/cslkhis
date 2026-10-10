package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理评估单分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NursingAssessmentQueryPageDTO extends PageParam {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS）
     */
    private Integer assessType;

    /**
     * 病区ID
     */
    private Long wardId;

    /**
     * 患者姓名
     */
    private String patientName;
}
