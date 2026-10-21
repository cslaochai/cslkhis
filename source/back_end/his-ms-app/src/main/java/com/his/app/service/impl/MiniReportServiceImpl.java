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

import java.util.List;

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
        // TODO(spi 扩展) 已发布报告分页待 MedicalTechFacadeService#myPublishedReportPage 扩展，
        // 见 spi-extension-requests.txt；现有端口无此能力，先返回空页，归属校验仍在本方收口。
        log.warn("[患者端报告] 报告分页端口待扩展，patientId={} reportType={}", dto.getPatientId(), dto.getReportType());
        return PageResult.of(0, dto.getPageNum(), dto.getPageSize(), 0, List.of());
    }

    @Override
    public AiReportVO myReportDetail(Long reportId) {
        // TODO(spi 扩展) 报告详情待 MedicalTechFacadeService#getPublishedReportDetail 扩展，
        // 见 spi-extension-requests.txt；现有端口无此能力，先返回 null（前端按无报告处理）。
        log.warn("[患者端报告] 报告详情端口待扩展，reportId={}", reportId);
        return null;
    }

    @Override
    public MiniReportPdfVO reportPdf(Long reportId) {
        // TODO(spi 扩展) 报告详情与 PDF 取数待 MedicalTechFacadeService 扩展后接回，
        // 见 spi-extension-requests.txt；现有端口取不到报告，按无权/无报告拒绝出件。
        log.warn("[患者端报告] 报告 PDF 端口待扩展，reportId={}", reportId);
        return MiniReportPdfVO.deny(403);
    }
}
