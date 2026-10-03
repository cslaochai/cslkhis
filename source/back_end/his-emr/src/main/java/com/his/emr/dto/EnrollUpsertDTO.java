package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入径登记入参（enrollUpsert：id 为空新增，非空仅允许在径状态改入径日期）。
 *
 * <p>患者/科室/诊断等快照一律服务端按 admissionId 取，不信前端传值。
 */
@Data
public class EnrollUpsertDTO implements Serializable {

    private Long id;

    /** 入院ID */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /** 模板ID */
    @NotNull(message = "路径模板不能为空")
    private Long pathwayId;

    /** 入径日期 */
    @NotNull(message = "入径日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollDate;

    /** 备注 */
    @Size(max = 512, message = "备注最长 512")
    private String remark;
}
