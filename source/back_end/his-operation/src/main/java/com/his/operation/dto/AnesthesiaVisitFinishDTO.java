package com.his.operation.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 完成术前访视（草稿 → 已完成）。
 */
@Data
public class AnesthesiaVisitFinishDTO implements Serializable {

    /**
     * 就诊次ID
     */
    @NotNull(message = "访视单ID不能为空")
    private Long visitId;

    /**
     * 访视结论：1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估
     */
    @NotNull(message = "访视结论不能为空（没有结论的访视等于没访视）")
    @AllowedValues(values = {"1", "2", "3"}, message = "访视结论取值不合法")
    private Integer conclusion;

    /**
     * 结论说明（结论非"可施行麻醉"时必填）
     */
    private String conclusionNote;
}
