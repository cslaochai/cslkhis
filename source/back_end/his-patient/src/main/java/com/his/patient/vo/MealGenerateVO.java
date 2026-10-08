package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 批量生成订餐的结果。
 */
@Data
public class MealGenerateVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mealDate;

    /**
     * 扫到的可订餐方案数（执行中 + 口服）
     */
    private Integer planCount;

    /**
     * 实际生成的餐条数
     */
    private Integer generatedCount;

    /**
     * 因已配送/已签收而未重生成的患者数（overwrite=true 时才可能非 0）
     */
    private Integer skippedCount;

    /**
     * 消息内容
     */
    private String message;
}
