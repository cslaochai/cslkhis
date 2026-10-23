package com.his.app.service.impl;

import com.his.common.exception.BusinessException;
import com.his.app.mapper.MiniDirectoryMapper;
import com.his.app.service.MiniDirectoryService;
import com.his.app.vo.MiniDeptSelectListVO;
import com.his.app.vo.MiniDoctorSelectListVO;
import com.his.app.vo.MiniScheduleSelectVO;
import com.his.app.vo.MiniPatientDetailVO;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.service.ClinicSourceFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.ClinicSourceBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniDirectoryServiceImpl implements MiniDirectoryService {

    private final MiniDirectoryMapper miniDirectoryMapper;
    private final PatientFacadeService patientFacadeService;
    private final ClinicSourceFacadeService clinicSourceFacadeService;

    @Override
    public List<MiniDeptSelectListVO> openDepartments() {
        return miniDirectoryMapper.selectOpenDepartments();
    }

    @Override
    public List<MiniDoctorSelectListVO> doctorsByDept(Long deptId) {
        return miniDirectoryMapper.selectDoctorsByDept(deptId);
    }

    @Override
    public List<MiniScheduleSelectVO> schedules(AppointQueryDTO queryDTO) {
        List<ClinicSourceBriefVO> sources = clinicSourceFacadeService.listPatientScheduleSources(queryDTO);
        return sources.stream().map(this::toScheduleVO).toList();
    }

    private MiniScheduleSelectVO toScheduleVO(ClinicSourceBriefVO source) {
        MiniScheduleSelectVO vo = new MiniScheduleSelectVO();
        vo.setId(source.getId());
        vo.setDeptId(source.getDeptId());
        vo.setDeptName(source.getDeptName());
        vo.setDoctorId(source.getDoctorId());
        vo.setDoctorName(source.getDoctorName());
        vo.setScheduleDate(source.getScheduleDate());
        vo.setStartTime(source.getStartTime());
        vo.setEndTime(source.getEndTime());
        vo.setRegistFee(source.getRegistFee());
        vo.setDiagnosisFee(source.getDiagnosisFee());
        vo.setDiagnosisTier(source.getDiagnosisTier());
        vo.setIsExpert(source.getIsExpert());
        return vo;
    }

    @Override
    public MiniPatientDetailVO patientProfile(Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        return miniDirectoryMapper.selectPatientById(patientId);
    }
}
