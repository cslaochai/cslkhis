package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.BizDiagTemplateUpsertDTO;
import com.his.emr.dto.BizDrugPackageUpsertDTO;
import com.his.emr.dto.BizRxTemplateUpsertDTO;
import com.his.emr.dto.DiagTemplateUpsertDTO;
import com.his.emr.entity.*;
import com.his.emr.mapper.*;
import com.his.emr.service.OtherTemplateService;
import com.his.emr.vo.*;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
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
public class OtherTemplateServiceImpl implements OtherTemplateService {

    private final BizDiagTemplateMapper diagTemplateMapper;
    private final BizRxTemplateMapper rxTemplateMapper;
    private final BizRxTemplateDetailMapper rxTemplateDetailMapper;
    private final BizDrugPackageMapper drugPackageMapper;
    private final BizDrugPackageDetailMapper drugPackageDetailMapper;

    // 常用诊断

    @Override
    public List<BizDiagTemplateVO> listDiagTemplates() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizDiagTemplate> list = diagTemplateMapper.selectList(
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
        diagTemplateMapper.delete(
                new LambdaQueryWrapper<BizDiagTemplate>()
                        .eq(BizDiagTemplate::getDoctorId, doctorId)
        );
        for (int i = 0; i < templates.size(); i++) {
            BizDiagTemplate t = templates.get(i);
            t.setId(null);
            t.setDoctorId(doctorId);
            t.setSortOrder(i);
            diagTemplateMapper.insert(t);
        }
        return true;
    }

    @Override
    public boolean deleteDiagTemplate(Long id) {
        return diagTemplateMapper.deleteById(id) > 0;
    }

    // 处方模板

    @Override
    public List<BizRxTemplateVO> listRxTemplates() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizRxTemplate> list = rxTemplateMapper.selectList(
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
        BizRxTemplate tpl = rxTemplateMapper.selectById(templateId);
        if (tpl != null) {
            tpl.setDetails(rxTemplateDetailMapper.selectList(
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
        rxTemplateMapper.insert(template);
        // 插入明细
        if (template.getDetails() != null) {
            for (BizRxTemplateDetail d : template.getDetails()) {
                d.setTemplateId(template.getId());
                rxTemplateDetailMapper.insert(d);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean deleteRxTemplate(Long id) {
        rxTemplateDetailMapper.delete(
                new LambdaQueryWrapper<BizRxTemplateDetail>()
                        .eq(BizRxTemplateDetail::getTemplateId, id)
        );
        return rxTemplateMapper.deleteById(id) > 0;
    }

    // 药品套餐

    @Override
    public List<BizDrugPackageVO> listDrugPackages() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizDrugPackage> list = drugPackageMapper.selectList(
                new LambdaQueryWrapper<BizDrugPackage>()
                        .eq(BizDrugPackage::getDoctorId, doctorId)
                        .orderByDesc(BizDrugPackage::getCreateTime)
        );
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toPackageVO).collect(Collectors.toList());
    }

    @Override
    public BizDrugPackageVO getDrugPackageDetail(Long packageId) {
        BizDrugPackage pkg = drugPackageMapper.selectById(packageId);
        if (pkg != null) {
            pkg.setDetails(drugPackageDetailMapper.selectList(
                    new LambdaQueryWrapper<BizDrugPackageDetail>()
                            .eq(BizDrugPackageDetail::getPackageId, packageId)
            ));
        }
        return toPackageVO(pkg);
    }

    @Override
    @Transactional
    public boolean saveDrugPackage(BizDrugPackageUpsertDTO upsertDTO) {
        Long doctorId = requireCurrentDoctorId();
        BizDrugPackage pkg = new BizDrugPackage();
        BeanUtils.copyProperties(upsertDTO, pkg);
        pkg.setDoctorId(doctorId);
        if (upsertDTO.getDetails() != null) {
            pkg.setDetails(upsertDTO.getDetails().stream().map(dto -> {
                BizDrugPackageDetail detail = new BizDrugPackageDetail();
                BeanUtils.copyProperties(dto, detail);
                return detail;
            }).collect(Collectors.toList()));
        }
        drugPackageMapper.insert(pkg);
        if (pkg.getDetails() != null) {
            for (BizDrugPackageDetail d : pkg.getDetails()) {
                d.setPackageId(pkg.getId());
                drugPackageDetailMapper.insert(d);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean deleteDrugPackage(Long id) {
        drugPackageDetailMapper.delete(
                new LambdaQueryWrapper<BizDrugPackageDetail>()
                        .eq(BizDrugPackageDetail::getPackageId, id)
        );
        return drugPackageMapper.deleteById(id) > 0;
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

    private BizDrugPackageVO toPackageVO(BizDrugPackage pkg) {
        if (pkg == null) {
            return null;
        }
        BizDrugPackageVO vo = new BizDrugPackageVO();
        BeanUtils.copyProperties(pkg, vo);
        if (pkg.getDetails() != null) {
            vo.setDetails(pkg.getDetails().stream().map(detail -> {
                BizDrugPackageDetailVO detailVO = new BizDrugPackageDetailVO();
                BeanUtils.copyProperties(detail, detailVO);
                return detailVO;
            }).collect(Collectors.toList()));
        }
        return vo;
    }
}
