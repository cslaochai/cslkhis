package com.his.miniapp.service.impl;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.service.BizScheduleService;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniDirectoryMapper;
import com.his.miniapp.service.MiniDirectoryService;
import com.his.miniapp.vo.MiniDeptSelectListVO;
import com.his.miniapp.vo.MiniDoctorSelectListVO;
import com.his.miniapp.vo.MiniScheduleSelectVO;
import com.his.miniapp.vo.MiniPatientDetailVO;
import com.his.patient.service.PatientGuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniDirectoryServiceImpl implements MiniDirectoryService {

    private final MiniDirectoryMapper miniDirectoryMapper;
    private final BizScheduleService bizScheduleService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public List<MiniDeptSelectListVO> openDepartments() {
        return miniDirectoryMapper.selectOpenDepartments();
    }

    @Override
    public List<MiniDoctorSelectListVO> doctorsByDept(Long deptId) {
        return miniDirectoryMapper.selectDoctorsByDept(deptId);
    }

    @Override
    public List<MiniScheduleSelectVO> schedules(ScheduleSelectQueryDTO queryDTO) {
        List<BizSchedule> list = bizScheduleService.scheduleSelectList(queryDTO);
        return list.stream().map(detail -> {
            MiniScheduleSelectVO vo = new MiniScheduleSelectVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public MiniPatientDetailVO patientProfile(Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        return miniDirectoryMapper.selectPatientById(patientId);
    }
}
