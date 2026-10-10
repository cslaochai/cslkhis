package com.his.appoint.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.*;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.entity.BizTriageRecord;
import com.his.appoint.enums.*;
import com.his.appoint.enums.OpdLogStatusEnum;
import com.his.appoint.mapper.*;
import com.his.appoint.service.DoctorStatusCacheService;
import com.his.appoint.service.BizQueueService;
import com.his.appoint.service.BizScheduleService;
import com.his.appoint.trigger.DayEndSettleTrigger;
import com.his.appoint.vo.*;
import com.his.charge.api.AppointChargeGateway;
import com.his.common.base.PageResult;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.patient.service.BizPatientService;
import com.his.patient.service.PatientGuardianService;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysClinicRoom;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.InsurancePolicyService;
import com.his.system.service.SysClinicRoomService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 挂号服务实现
 */
@Service
@RequiredArgsConstructor
public class BizQueueServiceImpl extends ServiceImpl<BizQueueMapper, BizQueue> implements BizQueueService {
    private final BizPatientService bizPatientService;

    private final BizScheduleMapper bizScheduleMapper;

    private final RedisSequenceService redisSequenceService;

    private final BizAppointInfoMapper bizAppointInfoMapper;

    private final ObjectProvider<AppointChargeGateway> appointChargeGateway;

    private final InsurancePolicyService insurancePolicyService;

    private final DoctorStatusCacheService doctorStatusCacheService;

    private final PatientGuardianService patientGuardianService;

    private final SysClinicRoomService sysClinicRoomService;

    private final OpdLogMapper opdLogMapper;

    private final BizTriageRecordMapper bizTriageRecordMapper;

    private final DayEndSettleTrigger dayEndSettleTrigger;

    private final SysMessageService sysMessageService;

    @Lazy
    private final BizScheduleService bizScheduleService;

    private final DeptScopeService deptScopeService;

    @Override
    public List<BizQueueListVO> getTodayQueueList(QueueTodayQueryDTO queueQueryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null || currentUser.getDeptId() == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQueue::getDeptId, currentUser.getDeptId())
                .eq(BizQueue::getDoctorId, currentUser.getEmployeeId())
                .eq(BizQueue::getVisitDate, LocalDate.now());
        wrapper.orderByAsc(BizQueue::getSequenceNo);
        List<BizQueue> list = baseMapper.selectList(wrapper);
        List<BizQueueListVO> resultList = new ArrayList<>(list.size());
        for (BizQueue queue : list) {
            BizQueueListVO vo = toBizQueueListVO(queue);
            if (vo != null) {
                resultList.add(vo);
            }
        }
        return resultList;
    }

    private BizQueueListVO toBizQueueListVO(BizQueue queue) {
        if (queue == null || queue.getRegistId() == null) {
            return null;
        }
        BizQueueListVO vo = BeanUtil.copyProperties(queue, BizQueueListVO.class);
        BizAppointInfo registInfo = bizAppointInfoMapper.selectById(queue.getRegistId());
        if (registInfo != null) {
            vo.setRegistNo(registInfo.getRegistNo());
            vo.setGender(registInfo.getGender());
            vo.setAge(registInfo.getAge());
            vo.setSettlementType(registInfo.getSettlementType());
            vo.setVisitType(registInfo.getVisitType());
            vo.setRevisitRecordId(registInfo.getRevisitRecordId());
            vo.setMedicalInsuranceType(registInfo.getMedicalInsuranceType());
            vo.setRegistStatus(registInfo.getRegistStatus());
        }
        fillRoomFromSchedule(vo, registInfo);
        return vo;
    }

    /**
     * 诊室快照的权威来源 = <b>排班</b>（医生-诊室绑定在排班上，不在分诊台）。
     *
     * <p>队列行上的诊室ID 是<b>签到入队时</b>写下的排班快照；本次改造之前入队
     * 的行没有这个快照，读出来是空的。读路径按挂号回查一次排班补齐<b>显示</b>，不回写数据库 ——
     * 写路径（签到入队 / 改排班）才是入口。诊室是「患者该去哪个房间」，显示成空会让
     * 护士以为这人没诊室可去。
     */
    private void fillRoomFromSchedule(BizQueueListVO vo, BizAppointInfo registInfo) {
        if (vo.getRoomId() != null || registInfo == null || registInfo.getScheduleId() == null) {
            return;
        }
        BizSchedule schedule = bizScheduleMapper
                .selectById(registInfo.getScheduleId());
        if (schedule != null) {
            vo.setRoomId(schedule.getRoomId());
            vo.setRoomName(schedule.getRoomName());
        }
    }

    @Override
    public PageResult<BizQueueListVO> listPage(QueueQueryDTO queueQueryDTO) {
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        List<Long> scoped = deptScopeService.scopedDeptIds(queueQueryDTO.getDeptId());
        if (scoped != null) {
            wrapper.in(BizQueue::getDeptId, scoped);
        }

        // 状态：多选优先于单选
        if (queueQueryDTO.getQueueStatuses() != null && !queueQueryDTO.getQueueStatuses().isEmpty()) {
            wrapper.in(BizQueue::getQueueStatus, queueQueryDTO.getQueueStatuses());
        } else {
            wrapper.eq(queueQueryDTO.getQueueStatus() != null, BizQueue::getQueueStatus, queueQueryDTO.getQueueStatus());
        }
        wrapper.eq(queueQueryDTO.getDoctorId() != null, BizQueue::getDoctorId, queueQueryDTO.getDoctorId());
        wrapper.eq(Boolean.TRUE.equals(queueQueryDTO.getUnTriageOnly()), BizQueue::getTriageStatus, 0);

        // 日期口径分两种：visit_date 是「今天该看谁」，arrive_time 是「今天几点到的」。
        // 跨日遗留的队列（昨天入队今天还没走完）只有前者能正确排除。
        if (TextUtil.hasText(queueQueryDTO.getVisitDate())) {
            wrapper.eq(BizQueue::getVisitDate, queueQueryDTO.getVisitDate());
        }
        if (TextUtil.hasText(queueQueryDTO.getStartTime()) && TextUtil.hasText(queueQueryDTO.getEndTime())) {
            wrapper.ge(BizQueue::getArriveTime, queueQueryDTO.getStartTime())
                    .le(BizQueue::getArriveTime, queueQueryDTO.getEndTime());
        } else if (TextUtil.hasText(queueQueryDTO.getDate())) {
            wrapper.ge(BizQueue::getArriveTime, queueQueryDTO.getDate() + " 00:00:00")
                    .le(BizQueue::getArriveTime, queueQueryDTO.getDate() + " 23:59:59");
        }

        if (TextUtil.hasText(queueQueryDTO.getKeyword())) {
            String kw = queueQueryDTO.getKeyword().trim();
            List<Long> registIds = bizAppointInfoMapper.selectList(new LambdaQueryWrapper<BizAppointInfo>()
                            .select(BizAppointInfo::getId)
                            .like(BizAppointInfo::getRegistNo, kw)
                            .last("LIMIT 500"))
                    .stream().map(BizAppointInfo::getId).collect(Collectors.toList());
            wrapper.and(w -> {
                w.like(BizQueue::getPatientName, kw)
                        .or().like(BizQueue::getPatientNo, kw)
                        .or().like(BizQueue::getQueueNo, kw);
                if (!registIds.isEmpty()) {
                    w.or().in(BizQueue::getRegistId, registIds);
                }
            });
        }

        wrapper.last("ORDER BY IFNULL(triage_level, 4) ASC, sequence_no ASC");

        Page<BizQueue> page = this.page(new Page<>(queueQueryDTO.getPageNum(), queueQueryDTO.getPageSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return new PageResult<>();
        }

        List<BizQueueListVO> resultList = new ArrayList<>(page.getRecords().size());
        for (BizQueue queue : page.getRecords()) {
            resultList.add(toListVO(queue));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), resultList);
    }

    /**
     * 队列实体 → 列表 VO，补齐挂号侧快照与分诊等级文案。
     */
    private BizQueueListVO toListVO(BizQueue queue) {
        BizQueueListVO vo = BeanUtil.copyProperties(queue, BizQueueListVO.class);
        vo.setTriageLevelText(TriageLevelEnum.getText(queue.getTriageLevel()));
        BizAppointInfo registInfo = queue.getRegistId() != null
                ? bizAppointInfoMapper.selectById(queue.getRegistId()) : null;
        if (registInfo != null) {
            vo.setRegistNo(registInfo.getRegistNo());
            vo.setGender(registInfo.getGender());
            vo.setAge(registInfo.getAge());
            vo.setSettlementType(registInfo.getSettlementType());
            vo.setVisitType(registInfo.getVisitType());
            // 批次E/E6：复诊带回原病历ID，医生站据此展示「复诊 · 关联原病历」
            vo.setRevisitRecordId(registInfo.getRevisitRecordId());
            vo.setMedicalInsuranceType(registInfo.getMedicalInsuranceType());
            vo.setRegistStatus(registInfo.getRegistStatus());
        }
        fillRoomFromSchedule(vo, registInfo);
        return vo;
    }

    @Override
    public Long patientIdOfRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        BizAppointInfo appointInfo = bizAppointInfoMapper.selectById(registId);
        return appointInfo == null ? null : appointInfo.getPatientId();
    }

    private boolean isRegistFeeSettled(AppointChargeGateway gateway, BizAppointInfo appoint) {
        if (appoint.getBillId() == null) {
            return true;
        }
        if (gateway == null) {
            return false;
        }
        AppointChargeGateway.BillBrief bill = gateway.getBill(appoint.getBillId());
        return bill != null && BillStatusEnum.PAID.getCode().equals(bill.getBillStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkInByRegistId(AppointCheckInUpdateDTO updateDTO) {
        BizAppointInfo appointInfo = bizAppointInfoMapper.selectById(updateDTO.getRegistId());
        if (appointInfo == null) {
            throw new BusinessException("挂号记录不存在");
        }
        deptScopeService.assertDeptAccessible(appointInfo.getDeptId());
        // 缴费门禁读的是账单状态（L2），不是支付状态列：账单是否付清由收款流水现算，
        // 所以「免收 = 压根没有账单」这条路径不需要任何假单据就能过。
        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        if (appointInfo.getBillId() != null && gateway == null) {
            throw new BusinessException("收费服务不可用，无法完成签到");
        }
        if (!isRegistFeeSettled(gateway, appointInfo)) {
            throw new BusinessException("该患者尚未支付，请先完成缴费");
        }
        // 检查是否已签到。按 (registId + 就诊日期) 判重：
        // 只按 registId 时，历史脚本造的脏数据（同一 registId 挂多行队列）会把
        // 「今天重新签到」误判成「已签到」；带上 visit_date 口径才与签到语义一致。
        LocalDate visitDate = appointInfo.getVisitDate() != null
                ? appointInfo.getVisitDate()
                : LocalDate.now();
        // 就诊日必须就是今天。签到的产物是「今天这个医生队列里的一行」，
        LocalDate today = LocalDate.now();
        if (!visitDate.equals(today)) {
            throw new BusinessException("该挂号就诊日为 " + visitDate + "，"
                    + (visitDate.isBefore(today) ? "已过期，请办理改约或退号重挂" : "尚未到就诊日，不能提前签到"));
        }
        LambdaQueryWrapper<BizQueue> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizQueue::getRegistId, updateDTO.getRegistId())
                .eq(BizQueue::getVisitDate, visitDate);
        Long existCount = baseMapper.selectCount(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("该患者已签到");
        }

        // 生成排队号（以诊室编号为前缀）
        String queueNo = generateQueueNo(appointInfo.getDeptId(), appointInfo.getScheduleId());

        // 创建排队记录
        BizQueue queue = new BizQueue();
        queue.setQueueNo(queueNo);
        queue.setRegistId(appointInfo.getId());
        queue.setVisitDate(visitDate);
        queue.setPatientId(appointInfo.getPatientId());
        queue.setPatientNo(appointInfo.getPatientNo());
        queue.setPatientName(appointInfo.getPatientName());
        queue.setDeptId(appointInfo.getDeptId());
        queue.setDeptName(appointInfo.getDeptName());
        queue.setDoctorId(appointInfo.getDoctorId());
        queue.setDoctorName(appointInfo.getDoctorName());
        queue.setSequenceNo(extractSequenceNo(queueNo));
        queue.setQueueType(QueueTypeEnum.NORMAL.getCode());
        queue.setQueueStatus(QueueStatusEnum.WAITING.getCode());
        queue.setArriveTime(LocalDateTime.now());
        queue.setTriageLevel(TriageLevelEnum.NON_URGENT.getCode());
        queue.setTriageStatus(0);
        BizSchedule schedule = appointInfo.getScheduleId() != null
                ? bizScheduleMapper.selectById(appointInfo.getScheduleId())
                : null;
        if (schedule != null) {
            queue.setRoomId(schedule.getRoomId());
            queue.setRoomName(schedule.getRoomName());
        }
        baseMapper.insert(queue);

        // 更新挂号状态为已签到
        appointInfo.setRegistStatus(AppointStatusEnum.CHECKED_IN.getCode());
        appointInfo.setArriveTime(LocalDateTime.now());
        bizAppointInfoMapper.updateById(appointInfo);
        return true;
    }

    /**
     * 生成排队号：呼叫代号（1~2 字母，来自诊室） + "-" + 3位当日序号。
     * 例：A001、BA042。不按旧模型的诊室编号（如 "10000101-01"），后者太长不好念。
     */
    private String generateQueueNo(Long deptId, Long scheduleId) {
        String prefix = null;

        // 根据排班ID获取诊室信息，取呼叨代号
        if (scheduleId != null) {
            BizScheduleMapper scheduleMapper = bizScheduleMapper;
            BizSchedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule != null && schedule.getRoomId() != null) {
                SysClinicRoom room = sysClinicRoomService.getById(schedule.getRoomId());
                if (room != null && TextUtil.hasText(room.getQueuePrefix())) {
                    prefix = room.getQueuePrefix();
                }
            }
        }

        // 兜底：前缀绝不能为空。队列号用 D{deptId}（如 D19580005），保证不同科室不串号。
        if (!TextUtil.hasText(prefix)) {
            prefix = "D" + deptId;
        }

        // 生成序号（当天该诊室的序号，Redis 递增）
        String dateStr = LocalDate.now().toString().replace("-", "").substring(4);
        long seq = redisSequenceService.next("queue:room:" + prefix + ":" + dateStr);
        return prefix + String.format("%03d", seq);
    }

    /**
     * 从排队号中提取序号。兼容两种格式：
     * - 旧格式：roomCode-seq（含 `-`），取最后一个 `-` 后的数字
     * - 新格式：prefix + seq（无 `-`），直接取末尾连续数字
     */
    private Integer extractSequenceNo(String queueNo) {
        if (queueNo == null || queueNo.isEmpty()) {
            return 0;
        }
        if (queueNo.contains("-")) {
            try {
                return Integer.parseInt(queueNo.substring(queueNo.lastIndexOf("-") + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        // 新格式：前缀(字母) + 序号(数字)，如 A001
        // 从后往前找第一个非数字位
        int i = queueNo.length() - 1;
        while (i >= 0 && Character.isDigit(queueNo.charAt(i))) {
            i--;
        }
        if (i < queueNo.length() - 1) {
            try {
                return Integer.parseInt(queueNo.substring(i + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueCallNextVO callNext(Long deptId, Long doctorId) {
        deptScopeService.assertDeptAccessible(deptId);
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQueue::getDeptId, deptId)
                .eq(BizQueue::getDoctorId, doctorId)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(BizQueue::getVisitDate, LocalDate.now())
                .isNotNull(BizQueue::getRegistId)
                .last("ORDER BY IFNULL(triage_level, 4) ASC, sequence_no ASC LIMIT 1");
        BizQueue queue = baseMapper.selectOne(wrapper);
        if (queue == null) {
            // 区分「我的队真空了」和「我有候诊但取号失败」—— 后者多是并发或数据异常，
            // 笼统报「没有候诊患者」会让医生以为今天没人挂号。
            Long mineWaiting = baseMapper.selectCount(new LambdaQueryWrapper<BizQueue>()
                    .eq(BizQueue::getDeptId, deptId)
                    .eq(BizQueue::getDoctorId, doctorId)
                    .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .eq(BizQueue::getVisitDate, LocalDate.now()));
            if (mineWaiting != null && mineWaiting > 0) {
                throw new BusinessException("您今日有 " + mineWaiting + " 位候诊患者但未能取号，请刷新队列");
            }
            throw new BusinessException("您当前没有候诊患者（患者需先在分诊站签到入队）");
        }

        LocalDateTime now = LocalDateTime.now();
        // CAS 抢占：条件里带 queue_status = 2。医生站与分诊台是两个终端，
        // 不带状态条件时两边能同时「接诊成功」——同一个患者被两条接诊流程各写一遍病历。
        LambdaUpdateWrapper<BizQueue> grab = new LambdaUpdateWrapper<>();
        grab.eq(BizQueue::getId, queue.getId())
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .set(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(BizQueue::getCallTime, now)
                .set(BizQueue::getStartTime, now)
                .set(BizQueue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被分诊台或其他终端接走，请刷新队列后重试");
        }

        // 抢占成功后再收上一位：同一医生同一时刻只允许一条「就诊中」。
        BizQueue previous = closeConsultingOf(deptId, doctorId, queue.getId());
        if (doctorId != null) {
            // 更新医生接诊状态到Redis
            doctorStatusCacheService.startConsulting(doctorId);
        }
        // 订阅消息口子（小程序一期）：叫号提醒。通道未启用/患者未注册时实现内部跳过，失败不外抛。
        try {
            sysMessageService.sendWechatToPatient(queue.getPatientId(), "queue_called",
                    "pages/queue/queue",
                    Map.of("科室", queue.getDeptName() == null ? "" : queue.getDeptName(),
                            "序号", queue.getSequenceNo() == null ? "" : String.valueOf(queue.getSequenceNo())),
                    "叫号提醒",
                    "轮到您就诊了，请前往诊室",
                    "queue", queue.getId());
        } catch (Exception e) {
            // 这里的 log 是父类 ServiceImpl 的 org.apache.ibatis.logging.Log，不支持 {} 占位符
            log.warn("叫号订阅消息发送失败 queueId=" + queue.getId() + " err=" + e.getMessage());
        }
        return buildCallNextVO(queue, previous, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueCallNextVO callSpecific(Long queueId) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() != QueueStatusEnum.WAITING.getCode()) {
            throw new BusinessException("只能呼叫候诊中的患者");
        }

        Integer targetLevel = queue.getTriageLevel() != null
                ? queue.getTriageLevel() : TriageLevelEnum.NON_URGENT.getCode();
        if (targetLevel > TriageLevelEnum.EMERGENCY.getCode()) {
            List<BizQueue> criticals = baseMapper.selectList(new LambdaQueryWrapper<BizQueue>()
                    .eq(BizQueue::getDeptId, queue.getDeptId())
                    .eq(BizQueue::getVisitDate, queue.getVisitDate())
                    .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .le(BizQueue::getTriageLevel, TriageLevelEnum.EMERGENCY.getCode())
                    .ne(BizQueue::getId, queue.getId())
                    .last("ORDER BY triage_level ASC, sequence_no ASC LIMIT 1"));
            if (!criticals.isEmpty()) {
                BizQueue critical = criticals.get(0);
                throw new BusinessException("队列中有危重患者 " + critical.getPatientName()
                        + "（" + TriageLevelEnum.labelOrUnknown(critical.getTriageLevel()) + " · " + critical.getQueueNo()
                        + "）尚未接诊，请先处置危重患者");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<BizQueue> grab = new LambdaUpdateWrapper<>();
        grab.eq(BizQueue::getId, queue.getId())
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .set(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(BizQueue::getCallTime, now)
                .set(BizQueue::getStartTime, now)
                .set(BizQueue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被其他终端接走，请刷新队列后重试");
        }
        BizQueue previous = closeConsultingOf(queue.getDeptId(), queue.getDoctorId(), queue.getId());
        if (queue.getDoctorId() != null) {
            doctorStatusCacheService.startConsulting(queue.getDoctorId());
        }
        return buildCallNextVO(queue, previous, now);
    }

    /**
     * 结束该医生所有还在「就诊中」的队列 —— 保证同一医生同一时刻只有一条就诊中。
     */
    private BizQueue closeConsultingOf(Long deptId, Long doctorId, Long excludeQueueId) {
        if (deptId == null || doctorId == null) {
            return null;
        }
        List<BizQueue> consulting = baseMapper.selectList(new LambdaQueryWrapper<BizQueue>()
                .eq(BizQueue::getDeptId, deptId)
                .eq(BizQueue::getDoctorId, doctorId)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .ne(excludeQueueId != null, BizQueue::getId, excludeQueueId)
                .orderByDesc(BizQueue::getCallTime)
                .orderByDesc(BizQueue::getId));
        if (consulting.isEmpty()) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        BizQueue todayPrevious = null;
        for (BizQueue stale : consulting) {
            LambdaUpdateWrapper<BizQueue> close = new LambdaUpdateWrapper<>();
            close.eq(BizQueue::getId, stale.getId())
                    .eq(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                    .set(BizQueue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode())
                    .set(BizQueue::getEndTime, now)
                    .set(BizQueue::getWaitDuration, calcMinutes(stale.getStartTime(), now));
            baseMapper.update(null, close);
            if (todayPrevious == null && today.equals(stale.getVisitDate())) {
                todayPrevious = stale;
            }
        }
        return todayPrevious;
    }

    /**
     * 组装叫号回执。状态必须是「已接诊后」的值 —— 实体是抢占前查出来的快照。
     */
    private QueueCallNextVO buildCallNextVO(BizQueue queue, BizQueue previous, LocalDateTime callTime) {
        QueueCallNextVO vo = new QueueCallNextVO();
        BizQueueListVO base = toBizQueueListVO(queue);
        if (base != null) {
            BeanUtil.copyProperties(base, vo);
        } else {
            BeanUtil.copyProperties(queue, vo);
        }
        vo.setQueueStatus(QueueStatusEnum.CONSULTING.getCode());
        vo.setCallTime(callTime);
        vo.setStartTime(callTime);
        vo.setCallCount(queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (previous != null) {
            vo.setPreviousPatientName(previous.getPatientName());
            vo.setPreviousQueueId(previous.getId());
        }
        return vo;
    }

    /**
     * 分钟差（负数按 0 计）。等待时长/就诊时长只允许是非负整数。
     */
    private Integer calcMinutes(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return (int) Math.max(0L, Duration.between(from, to).toMinutes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeQueue(Long queueId) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() != QueueStatusEnum.CONSULTING.getCode()) {
            throw new BusinessException("当前状态不允许完成");
        }
        queue.setQueueStatus(QueueStatusEnum.COMPLETED.getCode());
        queue.setEndTime(LocalDateTime.now());
        if (queue.getStartTime() != null) {
            long minutes = Duration.between(queue.getStartTime(), queue.getEndTime()).toMinutes();
            queue.setWaitDuration((int) minutes);
        }
        baseMapper.updateById(queue);

        // 清除医生接诊状态
        if (queue.getDoctorId() != null) {
            doctorStatusCacheService.finishConsulting(queue.getDoctorId());
        }

        // 同步更新挂号记录状态
        if (queue.getRegistId() != null) {
            BizAppointInfo registInfo = bizAppointInfoMapper.selectById(queue.getRegistId());
            if (registInfo != null) {
                registInfo.setRegistStatus(AppointStatusEnum.COMPLETED.getCode()); // 已完成
                bizAppointInfoMapper.updateById(registInfo);

                bizPatientService.markLastVisit(registInfo.getPatientId(), queue.getEndTime(),
                        registInfo.getDeptId(), registInfo.getDeptName(),
                        registInfo.getDoctorId(), registInfo.getDoctorName());
                bizPatientService.markFirstVisit(registInfo.getPatientId(), queue.getEndTime(),
                        registInfo.getDeptId(), registInfo.getDeptName(),
                        registInfo.getDoctorId(), registInfo.getDoctorName());
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean overdueQueue(Long queueId, String reason) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() != QueueStatusEnum.WAITING.getCode() && queue.getQueueStatus() != QueueStatusEnum.CONSULTING.getCode()) {
            throw new BusinessException("当前状态不允许过号处理");
        }
        // 过号置 OVERDUE(6) 而不是 CANCELLED(5)：5 是"已退号"，混用会让分诊台/门诊日志
        // 把过号患者显示成已退号。过号与退号是两件事，只在 is_overdue 上做标记不够。
        queue.setQueueStatus(QueueStatusEnum.OVERDUE.getCode());
        queue.setIsOverdue(1);
        queue.setOverdueTime(LocalDateTime.now());
        queue.setOverdueReason(reason);
        baseMapper.updateById(queue);

        // 同步更新挂号记录状态
        if (queue.getRegistId() != null) {
            BizAppointInfo registInfo = bizAppointInfoMapper.selectById(queue.getRegistId());
            if (registInfo != null) {
                registInfo.setRegistStatus(AppointStatusEnum.OVERDUE.getCode());
                bizAppointInfoMapper.updateById(registInfo);
            }
        }
        return true;
    }

    @Override
    public InsuranceEstimateVO estimateInsurance(InsuranceEstimateDTO dto) {
        InsuranceEstimateVO vo = new InsuranceEstimateVO();
        vo.setSettlementType(dto.getSettlementType());
        vo.setMedicalInsuranceType(dto.getMedicalInsuranceType());

        // 设置各项费用
        BigDecimal drugTotal = dto.getDrugTotal() != null ? dto.getDrugTotal() : BigDecimal.ZERO;
        BigDecimal inspectionTotal = dto.getInspectionTotal() != null ? dto.getInspectionTotal() : BigDecimal.ZERO;
        BigDecimal laboratoryTotal = dto.getLaboratoryTotal() != null ? dto.getLaboratoryTotal() : BigDecimal.ZERO;

        vo.setDrugTotal(drugTotal);
        vo.setInspectionTotal(inspectionTotal);
        vo.setLaboratoryTotal(laboratoryTotal);

        // 计算总费用
        BigDecimal totalFee = drugTotal.add(inspectionTotal).add(laboratoryTotal);
        vo.setTotalFee(totalFee);

        // 判断是否为医保患者（settlementType: 1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）
        boolean isInsurancePatient = dto.getSettlementType() != null && dto.getSettlementType() >= 2;

        // 设置医保类型显示名称
        if (!isInsurancePatient) {
            vo.setInsuranceType("自费");
        } else {
            vo.setInsuranceType(dto.getMedicalInsuranceType() != null ? dto.getMedicalInsuranceType() : "医保");
        }

        // 根据结算方式计算
        if (!isInsurancePatient) {
            // 自费
            vo.setCoverageRatio(BigDecimal.ZERO);
            vo.setInsurancePay(BigDecimal.ZERO);
            vo.setSelfPay(totalFee);
        } else {
            // 医保
            BigDecimal coverageRatio = insurancePolicyService.getCoverageRatio(dto.getSettlementType(), dto.getMedicalInsuranceType());
            vo.setCoverageRatio(coverageRatio);

            // 计算统筹支付
            BigDecimal insurancePay = totalFee.multiply(coverageRatio).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            vo.setInsurancePay(insurancePay);

            // 计算个人自付
            BigDecimal selfPay = totalFee.subtract(insurancePay);
            vo.setSelfPay(selfPay);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueCallNextVO callPatient(Long queueId) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        // 呼叫的两个入口态：候诊中(2)=常规叫号；已就诊(4)=**回诊**。
        // 回诊是真实门诊的高频场景（A 抽血/影像没出，医生叫 B，结果回来后把 A 叫回来看第二眼）：
        // closeConsultingOf 在叫下一位时把上一位置 4，但病历只是草稿并未结诊提交，
        // 队列必须回得去，否则医生只能让患者重挂号。国标口径：《病历书写基本规范》
        // 为「复诊记录」留了文体，前提就是回诊这个环节存在；叫号**不以结诊为前置**。
        // 结诊过的行（挂号单已置 4）不在放行范围 —— 病历已归档，再诊走复诊挂号。
        boolean reconsult = queue.getQueueStatus() == QueueStatusEnum.COMPLETED.getCode();
        int expectedStatus;
        if (queue.getQueueStatus() == QueueStatusEnum.WAITING.getCode()) {
            expectedStatus = QueueStatusEnum.WAITING.getCode();
        } else if (reconsult) {
            if (!LocalDate.now().equals(queue.getVisitDate())) {
                throw new BusinessException("仅支持当日回诊，该患者就诊日为 " + queue.getVisitDate());
            }
            CurrentUser me = UserUtils.getCurrentUser();
            if (me == null || me.getEmployeeId() == null || !me.getEmployeeId().equals(queue.getDoctorId())) {
                throw new BusinessException("只能回诊本人接诊过的患者");
            }
            BizAppointInfo registInfo = queue.getRegistId() == null
                    ? null : bizAppointInfoMapper.selectById(queue.getRegistId());
            if (registInfo == null) {
                throw new BusinessException("该队列未关联挂号记录，无法回诊");
            }
            if (registInfo.getRegistStatus() != null
                    && registInfo.getRegistStatus() == AppointStatusEnum.COMPLETED.getCode()) {
                throw new BusinessException("该患者已结诊（病历已提交），如需继续诊疗请挂复诊号");
            }
            expectedStatus = QueueStatusEnum.COMPLETED.getCode();
        } else {
            throw new BusinessException("只能呼叫候诊中的患者");
        }
        // 接诊医生：优先取队列上的医生（挂号/排班定下来的），取不到才退回当前登录人。
        Long doctorId = queue.getDoctorId();
        if (doctorId == null) {
            CurrentUser currentUser = UserUtils.getCurrentUser();
            if (currentUser != null && currentUser.getEmployeeId() != null) {
                doctorId = Long.valueOf(currentUser.getEmployeeId());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<BizQueue> grab = new LambdaUpdateWrapper<>();
        grab.eq(BizQueue::getId, queue.getId())
                .eq(BizQueue::getQueueStatus, expectedStatus)
                .set(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(BizQueue::getCallTime, now)
                .set(BizQueue::getStartTime, now)
                .set(BizQueue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被分诊台或其他终端接走，请刷新队列后重试");
        }

        // 旧实现把「上一位就诊中」改回**候诊中(2)**，等于把刚看完的人重新塞回候诊队列
        // （下次「接诊下一位」会再叫到他一次）；而且用 selectOne 取上一位，
        // 一旦库里有多条就诊中就直接抛 TooManyResults 500。这里统一收口成
        // 「结束上一位 + 按医生收口 + 回执告示」，与 callNext 同一套语义。
        BizQueue previous = closeConsultingOf(queue.getDeptId(), doctorId, queue.getId());
        if (doctorId != null) {
            //添加医生的状态到redis
            doctorStatusCacheService.startConsulting(doctorId);
        }
        QueueCallNextVO vo = buildCallNextVO(queue, previous, now);
        vo.setReconsult(reconsult);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallPatient(Long queueId) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() != QueueStatusEnum.CONSULTING.getCode()) {
            throw new BusinessException("只能重呼就诊中的患者");
        }
        queue.setCallTime(LocalDateTime.now());
        queue.setCallCount(queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        CurrentUser currentUser = UserUtils.getCurrentUser();
        doctorStatusCacheService.startConsulting(currentUser.getEmployeeId());
        return baseMapper.updateById(queue) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer rejoinQueue(Long queueId) {
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        // 两类人走这个入口：
        // ① 候诊中的复诊患者 —— 护士把他提上来优先看；
        // ② **已过号**的患者 —— 叫了没来、人又回来了。原先只收候诊中，
        //    过号(6)的行在分诊台上没有任何出口（按钮都不渲染），患者回来只能干等着，
        //    最后要么重挂号、要么投诉。过号不是终态，必须能回队。
        Integer st = queue.getQueueStatus();
        boolean waiting = st != null && st.equals(QueueStatusEnum.WAITING.getCode());
        boolean overdue = st != null && st.equals(QueueStatusEnum.OVERDUE.getCode());
        if (!waiting && !overdue) {
            throw new BusinessException("只能对候诊中或已过号的患者重新入队");
        }
        // 跨日的记录不能回队：叫号只取「就诊日 = 今天」的候诊行，把昨天的行改回候诊中
        // 等于制造一行谁都叫不到的记录 —— 页面上看不出异常，患者白等。隔日号请重新挂号。
        LocalDate visitDate = queue.getVisitDate() != null ? queue.getVisitDate() : LocalDate.now();
        if (!visitDate.equals(LocalDate.now())) {
            throw new BusinessException("该排队记录的就诊日是 " + visitDate + "，已跨日不能回队；"
                    + "请让患者重新挂号（或先办理改约）");
        }
        if (overdue) {
            queue.setQueueStatus(QueueStatusEnum.WAITING.getCode());
            // 过号标记要清掉：状态已经是候诊中，is_overdue 还留 1 会让分诊台的
            // 「过号」筛选与统计把这人算进去。过号时间/原因保留，作为发生过的事实留痕。
            queue.setIsOverdue(0);
            // 挂号状态跟着回到「已签到」，否则门诊日志里这人还是「已过号」
            if (queue.getRegistId() != null) {
                BizAppointInfo registInfo = bizAppointInfoMapper.selectById(queue.getRegistId());
                if (registInfo != null && registInfo.getRegistStatus() != null
                        && registInfo.getRegistStatus() == AppointStatusEnum.OVERDUE.getCode()) {
                    registInfo.setRegistStatus(AppointStatusEnum.CHECKED_IN.getCode());
                    bizAppointInfoMapper.updateById(registInfo);
                }
            }
        }

        // 查找**这位医生**当前最小的顺序号（口径必须等于取号范围：callNext 按 dept + doctorId 取号，
        // 医生站列表也按 doctorId 过滤）。
        // 原先只按 dept_id 算队首，是同一类「取号范围 ≠ 展示范围」的错：
        // 同科室还有第二个医生时，A 医生的护士点一次「优先」，B 医生队列里的候诊行
        // 序号会被整体 +1（他的屏幕上号会莫名往前跳一位），而且「优先」是相对别人队列排的，
        // A 医生的患者也不一定真占到 A 的队首。
        Long doctorId = queue.getDoctorId();
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQueue::getDeptId, queue.getDeptId())
                .eq(doctorId != null, BizQueue::getDoctorId, doctorId)
                .eq(BizQueue::getVisitDate, visitDate)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .ne(BizQueue::getId, queue.getId())
                .orderByAsc(BizQueue::getSequenceNo)
                .select(BizQueue::getSequenceNo)
                .last("LIMIT 1");
        BizQueue first = baseMapper.selectOne(wrapper);
        int headSeq = first != null && first.getSequenceNo() != null ? first.getSequenceNo() : 1;

        // 占到队首：**不再用「最小序号 - 1」**。那个写法有两个毛病：
        // ① 反复插队会插到 0、再往下是负数（序号列在「今日就诊」抽屉里是给用户看的，0/-1 像坏数据）；
        // ② Math.max(...,0) 把多次插队都压成 0 —— 并列之后谁先被叫就看数据库返回顺序，
        //    护士点一次「优先」未必真的优先。
        // 改成把当前候诊行整体后移一位，插队行占住原队首的位置：序号仍是唯一且递增的。
        baseMapper.update(null, new LambdaUpdateWrapper<BizQueue>()
                .eq(BizQueue::getDeptId, queue.getDeptId())
                .eq(doctorId != null, BizQueue::getDoctorId, doctorId)
                .eq(BizQueue::getVisitDate, visitDate)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .ne(BizQueue::getId, queue.getId())
                .ge(BizQueue::getSequenceNo, headSeq)
                .setSql("sequence_no = sequence_no + 1"));
        queue.setSequenceNo(headSeq);
        // 同时打上优先队列标记，供大屏与分诊台区分
        queue.setQueueType(QueueTypeEnum.PRIORITY.getCode());
        if (baseMapper.updateById(queue) <= 0) {
            throw new BusinessException("复诊插队失败");
        }
        // 把新序号回给前端 —— 护士要知道「现在排第几」，只说"已置为优先"等于没说
        return headSeq;
    }

    @Override
    public List<DoctorConsultingVO> getDoctorConsultingInfo(Long deptId) {
        // 查询今日排班，获取科室下的医生
        BizScheduleMapper scheduleMapper = bizScheduleMapper;
        LambdaQueryWrapper<BizSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        scheduleWrapper.in(scoped != null, BizSchedule::getDeptId, scoped)
                .eq(BizSchedule::getScheduleDate, LocalDate.now());
        List<BizSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);

        List<DoctorConsultingVO> result = new ArrayList<>();
        for (BizSchedule schedule : schedules) {
            // 从Redis获取医生状态
            int status = doctorStatusCacheService.getStatus(schedule.getDoctorId());

            // 只返回Redis中有状态记录的医生（状态不为0表示有活动）
            if (status > 0) {
                DoctorConsultingVO vo = new DoctorConsultingVO();
                vo.setScheduleId(schedule.getId());
                vo.setRoomId(schedule.getRoomId());
                vo.setRoomName(schedule.getRoomName());
                vo.setDoctorId(schedule.getDoctorId());
                vo.setDoctorName(schedule.getDoctorName());
                vo.setConsultStatus(status);
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public long countPaidUncheckedAppoints(Long deptId) {
        // 与分诊台「待签到」抽屉共用同一实现，保证「统计说 5 个、点开也是 5 个」。
        // 并且只算**今天**：原先不带日期，把历史所有没签到的挂号都算进来 ——
        // 分诊台上会显示「待签到 132 人」，而今天实际只有 3 个人要来看病。
        return listPaidUnchecked(deptId, LocalDate.now()).size();
    }

    @Override
    public QueueStatsVO getStatsCard(Long deptId) {
        QueueStatsVO stats = new QueueStatsVO();
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);

        // 查询各状态的队列数量
        long waiting = this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
        long consulting = this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode()));
        long completed = this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode()));
        long overdue = this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.OVERDUE.getCode()));

        // 待签到数量
        long unchecked = countPaidUncheckedAppoints(deptId);

        stats.setWaiting(waiting);
        stats.setConsulting(consulting);
        stats.setCompleted(completed);
        stats.setOverdue(overdue);
        stats.setUnchecked(unchecked);

        //挂号总数
        stats.setTotal(bizAppointInfoMapper.selectCount(new LambdaQueryWrapper<BizAppointInfo>()
                .in(scoped != null, BizAppointInfo::getDeptId, scoped)));

        // 从Redis获取坐诊医生数量
        BizScheduleMapper scheduleMapper = bizScheduleMapper;
        LambdaQueryWrapper<BizSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.in(scoped != null, BizSchedule::getDeptId, scoped)
                .eq(BizSchedule::getScheduleDate, LocalDate.now());
        List<BizSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);

        int doctorCount = 0;
        for (BizSchedule schedule : schedules) {
            int status = doctorStatusCacheService.getStatus(schedule.getDoctorId());
            if (status > 0) {
                doctorCount++;
            }
        }
        stats.setDoctorCount(doctorCount);

        return stats;
    }

    @Override
    public List<DoctorConsultingVO> getConsultingPatients(Long deptId) {
        // 查询当前就诊中的患者（按医生分组）
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .orderByAsc(BizQueue::getDoctorId);

        List<BizQueue> queues = baseMapper.selectList(wrapper);

        // 按医生ID分组
        Map<Long, DoctorConsultingVO> doctorMap = new LinkedHashMap<>();
        for (BizQueue queue : queues) {
            Long doctorId = queue.getDoctorId();
            if (doctorId == null) continue;

            DoctorConsultingVO vo = doctorMap.computeIfAbsent(doctorId, k -> {
                DoctorConsultingVO newVo = new DoctorConsultingVO();
                newVo.setDoctorId(doctorId);
                newVo.setDoctorName(queue.getDoctorName());
                newVo.setConsultingPatients(new ArrayList<>());
                return newVo;
            });

            // 添加患者信息
            ConsultingPatientVO patientVO = new ConsultingPatientVO();
            patientVO.setId(queue.getId());
            patientVO.setQueueNo(queue.getQueueNo());
            patientVO.setPatientName(queue.getPatientName());
            patientVO.setPatientNo(queue.getPatientNo());
            patientVO.setDoctorId(doctorId);
            patientVO.setDoctorName(queue.getDoctorName());
            vo.getConsultingPatients().add(patientVO);
        }

        return new ArrayList<>(doctorMap.values());
    }

    @Override
    public List<BizQueueListVO> getRevisitTimeoutPatients(Long deptId) {
        // 查询复诊患者（is_revisit=1）且等待超过1小时的
        // 签到时间 = arrive_time，当前时间 - arrive_time > 1小时
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        // 「今天」= visit_date（与列表/叫号同口径）。漏了它，昨天入队后一直没人收摊的
        // 「候诊中」会永久挂在这个面板上 —— 护士看到的是昨天的患者，还标着「等候 20时13分」。
        wrapper.in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getVisitDate, LocalDate.now())
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .isNotNull(BizQueue::getArriveTime)
                .le(BizQueue::getArriveTime, LocalDateTime.now().minusHours(1))
                .orderByAsc(BizQueue::getArriveTime);

        List<BizQueue> queues = baseMapper.selectList(wrapper);

        // 过滤出复诊患者
        List<BizQueueListVO> result = new ArrayList<>();
        for (BizQueue queue : queues) {
            // 查询挂号记录，判断是否复诊
            BizAppointInfo appointInfo = bizAppointInfoMapper.selectById(queue.getRegistId());
            if (appointInfo != null && appointInfo.getVisitType() != null && appointInfo.getVisitType() == VisitTypeEnum.REVISIT.getCode()) {
                BizQueueListVO vo = new BizQueueListVO();
                vo.setId(queue.getId());
                vo.setQueueNo(queue.getQueueNo());
                vo.setRegistId(queue.getRegistId());
                vo.setPatientId(queue.getPatientId());
                vo.setPatientNo(queue.getPatientNo());
                vo.setPatientName(queue.getPatientName());
                vo.setDoctorId(queue.getDoctorId());
                vo.setDoctorName(queue.getDoctorName());
                vo.setQueueStatus(queue.getQueueStatus());
                vo.setArriveTime(queue.getArriveTime());
                // 计算等待时长（分钟）
                if (queue.getArriveTime() != null) {
                    long waitMinutes = Duration.between(queue.getArriveTime(), LocalDateTime.now()).toMinutes();
                    vo.setWaitDuration((int) waitMinutes);
                }
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<DoctorStatsVO> getDoctorStats(Long deptId) {
        // 直接查询当前科室下的queue表数据，按医生分组统计
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scoped != null, BizQueue::getDeptId, scoped);
        List<BizQueue> queues = baseMapper.selectList(wrapper);

        // 按医生ID分组统计
        Map<Long, DoctorStatsVO> doctorMap = new LinkedHashMap<>();
        for (BizQueue queue : queues) {
            Long doctorId = queue.getDoctorId();
            if (doctorId == null) continue;

            DoctorStatsVO vo = doctorMap.computeIfAbsent(doctorId, k -> {
                DoctorStatsVO newVo = new DoctorStatsVO();
                newVo.setDoctorId(doctorId);
                newVo.setDoctorName(queue.getDoctorName());
                newVo.setCompletedCount(0);
                newVo.setWaitingCount(0);
                return newVo;
            });

            int status = queue.getQueueStatus();
            if (status == QueueStatusEnum.COMPLETED.getCode()) {
                vo.setCompletedCount(vo.getCompletedCount() + 1);
            } else if (status == QueueStatusEnum.WAITING.getCode()
                    || status == QueueStatusEnum.CONSULTING.getCode()) {
                vo.setWaitingCount(vo.getWaitingCount() + 1);
            }
        }

        return new ArrayList<>(doctorMap.values());
    }

    @Override
    public PageResult<OpdLogListVO> listPage(OpdLogQueryPageDTO queryDTO) {
        // 注意：这里**不**按当前登录用户的科室收窄。门诊日志是跨科室的查询分析页，
        // 而分诊台那套「只看本科室」的口径在 listPage 里（两者故意分开，见 OpdLogQueryDTO 注释）。
        // 之前 /queue/listPage 与 /queue/stats 都硬编码 currentUser.deptId，导致管理员（无科室）
        // 打开日志页拿到 0 条、统计条全 0 —— 看起来像"今天没人看病"。
        // 顺手把遗留结转掉：门诊日志是「昨天的号在不在」最直观的地方，
        // 打开时若还看到 9 天前的行挂着「已签到」，那就是没人给数据收尾。
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        Page<OpdLogListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OpdLogListVO> result = opdLogMapper.selectOpdLogPage(page, queryDTO);
        List<OpdLogListVO> records = result.getRecords();
        for (OpdLogListVO vo : records) {
            fillLogStatus(vo);
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(), records);
    }

    @Override
    public OpdLogStatsVO opdLogStats(OpdLogQueryPageDTO queryDTO) {
        OpdLogStatsVO stats = opdLogMapper.selectOpdLogStats(queryDTO);
        if (stats == null) {
            return new OpdLogStatsVO();
        }
        return stats;
    }

    /**
     * 就诊状态文案由后端给，避免前端各写一份映射。
     *
     * <p>{@code logStatus} 为 null 表示队列状态不在枚举内（库里存在列默认值 1 的历史脏数据）。
     * 这种情况**不回落**成「待签到」——前端据 {@code queueStatus} 渲染「未知(1)」，
     * 静默贴一个合法文案比显示未知更危险。
     */
    private void fillLogStatus(OpdLogListVO vo) {
        vo.setLogStatusLabel(OpdLogStatusEnum.getText(vo.getLogStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TriageRecordVO saveTriage(TriageUpsertDTO dto) {
        BizQueue queue = baseMapper.selectById(dto.getQueueId());
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (!TriageLevelEnum.isValid(dto.getTriageLevel())) {
            throw new BusinessException("分诊等级只能是 1~4");
        }
        // 已就诊/已过号/已退号的患者再分诊没有意义，反而会把队列上的「当前生效值」改乱。
        Integer status = queue.getQueueStatus();
        boolean changeable = status != null
                && (status.equals(QueueStatusEnum.WAITING.getCode())
                || status.equals(QueueStatusEnum.CONSULTING.getCode()));
        if (!changeable) {
            throw new BusinessException("该患者本次就诊已结束，不能再分诊");
        }

        // 诊室**不由分诊台决定**：唯一来源是排班（医生-诊室绑定在排班上，号源也是按排班放出的）。
        // 原先这里是「入参覆盖队列」，护士在分诊卡选一个诊室就能把挂号时定下的诊室改走 ——
        // 患者按排队号前缀（诊室编号）走，却被告知去另一个房间，直接引发客诉。
        // 现在只读：队列行上的快照（签到入队时写入）优先，老数据按挂号回查排班补齐。
        Long roomId = queue.getRoomId();
        String roomName = queue.getRoomName();
        if (roomId == null && queue.getRegistId() != null) {
            BizAppointInfo regist = bizAppointInfoMapper.selectById(queue.getRegistId());
            BizSchedule schedule = regist != null && regist.getScheduleId() != null
                    ? bizScheduleMapper.selectById(regist.getScheduleId())
                    : null;
            if (schedule != null) {
                roomId = schedule.getRoomId();
                roomName = schedule.getRoomName();
                // 顺手把快照补回队列，下次列表/叫号不用再回查
                queue.setRoomId(roomId);
                queue.setRoomName(roomName);
            }
        }
        if (roomId != null && !TextUtil.hasText(roomName)) {
            SysClinicRoom room = sysClinicRoomService.getById(roomId);
            roomName = room != null ? room.getName() : null;
        }

        // BMI 由后端算：前端算一遍后端再信一遍，迟早两边不一致。
        BigDecimal bmi = null;
        if (dto.getHeight() != null && dto.getWeight() != null
                && dto.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal h = dto.getHeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            bmi = dto.getWeight().divide(h.multiply(h), 1, RoundingMode.HALF_UP);
        }

        CurrentUser currentUser = UserUtils.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        OnDutyStaffVO dutyNurse = pickDutyNurse(queue.getDeptId(), currentUser);

        BizTriageRecord record = new BizTriageRecord();
        record.setQueueId(queue.getId());
        record.setRegistId(queue.getRegistId());
        record.setPatientId(queue.getPatientId());
        record.setPatientName(queue.getPatientName());
        record.setPatientNo(queue.getPatientNo());
        record.setTemperature(dto.getTemperature());
        record.setPulse(dto.getPulse());
        record.setRespiration(dto.getRespiration());
        record.setSystolicBp(dto.getSystolicBp());
        record.setDiastolicBp(dto.getDiastolicBp());
        record.setSpo2(dto.getSpo2());
        record.setHeight(dto.getHeight());
        record.setWeight(dto.getWeight());
        record.setBmi(bmi);
        record.setPainScore(dto.getPainScore());
        record.setChiefComplaint(dto.getChiefComplaint());
        record.setTriageLevel(dto.getTriageLevel());
        record.setRoomId(roomId);
        record.setRoomName(roomName);
        // 【sql/196】分诊护士 = 当天护理排班上的当班人，不是「谁登的浏览器」。
        // 以前这里恒等于 currentUser：管理员一分诊，单据上就写着"院长做的分诊"，
        // 而真正在岗的那个护士一条都记不上——排班表排了半天，业务上一个字都没用上。
        // 取不到当班护理就落 NULL：身份凭证 ≠ 当班责任，宁缺勿假。
        record.setTriageNurseId(dutyNurse == null ? null : dutyNurse.getStaffId());
        record.setTriageNurseName(dutyNurse == null ? null : dutyNurse.getStaffName());
        record.setTriageTime(now);
        record.setRemark(dto.getRemark());
        // 只增不改：重测体温/重新定级是**追加**一条，历史留痕不动
        bizTriageRecordMapper.insert(record);

        // 队列上存「当前生效值」快照，供列表与叫号排序使用
        queue.setTriageStatus(1);
        queue.setTriageLevel(dto.getTriageLevel());
        queue.setRoomId(roomId);
        queue.setRoomName(roomName);
        baseMapper.updateById(queue);

        return toTriageVO(record);
    }

    @Override
    public TriageDetailVO getTriageByQueueId(Long queueId) {
        TriageDetailVO detail = new TriageDetailVO();
        if (queueId == null) {
            return detail;
        }
        BizQueue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            return detail;
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        List<BizTriageRecord> list = bizTriageRecordMapper.selectList(
                new LambdaQueryWrapper<BizTriageRecord>()
                        .eq(BizTriageRecord::getQueueId, queueId)
                        .orderByDesc(BizTriageRecord::getTriageTime)
                        .orderByDesc(BizTriageRecord::getId));
        if (list.isEmpty()) {
            return detail;
        }
        List<TriageRecordVO> history = new ArrayList<>(list.size());
        for (BizTriageRecord record : list) {
            history.add(toTriageVO(record));
        }
        detail.setHistory(history);
        detail.setLatest(history.get(0));
        return detail;
    }

    private TriageRecordVO toTriageVO(BizTriageRecord record) {
        TriageRecordVO vo = BeanUtil.copyProperties(record, TriageRecordVO.class);
        vo.setTriageLevelText(TriageLevelEnum.getText(record.getTriageLevel()));
        return vo;
    }

    @Override
    public List<BizQueueListVO> listPaidUnchecked(Long deptId, LocalDate visitDate) {
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<BizAppointInfo> appointWrapper = new LambdaQueryWrapper<>();
        appointWrapper.in(scoped != null, BizAppointInfo::getDeptId, scoped)
                .eq(BizAppointInfo::getRegistStatus, AppointStatusEnum.REGISTERED.getCode())
                .eq(visitDate != null, BizAppointInfo::getVisitDate, visitDate);
        List<BizAppointInfo> appoints = bizAppointInfoMapper.selectList(appointWrapper);
        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        List<BizQueueListVO> result = new ArrayList<>();
        for (BizAppointInfo appoint : appoints) {
            // 挂号状态「已挂号」只说明还没签到，还得确认钱真的收了 ——
            // 未缴费也能挂号（挂完去收费处），把没交钱的人放进「待签到」会让护士白叫。
            // 免收的号没有账单，属于"没有欠账"，必须照常放进来（旧口径用 isNotNull(charge_id) 把他们整体筛掉了）。
            if (!isRegistFeeSettled(gateway, appoint)) {
                continue;
            }
            BizQueueListVO vo = new BizQueueListVO();
            vo.setRegistId(appoint.getId());
            vo.setRegistNo(appoint.getRegistNo());
            vo.setPatientId(appoint.getPatientId());
            vo.setPatientNo(appoint.getPatientNo());
            vo.setPatientName(appoint.getPatientName());
            vo.setGender(appoint.getGender());
            vo.setAge(appoint.getAge());
            vo.setDeptId(appoint.getDeptId());
            vo.setDeptName(appoint.getDeptName());
            vo.setDoctorId(appoint.getDoctorId());
            vo.setDoctorName(appoint.getDoctorName());
            vo.setRegistType(appoint.getRegistType());
            vo.setVisitType(appoint.getVisitType());
            vo.setVisitDate(appoint.getVisitDate());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<PatientQueueVO> myQueue(Long patientId) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        LocalDate from = LocalDate.now().minusDays(2);
        List<BizAppointInfo> appoints = bizAppointInfoMapper.selectList(
                new LambdaQueryWrapper<BizAppointInfo>()
                        .eq(BizAppointInfo::getPatientId, patientId)
                        .ge(BizAppointInfo::getVisitDate, from)
                        .ne(BizAppointInfo::getRegistStatus, AppointStatusEnum.CANCELLED.getCode())
                        .orderByDesc(BizAppointInfo::getVisitDate)
                        .orderByDesc(BizAppointInfo::getId));
        if (appoints.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> registIds = appoints.stream().map(BizAppointInfo::getId).collect(Collectors.toList());
        Map<Long, BizQueue> queueByRegist = this.list(new LambdaQueryWrapper<BizQueue>()
                        .in(BizQueue::getRegistId, registIds))
                .stream()
                .collect(Collectors.toMap(BizQueue::getRegistId, q -> q, (a, b) -> a));

        List<PatientQueueVO> list = new ArrayList<>();
        for (BizAppointInfo appoint : appoints) {
            BizQueue queue = queueByRegist.get(appoint.getId());
            PatientQueueVO vo = new PatientQueueVO();
            vo.setRegistId(appoint.getId());
            vo.setRegistNo(appoint.getRegistNo());
            vo.setPatientName(appoint.getPatientName());
            vo.setDeptId(appoint.getDeptId());
            vo.setDeptName(appoint.getDeptName());
            vo.setDoctorName(appoint.getDoctorName());
            vo.setVisitDate(appoint.getVisitDate());
            vo.setSlotStart(appoint.getSlotStart());
            vo.setSlotEnd(appoint.getSlotEnd());
            if (queue == null) {
                // 没有队列行 = 还没签到（挂上号不等于排上队），页面据此引导去签到
                vo.setCheckedIn(false);
                vo.setQueueStatusText("待签到");
                vo.setAheadCount(0);
            } else {
                vo.setCheckedIn(true);
                vo.setQueueId(queue.getId());
                vo.setQueueStatus(queue.getQueueStatus());
                vo.setQueueStatusText(QueueStatusEnum.getText(queue.getQueueStatus()));
                vo.setSequenceNo(queue.getSequenceNo());
                vo.setRoomName(queue.getRoomName());
                vo.setArriveTime(queue.getArriveTime());
                vo.setCallTime(queue.getCallTime());
                if (Objects.equals(queue.getQueueStatus(), QueueStatusEnum.WAITING.getCode())) {
                    vo.setAheadCount(countAhead(queue));
                    vo.setCurrentCalledNo(currentCalledNo(queue));
                } else {
                    vo.setAheadCount(0);
                }
            }
            list.add(vo);
        }
        return list;
    }

    /**
     * 前方候诊人数：同科室同医生当日、签到更早且仍候诊中的人数。
     * 医生为空的记录只按科室收窄 —— 位次宁可少报一点，也不能把别的医生的队列算进来虚增。
     */
    /**
     * 本次分诊的当班护士 —— 取当天护理排班上此刻在岗的人。
     *
     * <p>取值顺序见 {@link BizScheduleService#pickDutyStaff}：优先本人（若本人就在当班名单里），
     * 否则取此刻在岗/当日最近一班。<b>排班查不到就返回 null，不用登录人兜底</b> ——
     * 分诊是临床动作，责任人必须是真的当班护士；没有当班记录就留空，
     * 页面上显示「未记录护士」比显示一个没上班的人诚实。
     *
     * <p>查排班出错也返回 null：分诊是高频关键动作，不能被排班侧的异常拖死，
     * 更不能因为查不到人就整条分诊失败。
     */
    private OnDutyStaffVO pickDutyNurse(Long deptId, CurrentUser currentUser) {
        if (deptId == null) {
            return null;
        }
        try {
            Long selfEmpId = currentUser == null ? null : currentUser.getEmployeeId();
            return bizScheduleService.pickDutyStaff(deptId, StaffTypeEnum.NURSE.getCode(), selfEmpId);
        } catch (Exception ex) {
            return null;
        }
    }

    private int countAhead(BizQueue queue) {
        return (int) this.count(new LambdaQueryWrapper<BizQueue>()
                .eq(BizQueue::getDeptId, queue.getDeptId())
                .eq(queue.getDoctorId() != null, BizQueue::getDoctorId, queue.getDoctorId())
                .eq(BizQueue::getVisitDate, queue.getVisitDate())
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .lt(queue.getArriveTime() != null, BizQueue::getArriveTime, queue.getArriveTime()));
    }

    /**
     * 当前已叫到的序号：同口径队列里已被叫过（就诊中/已就诊/过号）的最大顺序号
     */
    private Integer currentCalledNo(BizQueue queue) {
        List<BizQueue> called = this.list(new LambdaQueryWrapper<BizQueue>()
                .eq(BizQueue::getDeptId, queue.getDeptId())
                .eq(queue.getDoctorId() != null, BizQueue::getDoctorId, queue.getDoctorId())
                .eq(BizQueue::getVisitDate, queue.getVisitDate())
                .in(BizQueue::getQueueStatus,
                        QueueStatusEnum.CONSULTING.getCode(),
                        QueueStatusEnum.COMPLETED.getCode(),
                        QueueStatusEnum.OVERDUE.getCode()));
        return called.stream()
                .map(BizQueue::getSequenceNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    @Override
    public DoctorStatusVO doctorStatusOf(Long doctorId) {
        DoctorStatusVO vo = new DoctorStatusVO();
        vo.setDoctorId(doctorId);
        vo.setStatus(doctorStatusCacheService.getStatus(doctorId));
        return vo;
    }

    @Override
    public DoctorStatusVO currentDoctorStatus() {
        return doctorStatusOf(currentLoginEmployeeId());
    }

    @Override
    public void setCurrentDoctorStatus(DoctorStatusSetDTO dto) {
        // 1 接诊中由 callNext 自己写，不接受前端指定
        if (!Objects.equals(dto.getStatus(), DoctorStatusCacheService.STATUS_IDLE)
                && !Objects.equals(dto.getStatus(), DoctorStatusCacheService.STATUS_PAUSED)) {
            throw new BusinessException("接诊状态只允许设置为 0（恢复接诊）或 2（暂离）");
        }
        doctorStatusCacheService.setStatus(currentLoginEmployeeId(), dto.getStatus());
    }

    @Override
    public List<DoctorStatusVO> batchDoctorStatus(DoctorStatusBatchQueryDTO dto) {
        List<DoctorStatusVO> list = new ArrayList<>();
        for (Long doctorId : dto.getDoctorIds()) {
            list.add(doctorStatusOf(doctorId));
        }
        return list;
    }

    /**
     * 接诊状态挂在员工ID上（与站内信 receiver_id 同一口径），不是用户的ID。
     */
    private Long currentLoginEmployeeId() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null || currentUser.getEmployeeId() == null) {
            throw new BusinessException("未获取到当前登录医生信息");
        }
        return currentUser.getEmployeeId();
    }

    private Long currentLoginDeptIdOrNull() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        return currentUser == null ? null : currentUser.getDeptId();
    }

    @Override
    public QueueStatsVO stats(QueueQueryDTO queueQueryDTO) {
        QueueStatsVO queueStatsVO = new QueueStatsVO();
        List<Long> scoped = deptScopeService.scopedDeptIds(queueQueryDTO.getDeptId());
        String dateStart, dateEnd;
        if (queueQueryDTO.getStartTime() != null && !queueQueryDTO.getStartTime().isEmpty()
                && queueQueryDTO.getEndTime() != null && !queueQueryDTO.getEndTime().isEmpty()) {
            dateStart = queueQueryDTO.getStartTime();
            dateEnd = queueQueryDTO.getEndTime();
        } else {
            String today = LocalDate.now().toString();
            dateStart = today + " 00:00:00";
            dateEnd = today + " 23:59:59";
        }
        // 统计口径必须走枚举：WAITING(2)/CALLED(3)/COMPLETED(4)/OVERDUE(6)
        // 之前 completed 数的是 5(已退号)、overdue 数的是 4(已就诊)，完全反了
        queueStatsVO.setWaiting(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .ge(BizQueue::getArriveTime, dateStart)
                .le(BizQueue::getArriveTime, dateEnd)));
        queueStatsVO.setConsulting(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .ge(BizQueue::getArriveTime, dateStart)
                .le(BizQueue::getArriveTime, dateEnd)));
        queueStatsVO.setCompleted(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode())
                .ge(BizQueue::getArriveTime, dateStart)
                .le(BizQueue::getArriveTime, dateEnd)));
        queueStatsVO.setOverdue(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.OVERDUE.getCode())
                .ge(BizQueue::getArriveTime, dateStart)
                .le(BizQueue::getArriveTime, dateEnd)));
        queueStatsVO.setUnchecked(countPaidUncheckedAppoints(queueQueryDTO.getDeptId()));
        queueStatsVO.setTotal(queueStatsVO.getWaiting() + queueStatsVO.getConsulting()
                + queueStatsVO.getCompleted() + queueStatsVO.getOverdue());

        // 未核验分级：今日候诊中、护士还没看过（triage_status=0）的人数。
        // 这**不再**是「叫不出号」的原因 —— 签到即给 4 级默认等级，未核验的照样能接诊。
        // 它的价值是提醒护士「这几位还没量体征/没定级」，页面文案不能说成「未分诊不能接诊」。
        queueStatsVO.setUnTriage(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(BizQueue::getVisitDate, LocalDate.now())
                .eq(BizQueue::getTriageStatus, 0)));

        // 危重待接诊：今日候诊中 1/2 级的人数。这是唯一还有硬约束的场景
        // （callSpecific 会拒绝越过他们去叫普通患者），所以必须在统计条上醒目给出。
        queueStatsVO.setCriticalWaiting(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(BizQueue::getVisitDate, LocalDate.now())
                .le(BizQueue::getTriageLevel, TriageLevelEnum.EMERGENCY.getCode())));

        // 候诊超时：现算，不落状态列
        queueStatsVO.setTimeout(this.count(new LambdaQueryWrapper<BizQueue>()
                .in(scoped != null, BizQueue::getDeptId, scoped)
                .eq(BizQueue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(BizQueue::getVisitDate, LocalDate.now())
                .le(BizQueue::getArriveTime, LocalDateTime.now().minusMinutes(30))));

        return queueStatsVO;
    }

    @Override
    public void checkInByRegist(AppointCheckInUpdateDTO updateDTO) {
        // 分诊台/自助机用的接口，患者 token 也调得到 —— 不校验归属就等于能替别人签到
        if (patientGuardianService.patientScopeViolated(patientIdOfRegist(updateDTO.getRegistId()))) {
            throw new BusinessException("无权为该就诊人签到");
        }
        if (!checkInByRegistId(updateDTO)) {
            throw new BusinessException("签到失败");
        }
    }

    @Override
    public QueueCallNextVO callNextByOperator(QueueCallNextDTO queueCallNextDTO) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("登录状态已失效，请重新登录");
        }
        // 指定 queueId = 「呼叫某位指定患者」（医生在列表里点人，可跨医生指定的对象由分诊台把关）
        if (queueCallNextDTO.getQueueId() != null) {
            return callSpecific(queueCallNextDTO.getQueueId());
        }
        // 不指定 = 「接诊下一位」：取号范围由服务端按科室 + 医生 + 就诊日收口，
        // 前端不要传任何「我以为下一位是谁」的信息 —— 那正是会接错人的地方。
        if (user.getDeptId() == null || user.getEmployeeId() == null) {
            throw new BusinessException("当前账号未绑定科室/员工，无法叫号");
        }
        return callNext(user.getDeptId(), user.getEmployeeId());
    }

    @Override
    public List<DoctorConsultingVO> doctorConsultingInfoOfCurrentDept() {
        Long deptId = currentLoginDeptIdOrNull();
        return deptId == null ? new ArrayList<>() : getDoctorConsultingInfo(deptId);
    }

    @Override
    public QueueStatsVO statsCardOfCurrentDept() {
        Long deptId = currentLoginDeptIdOrNull();
        return deptId == null ? new QueueStatsVO() : getStatsCard(deptId);
    }

    @Override
    public List<DoctorConsultingVO> consultingPatientsOfCurrentDept() {
        Long deptId = currentLoginDeptIdOrNull();
        return deptId == null ? new ArrayList<>() : getConsultingPatients(deptId);
    }

    @Override
    public List<BizQueueListVO> revisitTimeoutPatientsOfCurrentDept() {
        Long deptId = currentLoginDeptIdOrNull();
        return deptId == null ? new ArrayList<>() : getRevisitTimeoutPatients(deptId);
    }

    @Override
    public List<DoctorStatsVO> doctorStatsOfCurrentDept() {
        Long deptId = currentLoginDeptIdOrNull();
        return deptId == null ? new ArrayList<>() : getDoctorStats(deptId);
    }

    @Override
    public List<BizQueueListVO> uncheckedList(Long deptId, String visitDate) {
        Long effectiveDeptId = deptId != null ? deptId : currentLoginDeptIdOrNull();
        if (effectiveDeptId == null) {
            return new ArrayList<>();
        }
        LocalDate date = (visitDate == null || visitDate.isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(visitDate);
        return listPaidUnchecked(effectiveDeptId, date);
    }
}