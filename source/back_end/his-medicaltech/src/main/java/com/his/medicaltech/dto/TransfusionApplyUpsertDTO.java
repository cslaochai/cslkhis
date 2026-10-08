package com.his.medicaltech.dto;

import com.his.common.validation.InEnum;
import com.his.medicaltech.enums.BloodComponentEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 输血申请入参（新增 / 修改「待配血」的申请）。
 */
@Data
public class TransfusionApplyUpsertDTO implements Serializable {

    /**
     * 输血申请单ID（为空 = 新增；不为空 = 修改，仅允许改「待配血」的申请）
     */
    private Long id;

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空（输血必须挂在一次住院上）")
    private Long admissionId;

    /**
     * 受血者 ABO 血型（必填）：A/B/O/AB
     */
    private String patientAbo;

    /**
     * 受血者 Rh 血型（必填）：阳/阴（也接受「阳性/阴性/+/-」写法，服务端归一）
     */
    private String patientRh;

    /**
     * 血液品种（必填）：1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他
     */
    @NotNull(message = "血液品种不能为空")
    @InEnum(value = BloodComponentEnum.class, message = "血液品种取值不合法（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）")
    private Integer bloodComponent;

    /**
     * 规格（如 1.5U / 200ml / 1治疗量）
     */
    private String componentSpec;

    /**
     * 申请袋数（必填，>=1；配血累计不得超过此数）
     */
    private Integer bagCount;

    /**
     * 申请总量
     */
    private BigDecimal plannedAmount;

    /**
     * 总量单位：U / ml / 治疗量
     */
    private String amountUnit;

    /**
     * 输血目的（纠正贫血/补充凝血因子/提升血小板…）
     */
    private String transfusionPurpose;

    /**
     * 输血指征（Hb/HCT/PLT 指标 + 临床症状，缺了就是无指征用血）
     */
    @NotBlank(message = "输血指征不能为空（无指征用血是飞行检查的重点）")
    private String indication;

    /**
     * 输血前血红蛋白 Hb（g/L）
     */
    private BigDecimal preHb;

    /**
     * 输血前红细胞压积 HCT（%）
     */
    private BigDecimal preHct;

    /**
     * 输血前血小板 PLT（×10^9/L）
     */
    private Integer prePlt;

    /**
     * 既往输血史
     */
    private String transfusionHistory;

    /**
     * 既往输血反应史
     */
    private String reactionHistory;

    /**
     * 妊娠史（育龄女性）
     */
    private String pregnancyHistory;

    /**
     * 是否紧急用血：0-否 1-是（为空按否）
     */
    private Integer isEmergency;

    /**
     * 备注
     */
    private String remark;
}
