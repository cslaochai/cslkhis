package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查记录出参
 */
@Data
public class BizInspectionRecordVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /** 检查记录号（唯一） */
    private String recordNo;

    /**
     * 检查申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 检查申请单号
     */
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
     * 性别：1-男 2-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 开单科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 开单科室名称
     */
    private String applyDeptName;

    /**
     * 开单医生姓名
     */
    private String applyDoctorName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 执行检查科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionDeptId;

    /**
     * 执行检查科室名称
     */
    private String inspectionDeptName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 项目价格，单位：元
     */
    private BigDecimal price;

    /**
     * 预约检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;

    /**
     * 签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    /**
     * 执行检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 执行人
     */
    private String executeBy;

    /**
     * 检查所见（结果描述）
     */
    private String resultDescription;

    /**
     * 检查结论
     */
    private String resultConclusion;

    /**
     * 检查影像路径（多张以逗号分隔）
     */
    private String resultImage;

    /**
     * 记录状态：1-待签到 2-已签到 3-检查中 4-已执行 5-已审核 6-已发布
     */
    private Integer recordStatus;

    /** 审核人（初审） */
    private String auditBy;

    /**
     * 初审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 终审人
     */
    private String audit2By;

    /**
     * 终审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime audit2Time;

    /**
     * 报告生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /** 报告发布人 */
    private String reportBy;

    /**
     * 报告医师签名ID（为空 = 未签报告名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /**
     * 报告签名时刻
     */
    private LocalDateTime reportSignedTime;

    /**
     * 审核医师签名ID（为空 = 未签审核名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 审核签名时刻
     */
    private LocalDateTime auditSignedTime;

    /**
     * 检查项目类型（检查项目字典的项目类型：1-放射 2-超声 3-心电图 4-内镜 5-其他；查不到为 null）。
     *
     * <p>sql/138 分岗用：前端靠它决定这一行给技师的按钮是「拍片完成」还是「录入」。
     * 没有它前端只能把两个按钮都摆出来，技师点错一次就撞上服务端的拒绝 ——
     * 「点了才告诉我不能点」是最没必要的挫败感。
     */
    private Integer itemType;
}
