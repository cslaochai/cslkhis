package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.AttendingRelationTypeEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.AttendingBindDTO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizAttendingRelation;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizAttendingRelationMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.AttendingRelationService;
import com.his.patient.vo.AttendingRelationVO;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 住院管床关系服务实现。
 */
@Service
@RequiredArgsConstructor
public class AttendingRelationServiceImpl
        extends ServiceImpl<BizAttendingRelationMapper, BizAttendingRelation> implements AttendingRelationService {

    /**
     * 状态：有效
     */
    private static final int STATUS_ACTIVE = 1;
    /**
     * 状态：已结束
     */
    private static final int STATUS_ENDED = 0;

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDepartmentMapper departmentMapper;

    @Override
    public List<AttendingRelationVO> listByAdmission(Long admissionId, Integer status) {
        if (admissionId == null) {
            throw new BusinessException("住院登记ID不能为空");
        }
        List<BizAttendingRelation> rows = list(new LambdaQueryWrapper<BizAttendingRelation>()
                .eq(BizAttendingRelation::getAdmissionId, admissionId)
                .eq(status != null, BizAttendingRelation::getStatus, status)
                .orderByAsc(BizAttendingRelation::getRelationType)
                .orderByAsc(BizAttendingRelation::getId));
        return toVO(rows);
    }

    @Override
    public List<AttendingRelationVO> listByEmployee(Long employeeId, Integer status) {
        if (employeeId == null) {
            throw new BusinessException("医生ID不能为空");
        }
        List<BizAttendingRelation> rows = list(new LambdaQueryWrapper<BizAttendingRelation>()
                .eq(BizAttendingRelation::getEmployeeId, employeeId)
                .eq(status != null, BizAttendingRelation::getStatus, status)
                .orderByDesc(BizAttendingRelation::getEffectiveTime)
                .orderByAsc(BizAttendingRelation::getId));
        return toVO(rows);
    }

    @Override
    public Long bind(AttendingBindDTO dto) {
        BizAdmission admission = admissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("住院登记不存在或已删除");
        }
        // 已出院的不给改：历史关系是要留作凭证的，改它等于篡改病案
        if (admission.getAdmitStatus() == null || admission.getAdmitStatus() != 1) {
            throw new BusinessException("该住院已结束，不能再建立或转交管床关系");
        }
        SysEmployee doctor = employeeMapper.selectById(dto.getEmployeeId());
        if (doctor == null) {
            throw new BusinessException("所选医生不存在或已删除");
        }
        // 必须是医生：把护士/收费员指派成主管医生，页面上能存，出事了追责对象就是错的
        if (doctor.getEmpType() == null || doctor.getEmpType() != StaffTypeEnum.DOCTOR.getCode()) {
            throw new BusinessException("管床关系只能指派给医生（该员工岗位类别不是医生）");
        }

        Integer relationType = dto.getRelationType() == null
                ? AttendingRelationTypeEnum.ATTENDING.getCode() : dto.getRelationType();
        if (AttendingRelationTypeEnum.fromCode(relationType) == null) {
            throw new BusinessException("关系类型只允许 " + AttendingRelationTypeEnum.whitelistText());
        }
        LocalDateTime effective = parseTime(dto.getEffectiveTime());

        // 幂等：同一人同一类型重复指派不报错，直接把已结束的那条复活（避免撞唯一键）
        BizAttendingRelation exist = getOne(new LambdaQueryWrapper<BizAttendingRelation>()
                .eq(BizAttendingRelation::getAdmissionId, dto.getAdmissionId())
                .eq(BizAttendingRelation::getRelationType, relationType)
                .eq(BizAttendingRelation::getEmployeeId, dto.getEmployeeId())
                .last("LIMIT 1"));
        if (exist != null) {
            exist.setStatus(STATUS_ACTIVE);
            exist.setExpireTime(null);
            if (effective != null) {
                exist.setEffectiveTime(effective);
            }
            if (StringUtils.hasText(dto.getRemark())) {
                exist.setRemark(dto.getRemark());
            }
            updateById(exist);
            return exist.getId();
        }

        // 转交：主管/主诊组长是「唯一负责」的关系，新的人进来，旧的必须让位 ——
        // 这里置失效而不是删行，谁在什么时候把病人交出去要查得出来。
        if (AttendingRelationTypeEnum.exclusive(relationType)) {
            List<BizAttendingRelation> old = list(new LambdaQueryWrapper<BizAttendingRelation>()
                    .eq(BizAttendingRelation::getAdmissionId, dto.getAdmissionId())
                    .eq(BizAttendingRelation::getRelationType, relationType)
                    .eq(BizAttendingRelation::getStatus, STATUS_ACTIVE));
            for (BizAttendingRelation row : old) {
                row.setStatus(STATUS_ENDED);
                row.setExpireTime(effective == null ? LocalDateTime.now() : effective);
                updateById(row);
            }
        }

        BizAttendingRelation rel = new BizAttendingRelation();
        rel.setAdmissionId(admission.getAdmissionId());
        rel.setPatientId(admission.getPatientId());
        rel.setPatientName(patientNameOf(admission.getPatientId()));
        rel.setEmployeeId(doctor.getId());
        rel.setEmployeeName(doctor.getEmpName());
        rel.setDeptId(admission.getDeptId());
        rel.setDeptName(deptNameOf(admission.getDeptId()));
        rel.setWardId(admission.getWardId() == null ? 0L : admission.getWardId());
        rel.setBedId(admission.getBedId() == null ? 0L : admission.getBedId());
        rel.setRelationType(relationType);
        rel.setStatus(STATUS_ACTIVE);
        // 生效时间没传就退回入院时间：管床从入院那一刻起算，不是从点「指派」这一刻起算
        rel.setEffectiveTime(effective != null ? effective : admission.getAdmitTime());
        rel.setRemark(dto.getRemark());
        save(rel);
        return rel.getId();
    }

    @Override
    public void endById(Long id) {
        BizAttendingRelation rel = getById(id);
        if (rel == null) {
            throw new BusinessException("管床关系不存在或已删除");
        }
        if (rel.getStatus() != null && rel.getStatus() == STATUS_ENDED) {
            return;
        }
        rel.setStatus(STATUS_ENDED);
        rel.setExpireTime(LocalDateTime.now());
        updateById(rel);
    }

    @Override
    public void deleteById(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("管床关系不存在或已删除");
        }
        // 唯一键不含删除标志 → 物理删（软删的行会继续占着「同一次住院 + 同类型 + 同一医生」的键位）
        baseMapper.purgeById(id);
    }

    private List<AttendingRelationVO> toVO(List<BizAttendingRelation> rows) {
        List<AttendingRelationVO> vos = new ArrayList<>(rows.size());
        for (BizAttendingRelation row : rows) {
            AttendingRelationVO vo = new AttendingRelationVO();
            vo.setId(row.getId());
            vo.setAdmissionId(row.getAdmissionId());
            vo.setPatientId(row.getPatientId());
            vo.setPatientName(row.getPatientName());
            vo.setEmployeeId(row.getEmployeeId());
            vo.setEmployeeName(row.getEmployeeName());
            vo.setDeptId(row.getDeptId());
            vo.setDeptName(row.getDeptName());
            vo.setWardId(row.getWardId());
            vo.setBedId(row.getBedId());
            vo.setRelationType(row.getRelationType());
            vo.setRelationTypeText(AttendingRelationTypeEnum.getText(row.getRelationType()));
            vo.setStatus(row.getStatus());
            vo.setStatusText(row.getStatus() != null && row.getStatus() == STATUS_ACTIVE ? "有效" : "已结束");
            vo.setEffectiveTime(row.getEffectiveTime());
            vo.setExpireTime(row.getExpireTime());
            vo.setRemark(row.getRemark());
            vos.add(vo);
        }
        return vos;
    }

    private String patientNameOf(Long patientId) {
        if (patientId == null) {
            return "";
        }
        BizPatient patient = patientMapper.selectById(patientId);
        return patient == null ? "" : patient.getPatientName();
    }

    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return "";
        }
        SysDepartment dept = departmentMapper.selectById(deptId);
        return dept == null ? "" : dept.getDeptName();
    }

    /**
     * 时间解析：兼容「yyyy-MM-dd HH:mm:ss」与「yyyy-MM-dd HH:mm」，都解析不了才报错
     */
    private LocalDateTime parseTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDateTime.parse(value, FULL);
        } catch (DateTimeParseException ignored) {
            // 落到分钟级再试一次
        }
        try {
            return LocalDateTime.parse(value, MINUTE);
        } catch (DateTimeParseException e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm");
        }
    }
}
