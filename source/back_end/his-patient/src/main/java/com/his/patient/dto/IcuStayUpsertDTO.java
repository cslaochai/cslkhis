package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ICU 入科登记/在科期间修改入参。
 *
 * <p>患者、来源科室信息由服务端按 admissionId 重查快照，前端只传 admissionId。
 * 说明类字段只在服务端截列宽，此处不挂 {@code @Size}（AGENTS.md 第 3 条）。
 */
@Data
public class IcuStayUpsertDTO {

    /** 主键ID */
    private Long id;

    /** 入院ID */
    @NotNull(message = "请选择住院患者")
    private Long admissionId;

    /** ICU 床位ID */
    @NotNull(message = "ICU 床位不能为空")
    private Long bedId;

    /** 监护等级（1-特级 2-I级 3-II级） */
    @NotNull(message = "监护等级不能为空")
    private Integer careLevel;

    /** 入科时间 */
    @NotNull(message = "入科时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime;

    /** 入科诊断/原因 */
    private String inDiag;

    /** 入科 GCS（3~15） */
    private Integer inGcs;

    /** 备注 */
    private String remark;
}
