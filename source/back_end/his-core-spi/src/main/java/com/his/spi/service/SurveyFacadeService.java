package com.his.spi.service;

import com.his.spi.vo.PatientSurveyPaperVO;
import com.his.spi.vo.PatientSurveyTaskVO;

import java.util.List;

/**
 * 满意度患者填报端口（实现落在 his-ms-clinical）。
 *
 * <p>员工侧 {@code SurveyService} 的发放/回收/看板全部按岗位科室与登录员工收口，
 * 患者账号没有 employeeId 与岗位科室。本端口按就诊人ID取待填问卷、按发放单归属提交答卷，
 * 患者身份由调用方从登录态解析。</p>
 */
public interface SurveyFacadeService {

    /**
     * 我的待填问卷：未回收、未过期，按发放时间倒序。
     */
    List<PatientSurveyTaskVO> myPendingTasks(Long patientId);

    /**
     * 问卷卷面：题目清单 + 是否已填（已填可重填覆盖）。
     */
    PatientSurveyPaperVO paper(Long patientId, Long dispatchId);

    /**
     * 提交答卷。
     *
     * @param anonymousFlag 是否匿名（0/1）
     * @param commentText   开放意见（可空）
     * @param scores        逐题作答（题目ID → 得分/选项/文本，由调用方组装）
     * @return 答卷ID
     */
    Long submit(Long patientId, Long dispatchId, Integer anonymousFlag, String commentText,
                List<PatientSurveyAnswerItemVO> answers);

    /**
     * 逐题作答（跨模块提交用）。
     */
    @lombok.Data
    class PatientSurveyAnswerItemVO {
        /**
         * 题目ID
         */
        private Long itemId;
        /**
         * 得分（量表 1-5 / NPS 0-10）
         */
        private Integer score;
        /**
         * 选项文本（多选用「；」拼接）
         */
        private String optionLabel;
        /**
         * 文本题回答
         */
        private String textValue;
    }
}
