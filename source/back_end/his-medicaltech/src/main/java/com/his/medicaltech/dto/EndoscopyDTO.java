package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 内镜域入参
 */
public class EndoscopyDTO {

    /** 登记 / 修改 */
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

        /** 内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜） */
        private Integer endoType;

        /** 麻醉方式（1-无麻醉 2-表面麻醉 3-静脉麻醉 4-全身麻醉） */
        private Integer anesthesiaMethod;

        /** 检查部位 / 到达范围 */
        private String bodyPart;

        /** 检查目的 */
        private String examPurpose;

        /** 备注 */
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Query extends PageParam {
        /** 内镜检查号 */
        private String recordNo;

        /** 患者姓名 */
        private String patientName;

        /** 患者ID */
        private Long patientId;

        /** 内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜） */
        private Integer endoType;

        private Integer status;

        /** 开始日期 */
        private LocalDate startDate;

        /** 结束日期 */
        private LocalDate endDate;
    }

    /** 检查执行：内镜医师 / 所见 / 活检信息 */
    @Data
    public static class Execute {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 内镜医师（为空则取当前登录人） */
        private String endoscopist;

        /** 肠道准备质量 Boston 评分 0~9（肠镜） */
        private Integer bowelPrepScore;

        /** 幽门螺杆菌（0-未查 1-阴性 2-阳性，胃镜） */
        private Integer hpResult;

        /** 检查部位 / 到达范围 */
        private String bodyPart;

        /** 是否活检（0-否 1-是） */
        private Integer biopsyFlag;

        /** 活检部位 */
        private String biopsyPart;

        /** 活检块数 */
        private Integer biopsyCount;
    }

    /** 报告（内镜所见 / 诊断 / 建议） */
    @Data
    public static class Report {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 内镜所见 */
        @NotBlank(message = "内镜所见不能为空")
        private String findings;

        /** 内镜诊断 */
        @NotBlank(message = "内镜诊断不能为空")
        private String diagnosis;

        /** 建议 */
        private String suggestion;
    }

    /** 活检送病理（生成病理单并回填病理号） */
    @Data
    public static class BiopsySend {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        /** 活检部位 */
        @NotBlank(message = "活检部位不能为空")
        private String biopsyPart;

        /** 活检块数 */
        private Integer biopsyCount;
    }

    @Data
    public static class Audit {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;

        private String auditOpinion;
    }

    /** 只带一个记录ID的动作（签到 / 发布等） */
    @Data
    public static class IdOnly {
        @NotNull(message = "检查记录ID不能为空")
        private Long recordId;
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
