package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.vo.BizReportVO;
import com.his.miniapp.dto.ReportQueryPageDTO;
import com.his.miniapp.vo.MiniReportPdfVO;

/**
 * 患者端报告（检查/检验）：报告事实来自医技域，这里只加「绑定就诊人 + 只读已发布」这一层收口。
 */
public interface MiniReportService {

    /**
     * 我的报告分页
     */
    PageResult<BizReportVO> myReportPage(ReportQueryPageDTO dto);

    /**
     * 报告详情（含影像帧）；报告不存在时返回 null
     */
    BizReportVO myReportDetail(Long reportId);

    /**
     * 报告原文 PDF（打印）
     */
    MiniReportPdfVO reportPdf(Long reportId);
}
