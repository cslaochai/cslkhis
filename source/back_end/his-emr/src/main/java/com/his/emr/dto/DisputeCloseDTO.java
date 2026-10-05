package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结案入参。
 *
 * <p>结案必须收口三件事：处理途径 + 责任认定 + 赔偿金额（无赔偿填 0），
 * 缺一项不结案 —— 否则「赔了多少、谁的责任」永远查不到，台账就白建了。
 */
@Data
public class DisputeCloseDTO implements Serializable {

    @NotNull(message = "单据ID不能为空")
    private Long id;

    /**
     * 处理途径（1-院内协商 2-医调委调解 3-行政调解 4-司法鉴定 5-诉讼 6-其他）
     */
    @NotNull(message = "处理途径不能为空")
    private Integer dealType;

    /**
     * 责任认定（1-无责 2-轻微责任 3-次要责任 4-主要责任 5-完全责任）
     */
    @NotNull(message = "责任认定不能为空")
    private Integer dutyType;

    /**
     * 赔偿/补偿金额
     */
    @NotNull(message = "赔偿金额不能为空（无赔偿请填 0）")
    private BigDecimal compensation;

    /**
     * 调查结论/处理结果
     */
    @NotBlank(message = "调查结论/处理结果不能为空")
    private String conclusion;
}
