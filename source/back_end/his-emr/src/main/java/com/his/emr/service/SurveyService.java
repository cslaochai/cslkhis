package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.*;
import com.his.emr.support.FollowupTaskSnapshot;
import com.his.emr.vo.SurveyAnswerVO;
import com.his.emr.vo.SurveyDispatchVO;
import com.his.emr.vo.SurveyStatVO;

/**
 * 满意度评价发放/回收与看板服务。
 */
public interface SurveyService {

    // 发放与回收

    /**
     * 发放/回收分页（科室范围按登录岗位强制收口，手机号出参脱敏）
     */
    PageResult<SurveyDispatchVO> dispatchListPage(SurveyDispatchQueryPageDTO dto);

    /**
     * 发放单详情（外呼要拨号，这条给明文手机号）
     */
    SurveyDispatchVO dispatchGetById(Long id);

    /**
     * 为一条随访任务发放问卷（幂等：同一任务同一卷只有一条发放记录）。
     *
     * @param sourceType 1-随访任务自动发放 / 3-人工补发
     * @return 已回收的那条（重复调用返回既有发放单）；未配置启用问卷时返回 null，
     * 调用方（随访完成）据此留痕但**不阻断随访**
     */
    SurveyDispatchVO issueForFollowup(FollowupTaskSnapshot task, Integer sourceType,
                                      Integer channel, Integer expireDays);

    /**
     * 人工补发：按随访任务ID补一张卷（幂等口径与自动发放一致）。
     *
     * <p>与 {@link #issueForFollowup} 分开是因为任务快照的取法不同：自动发放时随访侧手上就有任务对象，
     * 人工补发只有一个ID。评价侧按同模块的随访任务读一次，不回调 FollowupTaskService，
     * 否则「完成随访→发卷」和「发卷→取任务」会构成 Bean 循环。
     */
    SurveyDispatchVO issueFromFollowup(SurveyDispatchIssueDTO dto);

    /**
     * 状态推进：标记已推送 / 标记患者拒答（不允许手工置「已回收」，那只能由答卷写入）
     */
    SurveyDispatchVO markDispatch(SurveyDispatchActionDTO dto);

    // 答卷

    /**
     * 答卷分页
     */
    PageResult<SurveyAnswerVO> answerListPage(SurveyAnswerQueryPageDTO dto);

    /**
     * 答卷详情（含逐题答案）
     */
    SurveyAnswerVO answerGetById(Long id);

    /**
     * 回收录入（同一次发放重填覆盖明细；低分自动转投诉）
     */
    SurveyAnswerVO submitAnswer(SurveyAnswerUpsertDTO dto);

    /**
     * 作废答卷（填错/重复；发放单退回待回收，答卷留档不删）
     */
    SurveyAnswerVO voidAnswer(SurveyAnswerVoidDTO dto);

    // 看板

    /**
     * 满意度看板（templateId/scene 可空=全部；科室范围强制收口）
     */
    SurveyStatVO stat(Long templateId, Integer scene, String dateFrom, String dateTo);
}
