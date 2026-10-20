package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.LisEqaDTO;
import com.his.medicaltech.vo.LisEqaVO;

/**
 * LIS 室间质评（EQA）服务：质评批次、盲样台账、结果上报与成绩判定、室间差及不合格整改的业务收口。
 */
public interface LisEqaService {

    /**
     * 质评批次分页查询。
     * 用于室间质评页「质评批次」标签的查询卡。
     */
    PageResult<LisEqaVO.PlanVO> planPage(LisEqaDTO.PlanQuery q);

    /**
     * 登记或修改质评批次（组织方、项目、上报截止与成绩回报日），返回批次编号。
     * 用于室间质评页「质评批次」标签的批次登记与调整。
     */
    String planUpsert(LisEqaDTO.PlanUpsert dto);

    /**
     * 归档批次结案；成绩未回报完或仍有未闭环的不合格整改时拒绝。
     * 用于室间质评页批次判定完成后的结案动作。
     */
    void archive(LisEqaDTO.PlanArchive dto);

    /**
     * 盲样台账分页查询。
     * 用于室间质评页「盲样台账」标签的查询卡。
     */
    PageResult<LisEqaVO.SampleVO> samplePage(LisEqaDTO.SampleQuery q);

    /**
     * 登记或修改单条盲样台账，返回盲样编号。
     * 用于室间质评页盲样台账的补录与纠正。
     */
    String sampleUpsert(LisEqaDTO.SampleUpsert dto);

    /**
     * 按样品序号 × 检验项目 × 仪器批量生成盲样台账骨架，已登记的组合不重复生成。
     * 用于收到组织方盲样后的一次性批量登记。
     */
    int sampleGenerate(LisEqaDTO.SampleGenerate dto);

    /**
     * 删除盲样台账，仅限尚未上报的盲样。
     * 用于盲样登记录错后的撤销。
     */
    void sampleDeleteById(Long id);

    /**
     * 录入本室对盲样的检测结果。
     * 用于室间质评页「盲样台账」标签的检测录入。
     */
    LisEqaVO.SampleVO test(LisEqaDTO.TestInput dto);

    /**
     * 向组织方上报本室盲样检测结果；超过上报截止只标记逾期、不拦截。
     * 用于盲样检测完成后的上报环节。
     */
    String report(LisEqaDTO.ReportInput dto);

    /**
     * 登记组织方下发的靶值、组 SD 与 TEa，由服务端判定 PT 得分与是否合格。
     * 用于室间质评页「成绩回报与判定」标签的录入判定。
     */
    LisEqaVO.JudgeVO returnScore(LisEqaDTO.ScoreReturn dto);

    /**
     * 重算本批次的仪器间互差：整体删除后按同项目、同样品两两重插。
     * 成绩回报判定收尾时刷新室间差时调用。
     */
    void rebuildCompare(Long planId);

    /**
     * 室间差（仪器间比对）分页查询。
     * 用于室间质评页「室间差比对」标签的查询卡。
     */
    PageResult<LisEqaVO.CompareVO> comparePage(LisEqaDTO.CompareQuery q);

    /**
     * 登记不合格项的原因与纠正措施，登记后待第二人复核。
     * 用于室间质评页不合格盲样判定后的整改登记。
     */
    void rectify(LisEqaDTO.Rectify dto);

    /**
     * 复核不合格项的整改，复核人不得是整改人本人，通过后该项闭环。
     * 用于室间质评页「成绩回报与判定」标签里的整改复核。
     */
    void rectifyReview(LisEqaDTO.RectifyReview dto);

    /**
     * 室间质评概览统计（批次、合格率、不合格项等）。
     * 用于室间质评页的概览统计展示。
     */
    LisEqaVO.StatsVO stats();
}
