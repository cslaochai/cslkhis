package com.his.miniapp.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.api.service.MedicalTechReportApi;
import com.his.api.vo.BizReportVO;
import com.his.miniapp.dto.ReportQueryPageDTO;
import com.his.miniapp.service.MiniReportService;
import com.his.miniapp.util.MiniappPdfStub;
import com.his.miniapp.vo.MiniReportPdfVO;
import com.his.api.service.PatientApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 患者端报告读取（二期）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniReportServiceImpl implements MiniReportService {

    /** 已发布 */
    private static final Integer PUBLISHED = 4;

    private final MedicalTechReportApi medicalTechReportApi;
    private final PatientApi patientApi;

    @Override
    public PageResult<BizReportVO> myReportPage(ReportQueryPageDTO dto) {
        if (!patientApi.canAccessPatient(dto.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的报告");
        }
        return medicalTechReportApi.selectReportPageVO(dto.getPatientId(), dto.getReportType(),
                PUBLISHED, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public BizReportVO myReportDetail(Long reportId) {
        BizReportVO report = medicalTechReportApi.getReportDetailVO(reportId);
        if (report == null) {
            return null;
        }
        if (patientApi.patientScopeViolated(report.getPatientId())) {
            throw new BusinessException("无权查看该报告");
        }
        if (!PUBLISHED.equals(report.getReportStatus())) {
            throw new BusinessException("报告尚未发布");
        }
        return report;
    }

    @Override
    public MiniReportPdfVO reportPdf(Long reportId) {
        BizReportVO report = medicalTechReportApi.getReportDetailVO(reportId);
        if (report == null || patientApi.patientScopeViolated(report.getPatientId())) {
            return MiniReportPdfVO.deny(403);
        }
        if (!PUBLISHED.equals(report.getReportStatus())) {
            return MiniReportPdfVO.deny(400);
        }
        log.info("[报告PDF口子] ===== 打印生成报告 PDF ===== reportNo={} patientId={}",
                report.getReportNo(), report.getPatientId());
        byte[] pdf = MiniappPdfStub.reportPdf(report.getReportNo(), report.getPatientName(),
                report.getItemName(), report.getApplyDeptName(), report.getApplyDoctorName(),
                report.getPublishTime());
        return MiniReportPdfVO.of(report.getReportNo() + ".pdf", pdf);
    }
}
