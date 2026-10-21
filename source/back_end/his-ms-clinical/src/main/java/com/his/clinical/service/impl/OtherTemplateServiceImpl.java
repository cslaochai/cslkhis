package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.clinical.dto.BizDiagTemplateUpsertDTO;
import com.his.clinical.dto.BizRxTemplateUpsertDTO;
import com.his.clinical.dto.DiagTemplateUpsertDTO;
import com.his.clinical.entity.*;
import com.his.clinical.mapper.*;
import com.his.clinical.service.OtherTemplateService;
import com.his.clinical.vo.*;
import com.his.common.entity.CurrentUser;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OtherTemplateServiceImpl extends ServiceImpl<BizDiagTemplateMapper, BizDiagTemplate> implements OtherTemplateService {

    private final BizDiagTemplateMapper bizDiagTemplateMapper;
    private final BizRxTemplateMapper bizRxTemplateMapper;
    private final BizRxTemplateDetailMapper bizRxTemplateDetailMapper;

    // 常用诊断

    @Override
    public List<BizDiagTemplateVO> listDiagTemplates() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizDiagTemplate> list = bizDiagTemplateMapper.selectList(
                new LambdaQueryWrapper<BizDiagTemplate>()
                        .eq(BizDiagTemplate::getDoctorId, doctorId)
                        .orderByAsc(BizDiagTemplate::getSortOrder)
        );
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizDiagTemplateVO vo = new BizDiagTemplateVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean saveDiagTemplates(DiagTemplateUpsertDTO saveDTO) {
        Long doctorId = requireCurrentDoctorId();
        List<BizDiagTemplateUpsertDTO> dtoList = saveDTO.getTemplates();
        List<BizDiagTemplate> templates = dtoList == null ? new ArrayList<>()
                : dtoList.stream().map(dto -> {
            BizDiagTemplate entity = new BizDiagTemplate();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        }).collect(Collectors.toList());
        // 先删后插
        bizDiagTemplateMapper.delete(
                new LambdaQueryWrapper<BizDiagTemplate>()
                        .eq(BizDiagTemplate::getDoctorId, doctorId)
        );
        for (int i = 0; i < templates.size(); i++) {
            BizDiagTemplate t = templates.get(i);
            t.setId(null);
            t.setDoctorId(doctorId);
            t.setSortOrder(i);
            bizDiagTemplateMapper.insert(t);
        }
        return true;
    }

    @Override
    public boolean deleteDiagTemplate(Long id) {
        return bizDiagTemplateMapper.deleteById(id) > 0;
    }

    // 处方模板

    @Override
    public List<BizRxTemplateVO> listRxTemplates() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizRxTemplate> list = bizRxTemplateMapper.selectList(
                new LambdaQueryWrapper<BizRxTemplate>()
                        .eq(BizRxTemplate::getDoctorId, doctorId)
                        .orderByDesc(BizRxTemplate::getCreateTime)
        );
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toRxVO).collect(Collectors.toList());
    }

    @Override
    public BizRxTemplateVO getRxTemplateDetail(Long templateId) {
        BizRxTemplate tpl = bizRxTemplateMapper.selectById(templateId);
        if (tpl != null) {
            tpl.setDetails(bizRxTemplateDetailMapper.selectList(
                    new LambdaQueryWrapper<BizRxTemplateDetail>()
                            .eq(BizRxTemplateDetail::getTemplateId, templateId)
            ));
        }
        return toRxVO(tpl);
    }

    @Override
    @Transactional
    public boolean saveRxTemplate(BizRxTemplateUpsertDTO upsertDTO) {
        Long doctorId = requireCurrentDoctorId();
        BizRxTemplate template = new BizRxTemplate();
        BeanUtils.copyProperties(upsertDTO, template);
        template.setDoctorId(doctorId);
        if (upsertDTO.getDetails() != null) {
            template.setDetails(upsertDTO.getDetails().stream().map(dto -> {
                BizRxTemplateDetail detail = new BizRxTemplateDetail();
                BeanUtils.copyProperties(dto, detail);
                return detail;
            }).collect(Collectors.toList()));
        }
        // 计算汇总
        if (template.getDetails() != null) {
            int count = template.getDetails().size();
            BigDecimal total = template.getDetails().stream()
                    .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            template.setDrugCount(count);
            template.setTotalAmount(total);
        }
        bizRxTemplateMapper.insert(template);
        // 插入明细
        if (template.getDetails() != null) {
            for (BizRxTemplateDetail d : template.getDetails()) {
                d.setTemplateId(template.getId());
                bizRxTemplateDetailMapper.insert(d);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean deleteRxTemplate(Long id) {
        bizRxTemplateDetailMapper.delete(
                new LambdaQueryWrapper<BizRxTemplateDetail>()
                        .eq(BizRxTemplateDetail::getTemplateId, id)
        );
        return bizRxTemplateMapper.deleteById(id) > 0;
    }

    private Long currentDoctorIdOrNull() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user != null ? user.getEmployeeId() : null;
    }

    private Long requireCurrentDoctorId() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            throw new BusinessException("未获取到用户信息");
        }
        return doctorId;
    }

    private BizRxTemplateVO toRxVO(BizRxTemplate template) {
        if (template == null) {
            return null;
        }
        BizRxTemplateVO vo = new BizRxTemplateVO();
        BeanUtils.copyProperties(template, vo);
        if (template.getDetails() != null) {
            vo.setDetails(template.getDetails().stream().map(detail -> {
                BizRxTemplateDetailVO detailVO = new BizRxTemplateDetailVO();
                BeanUtils.copyProperties(detail, detailVO);
                return detailVO;
            }).collect(Collectors.toList()));
        }
        return vo;
    }
}
