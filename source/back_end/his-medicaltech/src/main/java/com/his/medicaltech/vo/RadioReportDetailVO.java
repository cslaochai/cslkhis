package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.medicaltech.vo.ExamImageVO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 放射报告详情（写报告时一屏要有的东西，sql/138）。
 *
 * <p>影像帧 {@code images} 直接随详情出参：诊断医师打开一条检查就是为了看图，
 * 让他先等报告回来再发第二个请求取影像，等于每次开单都多一次往返，
 * 而且中间那瞬间的空白会被当成「没有片子」。
 */
@Data
public class RadioReportDetailVO {

    /** 检查/检验记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 检查/检验记录号 */
    private String recordNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    private String applyNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别（1-男 2-女） */
    private Integer gender;

    private String genderText;

    /** 年龄 */
    private Integer age;

    /** 就诊日期 */
    private LocalDate visitDate;

    private String itemCode;

    /** 项目名称 */
    private String itemName;

    private String bodyPart;

    /** 申请科室 */
    private String applyDeptName;

    /** 申请医生 */
    private String applyDoctorName;

    /** 临床诊断 */
    private String clinicalDiagnosis;

    /** 检查记录状态 */
    private Integer recordStatus;

    private String recordStatusText;

    // 报告正文

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /** 报告编号（唯一） */
    private String reportNo;

    /** 报告状态（1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废） */
    private Integer reportStatus;

    private String reportStatusText;

    /** 使用的报告模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 检查方法 */
    private String examMethod;

    /** 报告内容 */
    private String reportContent;

    /** 影像诊断 / 印象 */
    private String conclusion;

    /** 建议 */
    private String suggestions;

    /** 阴阳性（0-未判定 1-阴性 2-阳性 3-未见异常） */
    private Integer positiveFlag;

    private String positiveFlagText;

    /** 是否危急（0-否 1-是） */
    private Integer isCritical;

    /** 报告书写人 */
    private String writeBy;

    /** 报告书写人员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long writeById;

    /** 报告书写时间 */
    private LocalDateTime writeTime;

    /** 审核人（初审） */
    private String auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 发布人 */
    private String publishBy;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 报告版本号 */
    private Integer reportVersion;

    /** 退回原因 */
    private String rejectReason;

    /** 已登记胶片张数 */
    private Integer filmCount;

    /** 报告签名ID（null=未签） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /** 审核签名ID（null=未签） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    // 影像（sql/137）

    private List<ExamImageVO> images;
}
