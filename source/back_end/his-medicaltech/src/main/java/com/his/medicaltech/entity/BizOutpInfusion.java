package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 门诊输液单（M10）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_outp_infusion")
public class BizOutpInfusion extends BaseEntity {

    /**
     * 输液单号
     */
    private String infusionNo;

    /**
     * 来源治疗记录ID（G19 执行打卡行，可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treatmentRecordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 输注内容摘要（药名/组数，文本快照）
     */
    private String drugSummary;

    /**
     * 座位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long seatId;

    /**
     * 座位号
     */
    private String seatNo;

    /**
     * 皮试记录ID（需皮试的药物判读阴性后才可开始）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long skinTestId;

    /**
     * 状态（1-待皮试 2-待输注 3-输液中 4-已完成 5-已取消）
     */
    private Integer status;

    /**
     * 开始输注时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 起始滴速（滴/分）
     */
    private Integer dripRate;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 不良反应（0-无 1-有）
     */
    private Integer adverseFlag;

    /**
     * 不良反应描述
     */
    private String adverseDesc;

    /**
     * 责任护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 责任护士姓名
     */
    private String nurseName;

    /**
     * 取消原因
     */
    private String cancelReason;
}
