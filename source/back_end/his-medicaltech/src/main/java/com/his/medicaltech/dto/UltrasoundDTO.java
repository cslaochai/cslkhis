package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 超声域入参
 */
public class UltrasoundDTO {

    @Data
    public static class RecordUpsert {
        private Long id;

        /** 患者ID */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /** 患者号 */
        private String patientNo;

        /** 患者姓名 */
        @NotBlank(message = "患者姓名不能为空")
        private String patientName;

        /** 性别（1-男 2-女 9-未知） */
        private Integer gender;

        /** 年龄 */
        private Integer age;

        /** 就诊日期 */
        private LocalDate visitDate;

        /** 申请科室ID */
        private Long applyDeptId;

        /** 申请科室 */
        private String applyDeptName;

        /** 申请医生ID */
        private Long applyDoctorId;

        /** 申请医生 */
        private String applyDoctorName;

        /** 临床诊断 */
        private String clinicalDiagnosis;

        /** 超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内） */
        private Integer usType;

        /** 检查部位 */
        private String bodyPart;

        /** 检查目的 */
        private String examPurpose;

        /** 备注 */
        private String remark;
    }

    @Data
    public static class Query {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;

        /** 超声检查号 */
        private String recordNo;

        /** 患者姓名 */
        private String patientName;

        /** 患者ID */
        private Long patientId;

        /** 超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内） */
        private Integer usType;

        private Integer status;

        /** 开始日期 */
        private LocalDate startDate;

        /** 结束日期 */
        private LocalDate endDate;
    }

    /** 执行检查（检查医师 / 部位） */
    @Data
    public static class Execute {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 检查医师 */
        private String sonographer;

        /** 检查部位 */
        private String bodyPart;
    }

    /** 结构化测量值条目 */
    @Data
    public static class MeasureItem {
        private Long id;

        @NotBlank(message = "测量项名称不能为空")
        private String measureName;

        private String measureValue;

        /** 单位 */
        private String unit;

        private String referenceRange;

        private Integer sortOrder;
    }

    /** 保存测量值（整单覆盖式保存） */
    @Data
    public static class MeasureSave {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        private List<MeasureItem> measures;
    }

    /** 出具报告（所见 + 提示） */
    @Data
    public static class Report {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 超声所见 */
        @NotBlank(message = "超声所见不能为空")
        private String findings;

        /** 超声提示（结论） */
        @NotBlank(message = "超声提示不能为空")
        private String conclusion;

        /** 建议 */
        private String suggestion;
    }

    /** 只带记录ID的动作（签到 / 发布） */
    @Data
    public static class IdOnly {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;
    }

    @Data
    public static class Audit {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        private String auditOpinion;
    }

    @Data
    public static class Publish {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;
    }

    @Data
    public static class Cancel {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 取消原因 */
        @NotBlank(message = "取消原因不能为空")
        private String cancelReason;
    }
}
