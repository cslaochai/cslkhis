package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 近 30 日满意度回收统计一行（BiMapper#survey30d）。
 */
@Data
public class BiSurveyRowVO implements Serializable {

    /** 回收份数 */
    private Long count;

    /** 李克特均分（1.00~5.00） */
    private BigDecimal avgScore;

    /** 满意份数（均分 >= 4） */
    private Long satisfied;
}
