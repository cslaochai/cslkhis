package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 急诊分诊复评流水 —— 「分诊级别不是一评定终身」这一规范的载体。
 *
 * <p>每次复评落一行：复评前的级别、复评后的级别、当时体征与 MEWS、操作人、原因。
 * 只升不降的校验在服务端（{@code EmergencyServiceImpl#reevaluate}），这里只存事实。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emergency_reeval")
public class EmergencyReeval extends BaseEntity {

    /**
     * 急诊记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long emergencyId;

    /**
     * 急诊号（快照，台账按号可读）
     */
    private String emergencyNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 复评前分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer triageLevelBefore;

    /**
     * 复评后分诊级别（只升不降：后 &le; 前，数值越小越危重）
     */
    private Integer triageLevelAfter;

    /**
     * 复评时 MEWS 评分（0-14，WS/T 810—2022 五项版现算）
     */
    private Integer mewsScore;

    /**
     * 复评时意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应）
     */
    private Integer consciousness;

    /**
     * 复评时生命体征（JSON 快照；未复测则与登记时一致）
     */
    private String vitalSigns;

    /**
     * 复评原因（必填，截到 500 字符）
     */
    private String reason;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
