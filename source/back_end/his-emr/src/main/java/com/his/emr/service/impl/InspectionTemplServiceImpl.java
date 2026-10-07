package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.BizInspectionTemplateUpsertDTO;
import com.his.emr.dto.BizLaboratoryTemplateUpsertDTO;
import com.his.emr.entity.BizInspectionTemplate;
import com.his.emr.entity.BizLaboratoryTemplate;
import com.his.emr.mapper.BizInspectionTemplateMapper;
import com.his.emr.mapper.BizLaboratoryTemplateMapper;
import com.his.emr.service.InspectionTemplService;
import com.his.emr.vo.BizInspectionTemplateVO;
import com.his.emr.vo.BizLaboratoryTemplateVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生工作站服务实现
 */
@Service
@RequiredArgsConstructor
public class InspectionTemplServiceImpl implements InspectionTemplService {

    static final String[] HUIFANG_TYPE = {"糖尿病", "高血压", "冠心病"};
    private final BizInspectionTemplateMapper bizInspectionTemplateMapper;
    private final BizLaboratoryTemplateMapper bizLaboratoryTemplateMapper;

    @Override
    public List<BizInspectionTemplateVO> getInspectionTemplateList() {
        Long doctorId = currentEmployeeIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BizInspectionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizInspectionTemplate::getDoctorId, doctorId)
                .orderByAsc(BizInspectionTemplate::getSortOrder);
        List<BizInspectionTemplate> list = bizInspectionTemplateMapper.selectList(wrapper);
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizInspectionTemplateVO vo = new BizInspectionTemplateVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addInspectionTemplate(BizInspectionTemplateUpsertDTO upsertDTO) {
        Long doctorId = requireCurrentEmployeeId();
        BizInspectionTemplate template = new BizInspectionTemplate();
        BeanUtils.copyProperties(upsertDTO, template);
        template.setDoctorId(doctorId);
        return bizInspectionTemplateMapper.insert(template) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInspectionTemplate(Long id) {
        return bizInspectionTemplateMapper.deleteById(id) > 0;
    }

    // 检验申请模板
    @Override
    public List<BizLaboratoryTemplateVO> getLaboratoryTemplateList() {
        Long doctorId = currentEmployeeIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BizLaboratoryTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLaboratoryTemplate::getDoctorId, doctorId)
                .orderByAsc(BizLaboratoryTemplate::getSortOrder);
        List<BizLaboratoryTemplate> list = bizLaboratoryTemplateMapper.selectList(wrapper);
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizLaboratoryTemplateVO vo = new BizLaboratoryTemplateVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addLaboratoryTemplate(BizLaboratoryTemplateUpsertDTO upsertDTO) {
        Long doctorId = requireCurrentEmployeeId();
        BizLaboratoryTemplate template = new BizLaboratoryTemplate();
        BeanUtils.copyProperties(upsertDTO, template);
        template.setDoctorId(doctorId);
        return bizLaboratoryTemplateMapper.insert(template) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLaboratoryTemplate(Long id) {
        return bizLaboratoryTemplateMapper.deleteById(id) > 0;
    }

    private Long currentEmployeeIdOrNull() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        return currentUser != null && currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : null;
    }

    private Long requireCurrentEmployeeId() {
        Long doctorId = currentEmployeeIdOrNull();
        if (doctorId == null) {
            throw new BusinessException("未获取到当前用户信息");
        }
        return doctorId;
    }
}
