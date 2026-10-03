package com.his.report.service;

import com.his.common.base.PageResult;
import com.his.report.dto.QualityIssueQueryPageDTO;
import com.his.report.vo.QualityDimensionSelectListVO;
import com.his.report.vo.QualityIssueVO;
import com.his.report.vo.QualityRuleVO;
import com.his.report.vo.QualitySummaryVO;

import java.util.List;

/**
 * 数据质量报表（P5.3）。
 *
 * <p>一句话职责：<b>把"数据质量"从一句形容词变成一份能派人去改的清单。</b>
 *
 * <p>五个维度（完整性 / 一致性 / 及时性 / 唯一性 / 有效性）各自回答一个不同的问题，
 * 每条规则都必须给出"在多少条里命中了多少条"，并且每条问题都能定位到
 * 具体表、具体主键、具体患者。做不到这三点的报表，看的时候很有用，用的时候没用。
 */
public interface QualityService {

    /** 五维度总览 + 规则清单（一次拿全，供首屏） */
    QualitySummaryVO getSummary();

    /** 规则清单（可按维度过滤） */
    List<QualityRuleVO> getRuleList(String dimension);

    /** 维度字典（含每个维度的含义说明） */
    List<QualityDimensionSelectListVO> dimensionDict();

    /** 问题清单（分页，可按维度 / 规则 / 严重度 / 关键字过滤） */
    PageResult<QualityIssueVO> listIssuePage(QualityIssueQueryPageDTO dto);
}
