package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出院办理入参
 * <p>{@code dischargeWay}（离院方式）必填——它是病案首页的必填项，也是 DRG 分组与再入院判定的输入。
 * 死亡病例必须满足 {@code deathFlag=1} 且 {@code dischargeWay=5}，两者不一致直接拒绝。
 */
@Data
public class InpatientDischargeDTO {

    /** 入院ID（必填） */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /** 出院时间（不传取当前时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /** 出院医生ID */
    private Long dischargeDoctorId;

    /** 离院方式：1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他（必填） */
    @NotNull(message = "离院方式不能为空（病案首页必填项）")
    private Integer dischargeWay;

    /** 死亡标志（0-否 1-是） */
    private Integer deathFlag;

    /** 出院诊断（文本） */
    private String dischargeDiagnosis;

    /** 出院诊断ICD编码 */
    private String dischargeDiagnosisCode;

    /** 出院小结 / 出院带药医嘱 */
    private String dischargeSummary;

    /** 备注 */
    private String remark;
}
