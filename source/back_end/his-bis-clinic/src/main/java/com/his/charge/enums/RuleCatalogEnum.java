package com.his.charge.enums;

import lombok.Getter;

/**
 * 医保合规审核规则目录。
 */
@Getter
public enum RuleCatalogEnum {

    // A 组：编码依据一致性
    A01("A01", "主要诊断未编码", "A", 3, 0,
            "主要诊断必须填写 ICD-10 编码，否则无法参与 DRG/DIP 入组。请补编主诊断。"),
    A02("A02", "主要诊断编码不在医保目录内", "A", 3, 1,
            "该编码不在本院启用的医保 ICD-10 目录中，属未审核/自造编码，上传后必被医保端退回。请从目录中重新选择。"),
    A03("A03", "主要诊断与病历诊断不一致", "A", 2, 1,
            "清单主诊断与病历首页诊断编码不一致，二者必须一致。请核对后统一。"),
    A04("A04", "其他诊断缺编码", "A", 1, 1,
            "其他诊断只填了名称未填编码，无法参与 CC/MCC 判定。请补编。"),
    A05("A05", "手术操作未编码", "A", 2, 2,
            "手术操作缺 ICD-9-CM-3 编码，外科组无法入组。请补编。"),
    A06("A06", "手术操作无病历与收费依据", "A", 3, 2,
            "该手术操作编码未找到对应的病历记载或手术性收费，属典型「高套手术」。请提供依据或删除该编码。"),
    A07("A07", "有手术性治疗费用但无手术编码", "A", 3, 0,
            "存在手术性治疗收费却未编手术操作，属漏编（低编入组），并会导致费用与入组不匹配。请补编手术操作。"),
    A08("A08", "存在漏编诊断（低编入组）", "A", 2, 0,
            "检验/检查证据提示存在某诊断，但清单未编该诊断，疑似为压低费用而漏编（低编入组）。请核实后补编。"),

    // B 组：逻辑排他
    B01("B01", "诊断与患者性别矛盾", "B", 3, 1,
            "该诊断仅见于特定性别，与患者性别不符。请删除或更正。"),
    B02("B02", "诊断与患者年龄矛盾", "B", 3, 1,
            "该诊断仅见于特定年龄段，与患者年龄不符。请删除或更正。"),
    B03("B03", "诊断重复编码", "B", 2, 1,
            "同一 ICD 编码在诊断明细中重复出现，清单口径不允许重复。请合并。"),
    B04("B04", "主要诊断入院病情为「无」", "B", 2, 1,
            "入院病情为「无」表示入院时并不存在该情况，不能作为主要诊断。请核对主诊断选择。"),
    B05("B05", "标注 CC/MCC 但无对应并发症诊断", "B", 2, 1,
            "该诊断标注了 CC/MCC 级别（会提升病组权重），但诊断明细中找不到对应的并发症/合并症诊断。请补充依据或取消该标注。"),

    // C 组：住院指征（低编入组 / 低标入院）
    C01("C01", "住院天数不足且无手术操作", "C", 2, 0,
            "住院天数低于阈值且无手术操作，存在低标入院/低编入组嫌疑。请核实住院指征。"),
    C02("C02", "无住院级别诊疗行为", "C", 2, 0,
            "未见手术、检验、检查及治疗性收费，仅有药品或挂号费，不具备住院必要性。请核实。"),
    C03("C03", "疑似分解住院", "C", 3, 0,
            "同一患者在分解住院窗口内以相同主要诊断再次入院，属医保明令禁止的分解住院。请说明或合并结算。"),

    // D 组：分组倍率
    D01("D01", "费用倍率异常", "D", 3, 0,
            "费用倍率超出合理区间：高倍率指向高编高套，低倍率指向低编入组。请核对编码与费用。"),
    D02("D02", "未接入DRG分组方案", "D", 1, 0,
            "本地未接入医保局 DRG/DIP 分组方案，无法评估入组与倍率。请导入分组表后复查。");

    /**
     * 规则编码
     */
    private final String code;
    /**
     * 规则名称
     */
    private final String name;
    /**
     * 规则分组：A/B/C/D
     */
    private final String group;
    /**
     * 风险等级：1-提示 2-关注 3-高危
     */
    private final int risk;
    /**
     * 检查对象：0-清单级 1-诊断 2-手术操作
     */
    private final int targetType;
    /**
     * 默认整改建议
     */
    private final String suggestion;

    RuleCatalogEnum(String code, String name, String group, int risk, int targetType, String suggestion) {
        this.code = code;
        this.name = name;
        this.group = group;
        this.risk = risk;
        this.targetType = targetType;
        this.suggestion = suggestion;
    }

    /**
     * 风险等级 -> 风险分
     */
    public static int scoreOf(int risk) {
        switch (risk) {
            case 3:
                return 50;
            case 2:
                return 25;
            case 1:
                return 10;
            default:
                return 0;
        }
    }

    public static String groupLabel(String group) {
        if (group == null) {
            return "";
        }
        switch (group) {
            case "A":
                return "编码依据一致性";
            case "B":
                return "逻辑排他";
            case "C":
                return "住院指征";
            case "D":
                return "分组倍率";
            default:
                return group;
        }
    }

    /**
     * 风险等级中文，未知值不兜底成「安全」
     */
    public static String riskLabel(Integer level) {
        if (level == null) {
            return "未评估";
        }
        switch (level) {
            case 0:
                return "未发现";
            case 1:
                return "提示";
            case 2:
                return "关注";
            case 3:
                return "高危";
            default:
                return "未知(" + level + ")";
        }
    }
}
