package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 按已安排床位办理入院入参
 *
 * <p><b>科室不用传</b>：由已安排床位所属病区推导。跨科调配时这个推导尤其重要 ——
 * 入院科室必须是人实际躺着的那个科（床位所属科室），不是他最初想去但没床的那个科。
 *
 * <p>入院途径（{@code admitWay}）的口径与 {@code /patient/inpatient/admit} 完全一致：
 * <b>无证必须有途径</b>（病案首页必填），有住院证时服务端强制为门诊 1。
 *
 * <p><b>时间格式必须是 {@code yyyy-MM-dd HH:mm:ss}</b>（空格分隔）：
 * 一旦声明了该 pattern，Jackson 只认空格分隔，前端若传 ISO 的 {@code T} 会直接 400，
 * 且报错文案完全不指向时间字段（本项目已踩过两次）。
 */
@Data
public class BedWaitAdmitDTO {

    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /** 入院医生ID */
    @NotNull(message = "入院医生不能为空")
    private Long admitDoctorId;

    /** 入院途径：1-门诊 2-急诊 3-转院 4-其他（无证登记时必填） */
    private Integer admitWay;

    /** 入院时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /** 备注 */
    private String remark;
}
