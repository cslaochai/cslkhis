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
 *
 * <p><b>刻意不接收患者ID、申请科室、申请医生、床号</b>：一律由服务端从入院记录与当前登录用户推导。
 * 前端能传的"事实"只有：给哪次住院申请、受血者血型（本次鉴定结果）、要什么品种多少袋、
 * 为什么要输（指征）、以及输血前的检验指标。让前端传申请科室，就一定会出现
 * "申请科室"与入院科室打架的记录（同会诊/手术的坑）。
 *
 * <p><b>配血信息、发血信息、输注与核对信息都不在这个 DTO 里</b> ——
 * 它们分别是输血科、护理岗的动作，各走自己的端点。申请与配血混在一个入口，
 * 结果就是病区能把血袋号"顺手填上"，输血科失去配血权。
 *
 * <p><b>受血者血型（patientAbo / patientRh）必填</b>：没有血型，
 * 后面所有的"配血相合"都是空话。Rh 单独必填是因为
 * 患者基本信息.blood_type 只有 A/B/O/AB、**没有 Rh 维度**，
 * 而 Rh 阴性是稀有血型、直接决定备血方案。
 */
@Data
public class TransfusionApplyUpsertDTO implements Serializable {

    /** 输血申请单ID（为空 = 新增；不为空 = 修改，仅允许改「待配血」的申请） */
    private Long id;

    /** 入院ID（必填） */
    @NotNull(message = "入院ID不能为空（输血必须挂在一次住院上）")
    private Long admissionId;

    /** 受血者 ABO 血型（必填）：A/B/O/AB */
    private String patientAbo;

    /** 受血者 Rh 血型（必填）：阳/阴（也接受「阳性/阴性/+/-」写法，服务端归一） */
    private String patientRh;

    /** 血液品种（必填）：1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他 */
    @NotNull(message = "血液品种不能为空")
    @InEnum(value = BloodComponentEnum.class, message = "血液品种取值不合法（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）")
    private Integer bloodComponent;

    /** 规格（如 1.5U / 200ml / 1治疗量） */
    private String componentSpec;

    /** 申请袋数（必填，>=1；配血累计不得超过此数） */
    private Integer bagCount;

    /** 申请总量 */
    private BigDecimal plannedAmount;

    /** 总量单位：U / ml / 治疗量 */
    private String amountUnit;

    /** 输血目的（纠正贫血/补充凝血因子/提升血小板…） */
    private String transfusionPurpose;

    /** 输血指征（Hb/HCT/PLT 指标 + 临床症状，缺了就是无指征用血） */
    @NotBlank(message = "输血指征不能为空（无指征用血是飞行检查的重点）")
    private String indication;

    /** 输血前血红蛋白 Hb（g/L） */
    private BigDecimal preHb;

    /** 输血前红细胞压积 HCT（%） */
    private BigDecimal preHct;

    /** 输血前血小板 PLT（×10^9/L） */
    private Integer prePlt;

    /** 既往输血史 */
    private String transfusionHistory;

    /** 既往输血反应史 */
    private String reactionHistory;

    /** 妊娠史（育龄女性） */
    private String pregnancyHistory;

    /** 是否紧急用血：0-否 1-是（为空按否） */
    private Integer isEmergency;

    /** 备注 */
    private String remark;
}
