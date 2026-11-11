package com.his.app.service;

import com.his.common.base.PageResult;
import com.his.spi.vo.PatientCheckupRecordVO;

/**
 * 患者端体检报告（只读）。
 */
public interface MiniCheckupService {

    PageResult<PatientCheckupRecordVO> myRecords(Long patientId, int pageNum, int pageSize);

    PatientCheckupRecordVO recordDetail(Long patientId, Long recordId);
}
