package com.his.platform.vo;

import lombok.Data;

/**
 * ICD-10智能预测出参。
 *
 * 只回编码候选本身：权重/费率/费用属医保结算口径（DRG=权重×费率、DIP=分值×点值），
 * 统筹区未下发时按自家铁律放 NULL 不编造（同 MedpaySettleServiceImpl），预测入口不碰。
 */
@Data
public class Icd10PredictVO {

    /**
     * ICD-10编码
     */
    private String icdCode;

    /**
     * ICD-10名称
     */
    private String icdName;

    /**
     * ICD分类
     */
    private String icdCategory;

    /**
     * 匹配得分，越高越相关
     */
    private int score;
}
