package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.config.MisProperties;
import com.mis.dto.InspectionFinishDTO;
import com.mis.dto.InspectionItemUpsertDTO;
import com.mis.dto.InspectionQueryDTO;
import com.mis.dto.InspectionUpsertDTO;
import com.mis.entity.MssInspection;
import com.mis.entity.MssInspectionItem;
import com.mis.entity.MssRecover;
import com.mis.mapper.MssInspectionItemMapper;
import com.mis.mapper.MssInspectionMapper;
import com.mis.mapper.MssRecoverMapper;
import com.mis.service.InspectionService;
import com.mis.vo.ActionVO;
import com.mis.vo.InspectionDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MssInspectionMapper inspectionMapper;
    private final MssInspectionItemMapper itemMapper;
    private final MssRecoverMapper recoverMapper;
    private final MisProperties properties;

    @Override
    public PageVO<MssInspection> inspectionPage(InspectionQueryDTO q) {
        Page<MssInspection> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssInspection> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(MssInspection::getInspectionNo, kw)
                    .or().like(MssInspection::getTitle, kw)
                    .or().like(MssInspection::getTargetOrg, kw));
        }
        if (hasText(q.getInspectType())) {
            w.eq(MssInspection::getInspectType, q.getInspectType());
        }
        if (hasText(q.getStatus())) {
            w.eq(MssInspection::getStatus, q.getStatus());
        }
        w.orderByDesc(MssInspection::getId);
        Page<MssInspection> res = inspectionMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public ActionVO upsert(InspectionUpsertDTO dto) {
        MssInspection t;
        if (dto.getId() == null) {
            t = new MssInspection();
            t.setInspectionNo("FJ" + LocalDate.now().format(DAY)
                    + String.format("%03d", inspectionMapper.selectCount(null) + 1));
            t.setStatus("草稿");
        } else {
            t = inspectionMapper.selectById(dto.getId());
            if (t == null) {
                return ActionVO.fail("任务不存在");
            }
        }
        t.setTitle(dto.getTitle());
        t.setInspectType(dto.getInspectType());
        t.setTargetOrg(dto.getTargetOrg());
        t.setPlanStart(dto.getPlanStart());
        t.setPlanEnd(dto.getPlanEnd());
        t.setLeader(dto.getLeader());
        t.setMembers(dto.getMembers());
        t.setScopeDesc(dto.getScopeDesc());
        if (dto.getId() == null) {
            inspectionMapper.insert(t);
        } else {
            inspectionMapper.updateById(t);
        }
        return ActionVO.ok();
    }

    @Override
    public ActionVO start(Long id) {
        MssInspection t = inspectionMapper.selectById(id);
        if (t == null) {
            return ActionVO.fail("任务不存在");
        }
        if (!"草稿".equals(t.getStatus()) && !"待开展".equals(t.getStatus())) {
            return ActionVO.fail("当前状态不可开始检查");
        }
        t.setStatus("检查中");
        inspectionMapper.updateById(t);
        return ActionVO.ok();
    }

    @Override
    public ActionVO finish(InspectionFinishDTO dto) {
        MssInspection t = inspectionMapper.selectById(dto.getId());
        if (t == null) {
            return ActionVO.fail("任务不存在");
        }
        if (!"检查中".equals(t.getStatus())) {
            return ActionVO.fail("仅检查中的任务可办结");
        }
        t.setStatus("已完成");
        t.setResultSummary(dto.getResultSummary());
        inspectionMapper.updateById(t);
        return ActionVO.ok();
    }

    @Override
    public InspectionDetailVO getDetail(Long id) {
        MssInspection t = inspectionMapper.selectById(id);
        if (t == null) {
            return null;
        }
        List<MssInspectionItem> items = itemMapper.selectList(new LambdaQueryWrapper<MssInspectionItem>()
                .eq(MssInspectionItem::getInspectionId, id)
                .orderByAsc(MssInspectionItem::getId));
        BigDecimal total = BigDecimal.ZERO;
        for (MssInspectionItem it : items) {
            if (it.getInvolvedAmount() != null) {
                total = total.add(it.getInvolvedAmount());
            }
        }
        InspectionDetailVO vo = new InspectionDetailVO();
        vo.setTask(t);
        vo.setItems(items);
        vo.setTotalAmount(total);
        return vo;
    }

    @Override
    public ActionVO addItem(InspectionItemUpsertDTO dto) {
        MssInspection t = inspectionMapper.selectById(dto.getInspectionId());
        if (t == null) {
            return ActionVO.fail("任务不存在");
        }
        if (!"检查中".equals(t.getStatus())) {
            return ActionVO.fail("仅检查中的任务可录入问题");
        }
        MssInspectionItem item = new MssInspectionItem();
        item.setInspectionId(dto.getInspectionId());
        item.setProblemType(dto.getProblemType());
        item.setProblemDesc(dto.getProblemDesc());
        item.setEvidence(dto.getEvidence());
        item.setInvolvedAmount(dto.getInvolvedAmount());
        item.setDispose(dto.getDispose());
        itemMapper.insert(item);
        return ActionVO.ok();
    }

    @Override
    public ActionVO deleteItem(Long id) {
        MssInspectionItem item = itemMapper.selectById(id);
        if (item == null) {
            return ActionVO.fail("问题记录不存在");
        }
        // 本表无 del_flag，删除走物理删
        itemMapper.deleteById(id);
        return ActionVO.ok();
    }

    @Override
    public ActionVO toRecover(Long id) {
        MssInspection t = inspectionMapper.selectById(id);
        if (t == null) {
            return ActionVO.fail("任务不存在");
        }
        List<MssInspectionItem> items = itemMapper.selectList(new LambdaQueryWrapper<MssInspectionItem>()
                .eq(MssInspectionItem::getInspectionId, id)
                .eq(MssInspectionItem::getDispose, "追回"));
        if (items.isEmpty()) {
            return ActionVO.fail("请先录入处置方式为「追回」的问题明细");
        }
        BigDecimal amount = BigDecimal.ZERO;
        for (MssInspectionItem it : items) {
            if (it.getInvolvedAmount() != null) {
                amount = amount.add(it.getInvolvedAmount());
            }
        }
        MssRecover r = new MssRecover();
        r.setRecoverNo("ZJ" + LocalDate.now().format(DAY)
                + String.format("%03d", recoverMapper.selectCount(null) + 1));
        r.setSourceType(2);
        r.setSourceNo(t.getInspectionNo());
        r.setTargetOrgCode(properties.getMedins().getCode());
        r.setTargetOrgName(t.getTargetOrg() != null ? t.getTargetOrg() : properties.getMedins().getName());
        r.setRecoverAmount(amount);
        r.setReason(cut("飞检任务「" + nz(t.getTitle()) + "」发现违规问题 " + items.size() + " 项，涉及金额合计 " + amount.toPlainString() + " 元", 500));
        r.setStatus("待追回");
        recoverMapper.insert(r);
        return ActionVO.ok();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
