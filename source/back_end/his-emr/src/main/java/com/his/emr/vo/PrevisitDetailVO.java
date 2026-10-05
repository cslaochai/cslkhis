package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预问诊记录详情出参（患者端回显 + 医生站报告）
 */
@Data
public class PrevisitDetailVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 挂号ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 就诊科室名称 */
    private String deptName;

    /** 主症状 */
    private String mainSymptom;

    /** 问答明细JSON */
    private String answersJson;

    /** 患者补充描述 */
    private String freeText;

    /** 病史摘要（模型凝练或规则模板） */
    private String summaryAi;

    /** 摘要来源（1-模型 2-规则） */
    private Integer summarySource;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
