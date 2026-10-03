package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 病案首页保存入参（整份保存）
 * <p>诊断 / 手术明细按<b>整表替换</b>语义提交：传什么就是什么，不传即清空。
 * 这样前端的编辑态与库内状态天然一致，不需要逐行 diff。
 */
@Data
public class InpatientSummaryUpsertDTO {

    /** 入院ID（必填） */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /** 年龄单位（1-岁 2-月 3-天） */
    private Integer ageUnit;

    /** 入院途径（1-门诊 2-急诊 3-转院 4-其他） */
    private Integer admitWay;

    /** 离院方式：1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他 */
    private Integer dischargeWay;

    /** 是否抢救（0-否 1-是） */
    private Integer isRescue;

    /** 是否危重（0-否 1-是） */
    private Integer isCritical;

    /** 住院总费用 */
    private BigDecimal totalAmount;

    /** 西药费 */
    private BigDecimal westernDrugAmount;

    /** 中成药费 */
    private BigDecimal chineseDrugAmount;

    /** 中药饮片费 */
    private BigDecimal herbalAmount;

    /** 检查费 */
    private BigDecimal examAmount;

    /** 检验费 */
    private BigDecimal labAmount;

    /** 治疗费 */
    private BigDecimal treatmentAmount;

    /** 手术费 */
    private BigDecimal operationAmount;

    /** 耗材费 */
    private BigDecimal materialAmount;

    /** 床位费 */
    private BigDecimal bedAmount;

    /** 护理费 */
    private BigDecimal nursingAmount;

    /** 其他费用 */
    private BigDecimal otherAmount;

    /** 备注 */
    private String remark;

    /** 诊断明细（整表替换） */
    @Valid
    private List<DiagnosisItem> diagnoses;

    /** 手术操作明细（整表替换） */
    @Valid
    private List<OperationItem> operations;

    /** 诊断明细项 */
    @Data
    public static class DiagnosisItem {

        /** 诊断类型：1-主要诊断 2-其他诊断 */
        private Integer diagType;

        /** ICD-10 编码 */
        private String icdCode;

        /** 诊断名称 */
        @NotBlank(message = "诊断名称不能为空")
        private String icdName;

        /** 入院病情：1-有 2-临床未确定 3-情况不明 4-无 */
        private Integer admitCondition;

        /** 并发症合并症级别：NONE / CC / MCC */
        private String ccLevel;

        /** 诊断依据（病历中的支持性描述） */
        private String diagnosisBasis;
    }

    /** 手术操作明细项 */
    @Data
    public static class OperationItem {

        /** 是否主要手术（0-否 1-是） */
        private Integer isMain;

        /** ICD-9-CM-3 编码 */
        private String operationCode;

        /** 手术操作名称 */
        @NotBlank(message = "手术操作名称不能为空")
        private String operationName;

        /** 手术日期 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operationDate;

        /** 手术级别：1-一级 2-二级 3-三级 4-四级 */
        private Integer operationLevel;

        /** 切口等级：0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类 */
        private Integer incisionLevel;

        /** 麻醉方式：1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他 */
        private Integer anesthesiaType;

        /** 主刀医师ID */
        private Long surgeonId;

        /** 主刀医师姓名 */
        private String surgeonName;

        /** 助手姓名 */
        private String assistantName;

        /** 手术依据（手术记录中的支持性描述） */
        private String operationBasis;
    }
}
