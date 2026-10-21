package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.dto.ReportPageQueryDTO;
import com.his.miniapp.vo.MiniReportPdfVO;
import com.his.spi.vo.AiReportVO;

/**
 * 患者端报告（检查/检验）：报告事实来自医技域，这里只加「绑定就诊人 + 只读已发布」这一层收口。
 */
public interface MiniReportService {

    /**
     * 我的报告分页
     */
    PageResult<AiReportVO> myReportPage(ReportPageQueryDTO dto);

    /**
     * 报告详情（含影像帧）；报告不存在时返回 null
     */
    AiReportVO myReportDetail(Long reportId);

    /**
     * 报告原文 PDF（打印）
     */
    MiniReportPdfVO reportPdf(Long reportId);
}
