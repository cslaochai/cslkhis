package com.his.platform.service;

import com.his.platform.vo.SimClinicRosterVO;

import java.util.List;

/**
 * 仿真实验室门诊花名册：从真实岗位/账号/诊室数据组装出诊单元。
 */
public interface SimRosterService {

    /**
     * 门诊科室花名册：每科一个出诊医生单元，挂本科室分诊护士账号。
     */
    List<SimClinicRosterVO> clinicRoster();
}
