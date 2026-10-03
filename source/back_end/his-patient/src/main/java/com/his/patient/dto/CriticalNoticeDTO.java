package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病危重通知入参（外层空壳 + 内部静态类，同 DeathCertificateDTO 组织方式）。
 *
 * <p>时间入参一律宽进空格格式（AGENTS §3），前端 value-format 同口径传
 * {@code yyyy-MM-dd HH:mm:ss}，不许传 ISO T 分隔。
 */
public class CriticalNoticeDTO {

    /** 分页查询 */
    @Data
    public static class QueryPage {
        /** 关键字 */
        private String keyword;
        /** 通知类别（1-病危 2-病重） */
        private Integer noticeType;
        /** 状态（1-草稿 2-已签发 3-已签收 4-已作废） */
        private Integer noticeStatus;
        /** 开单科室ID */
        private Long deptId;
        /** 开始日期 */
        private LocalDate startDate;
        /** 结束日期 */
        private LocalDate endDate;
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 填写/修改草稿（一般项目由服务端按住院重查快照） */
    @Data
    public static class Upsert {
        /** 主键（雪花ID） */
        private Long id;
        /** 住院记录ID */
        @NotNull(message = "必须挂在一次住院记录上")
        private Long admissionId;
        /** 通知类别（1-病危 2-病重） */
        @NotNull(message = "通知类别不能为空")
        private Integer noticeType;
        /** 患者神志（1-清醒 2-嗜睡 3-意识模糊 4-昏迷 9-其他） */
        @NotNull(message = "患者神志不能为空")
        private Integer consciousnessStatus;
        /** 长度不加入参闸：服务端按列宽截断，粘贴超长说明不该变 400（AGENTS §3） */
        @NotBlank(message = "目前诊断不能为空")
        private String clinicalDiagnosis;
        /** 病情及危险因素 */
        @NotBlank(message = "病情及危险因素不能为空")
        private String conditionDesc;
        /** 可能的病情变化与预警事项 */
        @NotBlank(message = "预警事项不能为空")
        private String warningMatters;
        /** 医方已采取/拟采取的诊治措施与配合要求 */
        private String doctorMeasures;
        /** 告知时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull(message = "告知时间不能为空")
        private LocalDateTime notifyTime;
        /** 告知医师ID */
        private Long doctorId;
        /** 告知医师姓名 */
        private String doctorName;
        /** 见证医师ID */
        private Long witnessDoctorId;
        /** 见证医师姓名（可空） */
        private String witnessDoctorName;
        /** 备注 */
        private String remark;
    }

    /** 签发（医师电子签名锁定） */
    @Data
    public static class Issue {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少通知单ID")
        private Long id;
        /** 签名留证 IP，由 Controller 从请求侧写入，不接受前端自报 */
        private String clientIp;
    }

    /** 签收（家属手写签名 + 法定关系） */
    @Data
    public static class Acknowledge {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少通知单ID")
        private Long id;
        /** 签收人姓名 */
        @NotBlank(message = "签收人姓名不能为空")
        @Size(max = 50, message = "签收人姓名过长")
        private String signerName;
        /** 签收人与患者关系 */
        @NotNull(message = "签收人与患者的关系不能为空（法定必填）")
        private Integer signerRelation;
        /** 签收人证件号 */
        @Size(max = 20, message = "签收人证件号过长")
        private String signerIdCard;
        /** 签收人联系电话 */
        @Size(max = 20, message = "签收人电话过长")
        private String signerPhone;
        /** 手写签名 dataURL（data:image/png;base64,...），服务端校验前缀与长度 */
        @NotBlank(message = "签收人手写签名不能为空")
        private String signerSignature;
    }

    /** 作废 */
    @Data
    public static class VoidNotice {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少通知单ID")
        private Long id;
        /** 长度不加入参闸：服务端截到列宽，粘贴超长原因不该变 400（AGENTS §3） */
        @NotBlank(message = "作废原因不能为空（写清错在哪）")
        private String voidReason;
    }

    /** 打印回执计数 */
    @Data
    public static class Print {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少通知单ID")
        private Long id;
    }
}
