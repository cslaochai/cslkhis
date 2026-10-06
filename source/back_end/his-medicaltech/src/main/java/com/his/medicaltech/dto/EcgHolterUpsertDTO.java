package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Holter 动态心电分析入参（sql/173）。
 *
 * <p>日期入参一律空格分隔 pattern（AGENTS 铁律）：声明了 pattern 后 Jackson 只认
 * "yyyy-MM-dd HH:mm:ss"，前端 value-format 必须对齐，传 ISO T 分隔会直接 400。
 */
@Data
public class EcgHolterUpsertDTO {

    /**
     * 检查记录ID
     */
    @NotNull(message = "缺少检查记录")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 开始佩戴时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime wearStartTime;

    /**
     * 结束佩戴时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime wearEndTime;

    /**
     * 总心搏数
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
     * 最长停搏时长（ms）
     */
    private Integer longestPauseMs;

    /**
     * ST段异常发作阵数
     */
    private Integer stEpisodeCount;

    /**
     * 24小时逐时平均心率（JSON 数组字符串，下标 0~23 = 小时）
     */
    private String hourlyHrJson;
}
