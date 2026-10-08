package com.his.miniapp.service;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.miniapp.vo.MiniDeptSelectListVO;
import com.his.miniapp.vo.MiniDoctorSelectListVO;
import com.his.miniapp.vo.MiniPatientDetailVO;
import com.his.miniapp.vo.MiniScheduleSelectVO;

import java.util.List;

/**
 * 患者端挂号目录读侧（科室/医生/号源/就诊人档案）。
 */
public interface MiniDirectoryService {

    List<MiniDeptSelectListVO> openDepartments();

    List<MiniDoctorSelectListVO> doctorsByDept(Long deptId);

    List<MiniScheduleSelectVO> schedules(ScheduleSelectQueryDTO queryDTO);

    /**
     * 只允许访问当前账号绑定关系内的就诊人，越权直接拒绝。
     */
    MiniPatientDetailVO patientProfile(Long patientId);
}
