package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 点评批次 upsert 入参。
 *
 * <p>新建 = 建批并按日期范围随机抽样（抽样数即明细行数）；修改只允许改批次名称/专项主题/备注
 * —— 日期范围与抽样数决定了抽样口径，建批后不可改，否则台账回答不了"当时怎么抽的"。
 */
@Data
public class RxReviewBatchUpsertDTO {

    /**
     * id 为空 = 新建（抽样）；非空 = 改批次名称/专项主题/备注
     */
    private Long id;

    /**
     * 批次名称
     */
    @NotBlank(message = "批次名称不能为空")
    private String batchName;

    /**
     * 点评类型（1-常规点评 2-专项点评）
     */
    @NotNull(message = "点评类型不能为空")
    @Min(value = 1, message = "点评类型非法")
    @Max(value = 2, message = "点评类型非法")
    private Integer reviewType;

    /**
     * 专项主题（reviewType=2 必填）
     */
    private String specialty;

    /**
     * 处方就诊日期起
     */
    @NotNull(message = "就诊日期起不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateStart;

    /**
     * 处方就诊日期止
     */
    @NotNull(message = "就诊日期止不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateEnd;

    /**
     * 抽样处方数（新建必填；规范：每月门急诊 ≥100 张或 ≥总处方量 1‰）
     */
    @Min(value = 1, message = "抽样数至少 1 张")
    @Max(value = 2000, message = "单批抽样数上限 2000 张")
    private Integer sampleCount;

    /**
     * 备注
     */
    private String remark;
}
