package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.DischargeDrugDTO;
import com.his.patient.entity.BizDischargeDrug;
import com.his.patient.enums.DischargeDrugStatusEnum;
import com.his.patient.mapper.BizDischargeDrugMapper;
import com.his.patient.service.DischargeDrugService;
import com.his.system.service.DictCacheService;
import com.his.patient.vo.DischargeDrugSelectListVO;
import com.his.patient.vo.DischargeDrugVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 出院带药服务。
 *
 * <p>状态机：1 待发药 →（药房发药）→ 2 已发药。只有待发药可改/可删；
 * 发药是单向动作，已发药单不可回退（发错了走备注纠错留痕，不改状态）。
 */
@Service
@RequiredArgsConstructor
public class DischargeDrugServiceImpl implements DischargeDrugService {

    private static final String DICT_STATUS = "his_discharge_drug_status";

    private final BizDischargeDrugMapper drugMapper;
    private final DictCacheService dictText;

    @Transactional(rollbackFor = Exception.class)
    public DischargeDrugVO upsert(DischargeDrugDTO.Upsert dto) {
        BizDischargeDrug d;
        if (dto.getId() == null) {
            d = new BizDischargeDrug();
            d.setOrderNo(nextOrderNo());
            d.setAdmissionId(dto.getAdmissionId());
            d.setPatientId(requirePatientId(dto.getAdmissionId()));
            fillPatientSnapshot(d);
            d.setDispenseStatus(DischargeDrugStatusEnum.PENDING.getCode());
        } else {
            d = drugMapper.selectById(dto.getId());
            if (d == null || (d.getDelFlag() != null && d.getDelFlag() == 1)) {
                throw new BusinessException("带药单不存在");
            }
            if (!java.util.Objects.equals(d.getDispenseStatus(), DischargeDrugStatusEnum.PENDING.getCode())) {
                throw new BusinessException("已发药的带药单不能修改（发错请备注纠错留痕）");
            }
        }
        d.setDrugId(dto.getDrugId());
        d.setDrugName(dto.getDrugName().trim());
        d.setSpec(dto.getSpec());
        d.setDosage(dto.getDosage());
        d.setUnit(dto.getUnit());
        d.setQuantity(dto.getQuantity());
        d.setUsageText(dto.getUsageText());
        d.setDays(dto.getDays());
        d.setRemark(dto.getRemark());
        d.setCreateBy(UserUtils.getCurrentEmployeeName());
        if (dto.getId() != null) {
            d.setUpdateBy(UserUtils.getCurrentEmployeeName());
            d.setUpdateTime(TimeUtil.toSeconds(LocalDateTime.now()));
        }
        if (dto.getId() == null) {
            drugMapper.insert(d);
        } else {
            drugMapper.updateById(d);
        }
        return toVo(d);
    }

    public IPage<DischargeDrugVO> listPage(DischargeDrugDTO.QueryPage q) {
        LambdaQueryWrapper<BizDischargeDrug> w = new LambdaQueryWrapper<BizDischargeDrug>()
                .eq(q.getAdmissionId() != null, BizDischargeDrug::getAdmissionId, q.getAdmissionId())
                .eq(q.getPatientId() != null, BizDischargeDrug::getPatientId, q.getPatientId())
                .like(StringUtils.hasText(q.getDrugName()), BizDischargeDrug::getDrugName, tr(q.getDrugName()))
                .eq(q.getDispenseStatus() != null, BizDischargeDrug::getDispenseStatus, q.getDispenseStatus())
                .orderByDesc(BizDischargeDrug::getId);
        IPage<BizDischargeDrug> page = drugMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return page.convert(this::toVo);
    }

    /**
     * 按入院次列全部带药单（出院带药页选药下拉用）
     */
    public List<DischargeDrugSelectListVO> listByAdmission(Long admissionId) {
        return drugMapper.selectList(new LambdaQueryWrapper<BizDischargeDrug>()
                        .eq(BizDischargeDrug::getAdmissionId, admissionId)
                        .orderByDesc(BizDischargeDrug::getId))
                .stream().map(this::toSelectVo).collect(Collectors.toList());
    }

    public DischargeDrugVO getDetailById(Long id) {
        BizDischargeDrug d = drugMapper.selectById(id);
        if (d == null || (d.getDelFlag() != null && d.getDelFlag() == 1)) {
            throw new BusinessException("带药单不存在");
        }
        return toVo(d);
    }

    /**
     * 批量发药（药房岗）：只有待发药的单能发；部分单已发则整体失败，避免半批状态难对账
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DischargeDrugVO> dispense(DischargeDrugDTO.Dispense dto) {
        // 「请选择要发药的带药单」已收口到 DTO @NotEmpty + Controller @Valid
        List<BizDischargeDrug> list = dto.getIds().stream()
                .map(drugMapper::selectById).collect(Collectors.toList());
        for (BizDischargeDrug d : list) {
            if (d == null || (d.getDelFlag() != null && d.getDelFlag() == 1)) {
                throw new BusinessException("带药单不存在或已删除");
            }
            if (!java.util.Objects.equals(d.getDispenseStatus(), DischargeDrugStatusEnum.PENDING.getCode())) {
                throw new BusinessException("带药单 " + d.getOrderNo() + " 已发药，不能重复发药");
            }
        }
        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());
        String who = UserUtils.getCurrentEmployeeName();
        Long empId = UserUtils.getCurrentEmployeeId();
        for (BizDischargeDrug d : list) {
            d.setDispenseStatus(DischargeDrugStatusEnum.DISPENSED.getCode());
            d.setDispenseBy(empId);
            d.setDispenseName(who);
            d.setDispenseTime(now);
            if (StringUtils.hasText(dto.getRemark())) {
                d.setRemark((d.getRemark() == null ? "" : d.getRemark() + "；") + "发药备注：" + dto.getRemark().trim());
            }
            d.setUpdateBy(who);
            d.setUpdateTime(now);
            drugMapper.updateById(d);
        }
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDischargeDrug d = drugMapper.selectById(id);
        if (d == null || (d.getDelFlag() != null && d.getDelFlag() == 1)) {
            throw new BusinessException("带药单不存在");
        }
        if (!java.util.Objects.equals(d.getDispenseStatus(), DischargeDrugStatusEnum.PENDING.getCode())) {
            throw new BusinessException("已发药的带药单不能删除");
        }
        drugMapper.deleteById(id);
    }

    /**
     * MP 全局逻辑删除下 deleteById 自动走软删，这里只需要查活单时对齐 del_flag=0
     */
    private String tr(String s) {
        return s == null ? null : s.trim();
    }

    private Long requirePatientId(Long admissionId) {
        Long patientId = drugMapper.selectPatientIdByAdmission(admissionId);
        if (patientId == null) {
            throw new BusinessException("入院记录不存在（admissionId=" + admissionId + "）");
        }
        return patientId;
    }

    private void fillPatientSnapshot(BizDischargeDrug d) {
        BizDischargeDrug snap = drugMapper.selectPatientSnapshot(d.getAdmissionId());
        if (snap != null) {
            d.setPatientNo(snap.getPatientNo());
            d.setPatientName(snap.getPatientName());
        }
    }

    /**
     * 单号 DDA + yyyyMMddHHmmss + 3 位随机，撞库概率忽略；唯一索引兜底
     */
    private String nextOrderNo() {
        return "DDA" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private DischargeDrugVO toVo(BizDischargeDrug d) {
        DischargeDrugVO vo = new DischargeDrugVO();
        org.springframework.beans.BeanUtils.copyProperties(d, vo);
        vo.setDispenseStatusText(dictText.getDicDataLabel(DICT_STATUS, d.getDispenseStatus()));
        return vo;
    }

    private DischargeDrugSelectListVO toSelectVo(BizDischargeDrug d) {
        DischargeDrugSelectListVO vo = new DischargeDrugSelectListVO();
        org.springframework.beans.BeanUtils.copyProperties(toVo(d), vo);
        return vo;
    }
}
