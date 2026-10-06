package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.utils.UserUtils;
import com.his.system.dto.DoseLimitQueryPageDTO;
import com.his.system.dto.DoseLimitUpsertDTO;
import com.his.system.dto.DrugInteractionQueryPageDTO;
import com.his.system.dto.DrugInteractionUpsertDTO;
import com.his.system.entity.SysDrugDoseLimit;
import com.his.system.entity.SysDrugInteraction;
import com.his.system.mapper.SysDrugDoseLimitMapper;
import com.his.system.mapper.SysDrugInteractionMapper;
import com.his.system.mapper.SysDrugMapper;
import com.his.system.service.DrugKnowledgeService;
import com.his.system.support.DrugComponentPair;
import com.his.system.vo.DoseLimitVO;
import com.his.system.vo.DrugInteractionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 合理用药知识库维护实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugKnowledgeServiceImpl implements DrugKnowledgeService {

    /** 列宽（sql/130）：写库前一律截断，超长会把「保存」升级成 500，用户连原因都看不到（AGENTS §3） */
    private static final int WIDTH_COMPONENT = 50;
    private static final int WIDTH_TEXT = 500;
    private static final int WIDTH_NOTE = 200;

    private static final Set<Integer> SEVERITIES = Set.of(1, 2);
    private static final Set<String> DOSE_UNITS = Set.of("g", "mg", "ug");

    private final SysDrugInteractionMapper interactionMapper;
    private final SysDrugDoseLimitMapper doseLimitMapper;
    private final SysDrugMapper drugMapper;

    @Override
    public PageResult<DrugInteractionVO> interactionListPage(DrugInteractionQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysDrugInteraction> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getComponent())) {
            String kw = queryDTO.getComponent().trim();
            wrapper.and(w -> w.like(SysDrugInteraction::getComponentA, kw)
                    .or().like(SysDrugInteraction::getComponentB, kw));
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(SysDrugInteraction::getInteractionDesc, kw)
                    .or().like(SysDrugInteraction::getSuggestion, kw));
        }
        wrapper.eq(queryDTO.getSeverity() != null, SysDrugInteraction::getSeverity, queryDTO.getSeverity())
                .eq(queryDTO.getStatus() != null, SysDrugInteraction::getStatus, queryDTO.getStatus())
                // 禁忌排前面（要拦人的先审），同severity 按录入倒序，末尾带 id 保证分页不重不漏
                .orderByAsc(SysDrugInteraction::getSeverity)
                .orderByDesc(SysDrugInteraction::getId);

        Page<SysDrugInteraction> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        interactionMapper.selectPage(page, wrapper);
        List<DrugInteractionVO> records = page.getRecords().stream().map(this::toInteractionVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugInteractionVO interactionUpsert(DrugInteractionUpsertDTO upsertDTO) {
        if (!SEVERITIES.contains(upsertDTO.getSeverity())) {
            throw new BusinessException("严重度只能是 1-禁忌 或 2-慎用");
        }
        String pairKey = DrugComponentPair.buildKey(upsertDTO.getComponentA(), upsertDTO.getComponentB());

        LambdaQueryWrapper<SysDrugInteraction> dup = new LambdaQueryWrapper<>();
        dup.eq(SysDrugInteraction::getPairKey, pairKey)
                .ne(upsertDTO.getId() != null, SysDrugInteraction::getId, upsertDTO.getId());
        if (interactionMapper.exists(dup)) {
            throw new BusinessException("这两个成分的知识条目已存在（同成分对只允许一条，请改原条目或先删除）");
        }

        SysDrugInteraction entity = new SysDrugInteraction();
        entity.setPairKey(pairKey);
        entity.setSeverity(upsertDTO.getSeverity());
        entity.setInteractionDesc(cut(upsertDTO.getInteractionDesc(), WIDTH_TEXT, "相互作用后果"));
        entity.setSuggestion(cut(upsertDTO.getSuggestion(), WIDTH_TEXT, "处理建议"));
        entity.setStatus(upsertDTO.getStatus() == null ? 1 : upsertDTO.getStatus());
        entity.setRemark(cut(upsertDTO.getRemark(), WIDTH_TEXT, "备注"));

        String operator = UserUtils.getCurrentUser().getRealName();
        if (upsertDTO.getId() == null) {
            // 归一化后的顺序才是库里存的顺序（前端把谁写在前面对结果没有影响）
            List<String> parts = DrugComponentPair.parts(pairKey);
            entity.setComponentA(parts.get(0));
            entity.setComponentB(parts.get(1));
            entity.setCreateBy(operator);
            interactionMapper.insert(entity);
        } else {
            SysDrugInteraction old = interactionMapper.selectById(upsertDTO.getId());
            if (old == null) {
                throw new BusinessException("知识条目不存在");
            }
            List<String> parts = DrugComponentPair.parts(pairKey);
            entity.setComponentA(parts.get(0));
            entity.setComponentB(parts.get(1));
            entity.setId(upsertDTO.getId());
            entity.setUpdateBy(operator);
            interactionMapper.updateById(entity);
            // updateById 只写非 null 字段，等于「清空建议」这个动作会被静默忽略；显式置空补齐
            if (!StringUtils.hasText(upsertDTO.getSuggestion()) || !StringUtils.hasText(upsertDTO.getRemark())) {
                LambdaUpdateWrapper<SysDrugInteraction> uw = new LambdaUpdateWrapper<>();
                uw.eq(SysDrugInteraction::getId, entity.getId());
                if (!StringUtils.hasText(upsertDTO.getSuggestion())) {
                    uw.set(SysDrugInteraction::getSuggestion, null);
                }
                if (!StringUtils.hasText(upsertDTO.getRemark())) {
                    uw.set(SysDrugInteraction::getRemark, null);
                }
                interactionMapper.update(null, uw);
            }
        }
        log.info("合理用药知识-相互作用 保存：id={}, pairKey={}, severity={}",
                entity.getId(), pairKey, entity.getSeverity());
        return toInteractionVO(interactionMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void interactionDeleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少主键");
        }
        if (interactionMapper.selectById(id) == null) {
            // 列表页开着，另一个标签页已经把它删了 —— 静默成功会让行在刷新后又冒出来，说不清
            throw new BusinessException("知识条目不存在或已被删除");
        }
        // 物理删：uk_pair_key 不含 del_flag，软删会把这对成分永久占住
        interactionMapper.purgeById(id);
    }

    @Override
    public PageResult<DoseLimitVO> doseLimitListPage(DoseLimitQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysDrugDoseLimit> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getComponent())) {
            wrapper.like(SysDrugDoseLimit::getComponent, queryDTO.getComponent().trim());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(SysDrugDoseLimit::getNote, queryDTO.getKeyword().trim());
        }
        wrapper.eq(queryDTO.getStatus() != null, SysDrugDoseLimit::getStatus, queryDTO.getStatus())
                .orderByDesc(SysDrugDoseLimit::getId);

        Page<SysDrugDoseLimit> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        doseLimitMapper.selectPage(page, wrapper);
        List<DoseLimitVO> records = page.getRecords().stream().map(this::toDoseLimitVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DoseLimitVO doseLimitUpsert(DoseLimitUpsertDTO upsertDTO) {
        String component = cut(upsertDTO.getComponent(), WIDTH_COMPONENT, "成分");
        String unit = upsertDTO.getDoseUnit() == null ? "" : upsertDTO.getDoseUnit().trim().toLowerCase();
        if (!DOSE_UNITS.contains(unit)) {
            // 建表带 chk_dose_unit_unit，服务端先拦是为了给出人话，而不是让 CHECK 兜成 500
            throw new BusinessException("剂量单位只支持 g/mg/ug（IU、ml、片 无法与极量比较，见 sql/130 文件头第四条）");
        }
        if (upsertDTO.getMaxSingleDose() == null && upsertDTO.getMaxDailyDose() == null) {
            throw new BusinessException("单次最大量与每日最大量至少填一项，否则这条知识什么都不判");
        }
        checkNonNegative(upsertDTO.getMaxSingleDose(), "单次最大量");
        checkNonNegative(upsertDTO.getMaxDailyDose(), "每日最大量");

        LambdaQueryWrapper<SysDrugDoseLimit> dup = new LambdaQueryWrapper<>();
        dup.eq(SysDrugDoseLimit::getComponent, component)
                .ne(upsertDTO.getId() != null, SysDrugDoseLimit::getId, upsertDTO.getId());
        if (doseLimitMapper.exists(dup)) {
            throw new BusinessException("该成分已配置剂量上限（一个成分只允许一条）");
        }

        SysDrugDoseLimit entity = new SysDrugDoseLimit();
        entity.setComponent(component);
        entity.setDoseUnit(unit);
        entity.setMaxSingleDose(upsertDTO.getMaxSingleDose());
        entity.setMaxDailyDose(upsertDTO.getMaxDailyDose());
        entity.setNote(cut(upsertDTO.getNote(), WIDTH_NOTE, "口径说明"));
        entity.setStatus(upsertDTO.getStatus() == null ? 1 : upsertDTO.getStatus());
        entity.setRemark(cut(upsertDTO.getRemark(), WIDTH_TEXT, "备注"));

        String operator = UserUtils.getCurrentUser().getRealName();
        if (upsertDTO.getId() == null) {
            entity.setCreateBy(operator);
            doseLimitMapper.insert(entity);
        } else {
            if (doseLimitMapper.selectById(upsertDTO.getId()) == null) {
                throw new BusinessException("剂量上限条目不存在");
            }
            entity.setId(upsertDTO.getId());
            entity.setUpdateBy(operator);
            doseLimitMapper.updateById(entity);
            // 两个阈值都允许「留空=这一项不判」，而 updateById 不写 null —— 不显式清就永远清不掉
            if (upsertDTO.getMaxSingleDose() == null || upsertDTO.getMaxDailyDose() == null) {
                LambdaUpdateWrapper<SysDrugDoseLimit> uw = new LambdaUpdateWrapper<>();
                uw.eq(SysDrugDoseLimit::getId, entity.getId());
                if (upsertDTO.getMaxSingleDose() == null) {
                    uw.set(SysDrugDoseLimit::getMaxSingleDose, null);
                }
                if (upsertDTO.getMaxDailyDose() == null) {
                    uw.set(SysDrugDoseLimit::getMaxDailyDose, null);
                }
                doseLimitMapper.update(null, uw);
            }
        }
        log.info("合理用药知识-剂量上限 保存：id={}, component={}, unit={}",
                entity.getId(), component, unit);
        return toDoseLimitVO(doseLimitMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doseLimitDeleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少主键");
        }
        if (doseLimitMapper.selectById(id) == null) {
            throw new BusinessException("剂量上限条目不存在或已被删除");
        }
        doseLimitMapper.purgeById(id);
    }

    private DrugInteractionVO toInteractionVO(SysDrugInteraction entity) {
        DrugInteractionVO vo = new DrugInteractionVO();
        vo.setId(entity.getId());
        vo.setComponentA(entity.getComponentA());
        vo.setComponentB(entity.getComponentB());
        vo.setPairKey(entity.getPairKey());
        vo.setSeverity(entity.getSeverity());
        vo.setInteractionDesc(entity.getInteractionDesc());
        vo.setSuggestion(entity.getSuggestion());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateBy(entity.getCreateBy());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setDrugHitsA(drugMapper.countByComponent(entity.getComponentA()));
        vo.setDrugHitsB(drugMapper.countByComponent(entity.getComponentB()));
        return vo;
    }

    private DoseLimitVO toDoseLimitVO(SysDrugDoseLimit entity) {
        DoseLimitVO vo = new DoseLimitVO();
        vo.setId(entity.getId());
        vo.setComponent(entity.getComponent());
        vo.setDoseUnit(entity.getDoseUnit());
        vo.setMaxSingleDose(entity.getMaxSingleDose());
        vo.setMaxDailyDose(entity.getMaxDailyDose());
        vo.setNote(entity.getNote());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateBy(entity.getCreateBy());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setDrugHits(drugMapper.countByComponent(entity.getComponent()));
        return vo;
    }

    private void checkNonNegative(BigDecimal value, String label) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(label + "必须大于 0（留空表示这一项不判）");
        }
    }

    private String cut(String value, int max, String label) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            log.warn("合理用药知识字段超长已截断：{}（{} > {}）", label, trimmed.length(), max);
            return trimmed.substring(0, max);
        }
        return trimmed;
    }
}
