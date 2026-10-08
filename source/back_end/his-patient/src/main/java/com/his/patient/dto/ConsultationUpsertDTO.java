package com.his.patient.dto;

import com.his.common.validation.InEnum;
import com.his.patient.enums.ConsultCategoryEnum;
import com.his.patient.enums.ConsultScopeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊申请入参（新增 / 修改「待应答」的申请）。
 */
@Data
public class ConsultationUpsertDTO implements Serializable {

    /**
     * 会诊ID（为空 = 新增；不为空 = 修改，仅允许改「待应答」的申请）
     */
    private Long id;

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空（会诊必须挂在一次住院上）")
    private Long admissionId;

    /**
     * 会诊科室ID（必填）
     */
    @NotNull(message = "会诊科室不能为空")
    private Long toDeptId;

    /**
     * 会诊范围：1-科内 2-科间 3-全院（必填）
     */
    @NotNull(message = "会诊范围不能为空（1-科内 2-科间 3-全院）")
    @InEnum(value = ConsultScopeEnum.class, message = "会诊范围取值不合法（应为 1-科内 2-科间 3-全院）")
    private Integer consultType;

    /**
     * 会诊类别：1-普通科间 2-营养 3-药学 4-其他专科。
     * <p>由接口入口钉死（普通会诊页固定 1、营养会诊页固定 2），不接受页面自选 ——
     * 类别决定这单在哪个工作台出现，能被请求体随意改就等于能把会诊藏起来。
     */
    @InEnum(value = ConsultCategoryEnum.class, message = "会诊类别取值不合法（1-普通科间 2-营养 3-药学 4-其他专科）")
    private Integer consultCategory;

    /**
     * 是否急会诊：0-普通 1-急会诊（为空按普通）
     */
    private Integer isUrgent;

    /**
     * 指定会诊医生ID（可空 = 不指定，等会诊科室认领）
     */
    private Long doctorId;

    /**
     * 会诊理由（必填）
     */
    @NotBlank(message = "会诊理由不能为空（会诊方需要知道要解决什么问题）")
    private String reason;

    /**
     * 备注
     */
    private String remark;
}
