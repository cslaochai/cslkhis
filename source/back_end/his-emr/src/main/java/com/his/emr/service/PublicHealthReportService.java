package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.PublicHealthSubmitDTO;
import com.his.emr.entity.BizPublicHealthReport;
import com.his.emr.vo.BizPublicHealthReportVO;

/**
 * 公卫上报服务接口
 */
public interface PublicHealthReportService extends IService<BizPublicHealthReport> {

    /**
     * 查询上报记录列表
     */
    PageResult<BizPublicHealthReportVO> selectReportPage(Long patientId, Integer reportType, Integer reportStatus,
                                                         String patientName, String keyword, int pageNum, int pageSize);

    /**
     * 获取上报详情
     */
    BizPublicHealthReportVO getReportDetail(Long reportId);

    /**
     * 提交上报
     */
    BizPublicHealthReportVO submitReport(PublicHealthSubmitDTO submitDTO);

    /**
     * 审核上报；{@code auditBy} 为空时取当前登录用户实名
     */
    boolean auditReport(Long reportId, boolean approved, String auditBy, String remark);
}
