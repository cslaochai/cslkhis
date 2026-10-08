package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.LisQcDTO;
import com.his.medicaltech.entity.BizLisQcPlan;
import com.his.medicaltech.entity.BizLisQcRecord;
import com.his.medicaltech.mapper.BizLisQcPlanMapper;
import com.his.medicaltech.mapper.BizLisQcRecordMapper;
import com.his.medicaltech.service.LisQcService;
import com.his.medicaltech.support.WestgardRuleEngine;
import com.his.medicaltech.vo.LisQcVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LIS 室内质控服务
 */
@Service
@RequiredArgsConstructor
public class LisQcServiceImpl implements LisQcService {


    private final BizLisQcPlanMapper bizLisQcPlanMapper;
    private final BizLisQcRecordMapper bizLisQcRecordMapper;
    private final DictCacheService dictCacheService;

    // 计划

    public PageResult<LisQcVO.PlanVO> planPage(LisQcDTO.PlanQuery q) {
        LambdaQueryWrapper<BizLisQcPlan> w = new LambdaQueryWrapper<>();
        w.like(TextUtil.hasText(q.getItemName()), BizLisQcPlan::getItemName, TextUtil.trim(q.getItemName()))
                .like(TextUtil.hasText(q.getInstrumentName()), BizLisQcPlan::getInstrumentName, TextUtil.trim(q.getInstrumentName()))
                .eq(q.getStatus() != null, BizLisQcPlan::getStatus, q.getStatus())
                .orderByDesc(BizLisQcPlan::getId);
        Page<BizLisQcPlan> page = bizLisQcPlanMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<LisQcVO.PlanVO> vos = new ArrayList<>();
        for (BizLisQcPlan p : page.getRecords()) {
            vos.add(toPlanVo(p));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private LisQcVO.PlanVO toPlanVo(BizLisQcPlan p) {
        LisQcVO.PlanVO vo = new LisQcVO.PlanVO();
        BeanUtils.copyProperties(p, vo);
        vo.setQcLevelText(dictCacheService.getDicDataLabel(DictType.LIS_QC_LEVEL, p.getQcLevel()));
        vo.setStatusText(p.getStatus() != null && p.getStatus() == 1 ? "启用" : "停用");
        if (p.getMeanValue() != null && p.getSdValue() != null && p.getMeanValue().compareTo(BigDecimal.ZERO) != 0) {
            vo.setCvActual(p.getSdValue().divide(p.getMeanValue().abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        }
        vo.setRecordCount(bizLisQcRecordMapper.selectCount(
                new LambdaQueryWrapper<BizLisQcRecord>().eq(BizLisQcRecord::getPlanId, p.getId())).intValue());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public String planUpsert(LisQcDTO.PlanUpsert dto) {
        if (dto.getSdValue() != null && dto.getSdValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("标准差 SD 必须大于 0（SD=0 时 Z 值无法计算）");
        }
        BizLisQcPlan p;
        if (dto.getId() == null) {
            // 同（项目+仪器+水平）只允许一条启用中的计划 —— 否则录入结果时不知道挂哪份靶值
            if (TextUtil.hasText(dto.getItemCode())) {
                long dup = bizLisQcPlanMapper.selectCount(new LambdaQueryWrapper<BizLisQcPlan>()
                        .eq(BizLisQcPlan::getItemCode, dto.getItemCode())
                        .eq(TextUtil.hasText(dto.getInstrumentName()), BizLisQcPlan::getInstrumentName, dto.getInstrumentName())
                        .eq(BizLisQcPlan::getQcLevel, dto.getQcLevel() == null ? 2 : dto.getQcLevel())
                        .eq(BizLisQcPlan::getStatus, 1));
                if (dup > 0) {
                    throw new BusinessException("该检验项目同仪器同水平的质控计划已存在，请编辑原计划（避免同一项目挂两份靶值）");
                }
            }
            p = new BizLisQcPlan();
            BeanUtils.copyProperties(dto, p);
            p.setId(null);
            if (p.getQcLevel() == null) {
                p.setQcLevel(2);
            }
            if (p.getStatus() == null) {
                p.setStatus(1);
            }
            p.setPlanNo(nextPlanNo(dto.getItemCode(), p.getQcLevel()));
            bizLisQcPlanMapper.insert(p);
        } else {
            p = requirePlan(dto.getId());
            BeanUtils.copyProperties(dto, p);
            p.setId(dto.getId());
            p.setPlanNo(requirePlan(dto.getId()).getPlanNo());
            bizLisQcPlanMapper.updateById(p);
        }
        return p.getPlanNo();
    }

    @Transactional(rollbackFor = Exception.class)
    public void planToggle(Long planId, Integer status) {
        BizLisQcPlan p = requirePlan(planId);
        p.setStatus(status);
        bizLisQcPlanMapper.updateById(p);
    }

    // 结果录入（服务端判定）

    @Transactional(rollbackFor = Exception.class)
    public LisQcVO.RecordVO inputResult(LisQcDTO.ResultInput dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizLisQcPlan plan = requirePlan(dto.getPlanId());
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException("质控计划已停用（" + plan.getPlanNo() + "），不能录入质控结果");
        }
        if (plan.getExpireDate() != null && plan.getExpireDate().isBefore(LocalDate.now())) {
            throw new BusinessException("质控品已过效期（" + plan.getExpireDate() + "），先换批号再测");
        }
        BigDecimal z = WestgardRuleEngine.zScore(dto.getResultValue(), plan.getMeanValue(), plan.getSdValue());

        BizLisQcRecord r = new BizLisQcRecord();
        r.setPlanId(plan.getId());
        r.setPlanNo(plan.getPlanNo());
        r.setItemCode(plan.getItemCode());
        r.setItemName(plan.getItemName());
        r.setInstrumentName(plan.getInstrumentName());
        r.setQcLevel(plan.getQcLevel());
        r.setQcDate(LocalDate.now());
        r.setQcTime(LocalDateTime.now().withNano(0));
        r.setResultValue(dto.getResultValue());
        r.setZScore(z);
        r.setOperator(operatorUser.getRealName());
        r.setRemark(TextUtil.cut(dto.getRemark(), 480));

        if (z == null) {
            // 算不出 Z 绝不判在控：落到「未判定」（status=0），并提示补靶值
            r.setStatus(0);
            r.setViolatedRules(null);
            r.setHandleStatus(0);
        } else {
            WestgardRuleEngine.Verdict verdict = WestgardRuleEngine.evaluate(historyAsc(plan.getId(), 10),
                    z.doubleValue(), batchOthers(plan, r.getQcDate()));
            r.setStatus(verdict.getStatus());
            r.setViolatedRules(TextUtil.hasText(verdict.ruleText()) ? verdict.ruleText() : null);
            r.setHandleStatus(verdict.getStatus() == WestgardRuleEngine.OUT_OF_CONTROL ? 1 : 0);
        }
        bizLisQcRecordMapper.insert(r);
        return toRecordVo(r);
    }

    /**
     * 该计划最近 n 个点（时间升序，不含当前点）。同秒多点必须补 id 二级键，否则「前一点」取错，2-2s/7-T 全部失真
     */
    private List<WestgardRuleEngine.QcPoint> historyAsc(Long planId, int n) {
        List<BizLisQcRecord> latest = bizLisQcRecordMapper.selectList(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getPlanId, planId)
                .isNotNull(BizLisQcRecord::getZScore)
                .orderByDesc(BizLisQcRecord::getQcTime)
                .orderByDesc(BizLisQcRecord::getId)
                .last("LIMIT " + n));
        List<WestgardRuleEngine.QcPoint> out = new ArrayList<>();
        for (int i = latest.size() - 1; i >= 0; i--) {
            out.add(new WestgardRuleEngine.QcPoint(latest.get(i).getZScore().doubleValue()));
        }
        return out;
    }

    /**
     * R-4s 用：同项目+同仪器、同日其它水平的最近一点
     */
    private List<WestgardRuleEngine.QcPoint> batchOthers(BizLisQcPlan plan, LocalDate date) {
        List<BizLisQcPlan> siblings = bizLisQcPlanMapper.selectList(new LambdaQueryWrapper<BizLisQcPlan>()
                .eq(BizLisQcPlan::getItemCode, plan.getItemCode())
                .eq(TextUtil.hasText(plan.getInstrumentName()), BizLisQcPlan::getInstrumentName, plan.getInstrumentName())
                .ne(BizLisQcPlan::getId, plan.getId())
                .eq(BizLisQcPlan::getStatus, 1));
        List<WestgardRuleEngine.QcPoint> out = new ArrayList<>();
        for (BizLisQcPlan s : siblings) {
            List<BizLisQcRecord> rec = bizLisQcRecordMapper.selectList(new LambdaQueryWrapper<BizLisQcRecord>()
                    .eq(BizLisQcRecord::getPlanId, s.getId())
                    .eq(BizLisQcRecord::getQcDate, date)
                    .isNotNull(BizLisQcRecord::getZScore)
                    .orderByDesc(BizLisQcRecord::getQcTime)
                    .last("LIMIT 1"));
            rec.stream().map(x -> new WestgardRuleEngine.QcPoint(x.getZScore().doubleValue())).forEach(out::add);
        }
        return out;
    }

    // 记录查询 / 失控处理 / 复核

    public PageResult<LisQcVO.RecordVO> recordPage(LisQcDTO.RecordQuery q) {
        LambdaQueryWrapper<BizLisQcRecord> w = new LambdaQueryWrapper<>();
        w.eq(q.getPlanId() != null, BizLisQcRecord::getPlanId, q.getPlanId())
                .like(TextUtil.hasText(q.getItemName()), BizLisQcRecord::getItemName, TextUtil.trim(q.getItemName()))
                .like(TextUtil.hasText(q.getInstrumentName()), BizLisQcRecord::getInstrumentName, TextUtil.trim(q.getInstrumentName()))
                .eq(q.getStatus() != null, BizLisQcRecord::getStatus, q.getStatus())
                .eq(q.getHandleStatus() != null, BizLisQcRecord::getHandleStatus, q.getHandleStatus())
                .ge(q.getStartDate() != null, BizLisQcRecord::getQcDate, q.getStartDate())
                .le(q.getEndDate() != null, BizLisQcRecord::getQcDate, q.getEndDate())
                .orderByDesc(BizLisQcRecord::getQcTime)
                .orderByDesc(BizLisQcRecord::getId);
        Page<BizLisQcRecord> page = bizLisQcRecordMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<LisQcVO.RecordVO> vos = new ArrayList<>();
        for (BizLisQcRecord r : page.getRecords()) {
            vos.add(toRecordVo(r));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private LisQcVO.RecordVO toRecordVo(BizLisQcRecord r) {
        LisQcVO.RecordVO vo = new LisQcVO.RecordVO();
        BeanUtils.copyProperties(r, vo);
        vo.setQcLevelText(dictCacheService.getDicDataLabel(DictType.LIS_QC_LEVEL, r.getQcLevel()));
        vo.setStatusText(r.getStatus() == null || r.getStatus() == 0 ? "未判定"
                : dictCacheService.getDicDataLabel(DictType.LIS_QC_STATUS, r.getStatus()));
        vo.setHandleStatusText(dictCacheService.getDicDataLabel(DictType.LIS_QC_HANDLE_STATUS, r.getHandleStatus()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handle(LisQcDTO.Handle dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizLisQcRecord r = requireRecord(dto.getRecordId());
        if (r.getStatus() == null || r.getStatus() != 3) {
            throw new BusinessException("仅「失控」记录需要处理（当前：" + voStatusText(r.getStatus()) + "）");
        }
        if (r.getHandleStatus() != null && r.getHandleStatus() == 2) {
            throw new BusinessException("该失控记录已处理，不可重复处理");
        }
        r.setHandleStatus(2);
        r.setHandleCause(TextUtil.cut(dto.getHandleCause(), 480));
        r.setHandleMeasure(TextUtil.cut(dto.getHandleMeasure(), 480));
        r.setHandleBy(operatorUser.getRealName());
        r.setHandleTime(LocalDateTime.now().withNano(0));
        bizLisQcRecordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void review(LisQcDTO.Review dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizLisQcRecord r = requireRecord(dto.getRecordId());
        if (r.getStatus() == null || r.getStatus() != 3) {
            throw new BusinessException("仅「失控」记录需要复核");
        }
        if (r.getHandleStatus() == null || r.getHandleStatus() != 2) {
            throw new BusinessException("复核前必须先完成处理（当前：" + dictCacheService.getDicDataLabel(DictType.LIS_QC_HANDLE_STATUS, r.getHandleStatus()) + "）");
        }
        String who = operatorUser.getRealName();
        if (who != null && who.equals(r.getHandleBy())) {
            throw new BusinessException("复核人不得是处理人本人（" + who + "）——失控纠正必须第二人确认");
        }
        r.setReviewBy(who);
        r.setReviewTime(LocalDateTime.now().withNano(0));
        bizLisQcRecordMapper.updateById(r);
    }

    // 统计

    public LisQcVO.StatsVO stats() {
        LisQcVO.StatsVO vo = new LisQcVO.StatsVO();
        vo.setPlanCount(bizLisQcPlanMapper.selectCount(new LambdaQueryWrapper<BizLisQcPlan>()
                .eq(BizLisQcPlan::getStatus, 1)));
        vo.setTodayCount(bizLisQcRecordMapper.selectCount(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getQcDate, LocalDate.now())));
        vo.setInControl(bizLisQcRecordMapper.selectCount(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getQcDate, LocalDate.now()).eq(BizLisQcRecord::getStatus, 1)));
        vo.setWarning(bizLisQcRecordMapper.selectCount(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getQcDate, LocalDate.now()).eq(BizLisQcRecord::getStatus, 2)));
        vo.setOutOfControl(bizLisQcRecordMapper.selectCount(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getQcDate, LocalDate.now()).eq(BizLisQcRecord::getStatus, 3)));
        vo.setPendingHandle(bizLisQcRecordMapper.selectCount(new LambdaQueryWrapper<BizLisQcRecord>()
                .eq(BizLisQcRecord::getHandleStatus, 1)));
        if (vo.getTodayCount() > 0) {
            vo.setInControlRate(BigDecimal.valueOf(vo.getInControl())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(vo.getTodayCount()), 1, RoundingMode.HALF_UP));
        } else {
            vo.setInControlRate(BigDecimal.ZERO);
        }
        return vo;
    }

    // 内部

    private BizLisQcPlan requirePlan(Long id) {
        // C-非 web 入参：私有 helper 按主键捞单，被多个入口（DTO 字段与标量参数）复用，Bean Validation 不覆盖，保留
        if (id == null) {
            throw new BusinessException("质控计划ID不能为空");
        }
        BizLisQcPlan p = bizLisQcPlanMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("质控计划不存在：" + id);
        }
        return p;
    }

    private BizLisQcRecord requireRecord(Long id) {
        // C-非 web 入参：私有 helper 按主键捞单，被多个入口（DTO 字段与标量参数）复用，Bean Validation 不覆盖，保留
        if (id == null) {
            throw new BusinessException("质控记录ID不能为空");
        }
        BizLisQcRecord r = bizLisQcRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("质控记录不存在：" + id);
        }
        return r;
    }

    private String voStatusText(Integer status) {
        return status == null || status == 0 ? "未判定" : dictCacheService.getDicDataLabel(DictType.LIS_QC_STATUS, status);
    }

    private String nextPlanNo(String itemCode, Integer qcLevel) {
        String base = "QC" + (itemCode == null ? "NA" : itemCode) + "-" + qcLevel;
        BizLisQcPlan same = bizLisQcPlanMapper.selectOne(new LambdaQueryWrapper<BizLisQcPlan>()
                .eq(BizLisQcPlan::getPlanNo, base).last("LIMIT 1"));
        if (same == null) {
            return base;
        }
        return base + "-" + System.currentTimeMillis() % 100000;
    }

}
