package com.his.charge.support;

import java.util.Arrays;
import java.util.List;

/**
 * 低编入组的证据字典：检验/检查结果 -> 应有诊断。
 *
 * <p>逻辑：如果某条检验/检查明确提示了某诊断，而结算清单里没有编该诊断，
 * 那就有「为压低费用而漏编」的嫌疑 —— 这就是低编入组最典型的形态。</p>
 *
 * <p><b>这是启发式字典，不是医保目录。</b>当前只收了几条最典型、误报率最低的映射，
 * 目的是把「低编入组」这条链路跑通并留下可审计的判定依据（evidence 会写明命中了哪条检验）。
 * 生产环境应替换为「检验项目-诊断」对照表（可复用 `检验项目组套明细`），
 * 而不是继续在代码里加映射。</p>
 */
public final class LowCodingEvidenceDict {

    /**
     * 字典条目。icdPrefix 用来在清单里找「是否已经编了」，
     * 找不到才报低编 —— 避免已编还报。
     */
    private static final List<Entry> ENTRIES = Arrays.asList(
            new Entry("糖化血红蛋白", "2型糖尿病", "E11", "LAB"),
            new Entry("空腹血糖", "2型糖尿病", "E11", "LAB"),
            new Entry("肌钙蛋白", "急性心肌梗死", "I21", "LAB"),
            new Entry("D-二聚体", "肺栓塞", "I26", "LAB"),
            new Entry("降钙素原", "细菌性肺炎", "J15", "LAB"),
            new Entry("血气分析", "呼吸衰竭", "J96", "LAB"),
            new Entry("冠状动脉造影", "冠心病", "I25", "INSP"),
            new Entry("颅脑CT", "脑梗死", "I63", "INSP"),
            new Entry("胸部CT", "肺部感染", "J18", "INSP")
    );

    private LowCodingEvidenceDict() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    /**
     * 一条映射：证据关键词 -> 提示的诊断名与 ICD 前缀
     */
    public static final class Entry {
        private final String evidenceKeyword;
        private final String hintDiagnosis;
        private final String icdPrefix;
        private final String group;

        public Entry(String evidenceKeyword, String hintDiagnosis, String icdPrefix, String group) {
            this.evidenceKeyword = evidenceKeyword;
            this.hintDiagnosis = hintDiagnosis;
            this.icdPrefix = icdPrefix;
            this.group = group;
        }

        public String getEvidenceKeyword() {
            return evidenceKeyword;
        }

        public String getHintDiagnosis() {
            return hintDiagnosis;
        }

        /**
         * 命中该前缀即视为已编，用于防误报
         */
        public String getIcdPrefix() {
            return icdPrefix;
        }

        /**
         * 证据来源：LAB-检验 INSP-检查
         */
        public String getGroup() {
            return group;
        }
    }
}
