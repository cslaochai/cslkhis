package com.his.provider.service;

import com.his.provider.vo.BizReportVO;
import com.his.common.base.PageResult;

/**
 * 患者端报告读取端口（his-medicaltech 实现）。
 *
 * <p>miniapp 只依赖本端口，不直接依赖 his-medicaltech 的 {@code MedicalTechService}。
 */
public interface MedicalTechReportProvider {

    /**
     * 患者端报告分页（按就诊人 + 报告类型 + 状态）。
     */
    PageResult<BizReportVO> selectReportPageVO(Long patientId, Integer reportType, Integer reportStatus,
                                              int pageNum, int pageSize);

    /**
     * 报告详情（含影像帧），报告不存在时返回 null。
     */
    BizReportVO getReportDetailVO(Long reportId);
}
