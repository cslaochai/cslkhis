package com.his.emr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计维度项（key=码值/科室ID，name=展示名，count=条数，avgScore=均分，rate=百分比）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatItemVO implements Serializable {

    private String key;

    /**
     * 名称
     */
    private String name;

    private Long count;

    private BigDecimal avgScore;

    private BigDecimal rate;

    public SurveyStatItemVO(String key, String name, Long count) {
        this.key = key;
        this.name = name;
        this.count = count;
    }
}
