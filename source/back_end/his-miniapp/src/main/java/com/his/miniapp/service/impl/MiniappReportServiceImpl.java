package com.his.miniapp.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizReportVO;
import com.his.miniapp.dto.ReportQueryPageDTO;
import com.his.miniapp.service.MiniappReportService;
import com.his.miniapp.util.MiniappPdfStub;
import com.his.miniapp.vo.ReportPdfVO;
import com.his.patient.service.PatientGuardianService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 患者端报告读取（二期）。
 *
 * <p>两条闸：就诊人绑定关系（本人或已绑定的家人）、报告必须已发布。
 * 少了第一条，患者端换个 reportId 就能读别人的化验单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappReportServiceImpl implements MiniappReportService {

    /** 已发布 */
    private static final Integer PUBLISHED = 4;

    private final MedicalTechService medicalTechService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public PageResult<BizReportVO> myReportPage(ReportQueryPageDTO dto) {
        if (!patientGuardianService.canAccessPatient(dto.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的报告");
        }
        return medicalTechService.selectReportPageVO(dto.getPatientId(), dto.getReportType(),
                PUBLISHED, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public BizReportVO myReportDetail(Long reportId) {
        BizReportVO report = medicalTechService.getReportDetailVO(reportId);
        if (report == null) {
            return null;
        }
        if (patientGuardianService.patientScopeViolated(report.getPatientId())) {
            throw new BusinessException("无权查看该报告");
        }
        if (!PUBLISHED.equals(report.getReportStatus())) {
            throw new BusinessException("报告尚未发布");
        }
        return report;
    }

    @Override
    public ReportPdfVO reportPdf(Long reportId) {
        BizReportVO report = medicalTechService.getReportDetailVO(reportId);
        if (report == null || patientGuardianService.patientScopeViolated(report.getPatientId())) {
            return ReportPdfVO.deny(403);
        }
        if (!PUBLISHED.equals(report.getReportStatus())) {
            return ReportPdfVO.deny(400);
        }
        log.info("[报告PDF口子] ===== 打印桩生成报告 PDF ===== reportNo={} patientId={}",
                report.getReportNo(), report.getPatientId());
        byte[] pdf = MiniappPdfStub.reportPdf(report.getReportNo(), report.getPatientName(),
                report.getItemName(), report.getApplyDeptName(), report.getApplyDoctorName(),
                report.getPublishTime());
        return ReportPdfVO.of(report.getReportNo() + ".pdf", pdf);
    }
}
