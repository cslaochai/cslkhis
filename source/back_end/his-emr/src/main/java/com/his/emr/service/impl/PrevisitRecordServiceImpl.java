package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.PrevisitSubmitDTO;
import com.his.emr.entity.BizPrevisitRecord;
import com.his.emr.mapper.BizPrevisitRecordMapper;
import com.his.emr.service.PrevisitRecordService;
import com.his.emr.support.PrevisitQuestionnaireSupport;
import com.his.emr.vo.PrevisitDetailVO;
import com.his.emr.vo.PrevisitQuestionnaireVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrevisitRecordServiceImpl implements PrevisitRecordService {

    /** 补充描述入库上限：患者粘贴长文时截断，不让 TEXT 列被单条问卷撑爆 */
    private static final int FREE_TEXT_MAX = 1000;

    private final BizPrevisitRecordMapper previsitRecordMapper;

    private final BizAppointInfoMapper appointInfoMapper;

    private final ObjectMapper objectMapper;

    @Override
    public PrevisitQuestionnaireVO questionnaire() {
        PrevisitQuestionnaireVO vo = new PrevisitQuestionnaireVO();
        vo.setVersion(PrevisitQuestionnaireSupport.VERSION);
        vo.setMainSymptoms(PrevisitQuestionnaireSupport.MAIN_SYMPTOMS);
        vo.setCommonQuestions(PrevisitQuestionnaireSupport.COMMON_QUESTIONS);
        vo.setSymptomQuestions(PrevisitQuestionnaireSupport.SYMPTOM_QUESTIONS);
        return vo;
    }

    @Override
    public PrevisitDetailVO submit(PrevisitSubmitDTO dto) {
        BizAppointInfo appointInfo = appointInfoMapper.selectById(dto.getRegistId());
        if (appointInfo == null) {
            throw new BusinessException("挂号记录不存在");
        }
        // 归属再闸一道：入口（小程序端点）已按登录态校验过，这里按挂号记录反查患者落快照
        BizPrevisitRecord record = previsitRecordMapper.selectOne(
                new LambdaQueryWrapper<BizPrevisitRecord>()
                        .eq(BizPrevisitRecord::getRegistId, dto.getRegistId()));
        boolean isNew = record == null;
        if (isNew) {
            record = new BizPrevisitRecord();
            record.setRegistId(appointInfo.getId());
        }
        record.setPatientId(appointInfo.getPatientId());
        record.setPatientNo(appointInfo.getPatientNo());
        record.setPatientName(appointInfo.getPatientName());
        record.setDeptId(appointInfo.getDeptId());
        record.setDeptName(appointInfo.getDeptName());
        record.setMainSymptom(dto.getMainSymptom().trim());
        record.setAnswersJson(toJson(dto.getAnswers()));
        record.setFreeText(cut(dto.getFreeText(), FREE_TEXT_MAX));
        // 重新提交即重算：旧摘要作废，等 AI 环节重出
        record.setSummaryAi(null);
        record.setSummarySource(null);
        if (isNew) {
            previsitRecordMapper.insert(record);
        } else {
            previsitRecordMapper.updateById(record);
        }
        return toVo(record);
    }

    @Override
    public PrevisitDetailVO getByRegist(Long registId) {
        BizPrevisitRecord record = previsitRecordMapper.selectOne(
                new LambdaQueryWrapper<BizPrevisitRecord>()
                        .eq(BizPrevisitRecord::getRegistId, registId));
        return record == null ? null : toVo(record);
    }

    @Override
    public void saveSummary(Long registId, String summary, Integer source) {
        BizPrevisitRecord record = previsitRecordMapper.selectOne(
                new LambdaQueryWrapper<BizPrevisitRecord>()
                        .eq(BizPrevisitRecord::getRegistId, registId));
        if (record == null) {
            throw new BusinessException("预问诊记录不存在");
        }
        record.setSummaryAi(summary);
        record.setSummarySource(source);
        previsitRecordMapper.updateById(record);
    }

    private PrevisitDetailVO toVo(BizPrevisitRecord record) {
        PrevisitDetailVO vo = new PrevisitDetailVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private String toJson(List<PrevisitSubmitDTO.AnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            // 问答明细序列化失败不该拦提交：摘要还有主症状和补充描述可用
            return "[]";
        }
    }

    private static String cut(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }
}
