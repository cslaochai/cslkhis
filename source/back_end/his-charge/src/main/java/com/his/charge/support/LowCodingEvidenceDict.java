package com.his.charge.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 低编入组的证据字典：检验/检查结果 -> 应有诊断。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LowCodingEvidenceDict {

    /**
     * 字典条目。icdPrefix 用来在清单里找「是否已经编了」，
     * todo：为什么这里能写死？
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
