package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者端用药说明。
 *
 * <p>回答「这盒药到底怎么吃」。答案全部来自<b>处方医嘱本身</b>（单次剂量、频次、途径、疗程）
 * 加上<b>药品字典的客观属性</b>（是否皮试、是否冷链、储存条件、是否抗菌药），
 * 没有任何一处是算出来的。
 */
@Data
@Schema(description = "患者端用药说明")
public class PatientMedicationGuideVO {

    @Schema(description = "处方ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    @Schema(description = "处方号")
    private String prescriptionNo;

    @Schema(description = "就诊日期")
    private LocalDate visitDate;

    @Schema(description = "开方科室")
    private String deptName;

    @Schema(description = "开方医生")
    private String doctorName;

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

    @Schema(description = "药品明细")
    private List<PatientMedicationItemVO> items;

    /**
     * 固定免责提示，患者端每一份用药说明都必须带
     */
    @Schema(description = "免责提示")
    private String advice;
}
