package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 超声域出参
 */
public class UltrasoundVO {

    @Data
    public static class ListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 超声检查号
         */
        private String recordNo;

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
         * 超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内）
         */
        private Integer usType;

        private String usTypeText;

        /**
         * 检查部位
         */
        private String bodyPart;

        /**
         * 超声提示（结论）
         */
        private String conclusion;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 检查医师
         */
        private String sonographer;

        /**
         * 检查时间
         */
        private LocalDateTime executeTime;

        /**
         * 报告医师
         */
        private String reportBy;

        /**
         * 报告时间
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
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    public static class DetailVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 超声检查号
         */
        private String recordNo;

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
         * 超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内）
         */
        private Integer usType;

        private String usTypeText;

        /**
         * 检查部位
         */
        private String bodyPart;

        /**
         * 检查目的
         */
        private String examPurpose;

        /**
         * 超声所见
         */
        private String findings;

        /**
         * 超声提示（结论）
         */
        private String conclusion;

        /**
         * 建议
         */
        private String suggestion;

        /**
         * 检查医师
         */
        private String sonographer;

        /**
         * 检查时间
         */
        private LocalDateTime executeTime;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 报告医师
         */
        private String reportBy;

        /**
         * 报告时间
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

        private List<MeasureVO> measures;
    }

    @Data
    public static class MeasureVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long recordId;

        private String measureName;

        private String measureValue;

        /**
         * 单位
         */
        private String unit;

        private String referenceRange;

        /**
         * 0-正常 1-偏高 2-偏低 3-异常
         */
        private Integer abnormalFlag;

        private String abnormalFlagText;

        private Integer sortOrder;
    }

    @Data
    public static class StatsVO {
        private long pending;
        private long examining;
        private long pendingAudit;
        private long published;
        private long todayCount;
        /**
         * 总条数
         */
        private long total;
    }
}
