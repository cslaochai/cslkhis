package com.his.operation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 手术申请入参（新增 / 修改「待排期」的申请）。
 */
@Data
public class OperationApplyUpsertDTO implements Serializable {

    /**
     * 手术申请单ID（为空 = 新增；不为空 = 修改，仅允许改「待排期」的申请）
     */
    private Long id;

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空（手术必须挂在一次住院上）")
    private Long admissionId;

    /**
     * 拟施手术编码（ICD-9-CM-3，可空：没有编码也必须能申请，但不能没有名称）
     */
    private String plannedOperationCode;

    /**
     * 拟施手术名称（必填）
     */
    @NotBlank(message = "拟施手术名称不能为空")
    private String plannedOperationName;

    /**
     * 手术级别（1-一级 2-二级 3-三级 4-四级）
     */
    @Min(value = 1, message = "手术级别取值不合法（应为 1~4：一级~四级）")
    @Max(value = 4, message = "手术级别取值不合法（应为 1~4：一级~四级）")
    private Integer operationLevel;

    /**
     * 切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）
     */
    @Min(value = 0, message = "切口等级取值不合法（应为 0~3：0类/Ⅰ类/Ⅱ类/Ⅲ类）")
    @Max(value = 3, message = "切口等级取值不合法（应为 0~3：0类/Ⅰ类/Ⅱ类/Ⅲ类）")
    private Integer incisionLevel;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    @Min(value = 1, message = "麻醉方式取值不合法（应为 1~5：全麻/椎管内/神经阻滞/局麻/其他）")
    @Max(value = 5, message = "麻醉方式取值不合法（应为 1~5：全麻/椎管内/神经阻滞/局麻/其他）")
    private Integer anesthesiaType;

    /**
     * 术前诊断（必填；回写病历的"术前诊断"要素）
     */
    @NotBlank(message = "术前诊断不能为空（回写病历的术前诊断要素取自这里）")
    private String preopDiagnosis;

    /**
     * 手术指征/理由（必填：开一刀是一个医疗决定，必须写清为什么）
     */
    @NotBlank(message = "手术指征不能为空（开一刀是一个医疗决定，必须写清为什么）")
    private String operationReason;

    /**
     * 是否急诊手术：0-择期 1-急诊（为空按择期）
     */
    private Integer isEmergency;

    /**
     * 是否主要手术：0-否 1-是（为空按 1；同一次住院只允许一条主要手术申请）
     */
    private Integer isMain;

    /**
     * 备注
     */
    private String remark;
}
