package com.his.patient.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 护理文书录入 / 修改入参。
 *
 * <p>{@code id} 为空 = 新增（同一患者 + 同类型 + 同一测量时点已存在则由服务层拒绝，不静默覆盖）；
 * 不为空 = 修改（草稿/已提交可改，已归档拒改）。
 *
 * <p>取值约束由服务层校验：
 * <ul>
 *   <li>三测单/生命体征监测至少要有一个体征值（体温/脉搏/呼吸/血压/SpO2 之一），
 *       否则"录了一条什么都没有的记录"，曲线与统计都会被污染；</li>
 *   <li>护理记录单必须有 {@code nursingContent}（护理记录单的正文就是它的全部意义）。</li>
 * </ul>
 */
@Data
public class NursingRecordUpsertDTO implements Serializable {

    /**
     * 文书ID（为空 = 新增）
     */
    private Long id;

    /**
     * 入院ID（新增必填）
     */
    private Long admissionId;

    /**
     * 文书类型：1-三测单 2-护理记录单 3-生命体征监测
     */
    private Integer nursingType;

    /**
     * 测量 / 记录时间（新增必填；三测单按时点唯一）
     */
    private LocalDateTime measureTime;

    /**
     * 班次：1-白班 2-小夜班 3-大夜班
     */
    private Integer shift;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 血氧饱和度（%）
     */
    private Integer spo2;

    /**
     * 大便次数（次/日）
     */
    private Integer stoolCount;

    /**
     * 尿量（ml）
     */
    private Integer urineVolume;

    /**
     * 入量（ml）
     */
    private Integer intakeVolume;

    /**
     * 出量（ml）
     */
    private Integer outputVolume;

    /**
     * 护理级别：1-特级护理 2-一级护理 3-二级护理 4-三级护理
     */
    private Integer nursingLevel;

    /**
     * 护理措施与病情观察记录正文
     */
    private String nursingContent;

    /**
     * 备注
     */
    private String remark;
}
