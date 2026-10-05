package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 内镜域出参
 */
public class EndoscopyVO {

    @Data
    public static class ListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 内镜检查号
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
         * 内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜）
         */
        private Integer endoType;

        private String endoTypeText;

        /**
         * 麻醉方式（1-无麻醉 2-表面麻醉 3-静脉麻醉 4-全身麻醉）
         */
        private Integer anesthesiaMethod;

        private String anesthesiaMethodText;

        /**
         * 检查部位 / 到达范围
         */
        private String bodyPart;

        /**
         * 是否活检（0-否 1-是）
         */
        private Integer biopsyFlag;

        /**
         * 关联病理号
         */
        private String pathologyOrderNo;

        /**
         * 内镜诊断
         */
        private String diagnosis;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 内镜医师
         */
        private String endoscopist;

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
         * 内镜检查号
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
         * 内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜）
         */
        private Integer endoType;

        private String endoTypeText;

        /**
         * 麻醉方式（1-无麻醉 2-表面麻醉 3-静脉麻醉 4-全身麻醉）
         */
        private Integer anesthesiaMethod;

        private String anesthesiaMethodText;

        /**
         * 检查部位 / 到达范围
         */
        private String bodyPart;

        /**
         * 检查目的
         */
        private String examPurpose;

        /**
         * 肠道准备质量 Boston 评分
         */
        private Integer bowelPrepScore;

        /**
         * 幽门螺杆菌（0-未查 1-阴性 2-阳性）
         */
        private Integer hpResult;

        private String hpResultText;

        /**
         * 内镜所见
         */
        private String findings;

        /**
         * 内镜诊断
         */
        private String diagnosis;

        /**
         * 建议
         */
        private String suggestion;

        /**
         * 是否活检（0-否 1-是）
         */
        private Integer biopsyFlag;

        /**
         * 活检部位
         */
        private String biopsyPart;

        /**
         * 活检块数
         */
        private Integer biopsyCount;

        /**
         * 关联病理号
         */
        private String pathologyOrderNo;

        /**
         * 内镜医师
         */
        private String endoscopist;

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
    }

    @Data
    public static class StatsVO {
        private long pending;
        private long examining;
        private long pendingAudit;
        private long published;
        /**
         * 活检块数
         */
        private long biopsyCount;
        /**
         * 总条数
         */
        private long total;
    }
}
