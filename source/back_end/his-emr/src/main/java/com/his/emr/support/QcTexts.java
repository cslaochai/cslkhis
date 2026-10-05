package com.his.emr.support;

import com.his.common.enums.RecordQcTypeEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.emr.enums.QcGradeEnum;
import com.his.emr.enums.QcResultEnum;
import com.his.emr.enums.QcStatusEnum;
import com.his.emr.enums.RecordQcActionEnum;
import com.his.emr.enums.RecordQcFlowStatusEnum;
import com.his.emr.enums.RecordQcLevelEnum;
import com.his.patient.enums.InpatientRecordTypeEnum;

/**
 * 质控相关码值 → 中文的**唯一**映射处。
 *
 * <p>为什么不放在前端：两份码值表一定会有一份先过期，然后界面显示"未知"而没人发现。
 * 为什么不散落在 Service / VO / SQL 里：那样同一个码值会有三种说法。
 *
 * <p><b>码值 → 文案一律走枚举</b>：本类的每个方法只做"调对应枚举的 {@code labelOf}"这一件事，
 * 不内联 switch、不自己写「未知(xxx)」兜底 —— 未知码值由枚举 {@code labelOf} 返回空串（展示口径）；
 * 异常 / 审计场景若需保留原始码值，由调用侧改走枚举的 {@code labelOrUnknown}（见 AGENTS.md §13）。
 * 仅 {@code recordSource} 这类字符串码（无对应枚举）保留内部映射，兜底同样给空串。
 */
public final class QcTexts {

    private QcTexts() {
    }

    /**
     * 质控类型：0-综合 1-完整性 2-规范性 3-逻辑性 4-AI内涵质控
     */
    public static String qcType(Integer code) {
        if (code == null) {
            return null;
        }
        String label = RecordQcTypeEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 质控状态：1-待处理 2-已处理 3-已忽略
     */
    public static String qcStatus(Integer code) {
        if (code == null) {
            return null;
        }
        String label = QcStatusEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 质控结果：0-不通过 1-通过
     */
    public static String qcResult(Integer code) {
        if (code == null) {
            return null;
        }
        String label = QcResultEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 病历状态：1-草稿 2-已提交 3-已归档 4-已作废
     */
    public static String recordStatus(Integer code) {
        if (code == null) {
            return null;
        }
        String label = RecordStatusEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 三级质控流转状态：1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中
     */
    public static String qcFlowStatus(Integer code) {
        if (code == null) {
            return null;
        }
        String label = RecordQcFlowStatusEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 三级质控级别：1-科级 2-病案室 3-医务处
     */
    public static String qcFlowLevel(Integer code) {
        if (code == null) {
            return null;
        }
        String label = RecordQcLevelEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 三级质控流转动作：1-发起送审 2-审核通过 3-退回整改 4-整改提交 5-终审通过
     */
    public static String qcFlowAction(Integer code) {
        if (code == null) {
            return null;
        }
        String label = RecordQcActionEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 病历终审定级：1-甲级 2-乙级 3-丙级
     */
    public static String qcGrade(Integer code) {
        if (code == null) {
            return null;
        }
        String label = QcGradeEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 住院文书类型；门诊病历直接给"门诊病历"
     */
    public static String recordType(Integer code) {
        if (code == null) {
            return null;
        }
        String label = InpatientRecordTypeEnum.labelOf(code);
        return label != null ? label : "";
    }

    /**
     * 病历来源中文
     */
    public static String recordSource(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return switch (code.trim().toUpperCase()) {
            case "OUTPATIENT" -> "门诊病历";
            case "INPATIENT" -> "住院文书";
            default -> "";
        };
    }

    /**
     * 性别：按后端枚举 1-男 2-女，其余为未知码值
     */
    public static String gender(Integer code) {
        if (code == null) {
            return null;
        }
        SysGenderEnum g = SysGenderEnum.fromCode(code);
        return g != null ? g.getLabel() : "";
    }

    /**
     * 病历质量等级：有否决项必为丙级，否则按分数线（甲≥90 乙75~89 丙&lt;75）。
     * score 为空（旧版质控）返回 null —— 不猜等级。
     */
    public static String grade(Integer score, Integer severityMax) {
        if (score == null) {
            return null;
        }
        if (severityMax != null && severityMax >= QcSeverity.FATAL.getCode()) {
            return "丙";
        }
        if (score >= 90) {
            return "甲";
        }
        return score >= 75 ? "乙" : "丙";
    }
}
