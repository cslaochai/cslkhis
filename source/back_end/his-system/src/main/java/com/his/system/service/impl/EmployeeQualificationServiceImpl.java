package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.system.dto.EmployeeQualificationUpsertDTO;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysEmployeeQualification;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysEmployeeQualificationMapper;
import com.his.system.service.EmployeeQualificationService;
import com.his.system.vo.EmployeeQualificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工资格证书服务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeQualificationServiceImpl implements EmployeeQualificationService {

    private final SysEmployeeQualificationMapper qualificationMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    public List<EmployeeQualificationVO> listByEmployee(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        LambdaQueryWrapper<SysEmployeeQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployeeQualification::getEmployeeId, employeeId)
                .orderByAsc(SysEmployeeQualification::getCertType)
                .orderByAsc(SysEmployeeQualification::getIssueDate);
        return qualificationMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public void upsert(EmployeeQualificationUpsertDTO upsertDTO) {
        SysEmployee emp = employeeMapper.selectById(upsertDTO.getEmployeeId());
        if (emp == null) {
            throw new BusinessException("员工不存在");
        }

        SysEmployeeQualification entity = new SysEmployeeQualification();
        entity.setEmployeeId(upsertDTO.getEmployeeId());
        entity.setCertType(upsertDTO.getCertType());
        entity.setCertNo(upsertDTO.getCertNo().trim());
        entity.setIssueOrg(upsertDTO.getIssueOrg());
        entity.setIssueDate(upsertDTO.getIssueDate());
        entity.setValidUntil(upsertDTO.getValidUntil());
        entity.setRemark(upsertDTO.getRemark());

        // 撞唯一键先给业务提示（uk_emp_cert_type_no），不让 DuplicateKeyException 兜成 500
        LambdaQueryWrapper<SysEmployeeQualification> dup = new LambdaQueryWrapper<>();
        dup.eq(SysEmployeeQualification::getEmployeeId, entity.getEmployeeId())
                .eq(SysEmployeeQualification::getCertType, entity.getCertType())
                .eq(SysEmployeeQualification::getCertNo, entity.getCertNo())
                .ne(upsertDTO.getId() != null, SysEmployeeQualification::getId, upsertDTO.getId());
        if (qualificationMapper.exists(dup)) {
            throw new BusinessException("该员工已登记同类型、同号的证书，请勿重复添加");
        }

        if (upsertDTO.getId() == null) {
            qualificationMapper.insert(entity);
        } else {
            SysEmployeeQualification old = qualificationMapper.selectById(upsertDTO.getId());
            if (old == null) {
                throw new BusinessException("证书记录不存在");
            }
            // 证书不允许改挂到另一个人名下：id 来自前端，员工归属只认库里原值
            entity.setEmployeeId(old.getEmployeeId());
            entity.setId(upsertDTO.getId());
            qualificationMapper.updateById(entity);
        }
    }

    @Override
    public void deleteById(Long id) {
        // 本表无 del_flag，deleteById 即物理删（不会占着唯一键，见 sql/112 注释）
        qualificationMapper.deleteById(id);
    }

    private EmployeeQualificationVO toVO(SysEmployeeQualification entity) {
        EmployeeQualificationVO vo = new EmployeeQualificationVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setCertType(entity.getCertType());
        vo.setCertNo(entity.getCertNo());
        vo.setIssueOrg(entity.getIssueOrg());
        vo.setIssueDate(entity.getIssueDate());
        vo.setValidUntil(entity.getValidUntil());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
