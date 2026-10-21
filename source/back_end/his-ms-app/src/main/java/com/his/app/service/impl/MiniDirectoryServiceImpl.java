package com.his.app.service.impl;

import com.his.common.exception.BusinessException;
import com.his.app.mapper.MiniDirectoryMapper;
import com.his.app.service.MiniDirectoryService;
import com.his.app.vo.MiniDeptSelectListVO;
import com.his.app.vo.MiniDoctorSelectListVO;
import com.his.app.vo.MiniScheduleSelectVO;
import com.his.app.vo.MiniPatientDetailVO;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.service.PatientFacadeService;
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
        // TODO(spi 扩展) 患者端可挂号源下拉待 AppointFacadeService#listPatientScheduleSources 扩展
        // （返回 ClinicSourceBriefVO 列表，余号/挂号费/诊查费/时段），见 spi-extension-requests.txt；
        // 现有端口无此能力，先返回空列表，不直连排班域。
        log.warn("[挂号目录] 可挂号源端口待扩展，deptId={} doctorId={} visitDate={}",
                queryDTO == null ? null : queryDTO.getDeptId(),
                queryDTO == null ? null : queryDTO.getDoctorId(),
                queryDTO == null ? null : queryDTO.getVisitDate());
        return List.of();
    }

    @Override
    public MiniPatientDetailVO patientProfile(Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        return miniDirectoryMapper.selectPatientById(patientId);
    }
}
