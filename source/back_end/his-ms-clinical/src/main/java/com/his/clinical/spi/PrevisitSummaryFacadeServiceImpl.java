package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.clinical.entity.BizPrevisitRecord;
import com.his.clinical.mapper.BizPrevisitRecordMapper;
import com.his.common.exception.BusinessException;
import com.his.spi.service.PrevisitSummaryFacadeService;
import com.his.spi.vo.PrevisitDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PrevisitSummaryFacadeService 的病历域实现（叶子端口：只依赖本模块 mapper）。
 */
@Service
@RequiredArgsConstructor
public class PrevisitSummaryFacadeServiceImpl implements PrevisitSummaryFacadeService {

    private final BizPrevisitRecordMapper bizPrevisitRecordMapper;

    @Override
    public PrevisitDetailVO getByRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        BizPrevisitRecord record = bizPrevisitRecordMapper.selectOne(
                new LambdaQueryWrapper<BizPrevisitRecord>()
                        .eq(BizPrevisitRecord::getRegistId, registId));
        return record == null ? null : toVO(record);
    }

    @Override
    public void saveSummary(Long registId, String summary, Integer source) {
        BizPrevisitRecord record = bizPrevisitRecordMapper.selectOne(
                new LambdaQueryWrapper<BizPrevisitRecord>()
                        .eq(BizPrevisitRecord::getRegistId, registId));
        if (record == null) {
            throw new BusinessException("预问诊记录不存在");
        }
        record.setSummaryAi(summary);
        record.setSummarySource(source);
        bizPrevisitRecordMapper.updateById(record);
    }

    private PrevisitDetailVO toVO(BizPrevisitRecord record) {
        PrevisitDetailVO vo = new PrevisitDetailVO();
        vo.setId(record.getId());
        vo.setRegistId(record.getRegistId());
        vo.setPatientId(record.getPatientId());
        vo.setPatientName(record.getPatientName());
        vo.setDeptName(record.getDeptName());
        vo.setMainSymptom(record.getMainSymptom());
        vo.setAnswersJson(record.getAnswersJson());
        vo.setFreeText(record.getFreeText());
        vo.setSummaryAi(record.getSummaryAi());
        vo.setSummarySource(record.getSummarySource());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
