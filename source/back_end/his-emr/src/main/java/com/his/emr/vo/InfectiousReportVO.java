package com.his.emr.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 传染病报告卡 VO 集合。
 */
public class InfectiousReportVO {

    /**
     * 报卡行（*Text 文案由服务端给，前端不猜码值）
     */
    @Data
    public static class Row {
        /**
         * 主键
         */
        private String id;
        /**
         * 报卡编号
         */
        private String reportNo;
        /**
         * 患者ID
         */
        private String patientId;
        /**
         * 患者编号（快照）
         */
        private String patientNo;
        /**
         * 患者姓名（快照）
         */
        private String patientName;
        /**
         * 性别
         */
        private Integer gender;
        /**
         * 年龄（快照）
         */
        private Integer age;
        /**
         * 门诊就诊ID
         */
        private String registId;
        /**
         * 住院记录ID
         */
        private String inpId;
        /**
         * 发现/就诊科室ID（快照）
         */
        private String visitDeptId;
        /**
         * 发现/就诊科室（快照）
         */
        private String visitDeptName;
        /**
         * 病种ID
         */
        private String diseaseId;
        /**
         * 病种编码（快照）
         */
        private String diseaseCode;
        /**
         * 病种名称（快照）
         */
        private String diseaseName;
        /**
         * 传染病类别（快照，1甲/2乙/3丙）
         */
        private Integer infectiousClass;
        private String infectiousClassText;
        /**
         * ICD-10（快照）
         */
        private String icd10;
        /**
         * 报卡时限
         */
        private LocalDateTime reportDeadline;
        /**
         * 临床摘要
         */
        private String clinicalDesc;
        /**
         * 状态
         */
        private Integer reportStatus;
        private String reportStatusText;
        /**
         * 报卡次数
         */
        private Integer reportCount;
        /**
         * 填卡医生姓名（快照）
         */
        private String reportByName;
        /**
         * 填卡时间
         */
        private LocalDateTime reportTime;
        /**
         * 审核人姓名
         */
        private String auditByName;
        /**
         * 审核时间
         */
        private LocalDateTime auditTime;
        /**
         * 审核意见
         */
        private String auditOpinion;
        /**
         * 退报原因
         */
        private String returnReason;
        /**
         * 最近一次超时催报时间
         */
        private LocalDateTime notifyTime;
        /**
         * 直报时间
         */
        private LocalDateTime directTime;
        /**
         * 直报报文
         */
        private String directPayload;
        /**
         * 创建时间
         */
        private String createTime;
        /**
         * 备注
         */
        private String remark;

        /**
         * 是否已超时报卡时限（待审核且现在已过 deadline）——服务端判定，前端不比时间
         */
        private Boolean overdue;
        /**
         * 剩余小时数（负数=已超时）
         */
        private Long remainHours;
    }

    /**
     * 统计卡
     */
    @Data
    public static class Stats {
        private long pendingAudit;
        private long audited;
        private long directReported;
        private long returned;
        private long overduePending;
        private long todayNew;
        /**
         * 甲类在办（时限最紧，页面置顶提示）
         */
        private long classAPending;
    }

    /**
     * 详情（报卡 + 直报报文）
     */
    @Data
    public static class Detail {
        private Row card;
        /**
         * 直报报文（JSON 字符串；真实对接时原样外发）
         */
        private String directPayloadPreview;
    }

    /**
     * 病种下拉行
     */
    @Data
    public static class DiseaseSelectListVO {
        /**
         * 主键
         */
        private String id;
        /**
         * 病种编码（快照）
         */
        private String diseaseCode;
        /**
         * 病种名称（快照）
         */
        private String diseaseName;
        /**
         * 传染病类别（快照，1甲/2乙/3丙）
         */
        private Integer infectiousClass;
        private String infectiousClassText;
        private Integer deadlineHours;
        /**
         * ICD-10（快照）
         */
        private String icd10;
    }

    /**
     * 列表分页壳
     */
    @Data
    public static class PageVO {
        /**
         * 总条数
         */
        private Long total;
        /**
         * 页码
         */
        private Integer pageNum;
        /**
         * 每页条数
         */
        private Integer pageSize;
        private Integer pages;
        /**
         * 明细行集合
         */
        private List<Row> records;
    }

    /**
     * 统计 + 字典文案兜底用（预留扩展）
     */
    @Data
    public static class DictRow {
        private String dictValue;
        private String dictLabel;
    }
}
