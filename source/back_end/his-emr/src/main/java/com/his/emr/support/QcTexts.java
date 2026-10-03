package com.his.emr.support;

/**
 * 质控相关码值 → 中文的**唯一**映射处。
 *
 * <p>为什么不放在前端：两份码值表一定会有一份先过期，然后界面显示"未知"而没人发现。
 * 为什么不散落在 Service / VO / SQL 里：那样同一个码值会有三种说法。
 *
 * <p><b>未知码值一律渲染成「未知(码值)」，绝不回落到某个合法值</b> ——
 * 回落会把"数据有问题"伪装成"数据正常"，这是最难查的一类缺陷。
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
        return switch (code) {
            case 0 -> "综合质控";
            case 1 -> "完整性检查";
            case 2 -> "规范性检查";
            case 3 -> "逻辑性检查";
            case 4 -> "AI内涵质控";
            default -> unknown(code);
        };
    }

    /**
     * 质控状态：1-待处理 2-已处理 3-已忽略
     */
    public static String qcStatus(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "待处理";
            case 2 -> "已处理";
            case 3 -> "已忽略";
            default -> unknown(code);
        };
    }

    /**
     * 质控结果：0-不通过 1-通过
     */
    public static String qcResult(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> "不通过";
            case 1 -> "通过";
            default -> unknown(code);
        };
    }

    /**
     * 病历状态：1-草稿 2-已提交 3-已归档 4-已作废
     */
    public static String recordStatus(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            case 4 -> "已作废";
            default -> unknown(code);
        };
    }

    /**
     * 三级质控流转状态：1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中
     */
    public static String qcFlowStatus(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "科级待审";
            case 2 -> "病案室待审";
            case 3 -> "医务处待审";
            case 4 -> "终审通过";
            case 5 -> "整改中";
            default -> unknown(code);
        };
    }

    /**
     * 三级质控级别：1-科级 2-病案室 3-医务处
     */
    public static String qcFlowLevel(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "科级";
            case 2 -> "病案室";
            case 3 -> "医务处";
            default -> unknown(code);
        };
    }

    /**
     * 三级质控流转动作：1-发起送审 2-审核通过 3-退回整改 4-整改提交 5-终审通过
     */
    public static String qcFlowAction(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "发起送审";
            case 2 -> "审核通过";
            case 3 -> "退回整改";
            case 4 -> "整改提交";
            case 5 -> "终审通过";
            default -> unknown(code);
        };
    }

    /**
     * 病历终审定级：1-甲级 2-乙级 3-丙级
     */
    public static String qcGrade(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "甲级";
            case 2 -> "乙级";
            case 3 -> "丙级";
            default -> unknown(code);
        };
    }

    /**
     * 住院文书类型；门诊病历直接给"门诊病历"
     */
    public static String recordType(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "入院记录";
            case 2 -> "首次病程";
            case 3 -> "日常病程";
            case 4 -> "术前小结";
            case 5 -> "手术记录";
            case 6 -> "术后首次病程";
            case 7 -> "出院记录";
            case 8 -> "死亡记录";
            case 9 -> "会诊记录";
            case 10 -> "转科记录";
            case 11 -> "输血记录";
            default -> unknown(code);
        };
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
            default -> unknown(code);
        };
    }

    /**
     * 性别：按后端枚举 1-男 2-女，其余为未知码值
     */
    public static String gender(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "男";
            case 2 -> "女";
            case 9 -> "未知";
            default -> unknown(code);
        };
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

    private static String unknown(Object code) {
        return "未知(" + code + ")";
    }
}
