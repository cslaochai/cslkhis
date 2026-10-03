package com.his.miniapp.service;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.PatientDetailVO;

import java.util.List;

/**
 * 患者端挂号目录读侧（科室/医生/号源/就诊人档案）。
 */
public interface MiniappDirectoryService {

    List<DeptSelectListVO> openDepartments();

    List<DoctorSelectListVO> doctorsByDept(Long deptId);

    List<ScheduleSelectListVO> schedules(ScheduleSelectQueryDTO queryDTO);

    /**
     * 只允许访问当前账号绑定关系内的就诊人，越权直接拒绝。
     */
    PatientDetailVO patientProfile(Long patientId);
}
