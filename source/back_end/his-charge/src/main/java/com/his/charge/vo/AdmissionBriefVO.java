package com.his.charge.vo;

import com.his.charge.api.PatientGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 入院记录跨域摘要：收费域只读住院登记的最小字段集。
 *
 * <p>住院预交金、住院结算、欠费管控都只需要知道「这次住院挂在哪、谁管的、什么诊断」，
 * 不需要也不该知道对方实体的其余几十个字段。映射责任在 his-patient。
 *
 * @see PatientGateway
 */
@Data
@NoArgsConstructor
public class AdmissionBriefVO {

    /**
     * 入院ID（biz_admission 主键）
     */
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 入院时间（账期与日清单的起算点）
     */
    private LocalDateTime admitTime;

    /**
     * 经治医生ID（科室收入归属按主管医生算时要用）
     */
    private Long admitDoctorId;

    /**
     * 入院诊断（结算清单主诊断兜底）
     */
    private String diagnosis;
}
