package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.AttendingRelationStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.patient.dto.ChargeNurseBindDTO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizChargeNurseRelation;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizChargeNurseRelationMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.ChargeNurseRelationService;
import com.his.patient.vo.ChargeNurseRelationVO;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.DeptScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 住院责任护士关系服务实现。
 */
@Service
@RequiredArgsConstructor
public class ChargeNurseRelationServiceImpl
        extends ServiceImpl<BizChargeNurseRelationMapper, BizChargeNurseRelation>
        implements ChargeNurseRelationService {

    /** 责任护士关系类型（一次住院一名主责，预留扩展） */
    private static final int RELATION_CHARGE_NURSE = 1;

    private final BizAdmissionMapper bizAdmissionMapper;
    private final BizPatientMapper bizPatientMapper;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final DeptScopeService deptScopeService;

    @Override
    public List<ChargeNurseRelationVO> listByAdmission(Long admissionId, Integer status) {
        if (admissionId == null) {
            throw new BusinessException("住院登记ID不能为空");
        }
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        if (admission != null) {
            deptScopeService.assertDeptAccessible(admission.getDeptId());
        }
        List<BizChargeNurseRelation> rows = list(new LambdaQueryWrapper<BizChargeNurseRelation>()
                .eq(BizChargeNurseRelation::getAdmissionId, admissionId)
                .eq(status != null, BizChargeNurseRelation::getStatus, status)
                .orderByAsc(BizChargeNurseRelation::getId));
        return toVO(rows);
    }

    @Override
    public List<ChargeNurseRelationVO> listByEmployee(Long nurseId, Integer status) {
        if (nurseId == null) {
            throw new BusinessException("护士ID不能为空");
        }
        List<BizChargeNurseRelation> rows = list(new LambdaQueryWrapper<BizChargeNurseRelation>()
                .eq(BizChargeNurseRelation::getNurseId, nurseId)
                .eq(status != null, BizChargeNurseRelation::getStatus, status)
                .orderByDesc(BizChargeNurseRelation::getEffectiveTime)
                .orderByAsc(BizChargeNurseRelation::getId));
        return toVO(rows);
    }

    @Override
    public Long bind(ChargeNurseBindDTO dto) {
        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("住院登记不存在或已删除");
        }
        // 已出院的不改：历史归属是护理记录凭证，改它等于篡改留痕
        if (admission.getAdmitStatus() == null || admission.getAdmitStatus() != 1) {
            throw new BusinessException("该住院已结束，不能再建立或转交责任护士");
        }
        deptScopeService.assertDeptAccessible(admission.getDeptId());

        SysEmployee nurse = sysEmployeeMapper.selectById(dto.getNurseId());
        if (nurse == null) {
            throw new BusinessException("所选护士不存在或已删除");
        }
        // 必须是护理：把医生/收费员指派成责任护士，护理执行责任落错人
        if (nurse.getEmpType() == null || nurse.getEmpType() != StaffTypeEnum.NURSE.getCode()) {
            throw new BusinessException("责任护士只能指派给护理岗位（该员工岗位类别不是护理）");
        }

        LocalDateTime effective = parseTime(dto.getEffectiveTime());

        // 幂等：同一护士重复指派不报错，把已结束的那条复活
        BizChargeNurseRelation exist = getOne(new LambdaQueryWrapper<BizChargeNurseRelation>()
                .eq(BizChargeNurseRelation::getAdmissionId, dto.getAdmissionId())
                .eq(BizChargeNurseRelation::getRelationType, RELATION_CHARGE_NURSE)
                .eq(BizChargeNurseRelation::getNurseId, dto.getNurseId())
                .last("LIMIT 1"));
        if (exist != null) {
            exist.setStatus(AttendingRelationStatusEnum.ACTIVE.getCode());
            exist.setExpireTime(null);
            if (effective != null) {
                exist.setEffectiveTime(effective);
            }
            if (TextUtil.hasText(dto.getRemark())) {
                exist.setRemark(dto.getRemark());
            }
            updateById(exist);
            return exist.getId();
        }

        // 转交：主责唯一，新护士进来，旧的有效记录让位（置失效留痕，不删行）
        List<BizChargeNurseRelation> old = list(new LambdaQueryWrapper<BizChargeNurseRelation>()
                .eq(BizChargeNurseRelation::getAdmissionId, dto.getAdmissionId())
                .eq(BizChargeNurseRelation::getRelationType, RELATION_CHARGE_NURSE)
                .eq(BizChargeNurseRelation::getStatus, AttendingRelationStatusEnum.ACTIVE.getCode()));
        for (BizChargeNurseRelation row : old) {
            row.setStatus(AttendingRelationStatusEnum.ENDED.getCode());
            row.setExpireTime(effective == null ? LocalDateTime.now() : effective);
            updateById(row);
        }

        BizChargeNurseRelation rel = new BizChargeNurseRelation();
        rel.setAdmissionId(admission.getAdmissionId());
        rel.setPatientId(admission.getPatientId());
        rel.setPatientName(patientNameOf(admission.getPatientId()));
        rel.setNurseId(nurse.getId());
        rel.setNurseName(nurse.getEmpName());
        rel.setDeptId(admission.getDeptId());
        rel.setDeptName(deptNameOf(admission.getDeptId()));
        rel.setWardId(admission.getWardId() == null ? 0L : admission.getWardId());
        rel.setBedId(admission.getBedId() == null ? 0L : admission.getBedId());
        rel.setRelationType(RELATION_CHARGE_NURSE);
        rel.setStatus(AttendingRelationStatusEnum.ACTIVE.getCode());
        rel.setEffectiveTime(effective != null ? effective : admission.getAdmitTime());
        rel.setRemark(dto.getRemark());
        save(rel);
        return rel.getId();
    }

    @Override
    public void endById(Long id) {
        BizChargeNurseRelation rel = getById(id);
        if (rel == null) {
            throw new BusinessException("责任护士关系不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(rel.getDeptId());
        if (rel.getStatus() != null && rel.getStatus() == AttendingRelationStatusEnum.ENDED.getCode()) {
            return;
        }
        rel.setStatus(AttendingRelationStatusEnum.ENDED.getCode());
        rel.setExpireTime(LocalDateTime.now());
        updateById(rel);
    }

    @Override
    public void deleteById(Long id) {
        BizChargeNurseRelation rel = getById(id);
        if (rel == null) {
            throw new BusinessException("责任护士关系不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(rel.getDeptId());
        baseMapper.purgeById(id);
    }

    private List<ChargeNurseRelationVO> toVO(List<BizChargeNurseRelation> rows) {
        List<ChargeNurseRelationVO> vos = new ArrayList<>(rows.size());
        for (BizChargeNurseRelation row : rows) {
            ChargeNurseRelationVO vo = new ChargeNurseRelationVO();
            vo.setId(row.getId());
            vo.setAdmissionId(row.getAdmissionId());
            vo.setPatientId(row.getPatientId());
            vo.setPatientName(row.getPatientName());
            vo.setNurseId(row.getNurseId());
            vo.setNurseName(row.getNurseName());
            vo.setDeptId(row.getDeptId());
            vo.setDeptName(row.getDeptName());
            vo.setWardId(row.getWardId());
            vo.setBedId(row.getBedId());
            vo.setRelationType(row.getRelationType());
            vo.setRelationTypeText("责任护士");
            vo.setStatus(row.getStatus());
            vo.setStatusText(AttendingRelationStatusEnum.getLabel(row.getStatus()));
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
        BizPatient patient = bizPatientMapper.selectById(patientId);
        return patient == null ? "" : patient.getPatientName();
    }

    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return "";
        }
        SysDepartment dept = sysDepartmentMapper.selectById(deptId);
        return dept == null ? "" : dept.getDeptName();
    }

    /**
     * 兼容「yyyy-MM-dd HH:mm:ss」与「yyyy-MM-dd HH:mm」，都解析不了才报错
     */
    private LocalDateTime parseTime(String text) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDateTime.parse(value, DateFormats.DATETIME);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value, DateFormats.DATETIME_MINUTE);
        } catch (DateTimeParseException e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm");
        }
    }
}
