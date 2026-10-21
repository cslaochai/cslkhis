package com.his.app.service;

import com.his.app.vo.MiniDeptSelectListVO;
import com.his.spi.dto.AppointQueryDTO;
import com.his.app.vo.MiniDoctorSelectListVO;
import com.his.app.vo.MiniPatientDetailVO;
import com.his.app.vo.MiniScheduleSelectVO;

import java.util.List;

/**
 * 患者端挂号目录读侧（科室/医生/号源/就诊人档案）。
 */
public interface MiniDirectoryService {

    List<MiniDeptSelectListVO> openDepartments();

    List<MiniDoctorSelectListVO> doctorsByDept(Long deptId);

    List<MiniScheduleSelectVO> schedules(AppointQueryDTO queryDTO);

    /**
     * 只允许访问当前账号绑定关系内的就诊人，越权直接拒绝。
     */
    MiniPatientDetailVO patientProfile(Long patientId);
}
