package com.his.miniapp.service.impl;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.service.ScheduleService;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniappDirectoryMapper;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.support.RawRowValues;
import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.PatientDetailVO;
import com.his.patient.service.PatientGuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MiniappDirectoryServiceImpl implements MiniappDirectoryService {

    private final MiniappDirectoryMapper miniappDirectoryMapper;
    private final ScheduleService scheduleService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public List<DeptSelectListVO> openDepartments() {
        return miniappDirectoryMapper.selectOpenDepartments().stream().map(row -> {
            DeptSelectListVO vo = new DeptSelectListVO();
            vo.setId(RawRowValues.text(row, "id"));
            vo.setDeptName(RawRowValues.text(row, "dept_name"));
            vo.setDeptType(RawRowValues.integer(row, "dept_type"));
            vo.setDeptDesc(RawRowValues.text(row, "dept_desc"));
            return vo;
        }).toList();
    }

    @Override
    public List<DoctorSelectListVO> doctorsByDept(Long deptId) {
        return miniappDirectoryMapper.selectDoctorsByDept(deptId).stream().map(row -> {
            DoctorSelectListVO vo = new DoctorSelectListVO();
            vo.setId(RawRowValues.text(row, "id"));
            vo.setEmpName(RawRowValues.text(row, "emp_name"));
            vo.setDeptId(RawRowValues.longValue(row, "dept_id"));
            vo.setDeptName(RawRowValues.text(row, "dept_name"));
            vo.setTitle(RawRowValues.text(row, "title"));
            vo.setSpecialty(RawRowValues.text(row, "specialty"));
            vo.setIsExpert(RawRowValues.integer(row, "is_expert"));
            vo.setExpertPrice(RawRowValues.decimal(row, "expert_price"));
            return vo;
        }).toList();
    }

    @Override
    public List<ScheduleSelectListVO> schedules(ScheduleSelectQueryDTO queryDTO) {
        List<BizSchedule> list = scheduleService.scheduleSelectList(queryDTO);
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
        // 裸 SQL 的 Map 键是下划线列名（MyBatis 驼峰映射只对 Bean 生效），出参键名固定为驼峰
        Map<String, Object> row = miniappDirectoryMapper.selectPatientById(patientId);
        if (row == null) {
            return null;
        }
        PatientDetailVO vo = new PatientDetailVO();
        vo.setId(RawRowValues.text(row, "id"));
        vo.setPatientNo(RawRowValues.text(row, "patient_no"));
        vo.setPatientName(RawRowValues.text(row, "patient_name"));
        vo.setGender(RawRowValues.integer(row, "gender"));
        vo.setBirthDate(RawRowValues.date(row, "birth_date"));
        vo.setAge(RawRowValues.integer(row, "age"));
        vo.setPhone(RawRowValues.text(row, "phone"));
        vo.setIdCard(RawRowValues.text(row, "id_card"));
        vo.setBalance(RawRowValues.decimal(row, "balance"));
        vo.setVisitCount(RawRowValues.integer(row, "visit_count"));
        return vo;
    }
}
