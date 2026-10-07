package com.his.miniapp.service.impl;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.service.BizScheduleService;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniappDirectoryMapper;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.PatientDetailVO;
import com.his.miniapp.vo.PatientRowVO;
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
        return miniappDirectoryMapper.selectOpenDepartments().stream().map(row -> {
            DeptSelectListVO vo = new DeptSelectListVO();
            vo.setId(row.getId());
            vo.setDeptName(row.getDeptName());
            vo.setDeptType(row.getDeptType());
            vo.setDeptDesc(row.getDeptDesc());
            return vo;
        }).toList();
    }

    @Override
    public List<DoctorSelectListVO> doctorsByDept(Long deptId) {
        return miniappDirectoryMapper.selectDoctorsByDept(deptId).stream().map(row -> {
            DoctorSelectListVO vo = new DoctorSelectListVO();
            vo.setId(row.getId());
            vo.setEmpName(row.getEmpName());
            vo.setDeptId(row.getDeptId());
            vo.setDeptName(row.getDeptName());
            vo.setTitle(row.getTitle());
            vo.setSpecialty(row.getSpecialty());
            vo.setIsExpert(row.getIsExpert());
            vo.setExpertPrice(row.getExpertPrice());
            return vo;
        }).toList();
    }

    @Override
    public List<ScheduleSelectListVO> schedules(ScheduleSelectQueryDTO queryDTO) {
        List<BizSchedule> list = bizScheduleService.scheduleSelectList(queryDTO);
        return list.stream().map(detail -> {
            ScheduleSelectListVO vo = new ScheduleSelectListVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public PatientDetailVO patientProfile(Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人档案");
        }
        PatientRowVO row = miniappDirectoryMapper.selectPatientById(patientId);
        if (row == null) {
            return null;
        }
        PatientDetailVO vo = new PatientDetailVO();
        vo.setId(row.getId());
        vo.setPatientNo(row.getPatientNo());
        vo.setPatientName(row.getPatientName());
        vo.setGender(row.getGender());
        vo.setBirthDate(row.getBirthDate());
        vo.setAge(row.getAge());
        vo.setPhone(row.getPhone());
        vo.setIdCard(row.getIdCard());
        vo.setBalance(row.getBalance());
        vo.setVisitCount(row.getVisitCount());
        return vo;
    }
}
