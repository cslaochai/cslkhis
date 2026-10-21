package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者端用药说明。
 */
@Data
@Schema(description = "患者端用药说明")
public class PatientMedicationGuideVO {

    /** 处方ID */
    @Schema(description = "处方ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /** 处方号 */
    @Schema(description = "处方号")
    private String prescriptionNo;

    /** 就诊日期 */
    @Schema(description = "就诊日期")
    private LocalDate visitDate;

    /** 开方科室名称 */
    @Schema(description = "开方科室")
    private String deptName;

    /** 开方医生姓名 */
    @Schema(description = "开方医生")
    private String doctorName;

    /** 处方类型文案（由枚举 label 渲染） */
    @Schema(description = "处方类型文本")
    private String prescriptionTypeText;

    /**
     * 是否已发药。
     * <p>
     * 未发药的处方谈"怎么吃"没有意义（可能还没缴费、可能被药师退回），
     * 前端据此换成一句"这张方子还没取药"。
     */
    @Schema(description = "是否已发药")
    private boolean dispensed;

    /** 药品明细列表 */
    @Schema(description = "药品明细")
    private List<PatientMedicationItemVO> items;

    /**
     * 固定免责提示，患者端每一份用药说明都必须带
     */
    @Schema(description = "免责提示")
    private String advice;
}
