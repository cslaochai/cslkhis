package com.his.clinical.vo;

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

    /** 分组键（按码值分组时是码值文本，按科室分组时是科室ID的文本） */
    private String key;

    /**
     * 名称
     */
    private String name;

    /** 该分组条数 */
    private Long count;

    /** 该分组百分制均分（按百分制得分求平均） */
    private BigDecimal avgScore;

    /** 该分组占比（百分比，1 位小数；分母为 0 时给 0） */
    private BigDecimal rate;

    public SurveyStatItemVO(String key, String name, Long count) {
        this.key = key;
        this.name = name;
        this.count = count;
    }
}
