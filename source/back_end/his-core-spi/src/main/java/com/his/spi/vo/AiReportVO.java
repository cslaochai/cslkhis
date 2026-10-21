package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报告主档摘要：患者端报告解读与影像解读要读的正文、状态与归属列。
 */
@Data
@NoArgsConstructor
public class AiReportVO {

    /**
     * 报告ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 报告号
     */
    private String reportNo;

    /**
     * 报告类型（区分检验报告与检查报告）
     */
    private Integer reportType;

    /**
     * 报告状态
     */
    private Integer reportStatus;

    /**
     * 关联执行记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者ID（供调用方做数据范围核查）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 项目/检查名称
     */
    private String itemName;

    /**
     * 申请科室名称（患者端报告列表展示开单科室）
     */
    private String applyDeptName;

    /**
     * 申请医生姓名（患者端报告列表展示开单医生）
     */
    private String applyDoctorName;

    /**
     * 检查方法
     */
    private String examMethod;

    /**
     * 阳性标志
     */
    private Integer positiveFlag;

    /**
     * 是否危急（1-是）
     */
    private Integer isCritical;

    /**
     * 报告所见
     */
    private String reportContent;

    /**
     * 报告结论
     */
    private String conclusion;

    /**
     * 报告建议
     */
    private String suggestions;

    /**
     * 报告发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
