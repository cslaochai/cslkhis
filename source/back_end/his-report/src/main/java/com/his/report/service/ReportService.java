package com.his.report.service;

import com.his.report.vo.ChargeStatsVO;
import com.his.report.vo.DrugStatsVO;
import com.his.report.vo.MedicalTechStatsVO;
import com.his.report.vo.OutpatientStatsVO;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取门诊统计
     */
    OutpatientStatsVO getOutpatientStats(String startDate, String endDate);

    /**
     * 获取收费统计
     */
    ChargeStatsVO getChargeStats(String startDate, String endDate);

    /**
     * 获取药品统计
     */
    DrugStatsVO getDrugStats(String startDate, String endDate);

    /**
     * 获取医技统计
     */
    MedicalTechStatsVO getMedicalTechStats(String startDate, String endDate);
}
