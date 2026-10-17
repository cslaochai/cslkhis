package com.his.provider.service;

import com.his.provider.dto.ScheduleSelectQueryDTO;
import com.his.provider.vo.ClinicSourceVO;

import java.util.List;

/**
 * 患者端号源查询端口（his-system 实现，不走院内科室数据权限）。
 *
 * <p>患者身份没有 employeeId，不能走院内科室收口；按患者显式选择的 deptId/doctorId/visitDate 直接取数。
 */
public interface ClinicSourceProvider {

    /**
     * 查询号源列表（患者端挂号目录用）。
     */
    List<ClinicSourceVO> scheduleSelectListUnbounded(ScheduleSelectQueryDTO queryDTO);
}
