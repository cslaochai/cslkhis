package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ICU 监护记录单（ICU 监护记录单）：一条 = 一个时刻的床边记录。
 *
 * <p>只补 ICU 专有内容（GCS 分项、瞳孔、CVP、呼吸机参数、五类导管、出入量），
 * 普通病区三测仍走护理文书，两表不重复登记。
 * 唯一键 uk(stay_id, record_time) 挡住同一时刻写两条；出科后禁止再写。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_icu_monitor")
public class BizIcuMonitor extends BaseEntity implements Serializable {

    /**
     * 入科记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stayId;

    /**
     * 记录时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    /**
     * 体温 ℃
     */
    private BigDecimal temperature;

    /**
     * 脉搏次/分
     */
    private Integer pulse;

    /**
     * 呼吸次/分
     */
    private Integer respiratory;

    /**
     * 收缩压 mmHg
     */
    private Integer sbp;

    /**
     * 舒张压 mmHg
     */
    private Integer dbp;

    /**
     * 血氧饱和度（%）
     */
    private Integer spo2;

    /**
     * GCS 睁眼 1~4
     */
    private Integer gcsEye;

    /**
     * GCS 语言 1~5
     */
    private Integer gcsVerbal;

    /**
     * GCS 运动 1~6
     */
    private Integer gcsMotor;

    /**
     * GCS 总分（服务端按三项求和）
     */
    private Integer gcsTotal;

    /**
     * 瞳孔
     */
    private String pupil;

    /**
     * 中心静脉压 cmH2O
     */
    private BigDecimal cvp;

    /**
     * 呼吸支持：1-鼻导管/面罩 2-无创 3-有创 4-脱机
     */
    private Integer ventMode;

    /**
     * FiO2 %（21~100）
     */
    private Integer fio2;

    /**
     * 呼气末正压（cmH2O）
     */
    private BigDecimal peep;

    /**
     * 入量 ml
     */
    private BigDecimal intakeMl;

    /**
     * 出量 ml
     */
    private BigDecimal outputMl;

    /**
     * 液体平衡 = 入量 - 出量（服务端回算）
     */
    private BigDecimal fluidBalance;

    /**
     * 尿量 ml
     */
    private Integer urineMl;

    /**
     * 人工气道/气管插管 0-无 1-有（0-无 1-有）
     */
    private Integer hasAirway;

    /**
     * 中心静脉导管 0-无 1-有（0-无 1-有）
     */
    private Integer hasCvc;

    /**
     * 动脉置管 0-无 1-有（0-无 1-有）
     */
    private Integer hasArterial;

    /**
     * 导尿管 0-无 1-有（0-无 1-有）
     */
    private Integer hasCatheter;

    /**
     * 引流管 0-无 1-有（0-无 1-有）
     */
    private Integer hasDrain;

    /**
     * 病情观察
     */
    private String conditionDesc;

    /**
     * 处置/干预
     */
    private String handling;

    /**
     * 记录人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recorderId;

    /**
     * 记录人姓名
     */
    private String recorderName;

    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
}
