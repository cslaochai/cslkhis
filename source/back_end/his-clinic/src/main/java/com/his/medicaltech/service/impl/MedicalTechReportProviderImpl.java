package com.his.medicaltech.service.impl;

import com.his.provider.service.MedicalTechReportProvider;
import com.his.provider.vo.BizReportVO;
import com.his.common.base.PageResult;
import com.his.medicaltech.service.MedicalTechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 患者端报告读取端口实现（his-medicaltech）：委托 {@code MedicalTechService}，对外只暴露 his-api 契约。
 */
@Service
@RequiredArgsConstructor
public class MedicalTechReportProviderImpl implements MedicalTechReportProvider {

    private final MedicalTechService medicalTechService;

    @Override
    public PageResult<BizReportVO> selectReportPageVO(Long patientId, Integer reportType, Integer reportStatus,
                                                     int pageNum, int pageSize) {
        return medicalTechService.selectReportPageVO(patientId, reportType, reportStatus, pageNum, pageSize);
    }

    @Override
    public BizReportVO getReportDetailVO(Long reportId) {
        return medicalTechService.getReportDetailVO(reportId);
    }
}
