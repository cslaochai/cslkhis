package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 病理域出参
 */
public class PathologyVO {

    /**
     * 列表行
     */
    @Data
    public static class ListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 病理号
         */
        private String orderNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 年龄
         */
        private Integer age;

        /**
         * 就诊日期
         */
        private LocalDate visitDate;

        /**
         * 申请科室
         */
        private String applyDeptName;

        /**
         * 申请医生
         */
        private String applyDoctorName;

        /**
         * 临床诊断
         */
        private String clinicalDiagnosis;

        /**
         * 病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）
         */
        private Integer examType;

        /**
         * 标本类型（活检/切除/穿刺/脱落细胞等）
         */
        private String specimenType;

        /**
         * 取材部位
         */
        private String specimenPart;

        /**
         * 是否冰冻（0-否 1-是）
         */
        private Integer isFrozen;

        /**
         * 术中冰冻快速诊断结果
         */
        private String frozenResult;

        /**
         * 病理诊断
         */
        private String diagnosis;

        private Integer status;

        /**
         * 状态文案（服务端查字典回填，前端不自己拼）
         */
        private String statusText;

        /**
         * 初诊医师
         */
        private String reportBy;

        /**
         * 初诊时间
         */
        private LocalDateTime reportTime;

        /**
         * 审核医师
         */
        private String auditBy;

        /**
         * 审核时间
         */
        private LocalDateTime auditTime;

        /**
         * 发布人
         */
        private String publishBy;

        /**
         * 发布时间
         */
        private LocalDateTime publishTime;

        /**
         * 蜡块数（列表一并带出，避免前端再调一次）
         */
        private Integer blockCount;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
     * 详情
     */
    @Data
    public static class DetailVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 病理号
         */
        private String orderNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 年龄
         */
        private Integer age;

        /**
         * 就诊日期
         */
        private LocalDate visitDate;

        /**
         * 申请科室
         */
        private String applyDeptName;

        /**
         * 申请医生
         */
        private String applyDoctorName;

        /**
         * 临床诊断
         */
        private String clinicalDiagnosis;

        /**
         * 病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）
         */
        private Integer examType;

        private String examTypeText;

        /**
         * 标本类型（活检/切除/穿刺/脱落细胞等）
         */
        private String specimenType;

        /**
         * 取材部位
         */
        private String specimenPart;

        /**
         * 是否冰冻（0-否 1-是）
         */
        private Integer isFrozen;

        /**
         * 术中冰冻快速诊断结果
         */
        private String frozenResult;

        /**
         * 标本接收时间
         */
        private LocalDateTime receiveTime;

        /**
         * 标本接收人
         */
        private String receiveBy;

        /**
         * 取材时间
         */
        private LocalDateTime samplingTime;

        /**
         * 取材人
         */
        private String samplingBy;

        /**
         * 包埋时间
         */
        private LocalDateTime embeddingTime;

        /**
         * 包埋人
         */
        private String embeddingBy;

        /**
         * 制片（切片）
         */
        private LocalDateTime sliceTime;

        /**
         * 制片人
         */
        private String sliceBy;

        /**
         * 肉眼所见
         */
        private String grossFindings;

        /**
         * 镜下所见
         */
        private String microscopyFindings;

        /**
         * 免疫组化 / 特殊染色结果
         */
        private String ihcResult;

        /**
         * 病理诊断
         */
        private String diagnosis;

        /**
         * 建议
         */
        private String suggestion;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 初诊医师
         */
        private String reportBy;

        /**
         * 初诊时间
         */
        private LocalDateTime reportTime;

        /**
         * 审核医师
         */
        private String auditBy;

        /**
         * 审核时间
         */
        private LocalDateTime auditTime;

        /**
         * 发布人
         */
        private String publishBy;

        /**
         * 发布时间
         */
        private LocalDateTime publishTime;

        /**
         * 取消时间
         */
        private LocalDateTime cancelTime;

        /**
         * 取消原因
         */
        private String cancelReason;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        private List<BlockVO> blocks;
    }

    /**
     * 蜡块明细
     */
    @Data
    public static class BlockVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long orderId;

        private String blockNo;

        private String partDesc;

        private Integer blockCount;

        private Integer sliceCount;

        private String slideNo;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 取材人
         */
        private String samplingBy;

        /**
         * 取材时间
         */
        private LocalDateTime samplingTime;

        /**
         * 包埋人
         */
        private String embeddingBy;

        /**
         * 包埋时间
         */
        private LocalDateTime embeddingTime;

        /**
         * 制片人
         */
        private String sliceBy;

        /**
         * 制片（切片）
         */
        private LocalDateTime sliceTime;
    }

    /**
     * 统计卡
     */
    @Data
    public static class StatsVO {
        private long pendingReceive;
        private long processing;
        private long pendingAudit;
        private long published;
        private long frozenToday;
        /**
         * 总条数
         */
        private long total;
    }
}
