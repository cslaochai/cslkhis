package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 危急值出参
 * <p>
 * 在实体字段之外补三类<b>给前端直接用</b>的派生字段：中文状态、超时标志、带箭头的结果文本。
 * 放在后端算的原因：超时判定依赖服务器时间，前端算会因为客户端时钟不准而漂移。
 */
@Data
public class BizCriticalValueVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 危急值号
     */
    private String criticalNo;

    /**
     * 检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检验记录号
     */
    private String recordNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 性别文本
     */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 检验项目编码
     */
    private String itemCode;

    /**
     * 检验项目名称
     */
    private String itemName;

    /** 危急值结果值 */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 带方向箭头的结果文本，如 {@code 6.8 mmol/L ↑}
     */
    private String resultText;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 危急值类型（1-偏低 2-偏高）
     */
    private Integer criticalType;

    /**
     * 危急值类型文本
     */
    private String criticalTypeText;

    /**
     * 阈值说明
     */
    private String thresholdText;

    /**
     * 危急值描述
     */
    private String criticalDesc;

    /**
     * 报告科室
     */
    private String reportDeptName;

    /**
     * 报告人
     */
    private String reportBy;

    /**
     * 报告时间
     */
    private LocalDateTime reportTime;

    /**
     * 处置时限
     */
    private LocalDateTime deadlineTime;

    /**
     * 是否已超时未处置（实时计算）
     */
    private Boolean overdue;

    /**
     * 通知状态（0-未通知 1-已通知）
     */
    private Integer notifyStatus;

    /**
     * 闭环状态（1-待接收 2-已接收 3-已处置 4-已作废）
     */
    private Integer status;

    /**
     * 闭环状态文本
     */
    private String statusText;

    /**
     * 接收人
     */
    private String receiveBy;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 处置人
     */
    private String handleBy;

    /**
     * 处置时间
     */
    private LocalDateTime handleTime;

    /**
     * 处置措施
     */
    private String handleMeasure;

    /**
     * 来源（恒为 RULE）
     */
    private String source;
}
