package com.his.miniapp.service.impl;

import com.his.schedule.dto.ScheduleSelectQueryDTO;
import com.his.schedule.service.BizClinicSourceService;
import com.his.schedule.vo.ClinicSourceVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniDirectoryMapper;
import com.his.miniapp.service.MiniDirectoryService;
import com.his.miniapp.vo.MiniDeptSelectListVO;
import com.his.miniapp.vo.MiniDoctorSelectListVO;
import com.his.miniapp.vo.MiniScheduleSelectVO;
import com.his.miniapp.vo.MiniPatientDetailVO;
import com.his.spi.service.PatientFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniDirectoryServiceImpl implements MiniDirectoryService {

    private final MiniDirectoryMapper miniDirectoryMapper;
    private final BizClinicSourceService bizClinicSourceService;
    private final PatientFacadeService patientFacadeService;

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
        List<ClinicSourceVO> list = bizClinicSourceService.scheduleSelectListUnbounded(queryDTO).stream()
                .map(source -> {
                    ClinicSourceVO vo = new ClinicSourceVO();
                    BeanUtils.copyProperties(source, vo);
                    return vo;
                }).toList();
        return list.stream().map(detail -> {
            MiniScheduleSelectVO vo = new MiniScheduleSelectVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public MiniPatientDetailVO patientProfile(Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        return miniDirectoryMapper.selectPatientById(patientId);
    }
}
