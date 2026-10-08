package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 一条可定位的数据质量问题（P5.3）。
 */
@Data
@Schema(description = "数据质量问题明细（可定位）")
public class QualityIssueVO {

    /** 规则编码 */
    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    /** 维度 */
    @Schema(description = "所属维度码")
    private String dimension;

    @Schema(description = "所属维度")
    private String dimensionText;

    @Schema(description = "严重度（1-提示 2-警告 3-严重）")
    private Integer severity;

    @Schema(description = "严重度文案")
    private String severityText;

    @Schema(description = "问题所在表")
    private String tableName;

    @Schema(description = "记录ID（对应 tableName 的主键）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    @Schema(description = "记录业务单号")
    private String recordNo;

    /** 患者ID */
    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    @Schema(description = "患者号")
    private String patientNo;

    /** 患者姓名 */
    @Schema(description = "患者姓名")
    private String patientName;

    /** 科室名称 */
    @Schema(description = "科室")
    private String deptName;

    /** 医生姓名 */
    @Schema(description = "责任医师")
    private String doctorName;

    @Schema(description = "问题描述（具体到缺哪个字段、差多少金额）")
    private String detail;

    @Schema(description = "整改建议")
    private String suggestion;

    @Schema(description = "该规则本次检查的总数（分母）")
    private Long checkedTotal;

    @Schema(description = "问题发生/记录时间")
    private String occurredTime;
}
