package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理评估单分页查询。
 * admissionId / assessType 二选一或同传；不传 admissionId 时必须传 wardId（病区视角），
 * 否则等于全院捞评估单，超出了护士/质控的真实使用场景。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NursingAssessmentQueryPageDTO extends PageParam {

    /** 入院ID */
    private Long admissionId;

    /** 评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS） */
    private Integer assessType;

    /** 病区ID */
    private Long wardId;

    /** 患者姓名 */
    private String patientName;
}
