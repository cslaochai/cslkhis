package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.common.enums.DutyCallActionEnum;
import com.his.common.enums.DutyCallStatusEnum;
import com.his.common.enums.DutyLevelEnum;
import com.his.common.enums.DutyScopeEnum;
import com.his.common.enums.DutyShiftTypeEnum;
import com.his.common.enums.PublishStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.outpatient.dto.DutyCallArriveDTO;
import com.his.outpatient.dto.DutyCallCancelDTO;
import com.his.outpatient.dto.DutyCallPageQueryDTO;
import com.his.outpatient.dto.DutyCallUpsertDTO;
import com.his.outpatient.entity.BizDutyCall;
import com.his.outpatient.entity.BizDutyCallLog;
import com.his.outpatient.entity.BizDutyPost;
import com.his.outpatient.entity.BizDutyRoster;
import com.his.outpatient.entity.BizShift;
import com.his.outpatient.mapper.BizDutyCallLogMapper;
import com.his.outpatient.mapper.BizDutyCallMapper;
import com.his.outpatient.mapper.BizDutyPostMapper;
import com.his.outpatient.mapper.BizDutyRosterMapper;
import com.his.outpatient.mapper.BizShiftMapper;
import com.his.outpatient.service.DutyCallService;
import com.his.outpatient.service.DutyRosterService;
import com.his.outpatient.vo.DutyCallLogVO;
import com.his.outpatient.vo.DutyCallTargetVO;
import com.his.outpatient.vo.DutyCallVO;
import com.his.outpatient.vo.DutyOfficerVO;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.service.SysMessageService;
import com.his.spi.service.SystemConfigFacadeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.EmployeeBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 临床值班听班呼叫服务实现（sql/297）。
 *
 * <p>升级阶梯：二线超时 → 三线；三线超时 → 科主任 → 全院总值班。
 * 阶梯走完仍无人应答，就停在「已超时升级」态 —— 再往上叫没有意义，
 * 该找的是医务科（走 duty-coord 那条线），不是继续在值班体系里空转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DutyCallServiceImpl extends ServiceImpl<BizDutyCallMapper, BizDutyCall> implements DutyCallService {

    /**
     * 系统参数：听班呼叫到岗时限（分钟）
     */
    private static final String CONFIG_TIMEOUT_MINUTES = "duty.call.timeout_minutes";

    /**
     * 默认到岗时限（分钟）：系统参数缺省时的兜底
     */
    private static final int DEFAULT_TIMEOUT_MINUTES = 10;

    /**
     * 呼叫单默认时间窗（天）：呼叫流水是近几天的事，翻太久没有意义
     */
    private static final int DEFAULT_BACK_DAYS = 7;

    /**
     * 呼叫单默认未来窗口（天）
     */
    private static final int DEFAULT_FORWARD_DAYS = 7;

    /**
     * 夜班起点（小时）：与值班域「此刻属于哪一段」同口径
     */
    private static final int NIGHT_START_HOUR = 18;

    /**
     * 白班起点（小时）
     */
    private static final int DAY_START_HOUR = 8;

    private final BizDutyCallMapper bizDutyCallMapper;

    private final BizDutyCallLogMapper bizDutyCallLogMapper;

    private final BizDutyPostMapper bizDutyPostMapper;

    private final BizDutyRosterMapper bizDutyRosterMapper;

    private final BizShiftMapper bizShiftMapper;

    private final DutyRosterService dutyRosterService;

    private final SystemFacadeService systemFacadeService;

    private final SysMessageService sysMessageService;

    private final SystemConfigFacadeService systemConfigFacadeService;

    private final RedisSequenceService redisSequenceService;

    // ==================== 发起呼叫 ====================

    @Transactional(rollbackFor = Exception.class)
    public DutyCallVO call(DutyCallUpsertDTO dto) {
        CurrentOperator operator = currentOperator();
        LocalDate dutyDate = dto.getDutyDate() != null ? LocalDate.parse(dto.getDutyDate()) : LocalDate.now();
        int shiftType = dto.getShiftType() != null ? dto.getShiftType() : inferShiftType(LocalDateTime.now());
        int level = dto.getTargetLevel() != null ? dto.getTargetLevel() : DutyLevelEnum.SECOND.getCode();
        if (!DutyLevelEnum.isValid(level) || level == DutyLevelEnum.NONE.getCode()
                || level == DutyLevelEnum.FIRST.getCode()) {
            throw new BusinessException("被叫层级只能是二线或三线");
        }
        int timeoutMinutes = dto.getTimeoutMinutes() != null && dto.getTimeoutMinutes() > 0
                ? dto.getTimeoutMinutes() : configTimeoutMinutes();

        Long deptId = dto.getDeptId() != null ? dto.getDeptId() : operator.deptId();
        if (deptId == null) {
            throw new BusinessException("当前登录人没有所属科室，无法确定呼叫发起科室");
        }
        // 科室名：登录人所属科室直接用登录态里的快照，其余走主数据查询
        String deptName = Objects.equals(deptId, operator.deptId()) && TextUtil.hasText(operator.deptName())
                ? operator.deptName() : deptNameOf(deptId);

        // 目标人由排班解析：一线不能被叫（一线是留院值班，人就在科室），只认二/三线
        DutyCallTargetVO target = resolveTarget(deptId, dutyDate, shiftType, level);
        if (target == null || target.getEmployeeId() == null) {
            throw new BusinessException(String.format(
                    "%s %s 没有排 %s 听班值班人，无法发起呼叫；请先到「值班排班」页（临床医师值班）排班",
                    deptName, dutyDate, DutyLevelEnum.getLabel(level)));
        }

        LocalDateTime now = LocalDateTime.now();
        BizDutyCall call = new BizDutyCall();
        call.setCallNo(redisSequenceService.generateDutyCallNo());
        call.setDutyDate(dutyDate);
        call.setShiftType(shiftType);
        call.setDeptId(deptId);
        call.setDeptName(deptName);
        call.setTargetLevel(level);
        call.setTargetPostId(target.getPostId());
        call.setTargetPostName(target.getPostName());
        call.setTargetEmpId(target.getEmployeeId());
        call.setTargetEmpName(target.getEmployeeName());
        call.setTargetPhone(target.getPhone());
        call.setCallReason(dto.getCallReason().trim());
        call.setPatientName(TextUtil.trimToNull(dto.getPatientName()));
        call.setPatientLocation(TextUtil.trimToNull(dto.getPatientLocation()));
        call.setCallTime(now);
        call.setStatus(DutyCallStatusEnum.PENDING.getCode());
        call.setEscalateStatus(0);
        bizDutyCallMapper.insert(call);

        writeLog(call, DutyCallActionEnum.CALL, null, DutyCallStatusEnum.PENDING.getCode(), operator.id(), operator.name(),
                String.format("呼叫 %s %s，限 %d 分钟到岗", DutyLevelEnum.getLabel(level), target.getEmployeeName(), timeoutMinutes));

        notifyTarget(call, timeoutMinutes);

        log.info("[听班呼叫] 发起 callNo={} dept={} level={} target={} timeout={}min",
                call.getCallNo(), deptName, level, target.getEmployeeName(), timeoutMinutes);
        return toVO(call, timeoutMinutes, operator.employeeId());
    }

    // ==================== 到岗应答 ====================

    @Transactional(rollbackFor = Exception.class)
    public DutyCallVO arrive(DutyCallArriveDTO dto) {
        CurrentOperator operator = currentOperator();
        BizDutyCall call = requireCall(dto.getId());
        // 待到岗(0) 与 已超时升级(2) 都能应答：
        //   升级后呼叫单状态是 2，此时接手的正是当前被叫人（三线/总值班）——
        //   只允许 0 的话，升级出去的呼叫永远没人能应答，链路等于断在这里。
        if (!DutyCallStatusEnum.isWaiting(call.getStatus())
                && !Integer.valueOf(DutyCallStatusEnum.ESCALATED.getCode()).equals(call.getStatus())) {
            throw new BusinessException("该呼叫当前状态为「" + DutyCallStatusEnum.getLabelOrUnknown(call.getStatus())
                    + "」，不能重复应答到岗");
        }
        // 只有被叫人本人能应答：别人替答等于把责任转给了不在场的人
        if (!Objects.equals(call.getTargetEmpId(), operator.employeeId())) {
            throw new BusinessException("只有被叫值班人本人可以应答到岗");
        }
        Integer fromStatus = call.getStatus();
        LocalDateTime now = LocalDateTime.now();
        call.setArriveTime(now);
        call.setStatus(DutyCallStatusEnum.ARRIVED.getCode());
        bizDutyCallMapper.updateById(call);

        writeLog(call, DutyCallActionEnum.ARRIVE, fromStatus,
                DutyCallStatusEnum.ARRIVED.getCode(), operator.id(), operator.name(),
                TextUtil.hasText(dto.getArriveNote()) ? dto.getArriveNote().trim()
                        : String.format("已到岗（呼叫后 %d 分钟）", minutesBetween(call.getCallTime(), now)));

        // 闭环站内信待办：呼叫已处置完，收件箱不该一直挂着「待处理」。
        // ⚠ 两个码值都要关：呼叫刚发起时发的是 duty-call；若中途超时升级过，
        //   当前被叫人收到的是 duty-call-esc。只关一个码值，他到了岗收件箱还挂着"待处理"。
        try {
            sysMessageService.closeTodoByBiz(BizTypeEnum.DUTY_CALL.getType(), call.getId(), 1);
            sysMessageService.closeTodoByBiz(BizTypeEnum.DUTY_CALL_ESC.getType(), call.getId(), 1);
        } catch (Exception ex) {
            log.warn("[听班呼叫] 关闭待办失败 callNo={}：{}", call.getCallNo(), ex.getMessage());
        }

        log.info("[听班呼叫] 到岗 callNo={} target={}", call.getCallNo(), call.getTargetEmpName());
        return toVO(call, configTimeoutMinutes(), operator.employeeId());
    }

    // ==================== 取消呼叫 ====================

    @Transactional(rollbackFor = Exception.class)
    public DutyCallVO cancel(DutyCallCancelDTO dto) {
        CurrentOperator operator = currentOperator();
        BizDutyCall call = requireCall(dto.getId());
        if (!DutyCallStatusEnum.isWaiting(call.getStatus())) {
            throw new BusinessException("该呼叫当前状态为「" + DutyCallStatusEnum.getLabelOrUnknown(call.getStatus())
                    + "」，无需取消");
        }
        Long callerId = call.getCreateById();
        if (!Objects.equals(callerId, operator.employeeId())) {
            throw new BusinessException("只有呼叫发起人可以取消该呼叫");
        }
        call.setStatus(DutyCallStatusEnum.CANCELED.getCode());
        call.setCancelReason(dto.getCancelReason().trim());
        bizDutyCallMapper.updateById(call);

        writeLog(call, DutyCallActionEnum.CANCEL, DutyCallStatusEnum.PENDING.getCode(),
                DutyCallStatusEnum.CANCELED.getCode(), operator.id(), operator.name(), call.getCancelReason());

        try {
            // 同 arrive：两个码值都关（升级过的呼叫，升级通知也该一起撤掉）
            sysMessageService.closeTodoByBiz(BizTypeEnum.DUTY_CALL.getType(), call.getId(), 2);
            sysMessageService.closeTodoByBiz(BizTypeEnum.DUTY_CALL_ESC.getType(), call.getId(), 2);
        } catch (Exception ex) {
            log.warn("[听班呼叫] 关闭待办失败 callNo={}：{}", call.getCallNo(), ex.getMessage());
        }

        log.info("[听班呼叫] 取消 callNo={} reason={}", call.getCallNo(), call.getCancelReason());
        return toVO(call, configTimeoutMinutes(), operator.employeeId());
    }

    // ==================== 查询 ====================

    public PageResult<DutyCallVO> listPage(DutyCallPageQueryDTO q) {
        LocalDate begin = q.getBeginDate() != null ? q.getBeginDate() : LocalDate.now().minusDays(DEFAULT_BACK_DAYS);
        LocalDate end = q.getEndDate() != null ? q.getEndDate() : LocalDate.now().plusDays(DEFAULT_FORWARD_DAYS);
        Long currentEmpId = currentEmployeeId();
        LambdaQueryWrapper<BizDutyCall> w = new LambdaQueryWrapper<>();
        w.ge(BizDutyCall::getDutyDate, begin)
                .le(BizDutyCall::getDutyDate, end)
                .eq(q.getDeptId() != null, BizDutyCall::getDeptId, q.getDeptId())
                .eq(q.getTargetLevel() != null, BizDutyCall::getTargetLevel, q.getTargetLevel())
                .eq(q.getTargetEmpId() != null, BizDutyCall::getTargetEmpId, q.getTargetEmpId())
                .eq(q.getStatus() != null, BizDutyCall::getStatus, q.getStatus());
        // 「我的」两种视角（单选，互斥）：
        //   1 待我到岗 = 我是被叫人；2 我发起的 = 我是呼叫发起人（create_by_id 记的发起人）
        if (q.getMineType() != null && currentEmpId != null) {
            if (q.getMineType() == 1) {
                w.eq(BizDutyCall::getTargetEmpId, currentEmpId);
            } else if (q.getMineType() == 2) {
                w.eq(BizDutyCall::getCreateById, currentEmpId);
            }
        }
        // 二级键收口：同一天同班次内顺序必须稳定，否则分页会重复/漏行
        w.orderByDesc(BizDutyCall::getDutyDate)
                .orderByDesc(BizDutyCall::getShiftType)
                .orderByDesc(BizDutyCall::getId);
        IPage<BizDutyCall> page = bizDutyCallMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        int timeout = configTimeoutMinutes();
        List<DutyCallVO> list = page.getRecords().stream()
                .map(c -> toVO(c, timeout, currentEmpId)).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    public DutyCallVO detail(Long id) {
        BizDutyCall call = requireCall(id);
        DutyCallVO vo = toVO(call, configTimeoutMinutes(), currentEmployeeId());
        List<BizDutyCallLog> logs = bizDutyCallLogMapper.selectList(new LambdaQueryWrapper<BizDutyCallLog>()
                .eq(BizDutyCallLog::getCallId, id)
                .orderByAsc(BizDutyCallLog::getActionTime)
                .orderByAsc(BizDutyCallLog::getId));
        vo.setLogs(logs.stream().map(this::toLogVO).toList());
        return vo;
    }

    public List<DutyCallTargetVO> listTargets(Long deptId, LocalDate dutyDate, Integer shiftType) {
        LocalDate date = dutyDate != null ? dutyDate : LocalDate.now();
        int shift = shiftType != null ? shiftType : inferShiftType(LocalDateTime.now());
        if (deptId == null) {
            return List.of();
        }
        // 点位是权威：层级与响应形态都挂在点位上，不由排班行决定。
        // ⚠ 必须按班次筛点位 —— 白班与夜班是两个不同点位（呼吸内科二线白班 / 二线夜班），
        //   同一个 duty_level 下有两条。不筛就会把白班点位一起返回，
        //   而白班那一条往往没有当班排班 → callable=false，前端选层级时拿到错的那条。
        List<BizDutyPost> allPosts = bizDutyPostMapper.selectList(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getDutyScope, DutyScopeEnum.CLINICAL.getCode())
                .eq(BizDutyPost::getOrgType, 1)
                .eq(BizDutyPost::getOrgId, deptId)
                .eq(BizDutyPost::getStatus, 1)
                .in(BizDutyPost::getDutyLevel, DutyLevelEnum.SECOND.getCode(), DutyLevelEnum.THIRD.getCode())
                .orderByAsc(BizDutyPost::getDutyLevel)
                .orderByAsc(BizDutyPost::getId));
        if (allPosts.isEmpty()) {
            return List.of();
        }
        // 点位绑的班次决定它是白班还是夜班：cross_day=1 的是夜班（18:00→次日08:00）
        List<Long> postIds = allPosts.stream().map(BizDutyPost::getId).toList();
        Map<Long, Integer> shiftOfPost = new HashMap<>();
        for (BizShift s : bizShiftMapper.selectBatchIds(
                allPosts.stream().map(BizDutyPost::getShiftId).filter(Objects::nonNull).distinct().toList())) {
            shiftOfPost.put(s.getId(), Objects.equals(YesOrNoEnum.YES.getCode(), s.getCrossDay())
                    ? DutyShiftTypeEnum.NIGHT.getCode() : DutyShiftTypeEnum.DAY.getCode());
        }
        List<BizDutyPost> posts = allPosts.stream()
                .filter(p -> shiftOfPost.getOrDefault(p.getShiftId(), DutyShiftTypeEnum.DAY.getCode()) == shift)
                .toList();
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> matchedPostIds = posts.stream().map(BizDutyPost::getId).toList();
        List<BizDutyRoster> rows = bizDutyRosterMapper.selectList(new LambdaQueryWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getDutyDate, date)
                .eq(BizDutyRoster::getShiftType, shift)
                .eq(BizDutyRoster::getStatus, 1)
                .in(BizDutyRoster::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .in(BizDutyRoster::getPostId, matchedPostIds));
        List<DutyCallTargetVO> list = new ArrayList<>();
        for (BizDutyPost p : posts) {
            DutyCallTargetVO vo = new DutyCallTargetVO();
            vo.setPostId(p.getId());
            vo.setPostName(p.getPostName());
            vo.setTargetLevel(p.getDutyLevel());
            vo.setTargetLevelText(DutyLevelEnum.getLabel(p.getDutyLevel()));
            BizDutyRoster row = rows.stream()
                    .filter(r -> Objects.equals(r.getPostId(), p.getId()))
                    .min(Comparator.comparing(BizDutyRoster::getId))
                    .orElse(null);
            if (row != null) {
                // 换班后取实际值班人：叫的是现在真能接电话的那个人
                boolean substituted = row.getSubstituteEmpId() != null;
                vo.setEmployeeId(substituted ? row.getSubstituteEmpId() : row.getEmployeeId());
                vo.setEmployeeName(substituted ? row.getSubstituteEmpName() : row.getEmployeeName());
                vo.setPhone(substituted ? resolvePhone(row, vo.getEmployeeId()) : row.getPhone());
                vo.setCallable(true);
            } else {
                vo.setCallable(false);
            }
            list.add(vo);
        }
        return list;
    }

    // ==================== 超时升级 ====================

    @Transactional(rollbackFor = Exception.class)
    public int escalateOverdue() {
        int timeout = configTimeoutMinutes();
        LocalDateTime now = LocalDateTime.now();
        List<BizDutyCall> pending = bizDutyCallMapper.selectList(new LambdaQueryWrapper<BizDutyCall>()
                .eq(BizDutyCall::getStatus, DutyCallStatusEnum.PENDING.getCode())
                // 「是否升级过」落库：未升级过的才处理，重复升级会轰炸同一条呼叫
                .eq(BizDutyCall::getEscalateStatus, 0)
                .le(BizDutyCall::getCallTime, now.minusMinutes(timeout))
                .orderByAsc(BizDutyCall::getId));
        if (pending.isEmpty()) {
            return 0;
        }
        int escalated = 0;
        for (BizDutyCall call : pending) {
            try {
                if (escalateOne(call, timeout, now)) {
                    escalated++;
                }
            } catch (Exception ex) {
                // 单条失败不中断整批：升级是兜底动作，不能因为一条脏数据就停摆
                log.error("[听班呼叫] 升级失败 callNo={}：{}", call.getCallNo(), ex.getMessage(), ex);
            }
        }
        if (escalated > 0) {
            log.warn("[听班呼叫] 本轮升级 {} 条超时未到岗的呼叫（时限 {} 分钟）", escalated, timeout);
        }
        return escalated;
    }

    public int escalateOverdueManual() {
        return escalateOverdue();
    }

    /**
     * 升级一条呼叫：找上一层级的听班人 → 发升级通知 → 落升级标记。
     *
     * <p>阶梯：二线 → 三线 → 全院总值班。找不到更高层级的听班人就升总值班；
     * 总值班也升不动时停在「已超时升级」态（再往上叫属于医务科那条线）。
     */
    private boolean escalateOne(BizDutyCall call, int timeout, LocalDateTime now) {
        int fromLevel = call.getTargetLevel();
        int nextLevel = fromLevel == DutyLevelEnum.SECOND.getCode()
                ? DutyLevelEnum.THIRD.getCode() : 0;

        call.setEscalateStatus(1);
        call.setEscalateTime(now);
        call.setEscalateFromLevel(fromLevel);

        DutyCallTargetVO next = null;
        if (nextLevel > 0) {
            next = resolveTarget(call.getDeptId(), call.getDutyDate(), call.getShiftType(), nextLevel);
        }
        if (next != null && next.getEmployeeId() != null) {
            call.setTargetLevel(nextLevel);
            call.setTargetPostId(next.getPostId());
            call.setTargetPostName(next.getPostName());
            call.setTargetEmpId(next.getEmployeeId());
            call.setTargetEmpName(next.getEmployeeName());
            call.setTargetPhone(next.getPhone());
            call.setStatus(DutyCallStatusEnum.ESCALATED.getCode());
            bizDutyCallMapper.updateById(call);
            writeLog(call, DutyCallActionEnum.ESCALATE, DutyCallStatusEnum.PENDING.getCode(),
                    DutyCallStatusEnum.ESCALATED.getCode(), null, null,
                    String.format("%d 分钟未到岗，升级至 %s %s", timeout, DutyLevelEnum.getLabel(nextLevel), next.getEmployeeName()));
            notifyEscalate(call, next, timeout);
            return true;
        }

        // 没有更高层级的听班人 → 升级给全院总值班（行政协调线，与临床值班线不混）
        DutyOfficerHolder holder = currentDutyOfficer();
        if (holder != null && holder.employeeId() != null) {
            // targetLevel 保持原临床层级（这条呼叫本就是临床层级的），
            // 「当前该谁到岗」由 targetEmpId/Name + targetPostName 表达
            call.setTargetPostId(null);
            call.setTargetPostName("全院总值班");
            call.setTargetEmpId(holder.employeeId());
            call.setTargetEmpName(holder.employeeName());
            call.setTargetPhone(holder.phone());
            call.setStatus(DutyCallStatusEnum.ESCALATED.getCode());
            bizDutyCallMapper.updateById(call);
            writeLog(call, DutyCallActionEnum.ESCALATE, DutyCallStatusEnum.PENDING.getCode(),
                    DutyCallStatusEnum.ESCALATED.getCode(), null, null,
                    String.format("%d 分钟未到岗，科室无更高层级听班人，升级至全院总值班 %s", timeout, holder.employeeName()));
            notifyEscalate(call, null, timeout);
            return true;
        }

        // 阶梯走完：停在「已超时升级」，明确记录「没人接得住」
        call.setStatus(DutyCallStatusEnum.ESCALATED.getCode());
        bizDutyCallMapper.updateById(call);
        writeLog(call, DutyCallActionEnum.ESCALATE, DutyCallStatusEnum.PENDING.getCode(),
                DutyCallStatusEnum.ESCALATED.getCode(), null, null,
                String.format("%d 分钟未到岗，升级阶梯已走完（无更高层级听班人且未排总值班），需医务科介入", timeout));
        log.error("[听班呼叫] {} 超时无人在岗，升级阶梯走完 callNo={}", timeout, call.getCallNo());
        return true;
    }

    // ==================== 内部方法 ====================

    /**
     * 解析目标层级当班的听班值班人：点位（层级+形态）+ 排班行（人）。
     */
    private DutyCallTargetVO resolveTarget(Long deptId, LocalDate dutyDate, int shiftType, int level) {
        List<DutyCallTargetVO> targets = listTargets(deptId, dutyDate, shiftType);
        return targets.stream()
                .filter(t -> Objects.equals(t.getTargetLevel(), level))
                .filter(t -> Boolean.TRUE.equals(t.getCallable()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通知被叫人到岗（待办型：闭环动作是到岗应答，点已读不算到岗）。
     */
    private void notifyTarget(BizDutyCall call, int timeoutMinutes) {
        try {
            EmployeeBriefVO emp = systemFacadeService.findEmployee(call.getTargetEmpId());
            String name = emp != null && TextUtil.hasText(emp.getEmpName()) ? emp.getEmpName() : call.getTargetEmpName();
            String title = String.format("%s听班呼叫：%s", DutyLevelEnum.getLabel(call.getTargetLevel()), name);
            String content = String.format(
                    "%s %s 呼叫您（%s，%s）：%s%s%s请在 %d 分钟内应答到岗。",
                    call.getDeptName(),
                    call.getShiftType() == DutyShiftTypeEnum.NIGHT.getCode() ? "夜班" : "白班",
                    DutyLevelEnum.getLabel(call.getTargetLevel()),
                    TextUtil.hasText(call.getTargetPostName()) ? call.getTargetPostName() : "听班",
                    call.getCallReason(),
                    TextUtil.hasText(call.getPatientName()) ? "；患者：" + call.getPatientName() : "",
                    TextUtil.hasText(call.getPatientLocation()) ? "（" + call.getPatientLocation() + "）" : "",
                    timeoutMinutes);
            String payload = cn.hutool.json.JSONUtil.toJsonStr(new java.util.LinkedHashMap<String, Object>() {{
                put("patientName", call.getPatientName());
                put("callNo", call.getCallNo());
                put("targetLevelText", DutyLevelEnum.getLabel(call.getTargetLevel()));
                put("deptName", call.getDeptName());
            }});
            boolean ok = sysMessageService.sendSystemMessage(call.getTargetEmpId(), name, title, content,
                    BizTypeEnum.DUTY_CALL.getType(), call.getId(), "warning", payload, 0);
            if (!ok) {
                writeLog(call, DutyCallActionEnum.NOTIFY_FAIL, null, null, null, null, "站内信投递失败，请电话联系被叫值班人");
                log.warn("[听班呼叫] 站内信投递失败 callNo={} target={}", call.getCallNo(), call.getTargetEmpName());
            }
        } catch (Exception ex) {
            writeLog(call, DutyCallActionEnum.NOTIFY_FAIL, null, null, null, null, "站内信投递异常：" + ex.getMessage());
            log.warn("[听班呼叫] 通知异常 callNo={}：{}", call.getCallNo(), ex.getMessage());
        }
    }

    /**
     * 升级通知：发给更高层级的人，同时把原被叫人的待办关掉（他已被认定没到岗）。
     */
    private void notifyEscalate(BizDutyCall call, DutyCallTargetVO next, int timeoutMinutes) {
        try {
            EmployeeBriefVO emp = systemFacadeService.findEmployee(call.getTargetEmpId());
            String name = emp != null && TextUtil.hasText(emp.getEmpName()) ? emp.getEmpName() : call.getTargetEmpName();
            String title = String.format("听班呼叫超时升级：%s", call.getCallNo());
            String content = String.format(
                    "%s %s 呼叫 %s（%s）后 %d 分钟未到岗，现升级至您（%s）。原呼叫事由：%s%s%s请尽快到岗。",
                    call.getDeptName(),
                    call.getShiftType() == DutyShiftTypeEnum.NIGHT.getCode() ? "夜班" : "白班",
                    DutyLevelEnum.getLabel(call.getEscalateFromLevel()),
                    call.getTargetPostName(),
                    timeoutMinutes,
                    next != null ? DutyLevelEnum.getLabel(next.getTargetLevel()) + " " + next.getEmployeeName() : "全院总值班",
                    call.getCallReason(),
                    TextUtil.hasText(call.getPatientName()) ? "；患者：" + call.getPatientName() : "",
                    TextUtil.hasText(call.getPatientLocation()) ? "（" + call.getPatientLocation() + "）" : "");
            String payload = cn.hutool.json.JSONUtil.toJsonStr(new java.util.LinkedHashMap<String, Object>() {{
                put("patientName", call.getPatientName());
                put("callNo", call.getCallNo());
                put("deptName", call.getDeptName());
            }});
            sysMessageService.sendSystemMessage(call.getTargetEmpId(), name, title, content,
                    BizTypeEnum.DUTY_CALL_ESC.getType(), call.getId(), "urgent", payload, 0);
            // 原被叫人的待办闭环：他已被认定超时未到岗。
            // ⚠ 状态用「已关闭(2)」不是「已处理(1)」—— 二线压根没到岗，标成"已处理"会让收件箱
            //   看起来像他响应过了，追溯时得出错误结论（他明明是被升级掉的）。
            //   只关 duty-call 码值：升级通知走 duty-call-esc 独立码，不会被这一句误关。
            sysMessageService.closeTodoByBiz(BizTypeEnum.DUTY_CALL.getType(), call.getId(), 2);
        } catch (Exception ex) {
            log.warn("[听班呼叫] 升级通知失败 callNo={}：{}", call.getCallNo(), ex.getMessage());
        }
    }

    /**
     * 写呼叫流水（只增不改）。
     */
    private void writeLog(BizDutyCall call, DutyCallActionEnum action, Integer fromStatus, Integer toStatus,
                          Long operatorId, String operatorName, String note) {
        BizDutyCallLog log = new BizDutyCallLog();
        log.setCallId(call.getId());
        log.setCallNo(call.getCallNo());
        log.setActionType(action.getCode());
        log.setActionTime(LocalDateTime.now());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setActionNote(note);
        bizDutyCallLogMapper.insert(log);
    }

    private BizDutyCall requireCall(Long id) {
        BizDutyCall call = bizDutyCallMapper.selectById(id);
        if (call == null) {
            throw new BusinessException("听班呼叫单不存在");
        }
        return call;
    }

    private DutyCallVO toVO(BizDutyCall call, int timeoutMinutes, Long currentEmpId) {
        DutyCallVO vo = new DutyCallVO();
        vo.setId(call.getId());
        vo.setCallNo(call.getCallNo());
        vo.setDutyDate(call.getDutyDate());
        vo.setShiftType(call.getShiftType());
        vo.setShiftTypeText(call.getShiftType() == DutyShiftTypeEnum.NIGHT.getCode() ? "夜班" : "白班");
        vo.setDeptId(call.getDeptId());
        vo.setDeptName(call.getDeptName());
        vo.setTargetLevel(call.getTargetLevel());
        vo.setTargetLevelText(DutyLevelEnum.getLabelOrUnknown(call.getTargetLevel()));
        vo.setTargetPostId(call.getTargetPostId());
        vo.setTargetPostName(call.getTargetPostName());
        vo.setTargetEmpId(call.getTargetEmpId());
        vo.setTargetEmpName(call.getTargetEmpName());
        vo.setTargetPhone(call.getTargetPhone());
        vo.setCallReason(call.getCallReason());
        vo.setPatientName(call.getPatientName());
        vo.setPatientLocation(call.getPatientLocation());
        vo.setCallTime(call.getCallTime());
        vo.setArriveTime(call.getArriveTime());
        vo.setStatus(call.getStatus());
        vo.setStatusText(DutyCallStatusEnum.getLabelOrUnknown(call.getStatus()));
        vo.setEscalateStatus(call.getEscalateStatus());
        vo.setEscalateTime(call.getEscalateTime());
        vo.setEscalateFromLevel(call.getEscalateFromLevel());
        vo.setEscalateFromLevelText(call.getEscalateFromLevel() == null ? null
                : DutyLevelEnum.getLabelOrUnknown(call.getEscalateFromLevel()));
        vo.setCancelReason(call.getCancelReason());
        vo.setTimeoutMinutes(timeoutMinutes);
        if (call.getCallTime() != null) {
            // 待到岗：时限从呼叫时间算
            // 已超时升级：时限从升级时间重算 —— 新接手的被叫人（三线/总值班）拿到的是
            //   自己的那一份时限，不是「上一任已经超掉的那个」，否则他看到的是负数倒计时。
            LocalDateTime deadline = Integer.valueOf(DutyCallStatusEnum.ESCALATED.getCode()).equals(call.getStatus())
                    && call.getEscalateTime() != null
                    ? call.getEscalateTime().plusMinutes(timeoutMinutes)
                    : call.getCallTime().plusMinutes(timeoutMinutes);
            vo.setDeadlineTime(deadline);
            vo.setRemainSeconds(DutyCallStatusEnum.isWaiting(call.getStatus())
                    || Integer.valueOf(DutyCallStatusEnum.ESCALATED.getCode()).equals(call.getStatus())
                    ? Duration.between(LocalDateTime.now(), deadline).getSeconds() : null);
        }
        // 待到岗(0) 与 已超时升级(2) 都还能应答/取消：升级后接手的正是当前被叫人
        boolean open = DutyCallStatusEnum.isWaiting(call.getStatus())
                || Integer.valueOf(DutyCallStatusEnum.ESCALATED.getCode()).equals(call.getStatus());
        vo.setCanArrive(open
                && currentEmpId != null && Objects.equals(call.getTargetEmpId(), currentEmpId));
        vo.setCanCancel(open
                && currentEmpId != null && Objects.equals(call.getCreateById(), currentEmpId));
        return vo;
    }

    private DutyCallLogVO toLogVO(BizDutyCallLog row) {
        DutyCallLogVO vo = new DutyCallLogVO();
        vo.setId(row.getId());
        vo.setCallId(row.getCallId());
        vo.setCallNo(row.getCallNo());
        vo.setActionType(row.getActionType());
        vo.setActionTypeText(DutyCallActionEnum.getLabelOrUnknown(row.getActionType()));
        vo.setActionTime(row.getActionTime());
        vo.setFromStatus(row.getFromStatus());
        vo.setFromStatusText(row.getFromStatus() == null ? null : DutyCallStatusEnum.getLabelOrUnknown(row.getFromStatus()));
        vo.setToStatus(row.getToStatus());
        vo.setToStatusText(row.getToStatus() == null ? null : DutyCallStatusEnum.getLabelOrUnknown(row.getToStatus()));
        vo.setOperatorId(row.getOperatorId());
        vo.setOperatorName(row.getOperatorName());
        vo.setActionNote(row.getActionNote());
        return vo;
    }

    /**
     * 当前时刻属于哪一段：白班 08:00-18:00，夜班 18:00-次日 08:00（与值班域同口径）。
     */
    private int inferShiftType(LocalDateTime at) {
        int hour = at.getHour();
        return (hour < DAY_START_HOUR || hour >= NIGHT_START_HOUR)
                ? DutyShiftTypeEnum.NIGHT.getCode() : DutyShiftTypeEnum.DAY.getCode();
    }

    private int configTimeoutMinutes() {
        String v = systemConfigFacadeService.getConfigValue(CONFIG_TIMEOUT_MINUTES, null);
        if (!TextUtil.hasText(v)) {
            return DEFAULT_TIMEOUT_MINUTES;
        }
        try {
            int n = Integer.parseInt(v.trim());
            return n > 0 ? n : DEFAULT_TIMEOUT_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("[听班呼叫] 参数 {} 值非法（{}），回落默认 {} 分钟", CONFIG_TIMEOUT_MINUTES, v, DEFAULT_TIMEOUT_MINUTES);
            return DEFAULT_TIMEOUT_MINUTES;
        }
    }

    private String deptNameOf(Long deptId) {
        try {
            DeptBriefVO dept = systemFacadeService.findDepartment(deptId);
            if (dept != null && TextUtil.hasText(dept.getDeptName())) {
                return dept.getDeptName();
            }
        } catch (Exception ex) {
            log.warn("[听班呼叫] 查科室名失败 deptId={}：{}", deptId, ex.getMessage());
        }
        return "科室" + deptId;
    }

    private String resolvePhone(BizDutyRoster row, Long empId) {
        if (TextUtil.hasText(row.getPhone())) {
            return row.getPhone();
        }
        try {
            EmployeeBriefVO emp = systemFacadeService.findEmployee(empId);
            return emp == null ? null : emp.getPhone();
        } catch (Exception ex) {
            return null;
        }
    }

    private DutyOfficerHolder currentDutyOfficer() {
        try {
            DutyOfficerVO vo = dutyRosterService.current();
            if (vo == null || vo.getFound() == null || vo.getFound() != 1) {
                return null;
            }
            return new DutyOfficerHolder(vo.getEmployeeId(), vo.getEmployeeName(), vo.getPhone());
        } catch (Exception ex) {
            log.warn("[听班呼叫] 解析当前总值班失败：{}", ex.getMessage());
            return null;
        }
    }

    private CurrentOperator currentOperator() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        CurrentOperator op = new CurrentOperator();
        op.id = user.getEmployeeId();
        op.name = TextUtil.hasText(user.getRealName()) ? user.getRealName() : user.getEmployeeName();
        op.employeeId = user.getEmployeeId();
        op.deptId = user.getDeptId();
        op.deptName = user.getDeptName();
        return op;
    }

    private Long currentEmployeeId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getEmployeeId();
        } catch (Exception e) {
            return null;
        }
    }

    private static int minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return 0;
        }
        return (int) Duration.between(from, to).toMinutes();
    }

    // ===== 小值对象 =====

    private static final class CurrentOperator {
        private Long id;
        private String name;
        private Long employeeId;
        private Long deptId;
        private String deptName;

        Long id() { return id; }
        String name() { return name; }
        Long employeeId() { return employeeId; }
        Long deptId() { return deptId; }
        String deptName() { return deptName; }
    }

    private record DutyOfficerHolder(Long employeeId, String employeeName, String phone) {
    }
}
