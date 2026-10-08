package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 检验危急值闭环记录实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_critical_value")
public class BizCriticalValue extends BaseEntity {

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

    /**
     * 检验项目编码
     */
    private String itemCode;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 危急值结果值
     */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 危急值类型（1-偏低 2-偏高）
     */
    private Integer criticalType;

    /**
     * 阈值说明（如：高于 6 mmol/L）
     */
    private String thresholdText;

    /**
     * 危急值描述
     */
    private String criticalDesc;

    /**
     * 报告科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportDeptId;

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
     * 处置时限（报告时间 + 时限分钟）。
     * 「超时预警」由查询时按本字段实时计算，不落库为状态 —— 存下来必然与真实时间脱节。
     */
    private LocalDateTime deadlineTime;

    /**
     * 通知状态（0-未通知 1-已通知）
     */
    private Integer notifyStatus;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 超时升级状态（0-未升级 1-已升级）。
     * 「超时」本身实时算，但「有没有为这次超时发过升级通知」必须落库 ——
     * 否则每轮扫描都会重复发，升级消息变成消息轰炸。
     */
    private Integer escalateStatus;

    /**
     * 升级时间
     */
    private LocalDateTime escalateTime;

    /**
     * 闭环状态（1-待接收 2-已接收 3-已处置 4-已作废）
     */
    private Integer status;

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
