package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.QualityIssueQueryPageDTO;
import com.his.medicaltech.vo.QualityDimensionSelectListVO;
import com.his.medicaltech.vo.QualityIssueVO;
import com.his.medicaltech.vo.QualityRuleVO;
import com.his.medicaltech.vo.QualitySummaryVO;

import java.util.List;

/**
 * 数据质量报表（P5.3）。
 */
public interface QualityService {

    /**
     * 五维度总览 + 规则清单（一次拿全，供首屏）
     */
    QualitySummaryVO getSummary();

    /**
     * 规则清单（可按维度过滤）
     */
    List<QualityRuleVO> getRuleList(String dimension);

    /**
     * 维度字典（含每个维度的含义说明）
     */
    List<QualityDimensionSelectListVO> dimensionDict();

    /**
     * 问题清单（分页，可按维度 / 规则 / 严重度 / 关键字过滤）
     */
    PageResult<QualityIssueVO> listIssuePage(QualityIssueQueryPageDTO dto);
}
