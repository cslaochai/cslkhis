package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 草稿留痕列表出参（AI 管理台）
 */
@Data
public class AiDraftDiffListVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 接诊科室名称
     */
    private String deptName;

    /**
     * 终审医生姓名
     */
    private String doctorName;

    /**
     * AI草稿原文（截断2000字）
     */
    private String draftText;

    /**
     * 医生终稿（截断2000字）
     */
    private String finalText;

    /**
     * 差异分段JSON（0-相同 1-删 2-增）
     */
    private String diffJson;

    /**
     * 是否修改（1-有修改 0-未修改）
     */
    private Integer changed;

    /**
     * 留痕时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
