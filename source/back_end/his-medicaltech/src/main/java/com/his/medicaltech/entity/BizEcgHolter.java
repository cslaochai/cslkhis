package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Holter 动态心电分析（Holter 动态心电，sql/173）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ecg_holter")
public class BizEcgHolter extends BaseEntity {

    /**
     * 检查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 波形ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waveformId;

    /**
     * 开始佩戴时间
     */
    private LocalDateTime wearStartTime;

    /**
     * 结束佩戴时间
     */
    private LocalDateTime wearEndTime;

    /**
     * 总心搏数（24h 正常约 8~14 万）
     */
    private Integer totalBeats;

    /**
     * 平均心率（次/分）
     */
    private Integer avgHr;

    /**
     * 最快心率（次/分）
     */
    private Integer maxHr;

    /**
     * 最快心率时刻（HH:mm）
     */
    private String maxHrTime;

    /**
     * 最慢心率（次/分）
     */
    private Integer minHr;

    /**
     * 最慢心率时刻（HH:mm）
     */
    private String minHrTime;

    /**
     * 是否检出房颤（0-否 1-是）
     */
    private Integer afibFlag;

    /**
     * 房颤心搏数
     */
    private Integer afibBeats;

    /**
     * 室上性早搏总数
     */
    private Integer svcCount;

    /**
     * 室性早搏总数
     */
    private Integer pvcCount;

    /**
     * 室性心动过速阵数
     */
    private Integer vtCount;

    /**
     * 停搏（长间歇）次数
     */
    private Integer pauseCount;

    /**
     * 最长停搏时长（ms；>2000 为临床关注点）
     */
    private Integer longestPauseMs;

    /**
     * ST段异常发作阵数
     */
    private Integer stEpisodeCount;

    /**
     * 24小时逐时平均心率（JSON 数组，下标 0~23 = 小时）
     */
    private String hourlyHrJson;

    /**
     * 分析人
     */
    private String analysisBy;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;
}
