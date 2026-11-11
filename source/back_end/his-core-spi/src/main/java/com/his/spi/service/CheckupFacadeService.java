package com.his.spi.service;

import com.his.common.base.PageResult;
import com.his.spi.vo.PatientCheckupRecordVO;

/**
 * 体检报告患者查询端口（实现落在 his-ms-patient）。
 *
 * <p>只读：体检登记/结果录入/总检仍是体检中心员工动作；患者只看本人已登记单与已出的报告。</p>
 */
public interface CheckupFacadeService {

    /**
     * 我的体检记录（按体检日期倒序，不含明细）。
     */
    PageResult<PatientCheckupRecordVO> myRecords(Long patientId, int pageNum, int pageSize);

    /**
     * 体检报告详情（含逐项结果；调用方负责归属校验）。
     */
    PatientCheckupRecordVO recordDetail(Long recordId);
}
