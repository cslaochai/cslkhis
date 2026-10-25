package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.NurseScheduleChangeTypeEnum;
import com.his.common.enums.NurseScheduleUnitTypeEnum;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.common.util.TextUtil;
import com.his.outpatient.dto.NurseScheduleTemplateDTO;
import com.his.outpatient.dto.StaffScheduleUpsertDTO;
import com.his.outpatient.entity.BizNurseScheduleTemplate;
import com.his.outpatient.entity.BizNurseScheduleTemplateApply;
import com.his.outpatient.entity.BizNurseScheduleTemplateItem;
import com.his.outpatient.entity.BizNurseScheduleTemplateRule;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.mapper.BizNurseScheduleTemplateApplyMapper;
import com.his.outpatient.mapper.BizNurseScheduleTemplateItemMapper;
import com.his.outpatient.mapper.BizNurseScheduleTemplateMapper;
import com.his.outpatient.mapper.BizNurseScheduleTemplateRuleMapper;
import com.his.outpatient.mapper.BizScheduleMapper;
import com.his.outpatient.service.NurseScheduleTemplateService;
import com.his.outpatient.service.NurseScheduleLogService;
import com.his.outpatient.service.StaffScheduleService;
import com.his.outpatient.vo.NurseScheduleTemplateVO;
import com.his.outpatient.vo.NurseScheduleVO;
import com.his.spi.service.DeptScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 护理排班模板服务实现。套用模板走现有护理周排班底座（staffScheduleService.replaceDayAttendance，来源=TEMPLATE），
 * 与「复制上周」是同一条落库路径，避免另起炉灶写错事实类型。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NurseScheduleTemplateServiceImpl extends ServiceImpl<BizNurseScheduleTemplateMapper, BizNurseScheduleTemplate>
        implements NurseScheduleTemplateService {

    private final BizNurseScheduleTemplateItemMapper itemMapper;
    private final BizNurseScheduleTemplateRuleMapper ruleMapper;
    private final BizNurseScheduleTemplateApplyMapper applyMapper;
    private final BizScheduleMapper bizScheduleMapper;
    private final StaffScheduleService staffScheduleService;
    private final NurseScheduleLogService nurseScheduleLogService;
    private final DeptScopeService deptScopeService;

    private static final int DEL = 1;
    private static final int NOT_DEL = 0;

    // ---------------- 统计 ----------------

    @Override
    public NurseScheduleTemplateVO.Stats stats() {
        Long me = currentUserId();
        long myCount = count(new LambdaQueryWrapper<BizNurseScheduleTemplate>()
                .eq(BizNurseScheduleTemplate::getDelFlag, NOT_DEL)
                .eq(me != null, BizNurseScheduleTemplate::getCreateById, me));
        long deletedCount = count(new LambdaQueryWrapper<BizNurseScheduleTemplate>()
                .eq(BizNurseScheduleTemplate::getDelFlag, DEL));
        long sharedCount = count(new LambdaQueryWrapper<BizNurseScheduleTemplate>()
                .eq(BizNurseScheduleTemplate::getDelFlag, NOT_DEL)
                .eq(BizNurseScheduleTemplate::getScope, 1));
        List<Long> appliedIds = applyMapper.selectList(new LambdaQueryWrapper<BizNurseScheduleTemplateApply>()
                        .select(BizNurseScheduleTemplateApply::getTemplateId))
                .stream().map(BizNurseScheduleTemplateApply::getTemplateId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        long usedCount = appliedIds.isEmpty() ? 0
                : count(new LambdaQueryWrapper<BizNurseScheduleTemplate>()
                .eq(BizNurseScheduleTemplate::getDelFlag, NOT_DEL)
                .in(BizNurseScheduleTemplate::getId, appliedIds));
        NurseScheduleTemplateVO.Stats stats = new NurseScheduleTemplateVO.Stats();
        stats.setMyCount(myCount);
        stats.setDeletedCount(deletedCount);
        stats.setSharedCount(sharedCount);
        stats.setUsedCount(usedCount);
        return stats;
    }

    // ---------------- 模板分页 / 详情 ----------------

    @Override
    public PageResult<NurseScheduleTemplateVO.Template> pageTemplates(NurseScheduleTemplateDTO.PageQuery dto) {
        LambdaQueryWrapper<BizNurseScheduleTemplate> wrapper = new LambdaQueryWrapper<BizNurseScheduleTemplate>()
                .eq(BizNurseScheduleTemplate::getDelFlag, NOT_DEL)
                .eq(dto.getUnitType() != null, BizNurseScheduleTemplate::getUnitType, dto.getUnitType())
                .eq(dto.getWardId() != null, BizNurseScheduleTemplate::getWardId, dto.getWardId())
                .eq(dto.getStatus() != null, BizNurseScheduleTemplate::getStatus, dto.getStatus())
                .eq(dto.getScope() != null, BizNurseScheduleTemplate::getScope, dto.getScope())
                .and(TextUtil.hasText(dto.getKeyword()), w -> w.like(BizNurseScheduleTemplate::getName, dto.getKeyword()))
                .orderByDesc(BizNurseScheduleTemplate::getCreateTime);
        Page<BizNurseScheduleTemplate> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<Long> ids = page.getRecords().stream().map(BizNurseScheduleTemplate::getId).collect(Collectors.toList());
        Map<Long, Integer> itemCountMap = ids.isEmpty() ? Map.of() : itemMapper.selectList(
                        new LambdaQueryWrapper<BizNurseScheduleTemplateItem>()
                                .eq(BizNurseScheduleTemplateItem::getDelFlag, NOT_DEL)
                                .in(BizNurseScheduleTemplateItem::getTemplateId, ids))
                .stream().collect(Collectors.groupingBy(BizNurseScheduleTemplateItem::getTemplateId,
                        Collectors.summingInt(i -> 1)));
        Set<Long> appliedIds = applyMapper.selectList(new LambdaQueryWrapper<BizNurseScheduleTemplateApply>()
                        .select(BizNurseScheduleTemplateApply::getTemplateId))
                .stream().map(BizNurseScheduleTemplateApply::getTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        List<NurseScheduleTemplateVO.Template> records = page.getRecords().stream()
                .map(t -> toTemplateVO(t, itemCountMap.get(t.getId()),
                        appliedIds.contains(t.getId()))).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public NurseScheduleTemplateVO.Template detail(Long id) {
        BizNurseScheduleTemplate tpl = getById(id);
        if (tpl == null || DEL == tpl.getDelFlag()) {
            throw new BusinessException("模板不存在或已删除");
        }
        NurseScheduleTemplateVO.Template vo = toTemplateVO(tpl, null, existsApply(tpl.getId()));
        List<BizNurseScheduleTemplateItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<BizNurseScheduleTemplateItem>()
                        .eq(BizNurseScheduleTemplateItem::getDelFlag, NOT_DEL)
                        .eq(BizNurseScheduleTemplateItem::getTemplateId, id)
                        .orderByAsc(BizNurseScheduleTemplateItem::getWeekDay)
                        .orderByAsc(BizNurseScheduleTemplateItem::getEmployeeId));
        vo.setItems(items.stream().map(this::toItemVO).collect(Collectors.toList()));
        return vo;
    }

    // ---------------- 模板新增/修改/删除/启停 ----------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(NurseScheduleTemplateDTO.Upsert dto) {
        NurseScheduleVO.Ward ward = requireUnit(dto.getUnitType(), dto.getWardId());
        BizNurseScheduleTemplate tpl = new BizNurseScheduleTemplate();
        boolean isInsert = dto.getId() == null;
        if (!isInsert) {
            BizNurseScheduleTemplate exist = getById(dto.getId());
            if (exist == null || DEL == exist.getDelFlag()) {
                throw new BusinessException("模板不存在或已删除");
            }
            tpl.setId(dto.getId());
        }
        tpl.setName(TextUtil.trimToNull(dto.getName()));
        tpl.setUnitType(resolveUnitType(dto.getUnitType()));
        tpl.setWardId(ward.getWardId());
        tpl.setWardName(ward.getWardName());
        tpl.setWeekParity(dto.getWeekParity() == null ? 0 : dto.getWeekParity());
        tpl.setValidFrom(dto.getValidFrom());
        tpl.setValidUntil(dto.getValidUntil());
        tpl.setScope(dto.getScope() == null ? 0 : dto.getScope());
        tpl.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        tpl.setRemark(TextUtil.cut(TextUtil.trimToNull(dto.getRemark()), 500));

        if (isInsert) {
            tpl.setDelFlag(NOT_DEL);
            save(tpl);
        } else {
            updateById(tpl);
        }

        // 明细整批替换：先逻辑删旧行，再插新行（同事务）
        itemMapper.update(new BizNurseScheduleTemplateItem(), new LambdaUpdateWrapper<BizNurseScheduleTemplateItem>()
                .eq(BizNurseScheduleTemplateItem::getTemplateId, tpl.getId())
                .set(BizNurseScheduleTemplateItem::getDelFlag, DEL));
        int seq = 0;
        for (NurseScheduleTemplateDTO.Item src : dto.getItems()) {
            NurseScheduleVO.Nurse nurse = bizScheduleMapper.selectNurse(src.getEmployeeId());
            if (nurse == null) {
                throw new BusinessException("护士「" + (src.getEmployeeName() == null ? src.getEmployeeId() : src.getEmployeeName())
                        + "」不在在册护理名单里（需为在职护士/护师/护工）");
            }
            if (!Objects.equals(ward.getDeptId(), nurse.getDeptId())) {
                throw new BusinessException("护士「" + nurse.getNurseName() + "」属于「" + nurse.getDeptName()
                        + "」，不能排进「" + ward.getWardName() + "」——跨单元支援请先调整所属科室");
            }
            StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(src.getScheduleStatus());
            if (status == null) {
                throw new BusinessException("护士「" + nurse.getNurseName() + "」出勤状态取值不合法");
            }
            Long shiftId = null;
            if (status == StaffDutyStatusEnum.WORK) {
                if (src.getShiftId() == null) {
                    throw new BusinessException("护士「" + nurse.getNurseName() + "」排「上班」必须选班次");
                }
                NurseScheduleVO.ShiftOption shift = bizScheduleMapper.selectNursingShift(src.getShiftId());
                if (shift == null) {
                    throw new BusinessException("护士「" + nurse.getNurseName() + "」所选班次不存在、已停用或不是病区护理班次");
                }
                shiftId = shift.getShiftId();
            }
            BizNurseScheduleTemplateItem item = new BizNurseScheduleTemplateItem();
            item.setTemplateId(tpl.getId());
            item.setEmployeeId(nurse.getEmployeeId());
            item.setEmployeeName(nurse.getNurseName());
            item.setWeekDay(src.getWeekDay());
            item.setScheduleStatus(status.getCode());
            item.setShiftId(shiftId);
            item.setRemark(TextUtil.cut(TextUtil.trimToNull(src.getRemark()), 500));
            item.setDelFlag(NOT_DEL);
            itemMapper.insert(item);
            seq++;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BizNurseScheduleTemplate tpl = getById(id);
        if (tpl == null || DEL == tpl.getDelFlag()) {
            throw new BusinessException("模板不存在或已删除");
        }
        if (!deptScopeService.canAccessDept(tpl.getWardId() == null ? null : wardDeptId(tpl.getWardId()))) {
            throw new BusinessException("无权删除该模板（不在当前岗位的数据范围内）");
        }
        tpl.setDelFlag(DEL);
        updateById(tpl);
        itemMapper.update(new BizNurseScheduleTemplateItem(), new LambdaUpdateWrapper<BizNurseScheduleTemplateItem>()
                .eq(BizNurseScheduleTemplateItem::getTemplateId, id)
                .set(BizNurseScheduleTemplateItem::getDelFlag, DEL));
        return true;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        BizNurseScheduleTemplate tpl = getById(id);
        if (tpl == null || DEL == tpl.getDelFlag()) {
            throw new BusinessException("模板不存在或已删除");
        }
        BizNurseScheduleTemplate upd = new BizNurseScheduleTemplate();
        upd.setId(id);
        upd.setStatus(status);
        return updateById(upd);
    }

    // ---------------- 规则（班次实用排班模式） ----------------

    @Override
    public List<NurseScheduleTemplateVO.Rule> listRules() {
        List<BizNurseScheduleTemplateRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<BizNurseScheduleTemplateRule>()
                .eq(BizNurseScheduleTemplateRule::getDelFlag, NOT_DEL)
                .orderByDesc(BizNurseScheduleTemplateRule::getCreateTime));
        return rules.stream().map(r -> {
            NurseScheduleTemplateVO.Rule vo = new NurseScheduleTemplateVO.Rule();
            vo.setId(r.getId());
            vo.setRuleName(r.getRuleName());
            vo.setRuleCode(r.getRuleCode());
            vo.setPatternDetail(r.getPatternDetail());
            vo.setStatus(r.getStatus() == null ? 1 : r.getStatus());
            vo.setRemark(r.getRemark());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertRule(NurseScheduleTemplateDTO.RuleUpsert dto) {
        BizNurseScheduleTemplateRule rule = new BizNurseScheduleTemplateRule();
        rule.setId(dto.getId());
        rule.setRuleName(TextUtil.trimToNull(dto.getRuleName()));
        rule.setRuleCode(TextUtil.trimToNull(dto.getRuleCode()));
        rule.setPatternDetail(TextUtil.trimToNull(dto.getPatternDetail()));
        rule.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        rule.setRemark(TextUtil.cut(TextUtil.trimToNull(dto.getRemark()), 500));
        rule.setDelFlag(NOT_DEL);
        if (dto.getId() == null) {
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
    }

    @Override
    public boolean deleteRule(Long id) {
        BizNurseScheduleTemplateRule rule = new BizNurseScheduleTemplateRule();
        rule.setId(id);
        rule.setDelFlag(DEL);
        return ruleMapper.updateById(rule) > 0;
    }

    // ---------------- 套用模板到周（核心） ----------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseScheduleTemplateVO.Apply applyToWeek(NurseScheduleTemplateDTO.Apply dto) {
        NurseScheduleVO.Ward ward = requireUnit(dto.getUnitType(), dto.getWardId());
        BizNurseScheduleTemplate tpl = getById(dto.getTemplateId());
        if (tpl == null || DEL == tpl.getDelFlag()) {
            throw new BusinessException("模板不存在或已删除");
        }
        if (tpl.getStatus() == null || tpl.getStatus() != 1) {
            throw new BusinessException("模板已停用，请先启用再套用");
        }
        LocalDate today = LocalDate.now();
        LocalDate targetStart = dto.getTargetWeekStart() == null
                ? mondayOf(today).plusWeeks(1) : mondayOf(dto.getTargetWeekStart());
        LocalDate targetEnd = targetStart.plusDays(6);
        if (targetEnd.isBefore(today)) {
            throw new BusinessException("目标周 " + targetStart + " ~ " + targetEnd + " 已全部过去，不能套用");
        }

        List<BizNurseScheduleTemplateItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizNurseScheduleTemplateItem>()
                .eq(BizNurseScheduleTemplateItem::getDelFlag, NOT_DEL)
                .eq(BizNurseScheduleTemplateItem::getTemplateId, tpl.getId())
                .orderByAsc(BizNurseScheduleTemplateItem::getWeekDay)
                .orderByAsc(BizNurseScheduleTemplateItem::getEmployeeId));
        if (items.isEmpty()) {
            throw new BusinessException("模板没有任何明细行，无法套用");
        }

        // 目标周已排格（人|日）集合：已排的不覆盖（与复制上周同口径）
        Set<String> written = new HashSet<>();
        for (NurseScheduleVO.Cell c : bizScheduleMapper.selectCells(coreOrgTypeOf(ward), ward.getWardId(), targetStart, targetEnd)) {
            if (c.getEmployeeId() != null && c.getScheduleDate() != null) {
                written.add(c.getEmployeeId() + "|" + c.getScheduleDate());
            }
        }

        int created = 0, skippedFilled = 0, skippedPast = 0, skippedParity = 0, skippedExpired = 0, skippedInvalid = 0;
        Set<String> generated = new HashSet<>();
        for (BizNurseScheduleTemplateItem item : items) {
            LocalDate date = targetStart.plusDays(item.getWeekDay() - 1L);
            if (date.isBefore(today)) {
                skippedPast++;
                continue;
            }
            if (!inParity(tpl, date)) {
                skippedParity++;
                continue;
            }
            if (!inValidRange(tpl, date)) {
                skippedExpired++;
                continue;
            }
            String key = item.getEmployeeId() + "|" + date;
            if (written.contains(key) || generated.contains(key)) {
                skippedFilled++;
                continue;
            }
            NurseScheduleVO.Nurse nurse = bizScheduleMapper.selectNurse(item.getEmployeeId());
            if (nurse == null || !Objects.equals(ward.getDeptId(), nurse.getDeptId())) {
                skippedInvalid++;
                log.warn("套用模板 {} 跳过护士 {}：不在「{}」在册", tpl.getId(), item.getEmployeeId(), ward.getWardName());
                continue;
            }
            StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(item.getScheduleStatus());
            if (status == null) {
                skippedInvalid++;
                continue;
            }
            Long shiftId = null;
            String remark = null;
            if (status == StaffDutyStatusEnum.WORK && item.getShiftId() != null) {
                NurseScheduleVO.ShiftOption shift = bizScheduleMapper.selectNursingShift(item.getShiftId());
                if (shift != null) {
                    shiftId = shift.getShiftId();
                } else {
                    status = StaffDutyStatusEnum.REST;
                    remark = "套用模板：原班次已停用，按休息落";
                }
            }
            StaffScheduleUpsertDTO core = new StaffScheduleUpsertDTO();
            core.setScheduleDate(date);
            core.setOrgType(coreOrgTypeOf(ward));
            core.setOrgId(ward.getWardId());
            core.setEmployeeId(item.getEmployeeId());
            core.setDutyStatus(status.getCode());
            core.setShiftId(shiftId);
            core.setRemark(remark);
            staffScheduleService.replaceDayAttendance(core, StaffScheduleSourceEnum.TEMPLATE);
            generated.add(key);
            created++;
        }

        int skipped = skippedFilled + skippedPast + skippedParity + skippedExpired + skippedInvalid;
        String message = "套用模板《" + tpl.getName() + "》到 " + targetStart + " ~ " + targetEnd
                + " 完成：新增 " + created + " 格";
        if (skipped > 0) {
            message += "，跳过 " + skipped + " 格（已排 " + skippedFilled + " / 日期已过 " + skippedPast
                    + " / 单双周不匹配 " + skippedParity + " / 不在生效期 " + skippedExpired + " / 班次失效或跨单元 " + skippedInvalid + "）";
        }

        BizNurseScheduleTemplateApply apply = new BizNurseScheduleTemplateApply();
        apply.setTemplateId(tpl.getId());
        apply.setTemplateName(tpl.getName());
        apply.setUnitType(resolveUnitType(dto.getUnitType()));
        apply.setWardId(ward.getWardId());
        apply.setWardName(ward.getWardName());
        apply.setApplyWeekStart(targetStart);
        apply.setApplyWeekEnd(targetEnd);
        CurrentUser me = UserUtils.getCurrentUser();
        apply.setAppliedById(me == null ? null : me.getUserId());
        apply.setAppliedByName(me == null ? null : me.getRealName());
        apply.setGeneratedCount(created);
        apply.setSkippedCount(skipped);
        apply.setStatus(created > 0 ? 0 : 1);
        apply.setRemark(TextUtil.cut(TextUtil.trimToNull(message), 500));
        applyMapper.insert(apply);

        BizSchedule snap = new BizSchedule();
        snap.setOrgName(ward.getWardName());
        snap.setDeptName(ward.getDeptName());
        nurseScheduleLogService.record(null, NurseScheduleChangeTypeEnum.APPLY_TEMPLATE, created, message, snap, null, null);

        NurseScheduleTemplateVO.Apply applyVO = new NurseScheduleTemplateVO.Apply();
        applyVO.setId(apply.getId());
        applyVO.setTemplateId(apply.getTemplateId());
        applyVO.setTemplateName(apply.getTemplateName());
        applyVO.setUnitType(apply.getUnitType());
        applyVO.setWardId(apply.getWardId());
        applyVO.setWardName(apply.getWardName());
        applyVO.setApplyWeekStart(apply.getApplyWeekStart());
        applyVO.setApplyWeekEnd(apply.getApplyWeekEnd());
        applyVO.setAppliedByName(apply.getAppliedByName());
        applyVO.setAppliedAt(apply.getCreateTime());
        applyVO.setGeneratedCount(apply.getGeneratedCount());
        applyVO.setSkippedCount(apply.getSkippedCount());
        applyVO.setStatus(apply.getStatus());
        applyVO.setRemark(apply.getRemark());
        return applyVO;
    }

    @Override
    public PageResult<NurseScheduleTemplateVO.Apply> pageApplies(NurseScheduleTemplateDTO.ApplyPageQuery dto) {
        LambdaQueryWrapper<BizNurseScheduleTemplateApply> wrapper = new LambdaQueryWrapper<BizNurseScheduleTemplateApply>()
                .eq(dto.getTemplateId() != null, BizNurseScheduleTemplateApply::getTemplateId, dto.getTemplateId())
                .eq(dto.getUnitType() != null, BizNurseScheduleTemplateApply::getUnitType, dto.getUnitType())
                .eq(dto.getWardId() != null, BizNurseScheduleTemplateApply::getWardId, dto.getWardId())
                .orderByDesc(BizNurseScheduleTemplateApply::getCreateTime);
        Page<BizNurseScheduleTemplateApply> page = applyMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<NurseScheduleTemplateVO.Apply> records = page.getRecords().stream().map(a -> {
            NurseScheduleTemplateVO.Apply vo = new NurseScheduleTemplateVO.Apply();
            vo.setId(a.getId());
            vo.setTemplateId(a.getTemplateId());
            vo.setTemplateName(a.getTemplateName());
            vo.setUnitType(a.getUnitType());
            vo.setWardId(a.getWardId());
            vo.setWardName(a.getWardName());
            vo.setApplyWeekStart(a.getApplyWeekStart());
            vo.setApplyWeekEnd(a.getApplyWeekEnd());
            vo.setAppliedByName(a.getAppliedByName());
            vo.setAppliedAt(a.getCreateTime());
            vo.setGeneratedCount(a.getGeneratedCount());
            vo.setSkippedCount(a.getSkippedCount());
            vo.setStatus(a.getStatus());
            vo.setRemark(a.getRemark());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // ---------------- 私有 helper（复刻 BizScheduleServiceImpl 的同名逻辑） ----------------

    private NurseScheduleVO.Ward requireUnit(Integer unitType, Long unitId) {
        int type = resolveUnitType(unitType);
        if (unitId == null) {
            throw new BusinessException("请选择排班单元");
        }
        if (!NurseScheduleUnitTypeEnum.isValid(type)) {
            throw new BusinessException("排班单元类型只允许 1-病区 / 2-门诊科室");
        }
        NurseScheduleVO.Ward unit = bizScheduleMapper.selectUnit(type, unitId);
        if (unit == null) {
            throw new BusinessException(type == NurseScheduleUnitTypeEnum.WARD.getCode() ? "病区不存在或已停用" : "门诊科室不存在或已停用");
        }
        if (!deptScopeService.canAccessDept(unit.getDeptId())) {
            throw new BusinessException("无权操作「" + unit.getWardName() + "」的排班（不在当前岗位的数据范围内）");
        }
        return unit;
    }

    /** 单元类型收口：空/0 → 病区(1) */
    private static int resolveUnitType(Integer unitType) {
        return unitType == null || unitType == 0 ? NurseScheduleUnitTypeEnum.WARD.getCode() : unitType;
    }

    /** 护理单元在出勤底座落地的单元类型：病区→2，门诊科室→1（与 NurseScheduleUnitTypeEnum 反序） */
    private static int coreOrgTypeOf(NurseScheduleVO.Ward ward) {
        return resolveUnitType(ward.getUnitType()) == NurseScheduleUnitTypeEnum.CLINIC.getCode()
                ? OrgUnitTypeEnum.DEPT.getCode() : OrgUnitTypeEnum.WARD.getCode();
    }

    /** 周一为周首 */
    private static LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    /** 单双周匹配（0/null=每周；1=单周 ISO 奇数；2=双周 偶数） */
    private static boolean inParity(BizNurseScheduleTemplate tpl, LocalDate date) {
        Integer parity = tpl.getWeekParity();
        if (parity == null || parity == 0) {
            return true;
        }
        int weekNo = date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return parity == 1 ? weekNo % 2 == 1 : weekNo % 2 == 0;
    }

    /** 生效日期范围（valid_from/valid_until 任一为空视为不限） */
    private static boolean inValidRange(BizNurseScheduleTemplate tpl, LocalDate date) {
        if (tpl.getValidFrom() != null && date.isBefore(tpl.getValidFrom())) {
            return false;
        }
        return tpl.getValidUntil() == null || !date.isAfter(tpl.getValidUntil());
    }

    private boolean existsApply(Long templateId) {
        return applyMapper.selectCount(new LambdaQueryWrapper<BizNurseScheduleTemplateApply>()
                .eq(BizNurseScheduleTemplateApply::getTemplateId, templateId)) > 0;
    }

    /** 取模板所属病区的科室ID（删除鉴权用） */
    private Long wardDeptId(Long wardId) {
        NurseScheduleVO.Ward w = bizScheduleMapper.selectWard(wardId);
        return w == null ? null : w.getDeptId();
    }

    private NurseScheduleTemplateVO.Template toTemplateVO(BizNurseScheduleTemplate t, Integer itemCount, Boolean applied) {
        NurseScheduleTemplateVO.Template vo = new NurseScheduleTemplateVO.Template();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setUnitType(t.getUnitType());
        vo.setWardId(t.getWardId());
        vo.setWardName(t.getWardName());
        vo.setWeekParity(t.getWeekParity());
        vo.setValidFrom(t.getValidFrom());
        vo.setValidUntil(t.getValidUntil());
        vo.setScope(t.getScope());
        vo.setStatus(t.getStatus() == null ? 1 : t.getStatus());
        vo.setRemark(t.getRemark());
        vo.setCreatorName(t.getCreateBy());
        vo.setCreateTime(t.getCreateTime());
        vo.setItemCount(itemCount);
        vo.setApplied(applied);
        return vo;
    }

    private NurseScheduleTemplateVO.Item toItemVO(BizNurseScheduleTemplateItem it) {
        NurseScheduleTemplateVO.Item vo = new NurseScheduleTemplateVO.Item();
        vo.setId(it.getId());
        vo.setTemplateId(it.getTemplateId());
        vo.setEmployeeId(it.getEmployeeId());
        vo.setEmployeeName(it.getEmployeeName());
        vo.setWeekDay(it.getWeekDay());
        vo.setScheduleStatus(it.getScheduleStatus());
        vo.setShiftId(it.getShiftId());
        if (it.getShiftId() != null) {
            NurseScheduleVO.ShiftOption s = bizScheduleMapper.selectNursingShift(it.getShiftId());
            if (s != null) {
                vo.setShiftName(s.getShiftName());
            }
        }
        vo.setRemark(it.getRemark());
        return vo;
    }

    private Long currentUserId() {
        CurrentUser u = UserUtils.getCurrentUser();
        return u == null ? null : u.getUserId();
    }
}
