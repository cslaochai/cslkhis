package com.his.miniapp.service.impl;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.service.BizScheduleService;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniappDirectoryMapper;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.MiniappScheduleSelectVO;
import com.his.miniapp.vo.PatientProfileVO;
import com.his.patient.service.PatientGuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniappDirectoryServiceImpl implements MiniappDirectoryService {

    private final MiniappDirectoryMapper miniappDirectoryMapper;
    private final BizScheduleService bizScheduleService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public List<DeptSelectListVO> openDepartments() {
        return miniappDirectoryMapper.selectOpenDepartments();
    }

    @Override
    public List<DoctorSelectListVO> doctorsByDept(Long deptId) {
        return miniappDirectoryMapper.selectDoctorsByDept(deptId);
    }

    @Override
    public List<MiniappScheduleSelectVO> schedules(ScheduleSelectQueryDTO queryDTO) {
        List<BizSchedule> list = bizScheduleService.scheduleSelectList(queryDTO);
        return list.stream().map(detail -> {
            MiniappScheduleSelectVO vo = new MiniappScheduleSelectVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public PatientProfileVO patientProfile(Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        return miniappDirectoryMapper.selectPatientById(patientId);
    }
}
