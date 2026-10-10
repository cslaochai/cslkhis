package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.support.FeeCatalogResolver;
import com.his.common.base.PageResult;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.BillingStatusEnum;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.TreatmentDTO;
import com.his.emr.entity.BizTreatmentApply;
import com.his.emr.entity.BizTreatmentRecord;
import com.his.emr.enums.TreatmentExecStatusEnum;
import com.his.emr.enums.TreatmentRecordStatusEnum;
import com.his.emr.mapper.BizTreatmentApplyMapper;
import com.his.emr.mapper.BizTreatmentRecordMapper;
import com.his.emr.mapper.SysTreatmentItemMapper;
import com.his.emr.service.TreatmentService;
import com.his.emr.support.TreatmentChargeInvoker;
import com.his.emr.vo.RegistSnapshotVO;
import com.his.emr.vo.TreatmentItemSnapshotVO;
import com.his.emr.vo.TreatmentVO;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 门诊治疗站（G19）：治疗申请（疗程）→ 排期 → 按次打卡 → 按次计费。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentServiceImpl extends ServiceImpl<BizTreatmentRecordMapper, BizTreatmentRecord> implements TreatmentService {

    private static final int AMOUNT_SCALE = 2;
    private static final String UNIT_TIMES = "次";
    private static final int BACK_DAYS = 31;
    private static final int AHEAD_DAYS = 365;


    private final BizTreatmentApplyMapper bizTreatmentApplyMapper;

    private final BizTreatmentRecordMapper bizTreatmentRecordMapper;

    private final SysTreatmentItemMapper sysTreatmentItemMapper;

    private final RedisSequenceService redisSequenceService;

    private final DictCacheService dictCacheService;

    private final TreatmentChargeInvoker chargeInvoker;

    private final DeptScopeService deptScopeService;

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        return o instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(o));
    }

    private static Integer toInt(Object o) {
        if (o == null) {
            return null;
        }
        return o instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(o));
    }

    // 写入

    private static BigDecimal toDecimal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof BigDecimal b) {
            return b;
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        String s = String.valueOf(o);
        return TextUtil.hasText(s) ? new BigDecimal(s) : null;
    }

    public PageResult<TreatmentVO.ApplyVO> listPageApplies(TreatmentDTO.ApplyQuery q) {
        // 治疗申请按开单科室收口；执行流水（无科室列）在 execPage 里经申请单收口
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        LambdaQueryWrapper<BizTreatmentApply> w = new LambdaQueryWrapper<>();
        String kw = TextUtil.trim(q.getKeyword());
        w.and(TextUtil.hasText(kw), x -> x.like(BizTreatmentApply::getPatientName, kw)
                        .or().like(BizTreatmentApply::getPatientNo, kw)
                        .or().like(BizTreatmentApply::getApplyNo, kw)
                        .or().like(BizTreatmentApply::getItemName, kw))
                .eq(q.getPatientId() != null, BizTreatmentApply::getPatientId, q.getPatientId())
                .eq(q.getRegistId() != null, BizTreatmentApply::getRegistId, q.getRegistId())
                .in(scope != null, BizTreatmentApply::getDeptId, scope)
                .eq(q.getApplyStatus() != null, BizTreatmentApply::getApplyStatus, q.getApplyStatus())
                .eq(q.getTreatmentItemId() != null, BizTreatmentApply::getTreatmentItemId, q.getTreatmentItemId())
                .ge(q.getStartDate() != null, BizTreatmentApply::getStartDate, q.getStartDate())
                .le(q.getEndDate() != null, BizTreatmentApply::getStartDate, q.getEndDate())
                .orderByDesc(BizTreatmentApply::getApplyId);
        Page<BizTreatmentApply> page = bizTreatmentApplyMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<TreatmentVO.ApplyVO> records = new ArrayList<>();
        for (BizTreatmentApply a : page.getRecords()) {
            records.add(toApplyVo(a));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    public TreatmentVO.ApplyDetailVO getDetail(Long applyId) {
        BizTreatmentApply apply = requireApply(applyId);
        TreatmentVO.ApplyDetailVO detail = new TreatmentVO.ApplyDetailVO();
        detail.setApply(toApplyVo(apply));
        List<BizTreatmentRecord> rows = bizTreatmentRecordMapper.selectList(new LambdaQueryWrapper<BizTreatmentRecord>()
                .eq(BizTreatmentRecord::getApplyId, applyId)
                .orderByAsc(BizTreatmentRecord::getExecSeq));
        Map<Long, BizTreatmentApply> one = new HashMap<>();
        one.put(applyId, apply);
        detail.setExecList(toExecVos(rows, one));
        return detail;
    }

    /**
     * 按次流水分页（治疗台与台账共用一套过滤口径）
     */
    public PageResult<TreatmentVO.ExecVO> listPageExecs(TreatmentDTO.ExecQuery q) {
        Page<BizTreatmentRecord> page = execPage(q, q.getPageNum(), q.getPageSize());
        Map<Long, BizTreatmentApply> applies = applyMap(page.getRecords());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toExecVos(page.getRecords(), applies));
    }

    /**
     * 流水状态分布（与分页同一套过滤条件，但<b>不带上被统计的那一维</b>）。
     *
     * <p>否则页面按"已执行"过滤之后，"待执行"那一格必然显示 0，
     * 用户看到的是"没有待做的了"，而实际是"你正在只看已做的"。
     */
    public List<TreatmentVO.StatusCountVO> statusCount(TreatmentDTO.ExecQuery q) {
        List<TreatmentVO.StatusCountVO> out = new ArrayList<>();
        TreatmentDTO.ExecQuery ex = copy(q);
        ex.setExecStatus(null);
        for (int s = 0; s <= 2; s++) {
            TreatmentDTO.ExecQuery one = copy(ex);
            one.setExecStatus(s);
            out.add(countVo("exec-" + s, dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_EXEC_STATUS, s), execPage(one, 1, 1).getTotal()));
        }
        TreatmentDTO.ExecQuery ch = copy(q);
        ch.setChargeStatus(null);
        for (int s = 0; s <= 3; s++) {
            TreatmentDTO.ExecQuery one = copy(ch);
            one.setChargeStatus(s);
            out.add(countVo("charge-" + s, dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_CHARGE_STATUS, s), execPage(one, 1, 1).getTotal()));
        }
        return out;
    }

    public TreatmentVO.StatsVO stats() {
        LocalDate today = LocalDate.now();
        TreatmentVO.StatsVO v = new TreatmentVO.StatsVO();
        v.setRunningApplies(bizTreatmentApplyMapper.selectCount(new LambdaQueryWrapper<BizTreatmentApply>()
                .eq(BizTreatmentApply::getApplyStatus, TreatmentExecStatusEnum.PENDING.getCode())));
        Page<BizTreatmentRecord> todayPlan = execPage(queryOf(null, today, null), 1, 1);
        v.setTodayPlan(todayPlan.getTotal());
        v.setTodayDone(execPage(queryOf(TreatmentExecStatusEnum.DONE.getCode(), today, null), 1, 1).getTotal());
        v.setTodayPending(execPage(queryOf(TreatmentExecStatusEnum.PENDING.getCode(), today, null), 1, 1).getTotal());
        TreatmentDTO.ExecQuery overdue = queryOf(TreatmentExecStatusEnum.PENDING.getCode(), null, null);
        overdue.setOverdueOnly(true);
        v.setOverduePending(execPage(overdue, 1, 1).getTotal());
        TreatmentDTO.ExecQuery unbilled = queryOf(TreatmentExecStatusEnum.DONE.getCode(), null, null);
        unbilled.setUnbilledOnly(true);
        v.setUnbilled(execPage(unbilled, 1, 1).getTotal());
        v.setChargeFail(execPage(queryOf(null, null, BillingStatusEnum.FAILED.getCode()), 1, 1).getTotal());
        v.setTodayAmount(sumTodayChargeAmount(today));
        return v;
    }

    // 计费

    /**
     * 治疗项目候选（开单时选项目用，带单价与能解析出来的执行科室）
     */
    public List<TreatmentVO.ItemSelectListVO> itemSelectList(String keyword, Integer limit) {
        List<TreatmentVO.ItemSelectListVO> out = new ArrayList<>();
        for (TreatmentItemSnapshotVO row : sysTreatmentItemMapper.selectOptions(TextUtil.trim(keyword), Math.min(NumUtil.orDefault(limit, 50), 200))) {
            TreatmentVO.ItemSelectListVO v = new TreatmentVO.ItemSelectListVO();
            v.setItemId(row.getItemId());
            v.setItemCode(row.getItemCode());
            v.setItemName(row.getItemName());
            v.setItemType(row.getItemType());
            v.setItemTypeText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_ITEM_TYPE, v.getItemType()));
            v.setPrice(row.getPrice());
            v.setDuration(row.getDuration());
            v.setUsageMethod(row.getUsageMethod());
            v.setExecDeptId(row.getExecDeptId());
            v.setExecDeptName(row.getExecDeptName());
            out.add(v);
        }
        return out;
    }

    /**
     * 开单 / 改疗程。
     *
     * <p>改疗程只在「一次卡都没打过」时允许（次数、间隔、开始日、项目都可以重排）；
     * 已经开始的疗程只能对未执行的某一次单独改期 —— 已经做掉的那几次是既成事实。
     */
    @Transactional(rollbackFor = Exception.class)
    public TreatmentVO.ApplyDetailVO upsertApply(TreatmentDTO.ApplyUpsert dto) {
        LocalDate start = dto.getStartDate();
        checkDateWindow(start, "疗程开始日期");
        int totalTimes = NumUtil.orDefault(dto.getTotalTimes(), 1);
        if (totalTimes < 1 || totalTimes > 60) {
            throw new BusinessException("疗程总次数应在 1~60 之间，当前：" + totalTimes);
        }
        int interval = NumUtil.orDefault(dto.getIntervalDays(), 1);
        if (interval < 1 || interval > 30) {
            throw new BusinessException("间隔天数应在 1~30 之间，当前：" + interval);
        }

        RegistSnapshotVO regist = bizTreatmentApplyMapper.selectRegistSnapshot(dto.getRegistId());
        if (regist == null || regist.getRegistId() == null) {
            throw new BusinessException("挂号记录不存在：" + dto.getRegistId());
        }
        if (regist.getRefundTime() != null || Integer.valueOf(5).equals(regist.getRegistStatus())) {
            throw new BusinessException("该挂号已退号/已取消，不能在已作废的就诊上开治疗");
        }
        // 开单科室取挂号科室：受限岗位只能给本科室就诊开治疗
        deptScopeService.assertDeptAccessible(regist.getDeptId());
        TreatmentItemSnapshotVO item = sysTreatmentItemMapper.selectApplySnapshot(dto.getTreatmentItemId());
        if (item == null || item.getItemId() == null) {
            throw new BusinessException("治疗项目不存在或已删除：" + dto.getTreatmentItemId());
        }
        if (Integer.valueOf(0).equals(item.getStatus())) {
            throw new BusinessException("治疗项目「" + item.getItemName() + "」已停用，请改用其他项目");
        }
        BigDecimal price = item.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("治疗项目「" + item.getItemName()
                    + "」未在价表定价，不能开单（无价疗程每次打卡都会计费失败，请先补价）");
        }

        Long applyId = dto.getApplyId();
        BizTreatmentApply apply;
        if (applyId == null) {
            apply = new BizTreatmentApply();
            apply.setApplyNo(redisSequenceService.generateTreatmentApplyNo());
            apply.setApplyTime(TimeUtil.nowSeconds());
            apply.setApplyStatus(TreatmentExecStatusEnum.PENDING.getCode());
            apply.setDoneTimes(0);
        } else {
            apply = requireApply(applyId);
            if (Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(apply.getApplyStatus())) {
                throw new BusinessException("疗程已取消，不能重新排期");
            }
            if (NumUtil.orDefault(apply.getDoneTimes(), 0) > 0) {
                throw new BusinessException("疗程已完成 " + apply.getDoneTimes() + " 次，不能再整体改次数/改期，"
                        + "请对未执行的那几次单独改期");
            }
            // 一次没打过：待执行流水全部作废重建（这些行不可能有收费痕迹，物理删不留脏记录）
            long billed = bizTreatmentRecordMapper.selectCount(new LambdaQueryWrapper<BizTreatmentRecord>()
                    .eq(BizTreatmentRecord::getApplyId, applyId)
                    .in(BizTreatmentRecord::getChargeStatus, BillingStatusEnum.BILLED.getCode(), BillingStatusEnum.FAILED.getCode()));
            if (billed > 0) {
                throw new BusinessException("该疗程已有 " + billed + " 次产生过计费痕迹，不能整体重排");
            }
            bizTreatmentRecordMapper.delete(new LambdaQueryWrapper<BizTreatmentRecord>()
                    .eq(BizTreatmentRecord::getApplyId, applyId));
        }

        apply.setRegistId(regist.getRegistId());
        apply.setRegistNo(regist.getRegistNo());
        apply.setPatientId(regist.getPatientId());
        apply.setPatientNo(regist.getPatientNo());
        apply.setPatientName(regist.getPatientName());
        apply.setDeptId(regist.getDeptId());
        apply.setDeptName(regist.getDeptName());
        // 开单人优先取登录态：入参能传"医生"就等于谁都能替别人开单
        Long loginEmployee = UserUtils.getCurrentUser().getEmployeeId();
        apply.setDoctorId(loginEmployee != null ? loginEmployee : regist.getDoctorId());
        String loginName = UserUtils.getCurrentUser().getRealName();
        apply.setDoctorName(TextUtil.hasText(loginName) ? loginName : regist.getDoctorName());
        apply.setTreatmentItemId(item.getItemId());
        apply.setItemCode(item.getItemCode());
        apply.setItemName(item.getItemName());
        apply.setItemType(item.getItemType());
        apply.setExecDeptId(item.getExecDeptId());
        apply.setExecDeptName(item.getExecDeptName());
        apply.setPrice(price.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        apply.setTotalTimes(totalTimes);
        apply.setIntervalDays(interval);
        apply.setStartDate(start);
        if (apply.getDoneTimes() == null) {
            apply.setDoneTimes(0);
        }
        apply.setRemark(TextUtil.cut(dto.getRemark(), 500));

        if (applyId == null) {
            bizTreatmentApplyMapper.insert(apply);
        } else {
            bizTreatmentApplyMapper.updateById(apply);
        }
        buildSchedule(apply);
        log.info("门诊治疗开单成功 applyNo={} 项目={} 共 {} 次 开始 {} 单价 {}",
                apply.getApplyNo(), apply.getItemName(), totalTimes, start, apply.getPrice());
        return getDetail(apply.getApplyId());
    }

    // 排期与状态

    /**
     * 改期（单条待执行流水挪到别的日期）
     */
    @Transactional(rollbackFor = Exception.class)
    public TreatmentVO.ExecVO rescheduleExec(TreatmentDTO.ExecReschedule dto) {
        BizTreatmentRecord exec = requireExec(dto.getRecordId());
        BizTreatmentApply apply = requireApply(exec.getApplyId());
        if (!Integer.valueOf(TreatmentExecStatusEnum.PENDING.getCode()).equals(exec.getExecStatus())) {
            throw new BusinessException("第 " + exec.getExecSeq() + " 次已" + execStatusText(exec.getExecStatus()) + "，不能改期");
        }
        if (Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(apply.getApplyStatus())) {
            throw new BusinessException("疗程已取消，不能再改期");
        }
        checkDateWindow(dto.getPlanDate(), "计划执行日期");
        LocalDate old = exec.getPlanDate();
        exec.setPlanDate(dto.getPlanDate());
        exec.setRemark(TextUtil.cut("改期 " + (old == null ? "—" : old) + " → " + dto.getPlanDate()
                + "：" + TextUtil.trim(dto.getReason()), 500));
        bizTreatmentRecordMapper.updateById(exec);
        return toExecVo(exec, apply);
    }

    /**
     * 打卡：把这一行从「待执行」推到「已执行」，随后按次计费。
     *
     * <p>计费失败<b>不</b>回滚打卡（见类注释第 2 条）：计费经 {@link TreatmentChargeInvoker} 走
     * REQUIRES_NEW 独立事务，异常在这一层被挡住并留痕；同事务直调会让记账把整个事务标成
     * rollback-only，try-catch 也救不回来（提交时抛 UnexpectedRollbackException，打卡一起丢）。
     */
    @Transactional(rollbackFor = Exception.class)
    public TreatmentVO.ExecVO executeExec(TreatmentDTO.ExecExecute dto) {
        BizTreatmentRecord exec = requireExec(dto.getRecordId());
        BizTreatmentApply apply = requireApply(exec.getApplyId());
        String blocked = blockReason(exec, apply);
        if (blocked != null) {
            throw new BusinessException(blocked);
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        exec.setExecStatus(TreatmentExecStatusEnum.DONE.getCode());
        exec.setExecuteTime(now);
        exec.setRecordStatus(Integer.valueOf(0).equals(dto.getRecordStatus()) ? 0 : 1);
        exec.setResult(TextUtil.cut(TextUtil.hasText(dto.getResult()) ? dto.getResult()
                : ("第 " + exec.getExecSeq() + " 次治疗完成，过程顺利"), 1000));
        exec.setRemark(TextUtil.cut(dto.getRemark(), 500));
        Long employeeId = UserUtils.getCurrentUser().getEmployeeId();
        exec.setNurseId(employeeId);
        exec.setExecutorName(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), 50));
        bizTreatmentRecordMapper.updateById(exec);

        String chargeNote = billOnce(exec, apply);
        refreshApplyProgress(apply, now);

        TreatmentVO.ExecVO vo = toExecVo(exec, apply);
        vo.setChargeNotice(chargeNote);
        return vo;
    }

    /**
     * 补记：只针对「已执行但没记上钱」的行
     */
    @Transactional(rollbackFor = Exception.class)
    public TreatmentVO.ExecVO retryCharge(Long recordId) {
        BizTreatmentRecord exec = requireExec(recordId);
        BizTreatmentApply apply = requireApply(exec.getApplyId());
        if (!Integer.valueOf(TreatmentExecStatusEnum.DONE.getCode()).equals(exec.getExecStatus())) {
            throw new BusinessException("第 " + exec.getExecSeq() + " 次还没打卡，没有可补记的账");
        }
        if (Integer.valueOf(BillingStatusEnum.BILLED.getCode()).equals(exec.getChargeStatus())) {
            throw new BusinessException("第 " + exec.getExecSeq() + " 次已记账（记账单号 " + exec.getFeeNo() + "），不能重复补记");
        }
        String note = billOnce(exec, apply);
        TreatmentVO.ExecVO vo = toExecVo(exec, apply);
        vo.setChargeNotice(note);
        return vo;
    }

    // 组装 VO

    /**
     * 取消疗程：未执行的次数一并取消；已执行且已计费的行不退费（退费走收费窗口）
     */
    @Transactional(rollbackFor = Exception.class)
    public int cancelApply(TreatmentDTO.ApplyCancel dto) {
        BizTreatmentApply apply = requireApply(dto.getApplyId());
        if (Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(apply.getApplyStatus())) {
            throw new BusinessException("疗程已是取消状态，无需重复取消");
        }
        String reason = TextUtil.cut(TextUtil.trim(dto.getReason()), 500);
        List<BizTreatmentRecord> pending = bizTreatmentRecordMapper.selectList(new LambdaQueryWrapper<BizTreatmentRecord>()
                .eq(BizTreatmentRecord::getApplyId, apply.getApplyId())
                .eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.PENDING.getCode()));
        for (BizTreatmentRecord r : pending) {
            r.setExecStatus(TreatmentExecStatusEnum.CANCELLED.getCode());
            r.setRemark(TextUtil.cut("疗程取消：" + reason, 500));
            bizTreatmentRecordMapper.updateById(r);
        }
        apply.setApplyStatus(TreatmentExecStatusEnum.CANCELLED.getCode());
        apply.setRemark(TextUtil.cut((apply.getRemark() == null ? "" : apply.getRemark() + " | ") + "取消：" + reason, 500));
        bizTreatmentApplyMapper.updateById(apply);
        log.info("门诊治疗疗程已取消 applyNo={} 连带取消未执行 {} 次", apply.getApplyNo(), pending.size());
        return pending.size();
    }

    /**
     * 删除申请单：只有"一次都没执行过"的疗程可以删
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApply(Long applyId) {
        BizTreatmentApply apply = requireApply(applyId);
        long done = bizTreatmentRecordMapper.selectCount(new LambdaQueryWrapper<BizTreatmentRecord>()
                .eq(BizTreatmentRecord::getApplyId, applyId)
                .eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.DONE.getCode()));
        if (done > 0) {
            throw new BusinessException("该疗程已执行 " + done + " 次，执行流水是收费凭据的来源，不能删除（只能取消未执行的部分）");
        }
        bizTreatmentRecordMapper.delete(new LambdaQueryWrapper<BizTreatmentRecord>().eq(BizTreatmentRecord::getApplyId, applyId));
        bizTreatmentApplyMapper.deleteById(applyId);
    }

    /**
     * 一次打卡计费一次，把结果写回流水行。
     *
     * @return 给人看的一句话（成功/失败/未计费原因），页面当场提示
     */
    private String billOnce(BizTreatmentRecord exec, BizTreatmentApply apply) {
        BigDecimal price = apply.getPrice();
        if (price == null) {
            markCharge(exec, BillingStatusEnum.FAILED.getCode(), null, null, null, "开单时未取到单价快照，未计费");
            return "已打卡，但项目没有单价快照：本次未计费（请补价后点「补记」）";
        }
        BigDecimal amount = price.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(apply.getPatientId());
        dto.setPatientNo(apply.getPatientNo());
        dto.setPatientName(apply.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        dto.setEncounterId(apply.getRegistId());
        dto.setEncounterNo(apply.getRegistNo());
        // ★ 科室用开单快照：按项目类型反查只会去问住院医嘱，门诊治疗单号在那张表里恒查不到
        dto.setDeptId(apply.getDeptId());
        dto.setDeptName(apply.getDeptName());
        dto.setItemType(PaymentItemTypeEnum.TREATMENT.getCode());
        dto.setItemCode(apply.getItemCode());
        dto.setItemName(apply.getItemName());
        dto.setUnit(UNIT_TIMES);
        dto.setQuantity(BigDecimal.ONE);
        dto.setPrice(price);
        dto.setSourceType(FeeSourceTypeEnum.TREATMENT_APPLY.getCode());
        // 幂等锚点 = 执行流水行ID（一次打卡一行）；用申请单ID会把第 2 次以后的真账抹掉
        dto.setSourceId(exec.getRecordId());
        dto.setSourceNo(TextUtil.hasText(apply.getApplyNo())
                ? apply.getApplyNo() : ("EXEC-" + exec.getRecordId()));
        dto.setCatalogType(FeeCatalogResolver.byItemType(dto.getItemType()));
        dto.setRemark("门诊治疗按次计费（" + apply.getItemName() + " 第 " + exec.getExecSeq() + " 次）");
        try {
            BizFeeRecord booked = chargeInvoker.book(dto);
            if (booked == null) {
                markCharge(exec, BillingStatusEnum.FAILED.getCode(), null, null, null, "记账入参缺定位信息（挂号/患者快照），本次未计费");
                return "已打卡，但记账入参不全：本次未计费（见流水上的失败原因）";
            }
            markCharge(exec, BillingStatusEnum.BILLED.getCode(), booked.getFeeNo(), booked.getId(), amount, null);
            return "已打卡并计费 " + amount + " 元（记账行 " + booked.getFeeNo() + "）";
        } catch (Exception e) {
            // 记账失败不能否认"这次治疗做了"：留痕 + 给人补记的机会，而不是把打卡一起回滚
            log.error("门诊治疗按次计费失败 recordId={} applyNo={}", exec.getRecordId(), apply.getApplyNo(), e);
            markCharge(exec, BillingStatusEnum.FAILED.getCode(), null, null, null, "计费异常：" + e.getMessage());
            return "已打卡，但记账失败：" + TextUtil.cut(e.getMessage(), 200) + "（可点「补记」重试）";
        }
    }

    // 内部工具

    private void markCharge(BizTreatmentRecord exec, int status, String feeNo, Long feeId,
                            BigDecimal amount, String reason) {
        boolean done = status == BillingStatusEnum.BILLED.getCode();
        exec.setChargeStatus(status);
        // fee_no / fee_record_id：存的就是 L1 记账行的 (fee_no, fee_id)：
        // 打卡与记账行一一对应，补记的幂等就靠这对指针 + source_id 唯一键。
        exec.setFeeNo(feeNo);
        exec.setFeeRecordId(feeId);
        exec.setChargeAmount(amount);
        // MP 默认 NOT_NULL 更新策略：null 不会写库，所以"清掉失败原因"必须传空串，
        // 否则补记成功后页面上还挂着上一次的失败原因（看起来像又失败了一次）。
        exec.setChargeFailReason(done ? "" : TextUtil.cut(reason, BizTreatmentRecord.REASON_MAX));
        exec.setChargeTime(done ? TimeUtil.nowSeconds() : null);
        bizTreatmentRecordMapper.updateById(exec);
    }

    private void buildSchedule(BizTreatmentApply apply) {
        LocalDate start = apply.getStartDate() == null ? LocalDate.now() : apply.getStartDate();
        int interval = NumUtil.orDefault(apply.getIntervalDays(), 1);
        int total = NumUtil.orDefault(apply.getTotalTimes(), 1);
        for (int seq = 1; seq <= total; seq++) {
            BizTreatmentRecord r = new BizTreatmentRecord();
            r.setRecordNo(apply.getApplyNo() + "-" + String.format("%02d", seq));
            r.setApplyId(apply.getApplyId());
            r.setTreatmentItemId(apply.getTreatmentItemId());
            r.setExecSeq(seq);
            r.setPlanDate(start.plusDays((long) (seq - 1) * interval));
            r.setExecStatus(TreatmentExecStatusEnum.PENDING.getCode());
            r.setChargeStatus(BillingStatusEnum.UNBILLED.getCode());
            r.setRecordStatus(TreatmentRecordStatusEnum.NORMAL.getCode());
            bizTreatmentRecordMapper.insert(r);
        }
    }

    private void refreshApplyProgress(BizTreatmentApply apply, LocalDateTime execTime) {
        long done = bizTreatmentRecordMapper.selectCount(new LambdaQueryWrapper<BizTreatmentRecord>()
                .eq(BizTreatmentRecord::getApplyId, apply.getApplyId())
                .eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.DONE.getCode()));
        apply.setDoneTimes((int) done);
        apply.setExecuteTime(execTime);
        if (!Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(apply.getApplyStatus())) {
            apply.setApplyStatus(done > 0 ? TreatmentExecStatusEnum.DONE.getCode() : TreatmentExecStatusEnum.PENDING.getCode());
        }
        bizTreatmentApplyMapper.updateById(apply);
    }

    /**
     * 「这一行现在能不能打卡」的唯一口径：页面按钮显隐与后端写校验共用它。
     *
     * @return {@code null} 表示可以打卡；否则是不能打卡的原因
     */
    private String blockReason(BizTreatmentRecord exec, BizTreatmentApply apply) {
        if (Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(apply.getApplyStatus())) {
            return "疗程已取消，不能再打卡";
        }
        if (Integer.valueOf(TreatmentExecStatusEnum.DONE.getCode()).equals(exec.getExecStatus())) {
            return "第 " + exec.getExecSeq() + " 次已于 " + exec.getExecuteTime() + " 打卡，不能重复";
        }
        if (Integer.valueOf(TreatmentExecStatusEnum.CANCELLED.getCode()).equals(exec.getExecStatus())) {
            return "第 " + exec.getExecSeq() + " 次已取消，不能打卡";
        }
        LocalDate plan = exec.getPlanDate();
        if (plan == null) {
            return "第 " + exec.getExecSeq() + " 次没有计划执行日期，请先改期排定日期";
        }
        LocalDate today = LocalDate.now();
        if (plan.isAfter(today)) {
            return "未到执行日期（计划 " + plan + "），不能提前打卡";
        }
        BizTreatmentRecord earlier = bizTreatmentRecordMapper.selectOne(new LambdaQueryWrapper<BizTreatmentRecord>()
                .eq(BizTreatmentRecord::getApplyId, exec.getApplyId())
                .eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.PENDING.getCode())
                .lt(BizTreatmentRecord::getExecSeq, exec.getExecSeq())
                .orderByAsc(BizTreatmentRecord::getExecSeq)
                .last("LIMIT 1"));
        if (earlier != null) {
            return "请先完成第 " + earlier.getExecSeq() + " 次（疗程必须按次序执行）";
        }
        return null;
    }

    private TreatmentVO.ApplyVO toApplyVo(BizTreatmentApply a) {
        TreatmentVO.ApplyVO v = new TreatmentVO.ApplyVO();
        BeanUtils.copyProperties(a, v);
        v.setItemTypeText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_ITEM_TYPE, a.getItemType()));
        v.setApplyStatusText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_APPLY_STATUS, a.getApplyStatus()));
        int total = NumUtil.orDefault(a.getTotalTimes(), 1);
        int done = NumUtil.orDefault(a.getDoneTimes(), 0);
        v.setDoneTimes(done);
        v.setProgressText(done + "/" + total);
        v.setPendingTimes(Math.max(total - done, 0));
        v.setFinished(done >= total);
        v.setPlanAmount(a.getPrice() == null ? null
                : a.getPrice().multiply(BigDecimal.valueOf(total)).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        return v;
    }

    private List<TreatmentVO.ExecVO> toExecVos(List<BizTreatmentRecord> rows, Map<Long, BizTreatmentApply> applies) {
        List<TreatmentVO.ExecVO> out = new ArrayList<>();
        for (BizTreatmentRecord r : rows) {
            out.add(toExecVo(r, applies.get(r.getApplyId())));
        }
        return out;
    }

    private TreatmentVO.ExecVO toExecVo(BizTreatmentRecord r, BizTreatmentApply apply) {
        TreatmentVO.ExecVO v = new TreatmentVO.ExecVO();
        BeanUtils.copyProperties(r, v);
        v.setExecStatusText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_EXEC_STATUS, r.getExecStatus()));
        v.setChargeStatusText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_CHARGE_STATUS, r.getChargeStatus()));
        // execute_time / record_status 在老库是 NOT NULL 带默认值，未执行的行上是 MySQL 填的默认值，
        // 只有真的打过卡才有意义 —— 不按时机清空就会把"排期"显示成"已做"。
        boolean done = Integer.valueOf(TreatmentExecStatusEnum.DONE.getCode()).equals(r.getExecStatus());
        v.setExecuteTime(done ? r.getExecuteTime() : null);
        v.setRecordStatus(done ? r.getRecordStatus() : null);
        v.setRecordStatusText(done ? dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_RECORD_STATUS, r.getRecordStatus()) : null);
        LocalDate plan = r.getPlanDate();
        v.setOverdue(!done && plan != null && plan.isBefore(LocalDate.now()));
        if (apply != null) {
            v.setApplyNo(apply.getApplyNo());
            v.setPatientId(apply.getPatientId());
            v.setPatientNo(apply.getPatientNo());
            v.setPatientName(apply.getPatientName());
            v.setItemName(apply.getItemName());
            v.setItemType(apply.getItemType());
            v.setItemTypeText(dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_ITEM_TYPE, apply.getItemType()));
            v.setPrice(apply.getPrice());
            v.setDeptId(apply.getDeptId());
            v.setDeptName(apply.getDeptName());
            v.setExecDeptId(apply.getExecDeptId());
            v.setExecDeptName(apply.getExecDeptName());
        }
        String blocked = apply == null ? "疗程已删除，无法判定" : blockReason(r, apply);
        v.setCanExecute(blocked == null);
        v.setCannotExecuteReason(blocked);
        v.setCanRetryCharge(done && !Integer.valueOf(BillingStatusEnum.BILLED.getCode()).equals(r.getChargeStatus()));
        return v;
    }

    private Page<BizTreatmentRecord> execPage(TreatmentDTO.ExecQuery q, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizTreatmentRecord> w = new LambdaQueryWrapper<>();
        // 流水表无科室列：按申请单开单科室收口（分页/状态分布/统计共用本方法，口径天然一致）
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        if (scope != null) {
            List<Long> scopedApplyIds = bizTreatmentApplyMapper.selectList(new LambdaQueryWrapper<BizTreatmentApply>()
                            .select(BizTreatmentApply::getApplyId).in(BizTreatmentApply::getDeptId, scope))
                    .stream().map(BizTreatmentApply::getApplyId).toList();
            if (scopedApplyIds.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            w.in(BizTreatmentRecord::getApplyId, scopedApplyIds);
        }
        w.eq(q.getApplyId() != null, BizTreatmentRecord::getApplyId, q.getApplyId())
                .eq(q.getExecStatus() != null, BizTreatmentRecord::getExecStatus, q.getExecStatus())
                .eq(q.getChargeStatus() != null, BizTreatmentRecord::getChargeStatus, q.getChargeStatus())
                .eq(q.getPlanDate() != null, BizTreatmentRecord::getPlanDate, q.getPlanDate())
                .ge(q.getStartDate() != null, BizTreatmentRecord::getPlanDate, q.getStartDate())
                .le(q.getEndDate() != null, BizTreatmentRecord::getPlanDate, q.getEndDate());
        if (Boolean.TRUE.equals(q.getOverdueOnly())) {
            w.eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.PENDING.getCode())
                    .lt(BizTreatmentRecord::getPlanDate, LocalDate.now());
        }
        if (Boolean.TRUE.equals(q.getUnbilledOnly())) {
            w.eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.DONE.getCode())
                    .in(BizTreatmentRecord::getChargeStatus, BillingStatusEnum.UNBILLED.getCode(), BillingStatusEnum.FAILED.getCode());
        }
        if (q.getPatientId() != null || TextUtil.hasText(TextUtil.trim(q.getKeyword()))) {
            List<Long> applyIds = applyIds(q.getPatientId(), TextUtil.trim(q.getKeyword()));
            if (applyIds.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            w.in(BizTreatmentRecord::getApplyId, applyIds);
        }
        w.orderByAsc(BizTreatmentRecord::getPlanDate).orderByAsc(BizTreatmentRecord::getApplyId)
                .orderByAsc(BizTreatmentRecord::getExecSeq);
        return bizTreatmentRecordMapper.selectPage(new Page<>(pageNum, pageSize), w);
    }

    /**
     * 关键字/患者 → 申请单ID 集合（流水表上没有患者姓名列，只能先回申请单定位）
     */
    private List<Long> applyIds(Long patientId, String kw) {
        LambdaQueryWrapper<BizTreatmentApply> w = new LambdaQueryWrapper<>();
        w.select(BizTreatmentApply::getApplyId)
                .eq(patientId != null, BizTreatmentApply::getPatientId, patientId)
                .and(TextUtil.hasText(kw), x -> x.like(BizTreatmentApply::getPatientName, kw)
                        .or().like(BizTreatmentApply::getPatientNo, kw)
                        .or().like(BizTreatmentApply::getApplyNo, kw)
                        .or().like(BizTreatmentApply::getItemName, kw));
        List<Long> ids = new ArrayList<>();
        for (BizTreatmentApply a : bizTreatmentApplyMapper.selectList(w)) {
            ids.add(a.getApplyId());
        }
        return ids;
    }

    private Map<Long, BizTreatmentApply> applyMap(Collection<BizTreatmentRecord> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizTreatmentRecord r : rows) {
            if (r.getApplyId() != null) {
                ids.add(r.getApplyId());
            }
        }
        Map<Long, BizTreatmentApply> map = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (BizTreatmentApply a : bizTreatmentApplyMapper.selectBatchIds(ids)) {
            map.put(a.getApplyId(), a);
        }
        return map;
    }

    private BigDecimal sumTodayChargeAmount(LocalDate today) {
        List<BizTreatmentRecord> rows = bizTreatmentRecordMapper.selectList(new LambdaQueryWrapper<BizTreatmentRecord>()
                .select(BizTreatmentRecord::getChargeAmount)
                .eq(BizTreatmentRecord::getExecStatus, TreatmentExecStatusEnum.DONE.getCode())
                .eq(BizTreatmentRecord::getChargeStatus, BillingStatusEnum.BILLED.getCode())
                .eq(BizTreatmentRecord::getPlanDate, today));
        BigDecimal sum = BigDecimal.ZERO;
        for (BizTreatmentRecord r : rows) {
            if (r.getChargeAmount() != null) {
                sum = sum.add(r.getChargeAmount());
            }
        }
        return sum.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    private TreatmentDTO.ExecQuery queryOf(Integer execStatus, LocalDate planDate, Integer chargeStatus) {
        TreatmentDTO.ExecQuery q = new TreatmentDTO.ExecQuery();
        q.setExecStatus(execStatus);
        q.setPlanDate(planDate);
        q.setChargeStatus(chargeStatus);
        return q;
    }

    private TreatmentDTO.ExecQuery copy(TreatmentDTO.ExecQuery q) {
        TreatmentDTO.ExecQuery c = new TreatmentDTO.ExecQuery();
        BeanUtils.copyProperties(q, c);
        return c;
    }

    private TreatmentVO.StatusCountVO countVo(String key, String label, long count) {
        TreatmentVO.StatusCountVO v = new TreatmentVO.StatusCountVO();
        v.setKey(key);
        v.setLabel(label);
        v.setCount(count);
        return v;
    }

    private BizTreatmentApply requireApply(Long applyId) {
        // C-非 web 入参：除 web 入口外还被改期/执行/补记账用行记录上的 exec.getApplyId() 直调，
        // 那是库里读出来的值不是 HTTP 绑定入参，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("治疗申请单ID不能为空");
        }
        BizTreatmentApply a = bizTreatmentApplyMapper.selectById(applyId);
        if (a == null) {
            throw new BusinessException("治疗申请单不存在：" + applyId);
        }
        deptScopeService.assertDeptAccessible(a.getDeptId());
        return a;
    }

    private BizTreatmentRecord requireExec(Long recordId) {
        BizTreatmentRecord r = bizTreatmentRecordMapper.selectById(recordId);
        if (r == null) {
            throw new BusinessException("治疗执行流水不存在：" + recordId);
        }
        return r;
    }

    private void checkDateWindow(LocalDate date, String label) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today.minusDays(BACK_DAYS))) {
            throw new BusinessException(label + "不得早于 " + today.minusDays(BACK_DAYS) + "（历史疗程不补开）");
        }
        if (date.isAfter(today.plusDays(AHEAD_DAYS))) {
            throw new BusinessException(label + "不得晚于 " + today.plusDays(AHEAD_DAYS));
        }
    }

    private String execStatusText(Integer status) {
        return dictCacheService.getDicDataLabel(DictTypeConst.TREATMENT_EXEC_STATUS, status);
    }
}
