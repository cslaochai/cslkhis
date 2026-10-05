package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告出参
 */
@Data
public class BizReportVO {
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

    /**
     * 报告编号（唯一）
     */
    private String reportNo;

    /**
     * 报告类型：1-检查报告 2-检验报告
     */
    private Integer reportType;

    /**
     * 关联记录ID（检查记录ID或检验记录ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查/检验记录号
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
     * 项目名称
     */
    private String itemName;

    /**
     * 检查/检验科室名称
     */
    private String examDeptName;

    /**
     * 申请科室名称
     */
    private String applyDeptName;

    /**
     * 申请医生姓名
     */
    private String applyDoctorName;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 报告内容
     */
    private String reportContent;

    /**
     * 报告结论
     */
    private String conclusion;

    /**
     * 建议
     */
    private String suggestions;

    /**
     * 报告状态（1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废）
     */
    private Integer reportStatus;

    /**
     * 审核人（初审）
     */
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
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 发布人
     */
    private String publishBy;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 报告文件路径（PDF）
     */
    private String reportFilePath;

    /**
     * 随报告带出的影像帧（简化 PACS，sql/137）：
     * 报告 → 执行记录 → 申请单三跳由后端算完（ExamImageService.listByReportId），
     * 医生站与患者端小程序拿到就能直接画，不必各自再翻一遍申请单。
     */
    private java.util.List<com.his.medicaltech.vo.ExamImageVO> images;

    /**
     * 随报告带出的检验结果明细（仅 reportType=2 检验报告有值）。
     *
     * <p>报告正文 {@code reportContent} 只是一段自由文本，逐项的结果值、参考区间、
     * 上下箭头只在检验结果明细里 —— 不一起带出来，患者端就只能看到一整段话。
     */
    private java.util.List<com.his.medicaltech.vo.BizLabResultVO> labItems;
}

