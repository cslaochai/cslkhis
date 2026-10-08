package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 心电波形采集（心电波形采集，sql/173）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ecg_waveform")
public class BizEcgWaveform extends BaseEntity {

    /**
     * 波形号（唯一）
     */
    private String waveNo;

    /**
     * 检查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查记录号
     */
    private String recordNo;

    /**
     * 检查申请单ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
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
     * 心电类型（字典 his_ecg_type：1-常规静息心电图 2-24小时动态心电图）
     */
    private Integer ecgType;

    /**
     * 波形数据（JSON：{sampleRate,durationSec,gainMmPerMv,paperSpeedMmPerS,leads:[{name,samples[]}],rhythm:{name,samples[]}}）
     */
    private String waveData;

    /**
     * 采集设备号（模拟采集=SIM）
     */
    private String deviceNo;

    /**
     * 采集人
     */
    private String collectBy;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
}
