package com.his.medicaltech.vo;

import lombok.Data;

import java.util.List;

/**
 * 临床画像出参（过敏/慢病/危急值/异常检验/30天就诊/重复检查）。
 */
@Data
public class CdrClinicalSummaryVO {

    /** 是否有过敏史（取最近一次病历里的过敏史文本判断） */
    private Boolean allergyPositive;

    /** 过敏条目 */
    private List<AllergyItem> allergies;

    /** 慢病标签（从诊断名关键词命中） */
    private List<String> chronicTags;

    /** 危急值块（暂未接入检验明细，恒为 0） */
    private CriticalValueBlock criticalValues;

    /** 异常检验块（暂未接入检验明细，恒为 0） */
    private AbnormalLabBlock abnormalLabs;

    /** 近 30 天就诊统计 */
    private VisitStatBlock visits30d;

    /** 近 30 天同一项目重复检查（>=2 次） */
    private List<RepeatExamItem> repeatExams;

    /** 风险提示 */
    private List<String> warnings;

    @Data
    public static class AllergyItem {
        private String allergenName;
        private String allergySeverity;
        private String allergySymptoms;
        private String source;
    }

    @Data
    public static class CriticalValueBlock {
        private Integer totalCount;
        private Integer openCount;
        private List<LabItem> items;
    }

    @Data
    public static class AbnormalLabBlock {
        private Integer abnormalCount;
        private Integer unjudgedCount;
        private List<LabItem> items;
    }

    @Data
    public static class LabItem {
        private String itemName;
        private String resultValue;
        private String resultUnit;
        private String flagText;
        private String statusText;
    }

    @Data
    public static class VisitStatBlock {
        private Integer outpatientCount;
        private Integer emergencyCount;
        private Integer inpatientCount;
    }

    @Data
    public static class RepeatExamItem {
        private String itemName;
        private Integer count;
    }
}
