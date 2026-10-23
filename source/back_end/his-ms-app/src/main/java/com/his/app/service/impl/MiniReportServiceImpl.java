package com.his.app.service.impl;

import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.app.dto.ReportPageQueryDTO;
import com.his.app.service.MiniReportService;
import com.his.app.vo.MiniReportPdfVO;
import com.his.spi.service.MedicalTechFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.AiReportVO;
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

    private final MedicalTechFacadeService medicalTechFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public PageResult<AiReportVO> myReportPage(ReportPageQueryDTO dto) {
        if (!patientFacadeService.canAccessPatient(dto.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的报告");
        }
        return medicalTechFacadeService.myPublishedReportPage(
                dto.getPatientId(), dto.getReportType(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public AiReportVO myReportDetail(Long reportId) {
        AiReportVO report = medicalTechFacadeService.getPublishedReportDetail(reportId);
        // 端口只保证「已发布」，归属边界仍在本方收口：非本人可读范围按无报告处理
        if (report == null || !patientFacadeService.canAccessPatient(report.getPatientId())) {
            return null;
        }
        return report;
    }

    @Override
    public MiniReportPdfVO reportPdf(Long reportId) {
        // 报告 PDF 导出依赖文件存储与二进制下发能力，当前仅落文本报告、无文件读取实现，
        // 暂按无权/无报告拒绝出件，待文件存储建成后再接。
        log.warn("[患者端报告] 报告 PDF 导出能力未建，reportId={}", reportId);
        return MiniReportPdfVO.deny(403);
    }
}
