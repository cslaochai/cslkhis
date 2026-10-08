package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 放射诊断工作台列表行（sql/138）。
 */
@Data
public class RadioReportListVO {

    /**
     * 检查/检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查/检验记录号
     */
    private String recordNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    private String applyNo;

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
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    private String bodyPart;

    /**
     * 检查记录状态（4-已出结果 5-已审核 6-已发布）
     */
    private Integer recordStatus;

    private String recordStatusText;

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

    // 报告侧（全部可为空 = 还没写报告）

    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /**
     * 报告编号（唯一）
     */
    private String reportNo;

    /**
     * 报告状态（null-未写报告 0-草稿 1-待审核 3-已审核 4-已发布 5-已作废）
     */
    private Integer reportStatus;

    private String reportStatusText;

    /**
     * 检查方法
     */
    private String examMethod;

    /**
     * 阴阳性（0-未判定 1-阴性 2-阳性 3-未见异常）
     */
    private Integer positiveFlag;

    private String positiveFlagText;

    /**
     * 是否危急（0-否 1-是）
     */
    private Integer isCritical;

    /**
     * 报告书写人
     */
    private String writeBy;

    /**
     * 报告书写时间
     */
    private LocalDateTime writeTime;

    /**
     * 审核人（初审）
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
     * 报告版本号（被退回重写过几次看这个）
     */
    private Integer reportVersion;

    /**
     * 退回原因
     */
    private String rejectReason;

    /**
     * 已登记胶片张数
     */
    private Integer filmCount;

    /**
     * 报告签名ID（null=未签）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /**
     * 审核签名ID（null=未签）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
