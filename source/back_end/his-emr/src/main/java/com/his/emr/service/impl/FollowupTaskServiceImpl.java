package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.AppointUpsertDTO;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.enums.AppointSourceEnum;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.appoint.enums.RevisitSourceEnum;
import com.his.appoint.enums.VisitTypeEnum;
import com.his.appoint.service.AppointService;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.SensitiveMaskUtil;
import com.his.emr.dto.FollowupQueryDTO;
import com.his.emr.dto.FollowupTaskDTO;
import com.his.emr.entity.BizFollowupTask;
import com.his.emr.enums.*;
import com.his.emr.mapper.BizFollowupTaskMapper;
import com.his.emr.service.FollowupTaskService;
import com.his.emr.service.SurveyService;
import com.his.emr.support.FollowupTaskSnapshot;
import com.his.emr.vo.BizFollowupTaskVO;
import com.his.emr.vo.FollowupStatVO;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 随访任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowupTaskServiceImpl extends ServiceImpl<BizFollowupTaskMapper, BizFollowupTask> implements FollowupTaskService {
    /**
     * 出院一键生成的默认随访天数
     */
    private static final int DEFAULT_DAYS_OFFSET = 7;
    /**
     * 任务上已挂的复诊号处于这些状态时允许重新生成 —— 号已经作废（退号/过号/爽约），
     * 患者确实还得再复诊一次；拦住会把任务永久卡死。
     */
    private static final Set<Integer> REGENERABLE_APPOINT_STATUS = Set.of(
            AppointStatusEnum.CANCELLED.getCode(),
            AppointStatusEnum.OVERDUE.getCode(),
            AppointStatusEnum.NO_SHOW.getCode());
    private final DeptScopeProvider deptScopeProvider;
    private final AppointService appointService;

    private final BizFollowupTaskMapper taskMapper;

    /**
     * 满意度评价：随访完成时自动发一张卷（方向单向，评价侧不回依赖本服务，见 FollowupTaskSnapshot）
     */
    private final SurveyService surveyService;

    /**
     * 患者触达（G-06）：站内信通道常通，微信订阅消息未启用时内部静默降级
     */
    private final com.his.system.service.SysMessageService sysMessageService;

    /**
     * 电话外呼通道（G-15）：mock=人工登记待呼，真实线路接入前绝不假装已呼出
     */
    private final com.his.emr.service.FollowupCallChannelService followupCallChannelService;

    private static long toLong(Map<String, Object> row, String key) {
        Object v = row == null ? null : row.get(key);
        return v == null ? 0L : new BigDecimal(String.valueOf(v)).longValue();
    }

    /**
     * 写库前先截到列宽：超长原因让服务端截断，而不是让 insert 报 Data too long 变成 500
     */
    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Override
    public PageResult<BizFollowupTaskVO> listPage(FollowupQueryDTO dto) {
        FollowupQueryDTO q = dto == null ? new FollowupQueryDTO() : dto;
        LambdaQueryWrapper<BizFollowupTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(q.getPatientId() != null, BizFollowupTask::getPatientId, q.getPatientId())
                .like(StringUtils.hasText(q.getPatientName()), BizFollowupTask::getPatientName,
                        q.getPatientName() == null ? null : q.getPatientName().trim())
                .eq(q.getFollowupType() != null, BizFollowupTask::getFollowupType, q.getFollowupType())
                .eq(q.getFollowupStatus() != null, BizFollowupTask::getFollowupStatus, q.getFollowupStatus());
        if (Boolean.TRUE.equals(q.getOverdueOnly())) {
            // 逾期是派生条件（状态 1/2 且计划时间已过），库里没有 also 不存在的「已逾期」状态
            wrapper.in(BizFollowupTask::getFollowupStatus, FollowupTaskStatusEnum.PENDING.getCode(), FollowupTaskStatusEnum.DOING.getCode())
                    .lt(BizFollowupTask::getFollowupTime, LocalDateTime.now());
        }
        List<Long> scope = scopedDeptIds(q.getDeptId());
        if (scope != null) {
            wrapper.in(BizFollowupTask::getDeptId, scope);
        }
        wrapper.orderByAsc(BizFollowupTask::getFollowupTime)
                // 同秒计划任务排序不稳定 → 补 id 二级键，否则翻页可能重复/丢行
                .orderByAsc(BizFollowupTask::getId);

        Page<BizFollowupTask> page = this.page(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<BizFollowupTaskVO> voList = page.getRecords().stream()
                .map(t -> toVo(t, false)).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public FollowupStatVO stat() {
        List<Long> scope = scopedDeptIds(null);
        FollowupStatVO vo = new FollowupStatVO();
        Map<String, Object> row = taskMapper.statOverview(scope);
        long pending = toLong(row, "pending");
        long doing = toLong(row, "doing");
        long done = toLong(row, "done");
        long cancelled = toLong(row, "cancelled");
        vo.setPendingCount(pending);
        vo.setDoingCount(doing);
        vo.setDoneCount(done);
        vo.setCancelledCount(cancelled);
        vo.setTotalCount(toLong(row, "total"));
        vo.setTodayDueCount(toLong(row, "today_due"));
        vo.setOverdueCount(toLong(row, "overdue"));
        vo.setDoneTodayCount(toLong(row, "done_today"));
        vo.setRevisitCount(toLong(row, "revisit_cnt"));
        long shouldDo = pending + doing + done;
        vo.setCompleteRate(shouldDo == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(done).multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(shouldDo), 1, RoundingMode.HALF_UP));

        // 类型分布必须四档全出（含 0）：GROUP BY 只回有任务的类型，看板上"少了两档"
        // 会被读成「本院没有这类随访」，而事实是这类今天恰好没活。
        Map<Integer, Long> typeCount = new HashMap<>();
        for (Map<String, Object> r : taskMapper.countByType(scope)) {
            typeCount.put((int) toLong(r, "k"), toLong(r, "c"));
        }
        // 按码值升序出，与旧 Map 键序一致（枚举声明序不等于码值序，不能直接 values()）
        List<FollowupStatVO.StatItem> byType = new ArrayList<>();
        List<FollowupTypeEnum> types = new ArrayList<>(Arrays.asList(FollowupTypeEnum.values()));
        types.sort(Comparator.comparingInt(FollowupTypeEnum::getCode));
        for (FollowupTypeEnum type : types) {
            int k = type.getCode();
            byType.add(new FollowupStatVO.StatItem(String.valueOf(k), type.getLabel(),
                    typeCount.getOrDefault(k, 0L)));
        }
        vo.setByType(byType);

        List<FollowupStatVO.DeptPending> byDept = new ArrayList<>();
        for (Map<String, Object> r : taskMapper.countDeptPending(scope)) {
            FollowupStatVO.DeptPending item = new FollowupStatVO.DeptPending();
            item.setDeptId(toLong(r, "d"));
            item.setDeptName(String.valueOf(r.get("n")));
            item.setPendingCount(toLong(r, "c"));
            item.setDoneCount(toLong(r, "done"));
            byDept.add(item);
        }
        vo.setByDeptPending(byDept);
        vo.setStatTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFollowupTaskVO upsertTask(FollowupTaskDTO.Upsert dto) {
        BizFollowupTask task;
        if (dto.getId() == null) {
            task = new BizFollowupTask();
            task.setTaskNo(nextTaskNo(null));
            task.setPatientId(dto.getPatientId());
            fillPatientSnapshot(task);
            task.setFollowupStatus(FollowupTaskStatusEnum.PENDING.getCode());
        } else {
            task = this.getById(dto.getId());
            if (task == null) {
                throw new BusinessException("随访任务不存在");
            }
            assertDeptAccessible(task.getDeptId());
            if (task.getFollowupStatus() != null && task.getFollowupStatus() != FollowupTaskStatusEnum.PENDING.getCode()) {
                throw new BusinessException("只有待随访的任务可以修改");
            }
            if (!Objects.equals(task.getPatientId(), dto.getPatientId())) {
                task.setPatientId(dto.getPatientId());
                fillPatientSnapshot(task);
            }
        }
        task.setFollowupType(dto.getFollowupType());
        task.setFollowupTime(dto.getFollowupTime().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        task.setFollowupContent(dto.getFollowupContent());
        if (StringUtils.hasText(dto.getPhone())) {
            task.setPhone(dto.getPhone().trim());
        }
        if (dto.getRemark() != null) {
            task.setRemark(dto.getRemark());
        }
        String who = UserUtils.getCurrentEmployeeName();
        if (dto.getId() == null) {
            task.setCreateBy(who);
            this.save(task);
        } else {
            task.setUpdateBy(who);
            task.setUpdateTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            this.updateById(task);
        }
        return toVo(task, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFollowupTask createFromDischarge(FollowupTaskDTO.FromDischarge dto) {
        Map<String, Object> snap = taskMapper.selectDischargeSnapshot(dto.getDischargeId());
        if (snap == null || snap.get("patientId") == null) {
            throw new BusinessException("出院记录不存在（dischargeId=" + dto.getDischargeId() + "）");
        }
        // 幂等：同一出院记录已生成过任务（remark 前缀锚定 dischargeId）则原样返回，不重复生成。
        // ⚠ 幂等锚点不进 task_no：task_no 只有 VARCHAR(32)，嵌入 19 位雪花 ID 必截断（Data too long）。
        String anchor = "G20-FUV:" + dto.getDischargeId() + "|";
        BizFollowupTask existed = this.getOne(new LambdaQueryWrapper<BizFollowupTask>()
                .likeRight(BizFollowupTask::getRemark, anchor)
                .orderByDesc(BizFollowupTask::getId)
                .last("LIMIT 1"), false);
        if (existed != null) {
            return existed;
        }

        LocalDateTime dischargeTime = LocalDateTime.parse((String) snap.get("dischargeTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int days = dto.getDaysOffset() == null || dto.getDaysOffset() < 1 ? DEFAULT_DAYS_OFFSET : dto.getDaysOffset();
        int type = dto.getFollowupType() == null ? 1 : dto.getFollowupType();

        BizFollowupTask task = new BizFollowupTask();
        task.setTaskNo(nextTaskNo(null));
        task.setPatientId(((Number) snap.get("patientId")).longValue());
        task.setPatientNo((String) snap.get("patientNo"));
        task.setPatientName((String) snap.get("patientName"));
        task.setPhone((String) snap.get("phone"));
        task.setDeptId(snap.get("deptId") == null ? null : ((Number) snap.get("deptId")).longValue());
        task.setDeptName((String) snap.get("deptName"));
        String diagnosis = (String) snap.get("diagnosis");
        task.setDiagnosis(diagnosis);
        task.setFollowupType(type);
        task.setFollowupTime(dischargeTime.plusDays(days).truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        task.setFollowupContent(StringUtils.hasText(dto.getFollowupContent())
                ? dto.getFollowupContent() : buildDefaultContent(type, diagnosis));
        task.setFollowupStatus(FollowupTaskStatusEnum.PENDING.getCode());
        task.setRemark(anchor + "按出院记录 " + snap.get("dischargeNo") + " 自动生成（出院后 " + days + " 天）");
        task.setCreateBy(UserUtils.getCurrentEmployeeName());
        this.save(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFollowupTaskVO createTaskFromDischarge(FollowupTaskDTO.FromDischarge dto) {
        return toVo(createFromDischarge(dto), true);
    }

    @Override
    public int autoCreateFromDischarge(int limit) {
        List<Map<String, Object>> rows = taskMapper.selectDischargesWithoutTask(limit);
        int created = 0;
        for (Map<String, Object> row : rows) {
            Long dischargeId = row.get("dischargeId") == null ? null : ((Number) row.get("dischargeId")).longValue();
            if (dischargeId == null) {
                continue;
            }
            try {
                FollowupTaskDTO.FromDischarge dto = new FollowupTaskDTO.FromDischarge();
                dto.setDischargeId(dischargeId);
                dto.setFollowupType(toLong(row, "hasOperation") > 0 ? 4 : 1);
                createFromDischarge(dto);
                created++;
            } catch (Exception e) {
                // 单条失败只留痕：欠账仍在下一轮的扫描缺口里，不能因为一个人建不了就整批停住
                log.warn("[出院随访] dischargeId={} 自动补建随访计划失败：{}", dischargeId, e.getMessage());
            }
        }
        if (created > 0) {
            log.info("[出院随访] 自动补建随访计划 {} 条（本轮待补 {} 条）", created, rows.size());
        }
        return created;
    }

    @Override
    public BizFollowupTaskVO getFollowupTaskDetail(Long taskId) {
        return toVo(loadTask(taskId), true);
    }

    private BizFollowupTask loadTask(Long taskId) {
        BizFollowupTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException("随访任务不存在");
        }
        assertDeptAccessible(task.getDeptId());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startFollowup(Long taskId, Long executorId, String executorName) {
        BizFollowupTask task = loadTask(taskId);
        if (!Integer.valueOf(FollowupTaskStatusEnum.PENDING.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("只有待随访的任务可以开始随访（当前状态：" + statusName(task.getFollowupStatus()) + "）");
        }

        task.setFollowupStatus(FollowupTaskStatusEnum.DOING.getCode());
        task.setExecutorId(executorId == null ? UserUtils.getCurrentEmployeeId() : executorId);
        task.setExecutorName(StringUtils.hasText(executorName) ? executorName : UserUtils.getCurrentEmployeeName());
        boolean updated = this.updateById(task);
        if (updated) {
            // 患者触达（G-06）：开始随访即把随访内容推给患者，患者可在小程序「我的随访」反馈。
            // 发信失败不影响随访推进（旁路），与满意度问卷同一个姿态。
            try {
                // 展示口径走枚举 getText；脏码值不回落成某个合法类型名，退回中性文案「随访」
                String typeText = FollowupTypeEnum.getText(task.getFollowupType());
                sysMessageService.sendWechatToPatient(task.getPatientId(), "followup_started",
                        "pages/followup/followup",
                        Map.of("随访类型", StringUtils.hasText(typeText) ? typeText : "随访"),
                        "随访通知",
                        StringUtils.hasText(task.getFollowupContent())
                                ? task.getFollowupContent() : "您有一份随访计划，请查看详情并反馈近况",
                        "followup", task.getId());
            } catch (Exception e) {
                log.warn("[随访] 任务 {} 患者触达发送失败（不影响随访）：{}", task.getTaskNo(), e.getMessage());
            }
        }
        return updated;
    }

    /**
     * 完成随访：置终态 + 自动发放满意度问卷。
     *
     * <p>发卷失败不回滚随访：打没打通、填没填是评价域的事，把「电话已打完」这个既成事实
     * 因为旁路配置缺失（没配问卷）而退回待随访，等于让护士再打一遍。失败只留痕。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeFollowup(Long taskId, String result) {
        BizFollowupTask task = loadTask(taskId);
        if (!Integer.valueOf(FollowupTaskStatusEnum.DOING.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("只有随访中的任务可以完成（当前状态：" + statusName(task.getFollowupStatus()) + "）");
        }

        task.setFollowupStatus(FollowupTaskStatusEnum.DONE.getCode());
        task.setExecuteTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        task.setExecuteResult(result);
        boolean updated = this.updateById(task);
        try {
            surveyService.issueForFollowup(FollowupTaskSnapshot.of(task),
                    SurveySourceEnum.FOLLOWUP.getCode(), null, null);
        } catch (Exception e) {
            log.warn("[随访] 任务 {} 完成，但满意度问卷发放失败（不影响随访结果）：{}", task.getTaskNo(), e.getMessage());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelFollowup(Long taskId, String reason) {
        BizFollowupTask task = loadTask(taskId);
        if (Integer.valueOf(FollowupTaskStatusEnum.DONE.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("已完成的任务不允许取消");
        }

        task.setFollowupStatus(FollowupTaskStatusEnum.CANCELLED.getCode());
        task.setRemark(StringUtils.hasText(reason) ? cut(reason, 512) : "已取消");
        return this.updateById(task);
    }

    @Override
    public List<BizFollowupTaskVO> listForPatient(Long patientId) {
        // 患者端只按任务上的 patientId 收窄：没有科室岗位概念，不做科室收口
        List<BizFollowupTask> tasks = this.list(new LambdaQueryWrapper<BizFollowupTask>()
                .eq(BizFollowupTask::getPatientId, patientId)
                .orderByDesc(BizFollowupTask::getFollowupTime)
                .orderByDesc(BizFollowupTask::getId));
        return tasks.stream().map(t -> toVo(t, false)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyFromPatient(Long taskId, Long patientId, String replyText) {
        if (!StringUtils.hasText(replyText)) {
            throw new BusinessException("反馈内容不能为空");
        }
        BizFollowupTask task = this.getById(taskId);
        // 归属第一道闸：任务必须真的属于该患者，改 taskId 就能替别人写反馈是事故
        if (task == null || !Objects.equals(task.getPatientId(), patientId)) {
            throw new BusinessException("随访任务不存在");
        }
        if (Integer.valueOf(FollowupTaskStatusEnum.CANCELLED.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("已取消的随访任务不再收集反馈");
        }
        task.setPatientReply(cut(replyText, 500));
        task.setPatientReplyTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFollowupTaskVO registerCall(Long taskId) {
        BizFollowupTask task = loadTask(taskId);
        if (Integer.valueOf(FollowupTaskStatusEnum.CANCELLED.getCode()).equals(task.getFollowupStatus())
                || Integer.valueOf(FollowupTaskStatusEnum.DONE.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("已取消或已完成的随访任务不再外呼");
        }
        if (Integer.valueOf(FollowupCallStatusEnum.WAITING.getCode()).equals(task.getCallStatus())) {
            throw new BusinessException("该任务已登记待外呼，请拨打后回填结果");
        }
        FollowupCallChannelEnum channel = followupCallChannelService.dial(task);
        task.setCallChannel(channel.getCode());
        task.setCallStatus(FollowupCallStatusEnum.WAITING.getCode());
        task.setCallTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        task.setCallAttempts(task.getCallAttempts() == null ? 1 : task.getCallAttempts() + 1);
        touch(task);
        this.updateById(task);
        return toVo(task, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordCallResult(com.his.emr.dto.FollowupCallResultDTO dto) {
        BizFollowupTask task = loadTask(dto.getId());
        if (!Integer.valueOf(FollowupCallStatusEnum.WAITING.getCode()).equals(task.getCallStatus())) {
            throw new BusinessException("只有待外呼的任务可以回填外呼结果");
        }
        boolean connected = Boolean.TRUE.equals(dto.getConnected());
        task.setCallStatus(connected ? FollowupCallStatusEnum.CONNECTED.getCode()
                : FollowupCallStatusEnum.NO_ANSWER.getCode());
        task.setCallTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        // 追加不覆盖：出院自动生成的任务靠 remark 前缀锚定幂等，整列覆写会把锚洗掉
        if (StringUtils.hasText(dto.getRemark())) {
            String extra = "[外呼" + (connected ? "已接通" : "未接通") + "] " + cut(dto.getRemark(), 200);
            task.setRemark(cut(StringUtils.hasText(task.getRemark())
                    ? task.getRemark() + "\n" + extra : extra, 512));
        }
        touch(task);
        boolean updated = this.updateById(task);
        // 接通 = 电话里确认了随访事实 → 任务转随访中，走既有 startFollowup（执行人 + 患者触达链路同口径）
        if (updated && connected
                && Integer.valueOf(FollowupTaskStatusEnum.PENDING.getCode()).equals(task.getFollowupStatus())) {
            startFollowup(task.getId(), null, null);
        }
        return updated;
    }

    private void touch(BizFollowupTask task) {
        task.setUpdateBy(UserUtils.getCurrentEmployeeName());
        task.setUpdateTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
    }

    /**
     * 随访任务 -> 复诊号（复诊来源 4）。
     *
     * <p>建号一律走 {@code AppointService#addAppoint}，不在随访侧另写一套：
     * 号源扣减、收费策略判定、判重、「0 元也要建收费单」这些口径全在那条路径上，
     * 复制一份迟早漂移。这里只补「同一个任务是否已生成过」这道任务侧闸门。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BizFollowupTaskVO createRevisitAppoint(FollowupTaskDTO.CreateRevisit dto) {
        BizFollowupTask task = loadTask(dto.getTaskId());
        if (Integer.valueOf(FollowupTaskStatusEnum.CANCELLED.getCode()).equals(task.getFollowupStatus())) {
            throw new BusinessException("已取消的随访任务不再生成复诊号");
        }
        if (task.getRevisitAppointId() != null) {
            BizAppointInfo previous = appointService.getById(task.getRevisitAppointId());
            // 只挡「那张号还有效」：已退号/过号/爽约的号作废了，患者确实还得再约一次，
            // 一刀切拦住会让这个任务永久卡死，只能去库里改数据。
            if (previous != null && !REGENERABLE_APPOINT_STATUS.contains(previous.getRegistStatus())) {
                throw new BusinessException("该随访任务已生成复诊号（" + previous.getRegistNo() + "），请勿重复操作");
            }
        }

        AppointUpsertDTO appointDTO = new AppointUpsertDTO();
        appointDTO.setPatientId(task.getPatientId());
        appointDTO.setScheduleId(dto.getScheduleId());
        appointDTO.setSlotId(dto.getSlotId());
        appointDTO.setVisitType(VisitTypeEnum.REVISIT.getCode());
        appointDTO.setRevisitSource(RevisitSourceEnum.FOLLOWUP_PLAN.getCode());
        appointDTO.setRevisitRecordId(dto.getRevisitRecordId());
        // 院内人员按计划代约 -> 挂号渠道 4（预约）：扣预约池，不吃当天现场号
        appointDTO.setRegistSource(AppointSourceEnum.APPOINTMENT.getCode());
        appointDTO.setSettlementType(dto.getSettlementType() == null ? 1 : dto.getSettlementType());
        BizAppointInfo appoint = appointService.addAppoint(appointDTO);

        task.setRevisitRecordId(dto.getRevisitRecordId());
        task.setRevisitAppointId(appoint.getId());
        task.setUpdateBy(UserUtils.getCurrentEmployeeName());
        task.setUpdateTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        this.updateById(task);
        return toVo(task, true);
    }

    /**
     * @param plainPhone 编辑回显给明文，列表一律脱敏并清空明文
     */
    private BizFollowupTaskVO toVo(BizFollowupTask entity, boolean plainPhone) {
        BizFollowupTaskVO vo = new BizFollowupTaskVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setPhoneMasked(SensitiveMaskUtil.maskPhone(entity == null ? null : entity.getPhone()));
        if (!plainPhone) {
            vo.setPhone(null);
        }
        if (entity != null) {
            vo.setOverdue(isOverdue(entity));
        }
        return vo;
    }

    /**
     * 逾期 = 还没做完且计划时间已过，现算不落列（状态列没人翻，数出来永远偏小）
     */
    private boolean isOverdue(BizFollowupTask task) {
        Integer status = task.getFollowupStatus();
        boolean open = Integer.valueOf(FollowupTaskStatusEnum.PENDING.getCode()).equals(status)
                || Integer.valueOf(FollowupTaskStatusEnum.DOING.getCode()).equals(status);
        return open && task.getFollowupTime() != null && task.getFollowupTime().isBefore(LocalDateTime.now());
    }

    /**
     * 手动新建：FUV+yyyyMMddHHmmss+3 位随机（task_no VARCHAR(32)：3+14+3=20，安全余量足够）
     */
    private String nextTaskNo(Long dischargeId) {
        String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        return "FUV" + ts + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    /**
     * 患者快照：号/名/电话/科室一律服务端重查，不信任前端传值。
     *
     * <p>科室取患者「最近就诊科室」；手工新建时患者还没有就诊科室（门诊新档案）就落到
     * 当前登录岗位所在科室 —— 没有科室的任务对受限角色永久不可见，等于把人刚建的单子藏起来。
     */
    private void fillPatientSnapshot(BizFollowupTask task) {
        Map<String, Object> snap = taskMapper.selectPatientSnapshot(task.getPatientId());
        if (snap == null) {
            throw new BusinessException("患者不存在（patientId=" + task.getPatientId() + "）");
        }
        task.setPatientNo((String) snap.get("patientNo"));
        task.setPatientName((String) snap.get("patientName"));
        if (!StringUtils.hasText(task.getPhone())) {
            task.setPhone((String) snap.get("phone"));
        }
        if (snap.get("deptId") != null) {
            task.setDeptId(((Number) snap.get("deptId")).longValue());
            task.setDeptName((String) snap.get("deptName"));
        } else {
            Long ownDept = UserUtils.getCurrentUser() == null ? null : UserUtils.getCurrentUser().getDeptId();
            task.setDeptId(ownDept);
            task.setDeptName(ownDept == null ? null : taskMapper.selectDeptName(ownDept));
        }
    }

    private String buildDefaultContent(int type, String diagnosis) {
        String d = StringUtils.hasText(diagnosis) ? diagnosis : "出院诊断待补录";
        return switch (type) {
            case 2 -> "慢病随访：请复诊评估「" + d + "」控制情况，遵医嘱规律用药";
            case 3 -> "用药指导：出院带药用法确认与不良反应随访（" + d + "）";
            case 4 -> "术后随访：伤口愈合与功能恢复情况随访（" + d + "）";
            default -> "复诊提醒：出院后请按医嘱复查（" + d + "）";
        };
    }

    private void assertDeptAccessible(Long deptId) {
        if (!deptScopeProvider.canAccessDept(deptId)) {
            throw new BusinessException("该随访任务所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * null=不限科室；非空=收口集合（保证非空，IN () 是语法错误，空集合一律当配置缺失拒掉）
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeProvider.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return List.of(resolved);
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看随访数据（请在系统管理为岗位分配科室）");
        }
        return List.copyOf(allowed);
    }

    private String statusName(Integer status) {
        String label = FollowupTaskStatusEnum.getText(status);
        return label == null ? "未知" : label;
    }
}
