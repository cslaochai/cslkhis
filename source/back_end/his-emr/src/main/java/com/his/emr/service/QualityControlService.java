package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.QcCandidateQueryPageDTO;
import com.his.emr.dto.QcExecuteDTO;
import com.his.emr.dto.QcQueryPageDTO;
import com.his.emr.entity.BizQualityControl;
import com.his.emr.support.QcIssue;
import com.his.emr.vo.BizQualityControlVO;
import com.his.emr.vo.QcCandidateVO;
import com.his.emr.vo.QcDimensionSelectListVO;
import com.his.emr.vo.QcOverviewVO;
import com.his.emr.vo.QcRuleMetricVO;
import com.his.emr.vo.QcTypeSelectListVO;

import java.util.List;

/**
 * 病案质控服务接口。
 *
 * <p><b>质控单是留痕，不是状态</b>：同一份病历可以被反复质控，每次生成一张新单，
 * 旧单不删不改。理由很实际 —— 质控员需要回答"这份病历整改前后各扣了多少分"，
 * 覆盖式更新会把这个能力直接删掉。
 */
public interface QualityControlService extends IService<BizQualityControl> {

    /**
     * 质控单分页
     */
    PageResult<BizQualityControlVO> selectQcPage(QcQueryPageDTO query);

    /**
     * 质控单详情（含问题明细）
     */
    BizQualityControlVO getQcDetail(Long qcId);

    /**
     * 对一份病历执行质控并落库
     */
    BizQualityControlVO executeQc(QcExecuteDTO dto);

    /**
     * 批量执行质控（病案室月末批量质控）
     */
    List<BizQualityControlVO> executeQcBatch(String recordSource, List<Long> recordIds, Integer qcType);

    /**
     * 处理质控问题
     */
    boolean handleQc(Long qcId, boolean ignore, String remark);

    /**
     * 质控概览
     */
    QcOverviewVO getOverview();

    /**
     * 规则清单与命中统计（含从未命中的规则）
     */
    List<QcRuleMetricVO> listRuleMetric(Integer dimension);

    /**
     * 某质控单的问题明细
     */
    List<QcIssue> listIssueByQc(Long qcId);

    /**
     * 待质控病历候选分页
     */
    PageResult<QcCandidateVO> listCandidatePage(QcCandidateQueryPageDTO query);

    /**
     * 维度字典
     */
    List<QcDimensionSelectListVO> dimensionDict();

    /**
     * 质控类型字典（0 综合 / 1~3 三维度 / 4 AI 内涵质控）
     */
    List<QcTypeSelectListVO> qcTypeDict();
}
