package com.his.emr.service;

import com.his.emr.dto.PrevisitSubmitDTO;
import com.his.emr.vo.PrevisitDetailVO;
import com.his.emr.vo.PrevisitQuestionnaireVO;

/**
 * 预问诊服务（G-05）
 */
public interface PrevisitRecordService {

    /**
     * 量表（题目结构随接口下发，前端不写死）
     */
    PrevisitQuestionnaireVO questionnaire();

    /**
     * 提交问卷（按挂号 upsert：重复提交覆盖更新）。
     * 患者/科室快照从挂号记录反查，不看前端传的。
     */
    PrevisitDetailVO submit(PrevisitSubmitDTO dto);

    /**
     * 按挂号取预问诊记录（无则 null）
     */
    PrevisitDetailVO getByRegist(Long registId);

    /**
     * 写回病史摘要（供 his-ai 的摘要环节落库；source：1-模型 2-规则）
     */
    void saveSummary(Long registId, String summary, Integer source);
}
