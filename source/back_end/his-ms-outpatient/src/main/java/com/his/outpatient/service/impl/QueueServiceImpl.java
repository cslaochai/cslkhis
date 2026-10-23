package com.his.outpatient.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.outpatient.dto.*;
import com.his.spi.dto.AppointCheckInUpdateDTO;
import com.his.outpatient.entity.AppointInfo;
import com.his.outpatient.entity.Queue;
import com.his.outpatient.entity.TriageRecord;
import com.his.common.enums.*;
import com.his.outpatient.mapper.AppointInfoMapper;
import com.his.outpatient.mapper.QueueMapper;
import com.his.outpatient.mapper.TriageRecordMapper;
import com.his.outpatient.mapper.ClincOpdLogMapper;
import com.his.outpatient.service.QueueService;
import com.his.outpatient.service.DoctorStatusCacheService;
import com.his.outpatient.trigger.DayEndSettleTrigger;
import com.his.outpatient.vo.*;
import com.his.spi.vo.PatientQueueVO;
import com.his.spi.vo.BillBriefVO;
import com.his.common.base.PageResult;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.SettlementModeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.service.ClinicSourceFacadeService;
import com.his.spi.service.DutyRosterFacadeService;
import com.his.spi.vo.ClinicRoomBriefVO;
import com.his.spi.vo.ClinicSourceBriefVO;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.DutyStaffBriefVO;
import com.his.common.entity.CurrentUser;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.RedisSequenceService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
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
public class QueueServiceImpl extends ServiceImpl<QueueMapper, Queue> implements QueueService {
    private final PatientFacadeService patientFacadeService;

    private final ChargeFacadeService chargeFacadeService;

    private final RedisSequenceService redisSequenceService;

    private final AppointInfoMapper appointInfoMapper;

    private final DoctorStatusCacheService doctorStatusCacheService;

    private final SystemFacadeService systemFacadeService;

    private final ClincOpdLogMapper clincOpdLogMapper;

    private final TriageRecordMapper triageRecordMapper;

    private final DayEndSettleTrigger dayEndSettleTrigger;

    private final DeptScopeService deptScopeService;

    private final ClinicSourceFacadeService clinicSourceFacadeService;

    private final DutyRosterFacadeService dutyRosterFacadeService;

    @Override
    public List<BizQueueListVO> getTodayQueueList(QueueTodayQueryDTO queueQueryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null || currentUser.getDeptId() == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Queue::getDeptId, currentUser.getDeptId())
                .eq(Queue::getDoctorId, currentUser.getEmployeeId())
                .eq(Queue::getVisitDate, LocalDate.now());
        wrapper.orderByAsc(Queue::getSequenceNo);
        List<Queue> list = baseMapper.selectList(wrapper);
        List<BizQueueListVO> resultList = new ArrayList<>(list.size());
        for (Queue queue : list) {
            BizQueueListVO vo = toBizQueueListVO(queue);
            if (vo != null) {
                resultList.add(vo);
            }
        }
        return resultList;
    }

    private BizQueueListVO toBizQueueListVO(Queue queue) {
        if (queue == null || queue.getRegistId() == null) {
            return null;
        }
        BizQueueListVO vo = BeanUtil.copyProperties(queue, BizQueueListVO.class);
        AppointInfo registInfo = appointInfoMapper.selectById(queue.getRegistId());
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
     * 诊室快照的权威来源
     */
    private void fillRoomFromSchedule(BizQueueListVO vo, AppointInfo registInfo) {
        if (vo == null || registInfo == null || registInfo.getScheduleId() == null) {
            return;
        }
        ClinicSourceBriefVO source = clinicSourceFacadeService.findClinicSource(registInfo.getScheduleId());
        if (source == null) {
            return;
        }
        if (vo.getRoomId() == null) {
            vo.setRoomId(source.getRoomId());
        }
        if (!TextUtil.hasText(vo.getRoomName())) {
            vo.setRoomName(source.getRoomName());
        }
    }

    @Override
    public PageResult<BizQueueListVO> listPage(QueueQueryDTO queueQueryDTO) {
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        List<Long> scoped = deptScopeService.scopedDeptIds(queueQueryDTO.getDeptId());
        if (scoped != null) {
            wrapper.in(Queue::getDeptId, scoped);
        }

        // 状态：多选优先于单选
        if (queueQueryDTO.getQueueStatuses() != null && !queueQueryDTO.getQueueStatuses().isEmpty()) {
            wrapper.in(Queue::getQueueStatus, queueQueryDTO.getQueueStatuses());
        } else {
            wrapper.eq(queueQueryDTO.getQueueStatus() != null, Queue::getQueueStatus, queueQueryDTO.getQueueStatus());
        }
        wrapper.eq(queueQueryDTO.getDoctorId() != null, Queue::getDoctorId, queueQueryDTO.getDoctorId());
        wrapper.eq(Boolean.TRUE.equals(queueQueryDTO.getUnTriageOnly()), Queue::getTriageStatus, 0);

        // 日期口径分两种：visit_date 是「今天该看谁」，arrive_time 是「今天几点到的」。
        // 跨日遗留的队列（昨天入队今天还没走完）只有前者能正确排除。
        if (TextUtil.hasText(queueQueryDTO.getVisitDate())) {
            wrapper.eq(Queue::getVisitDate, queueQueryDTO.getVisitDate());
        }
        if (TextUtil.hasText(queueQueryDTO.getStartTime()) && TextUtil.hasText(queueQueryDTO.getEndTime())) {
            wrapper.ge(Queue::getArriveTime, queueQueryDTO.getStartTime())
                    .le(Queue::getArriveTime, queueQueryDTO.getEndTime());
        } else if (TextUtil.hasText(queueQueryDTO.getDate())) {
            wrapper.ge(Queue::getArriveTime, queueQueryDTO.getDate() + " 00:00:00")
                    .le(Queue::getArriveTime, queueQueryDTO.getDate() + " 23:59:59");
        }

        if (TextUtil.hasText(queueQueryDTO.getKeyword())) {
            String kw = queueQueryDTO.getKeyword().trim();
            List<Long> registIds = appointInfoMapper.selectList(new LambdaQueryWrapper<AppointInfo>()
                            .select(AppointInfo::getId)
                            .like(AppointInfo::getRegistNo, kw)
                            .last("LIMIT 500"))
                    .stream().map(AppointInfo::getId).collect(Collectors.toList());
            wrapper.and(w -> {
                w.like(Queue::getPatientName, kw)
                        .or().like(Queue::getPatientNo, kw)
                        .or().like(Queue::getQueueNo, kw);
                if (!registIds.isEmpty()) {
                    w.or().in(Queue::getRegistId, registIds);
                }
            });
        }

        // 排序口径：门诊行 triage_level 为 null（→4），等价按 sequence_no 纯 FIFO；
        // 急诊科直录的行带 I~IV 级，仍按级别优先。两套队列共用一张表，排序只此一处。
        wrapper.last("ORDER BY IFNULL(triage_level, 4) ASC, sequence_no ASC");

        Page<Queue> page = this.page(new Page<>(queueQueryDTO.getPageNum(), queueQueryDTO.getPageSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return new PageResult<>();
        }

        List<BizQueueListVO> resultList = new ArrayList<>(page.getRecords().size());
        for (Queue queue : page.getRecords()) {
            resultList.add(toListVO(queue));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), resultList);
    }

    /**
     * 队列实体 → 列表 VO，补齐挂号侧快照与分诊等级文案。
     */
    private BizQueueListVO toListVO(Queue queue) {
        BizQueueListVO vo = BeanUtil.copyProperties(queue, BizQueueListVO.class);
        vo.setTriageLevelText(TriageLevelEnum.getLabel(queue.getTriageLevel()));
        AppointInfo registInfo = queue.getRegistId() != null
                ? appointInfoMapper.selectById(queue.getRegistId()) : null;
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
        AppointInfo appointInfo = appointInfoMapper.selectById(registId);
        return appointInfo == null ? null : appointInfo.getPatientId();
    }

    private boolean isRegistFeeSettled(ChargeFacadeService gateway, AppointInfo appoint) {
        if (appoint.getBillId() == null) {
            return true;
        }
        BillBriefVO bill = gateway.getBill(appoint.getBillId());
        return bill != null && BillStatusEnum.PAID.getCode().equals(bill.getBillStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkInByRegistId(AppointCheckInUpdateDTO updateDTO) {
        AppointInfo appointInfo = appointInfoMapper.selectById(updateDTO.getRegistId());
        if (appointInfo == null) {
            throw new BusinessException("挂号记录不存在");
        }
        deptScopeService.assertDeptAccessible(appointInfo.getDeptId());
        // 收费服务为编译期强依赖（his-charge 已引入），实现缺失直接启动失败
        if (!isRegistFeeSettled(chargeFacadeService, appointInfo)) {
            throw new BusinessException("该患者尚未支付，请先完成缴费");
        }
        LocalDate visitDate = appointInfo.getVisitDate() != null
                ? appointInfo.getVisitDate()
                : LocalDate.now();
        // 就诊日必须就是今天。签到的产物是「今天这个医生队列里的一行」，
        LocalDate today = LocalDate.now();
        if (!visitDate.equals(today)) {
            throw new BusinessException("该挂号就诊日为 " + visitDate + "，"
                    + (visitDate.isBefore(today) ? "已过期，请办理改约或退号重挂" : "尚未到就诊日，不能提前签到"));
        }
        LambdaQueryWrapper<Queue> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Queue::getRegistId, updateDTO.getRegistId())
                .eq(Queue::getVisitDate, visitDate);
        Long existCount = baseMapper.selectCount(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("该患者已签到");
        }

        // 生成排队号（以诊室编号为前缀）
        String queueNo = generateQueueNo(appointInfo.getDeptId(), appointInfo.getScheduleId());

        // 创建排队记录
        Queue queue = new Queue();
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
        // 门诊无急诊式分级（2026-10-22 收口）：不再默认写 triage_level=4 —— 门诊队列就是
        // 先到先得，排序只认 sequence_no；triage_level 留 null，listPage/callNext 的
        // IFNULL(triage_level,4) 排序自然退化成 FIFO，急诊行（急诊科直录）仍按级别优先。
        // triage_status 列 NOT NULL 无默认值，写 1=可接诊（门诊没有「护士核验」流程，
        // 急诊分级由 EmergencyServiceImpl 另写 I~IV 级）。
        queue.setTriageStatus(1);
        // 叫号次数：新队列初始 0（还没叫过号）。biz_queue.call_count 列无默认值，
        // 漏 set 会被 MySQL 拒收报 "Field 'call_count' doesn't have a default value"
        // （急诊建队同口径已 setCallCount(0)，此处与之一致）。
        queue.setCallCount(0);
        // 挂号类型快照：biz_queue.regist_type 无默认值，漏 set 会被 MySQL 拒收报
        // "Field 'regist_type' doesn't have a default value"（500 系统内部错误），
        // 且前端队列列表的「普通号/专家号」标签正读这一列。历史挂号无类型时兜底普通号。
        queue.setRegistType(appointInfo.getRegistType() != null
                ? appointInfo.getRegistType() : RegistTypeEnum.NORMAL.getCode());
        // 诊室快照：按挂号 scheduleId 回查号源补齐 roomId/roomName，查不到则留空，不编造
        ClinicSourceBriefVO signedSource = appointInfo.getScheduleId() == null
                ? null : clinicSourceFacadeService.findClinicSource(appointInfo.getScheduleId());
        if (signedSource != null) {
            queue.setRoomId(signedSource.getRoomId());
            queue.setRoomName(signedSource.getRoomName());
        }
        baseMapper.insert(queue);

        // 更新挂号状态为已签到
        appointInfo.setRegistStatus(AppointStatusEnum.CHECKED_IN.getCode());
        appointInfo.setArriveTime(LocalDateTime.now());
        // 守卫下沉到 WHERE：仅「已挂号」可签到，判重计数存在并发窗口，重复签到时后到一笔 0 行失败
        int checkedIn = appointInfoMapper.update(appointInfo, new LambdaUpdateWrapper<AppointInfo>()
                .eq(AppointInfo::getId, appointInfo.getId())
                .eq(AppointInfo::getRegistStatus, AppointStatusEnum.REGISTERED.getCode()));
        if (checkedIn == 0) {
            throw new BusinessException("该患者已签到或挂号状态已变更，请刷新后重试");
        }
        return true;
    }

    /**
     * 生成排队号：呼叫代号（1~2 字母，来自诊室） + "-" + 3位当日序号。
     * 例：A001、BA042。不按旧模型的诊室编号（如 "10000101-01"），后者太长不好念。
     *
     * <p>原实现按 scheduleId 回查排班号源拿 roomId，再经 SystemFacadeService#findClinicRoom 取呼号前缀。
     * 跨模块改走 spi 后当前无「按号源ID取诊室快照」端口（已登记 ClinicSourceFacadeService#findClinicSource），
     * 端口补齐前直接走科室兜底前缀，不回查排班。
     */
    private String generateQueueNo(Long deptId, Long scheduleId) {
        String prefix = null;
        // 优先按号源 → 诊室取呼号前缀；取不到再走科室兜底
        if (scheduleId != null) {
            ClinicSourceBriefVO source = clinicSourceFacadeService.findClinicSource(scheduleId);
            if (source != null && source.getRoomId() != null) {
                ClinicRoomBriefVO room = systemFacadeService.findClinicRoom(source.getRoomId());
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
     * 按诊室前缀直接生成排队号（改派换诊室用，没有新号源可回查）。
     * 取不到诊室前缀时退回科室兜底 D{deptId}，与 {@link #generateQueueNo} 的兜底口径一致。
     */
    private String buildQueueNoByRoom(Long deptId, Long roomId) {
        String prefix = null;
        if (roomId != null) {
            ClinicRoomBriefVO room = systemFacadeService.findClinicRoom(roomId);
            if (room != null && TextUtil.hasText(room.getQueuePrefix())) {
                prefix = room.getQueuePrefix();
            }
        }
        if (!TextUtil.hasText(prefix)) {
            prefix = "D" + deptId;
        }
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
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Queue::getDeptId, deptId)
                .eq(Queue::getDoctorId, doctorId)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(Queue::getVisitDate, LocalDate.now())
                .isNotNull(Queue::getRegistId)
                .last("ORDER BY IFNULL(triage_level, 4) ASC, sequence_no ASC LIMIT 1");
        Queue queue = baseMapper.selectOne(wrapper);
        if (queue == null) {
            Long mineWaiting = baseMapper.selectCount(new LambdaQueryWrapper<Queue>()
                    .eq(Queue::getDeptId, deptId)
                    .eq(Queue::getDoctorId, doctorId)
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .eq(Queue::getVisitDate, LocalDate.now()));
            if (mineWaiting != null && mineWaiting > 0) {
                throw new BusinessException("您今日有 " + mineWaiting + " 位候诊患者但未能取号，请刷新队列");
            }
            throw new BusinessException("您当前没有候诊患者（患者需先在分诊站签到入队）");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Queue> grab = new LambdaUpdateWrapper<>();
        grab.eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .set(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(Queue::getCallTime, now)
                .set(Queue::getStartTime, now)
                .set(Queue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被分诊台或其他终端接走，请刷新队列后重试");
        }

        // 抢占成功后再收上一位：同一医生同一时刻只允许一条「就诊中」。
        Queue previous = closeConsultingOf(deptId, doctorId, queue.getId());
        if (doctorId != null) {
            // 更新医生接诊状态到Redis
            doctorStatusCacheService.startConsulting(doctorId);
        }
        return buildCallNextVO(queue, previous, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueCallNextVO callSpecific(Long queueId) {
        Queue queue = baseMapper.selectById(queueId);
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
            List<Queue> criticals = baseMapper.selectList(new LambdaQueryWrapper<Queue>()
                    .eq(Queue::getDeptId, queue.getDeptId())
                    .eq(Queue::getVisitDate, queue.getVisitDate())
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .le(Queue::getTriageLevel, TriageLevelEnum.EMERGENCY.getCode())
                    .ne(Queue::getId, queue.getId())
                    .last("ORDER BY triage_level ASC, sequence_no ASC LIMIT 1"));
            if (!criticals.isEmpty()) {
                Queue critical = criticals.get(0);
                throw new BusinessException("队列中有危重患者 " + critical.getPatientName()
                        + "（" + TriageLevelEnum.getLabelOrUnknown(critical.getTriageLevel()) + " · " + critical.getQueueNo()
                        + "）尚未接诊，请先处置危重患者");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Queue> grab = new LambdaUpdateWrapper<>();
        grab.eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .set(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(Queue::getCallTime, now)
                .set(Queue::getStartTime, now)
                .set(Queue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被其他终端接走，请刷新队列后重试");
        }
        Queue previous = closeConsultingOf(queue.getDeptId(), queue.getDoctorId(), queue.getId());
        if (queue.getDoctorId() != null) {
            doctorStatusCacheService.startConsulting(queue.getDoctorId());
        }
        return buildCallNextVO(queue, previous, now);
    }

    /**
     * 结束该医生所有还在「就诊中」的队列 —— 保证同一医生同一时刻只有一条就诊中。
     */
    private Queue closeConsultingOf(Long deptId, Long doctorId, Long excludeQueueId) {
        if (deptId == null || doctorId == null) {
            return null;
        }
        List<Queue> consulting = baseMapper.selectList(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getDeptId, deptId)
                .eq(Queue::getDoctorId, doctorId)
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .ne(excludeQueueId != null, Queue::getId, excludeQueueId)
                .orderByDesc(Queue::getCallTime)
                .orderByDesc(Queue::getId));
        if (consulting.isEmpty()) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        Queue todayPrevious = null;
        for (Queue stale : consulting) {
            LambdaUpdateWrapper<Queue> close = new LambdaUpdateWrapper<>();
            close.eq(Queue::getId, stale.getId())
                    .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                    .set(Queue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode())
                    .set(Queue::getEndTime, now)
                    .set(Queue::getWaitDuration, calcMinutes(stale.getStartTime(), now));
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
    private QueueCallNextVO buildCallNextVO(Queue queue, Queue previous, LocalDateTime callTime) {
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
        Queue queue = baseMapper.selectById(queueId);
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
        // 守卫下沉到 WHERE：仅「就诊中」可完成，多窗口/多终端并发完成时后到者 0 行失败
        int completed = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode()));
        if (completed == 0) {
            throw new BusinessException("当前状态不允许完成，请刷新后重试");
        }

        // 清除医生接诊状态
        if (queue.getDoctorId() != null) {
            doctorStatusCacheService.finishConsulting(queue.getDoctorId());
        }

        // 同步更新挂号记录状态
        if (queue.getRegistId() != null) {
            AppointInfo registInfo = appointInfoMapper.selectById(queue.getRegistId());
            if (registInfo != null) {
                registInfo.setRegistStatus(AppointStatusEnum.COMPLETED.getCode()); // 已完成
                appointInfoMapper.updateById(registInfo);

                patientFacadeService.markLastVisit(registInfo.getPatientId(), queue.getEndTime(),
                        registInfo.getDeptId(), registInfo.getDeptName(),
                        registInfo.getDoctorId(), registInfo.getDoctorName());
                patientFacadeService.markFirstVisit(registInfo.getPatientId(), queue.getEndTime(),
                        registInfo.getDeptId(), registInfo.getDeptName(),
                        registInfo.getDoctorId(), registInfo.getDoctorName());
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean overdueQueue(Long queueId, String reason) {
        Queue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        // 过号只受理「候诊中」：过号的真实场景是「叫了没到、人不在」，而就诊中(3)的患者
        // 已经坐在医生面前 —— 对就诊中过号等于让分诊台把医生正在接诊的患者踢出队列，
        // 与「分诊台不得抢占/收口医生当前就诊」是同一条红线（叫号权已收归医生工作站）。
        if (queue.getQueueStatus() != QueueStatusEnum.WAITING.getCode()) {
            throw new BusinessException("只能对候诊中的患者过号（就诊中的患者由医生工作站处理）");
        }
        // 过号置 OVERDUE(6) 而不是 CANCELLED(5)：5 是"已退号"，混用会让分诊台/门诊日志
        // 把过号患者显示成已退号。过号与退号是两件事，只在 is_overdue 上做标记不够。
        queue.setQueueStatus(QueueStatusEnum.OVERDUE.getCode());
        queue.setIsOverdue(1);
        queue.setOverdueTime(LocalDateTime.now());
        queue.setOverdueReason(reason);
        // 守卫下沉到 WHERE：仅「候诊中」可过号，并发叫号/完成等状态变更时 0 行失败
        int overdue = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
        if (overdue == 0) {
            throw new BusinessException("当前状态不允许过号处理，请刷新后重试");
        }

        // 同步更新挂号记录状态
        if (queue.getRegistId() != null) {
            AppointInfo registInfo = appointInfoMapper.selectById(queue.getRegistId());
            if (registInfo != null) {
                registInfo.setRegistStatus(AppointStatusEnum.OVERDUE.getCode());
                appointInfoMapper.updateById(registInfo);
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

        // 本单结算方式（两档）：1-全自费 2-医保联网。是否走统筹只看本单是否联网；
        // 险种不再用 settlementType 表达（它已合并为两档），改为由费别 medicalInsuranceType 解析。
        boolean isInsurancePatient = SettlementModeEnum.INSURANCE.getCode().equals(dto.getSettlementType());

        // 设置医保类型显示名称
        if (!isInsurancePatient) {
            vo.setInsuranceType("自费");
        } else {
            vo.setInsuranceType(dto.getMedicalInsuranceType() != null ? dto.getMedicalInsuranceType() : "医保");
        }

        // 根据结算方式计算
        if (!isInsurancePatient) {
            // 全自费
            vo.setCoverageRatio(BigDecimal.ZERO);
            vo.setInsurancePay(BigDecimal.ZERO);
            vo.setSelfPay(totalFee);
        } else {
            // 医保联网：统筹比例需按费别码值取政策比例；本入参 medicalInsuranceType 为费别名称(String)，
            // 与 ChargeFacadeService#getCoverageRatio 的码值(Integer)入参未对齐，暂按 0（全额自付）预估，
            // 不编造统筹比例；真实结算比例以收费域账单为准。
            BigDecimal coverageRatio = BigDecimal.ZERO;
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
        Queue queue = baseMapper.selectById(queueId);
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
            AppointInfo registInfo = queue.getRegistId() == null
                    ? null : appointInfoMapper.selectById(queue.getRegistId());
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
        LambdaUpdateWrapper<Queue> grab = new LambdaUpdateWrapper<>();
        grab.eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, expectedStatus)
                .set(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .set(Queue::getCallTime, now)
                .set(Queue::getStartTime, now)
                .set(Queue::getCallCount, queue.getCallCount() != null ? queue.getCallCount() + 1 : 1);
        if (baseMapper.update(null, grab) == 0) {
            throw new BusinessException("该患者刚被分诊台或其他终端接走，请刷新队列后重试");
        }

        // 旧实现把「上一位就诊中」改回**候诊中(2)**，等于把刚看完的人重新塞回候诊队列
        // （下次「接诊下一位」会再叫到他一次）；而且用 selectOne 取上一位，
        // 一旦库里有多条就诊中就直接抛 TooManyResults 500。这里统一收口成
        // 「结束上一位 + 按医生收口 + 回执告示」，与 callNext 同一套语义。
        Queue previous = closeConsultingOf(queue.getDeptId(), doctorId, queue.getId());
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
        Queue queue = baseMapper.selectById(queueId);
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
        // 守卫下沉到 WHERE：重呼仅限「就诊中」，防并发完成后用陈旧快照把队列写回就诊中
        int recalled = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode()));
        if (recalled == 0) {
            throw new BusinessException("只能重呼就诊中的患者，请刷新后重试");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer rejoinQueue(Long queueId, Integer mode) {
        Queue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        // 两种语义必须分开（2026-10-22 门诊版收口）：
        // mode=1 置为优先 —— 候诊中的患者（复诊回诊 / 老幼特殊）由护士提上来，**插到队首**；
        // mode=2 过号回队 —— 叫了没来、人又回来了，**排到队尾**（不插队，避免挤掉正常排队的人）。
        // 原先两者共用"插队首"，过号患者一回来就顶到第一位，对排在前面的患者不公平（投诉高发点）。
        int m = mode == null ? 1 : mode;
        Integer st = queue.getQueueStatus();
        boolean waiting = st != null && st.equals(QueueStatusEnum.WAITING.getCode());
        boolean overdue = st != null && st.equals(QueueStatusEnum.OVERDUE.getCode());
        if (m == 1) {
            if (!waiting) {
                throw new BusinessException("只能对候诊中的患者置为优先");
            }
        } else if (m == 2) {
            if (!overdue) {
                throw new BusinessException("只能对已过号的患者执行过号回队");
            }
        } else {
            throw new BusinessException("mode 取值不合法（1-置为优先 2-过号回队）");
        }
        // 跨日的记录不能回队：叫号只取「就诊日 = 今天」的候诊行，把昨天的行改回候诊中
        // 等于制造一行谁都叫不到的记录 —— 页面上看不出异常，患者白等。隔日号请重新挂号。
        LocalDate visitDate = queue.getVisitDate() != null ? queue.getVisitDate() : LocalDate.now();
        if (!visitDate.equals(LocalDate.now())) {
            throw new BusinessException("该排队记录的就诊日是 " + visitDate + "，已跨日不能回队；"
                    + "请让患者重新挂号（或先办理改约）");
        }
        // 取号范围口径：只动**这位医生**的队列（callNext 按 dept + doctorId 取号，医生站列表也按
        // doctorId 过滤）。原先只按 dept_id 操作会让同科室其他医生的候诊序号被整体挤动。
        Long doctorId = queue.getDoctorId();
        int newSeq;
        if (m == 1) {
            // ===== 置为优先：插到该医生候诊队首 =====
            LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Queue::getDeptId, queue.getDeptId())
                    .eq(doctorId != null, Queue::getDoctorId, doctorId)
                    .eq(Queue::getVisitDate, visitDate)
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .ne(Queue::getId, queue.getId())
                    .orderByAsc(Queue::getSequenceNo)
                    .select(Queue::getSequenceNo)
                    .last("LIMIT 1");
            Queue first = baseMapper.selectOne(wrapper);
            int headSeq = first != null && first.getSequenceNo() != null ? first.getSequenceNo() : 1;
            // 占到队首：不再用「最小序号 - 1」（反复插队会插到 0/-1；Math.max(0,..) 会把多次插队
            // 压成并列）。改成把当前候诊行整体后移一位，插队行占住原队首：序号仍唯一且递增。
            baseMapper.update(null, new LambdaUpdateWrapper<Queue>()
                    .eq(Queue::getDeptId, queue.getDeptId())
                    .eq(doctorId != null, Queue::getDoctorId, doctorId)
                    .eq(Queue::getVisitDate, visitDate)
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .ne(Queue::getId, queue.getId())
                    .ge(Queue::getSequenceNo, headSeq)
                    .setSql("sequence_no = sequence_no + 1"));
            queue.setSequenceNo(headSeq);
            // 优先标记，供大屏与分诊台区分
            queue.setQueueType(QueueTypeEnum.PRIORITY.getCode());
            newSeq = headSeq;
        } else {
            // ===== 过号回队：排到该医生候诊队尾 =====
            queue.setQueueStatus(QueueStatusEnum.WAITING.getCode());
            // 过号标记要清掉：状态已经是候诊中，is_overdue 还留 1 会让分诊台的
            // 「过号」筛选与统计把这人算进去。过号时间/原因保留，作为发生过的事实留痕。
            queue.setIsOverdue(0);
            // 挂号状态跟着回到「已签到」，否则门诊日志里这人还是「已过号」
            if (queue.getRegistId() != null) {
                AppointInfo registInfo = appointInfoMapper.selectById(queue.getRegistId());
                if (registInfo != null && registInfo.getRegistStatus() != null
                        && registInfo.getRegistStatus() == AppointStatusEnum.OVERDUE.getCode()) {
                    registInfo.setRegistStatus(AppointStatusEnum.CHECKED_IN.getCode());
                    appointInfoMapper.updateById(registInfo);
                }
            }
            // 取该医生候诊中当前最大序号 → 排到队尾（+1），不插队
            LambdaQueryWrapper<Queue> tailWrapper = new LambdaQueryWrapper<>();
            tailWrapper.eq(Queue::getDeptId, queue.getDeptId())
                    .eq(doctorId != null, Queue::getDoctorId, doctorId)
                    .eq(Queue::getVisitDate, visitDate)
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .select(Queue::getSequenceNo)
                    .orderByDesc(Queue::getSequenceNo)
                    .last("LIMIT 1");
            Queue tail = baseMapper.selectOne(tailWrapper);
            int tailSeq = tail != null && tail.getSequenceNo() != null ? tail.getSequenceNo() : 0;
            queue.setSequenceNo(tailSeq + 1);
            queue.setQueueType(QueueTypeEnum.NORMAL.getCode());
            newSeq = tailSeq + 1;
        }
        // 守卫下沉到 WHERE：写回仅限「候诊中/已过号」原状态，防并发叫号/完成后用陈旧快照复活队列
        int rejoined = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .in(Queue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(), QueueStatusEnum.OVERDUE.getCode()));
        if (rejoined == 0) {
            throw new BusinessException("该队列状态已变更，重新入队失败，请刷新后重试");
        }
        // 把新序号回给前端 —— 护士要知道「现在排第几」，只说"已入队"等于没说
        return newSeq;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer moveQueue(Long queueId, Integer step) {
        Queue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() == null
                || !queue.getQueueStatus().equals(QueueStatusEnum.WAITING.getCode())) {
            throw new BusinessException("只能对候诊中的患者调号");
        }
        if (step == null || step == 0) {
            throw new BusinessException("移动步数不能为 0");
        }
        LocalDate visitDate = queue.getVisitDate() != null ? queue.getVisitDate() : LocalDate.now();
        Long doctorId = queue.getDoctorId();
        // 取号范围口径与 callNext/rejoinQueue 一致：只动这位医生同日候诊中的行
        List<Queue> waiting = baseMapper.selectList(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getDeptId, queue.getDeptId())
                .eq(doctorId != null, Queue::getDoctorId, doctorId)
                .eq(Queue::getVisitDate, visitDate)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .orderByAsc(Queue::getSequenceNo));
        if (waiting.size() <= 1) {
            return queue.getSequenceNo();
        }
        int idx = -1;
        for (int i = 0; i < waiting.size(); i++) {
            if (waiting.get(i).getId().equals(queue.getId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new BusinessException("该患者不在候诊队列中，请刷新后重试");
        }
        int target = Math.max(0, Math.min(waiting.size() - 1, idx + step));
        if (target == idx) {
            return queue.getSequenceNo();
        }
        int toSeq = waiting.get(target).getSequenceNo();
        List<Long> shiftedIds;
        String shiftSql;
        if (target > idx) {
            // 后移：被跨过的行（idx+1..target）整体前移一位
            shiftedIds = waiting.subList(idx + 1, target + 1).stream()
                    .map(Queue::getId).collect(Collectors.toList());
            shiftSql = "sequence_no = sequence_no - 1";
        } else {
            // 前移：被跨过的行（target..idx-1）整体后移一位
            shiftedIds = waiting.subList(target, idx).stream()
                    .map(Queue::getId).collect(Collectors.toList());
            shiftSql = "sequence_no = sequence_no + 1";
        }
        baseMapper.update(null, new LambdaUpdateWrapper<Queue>()
                .in(Queue::getId, shiftedIds)
                .setSql(shiftSql));
        // 当前行写入目标位置，守卫仅候诊中：防并发叫号/完成后用陈旧快照把已接诊的行再插回去
        queue.setSequenceNo(toSeq);
        int updated = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
        if (updated == 0) {
            throw new BusinessException("该队列状态已变更，调号失败，请刷新后重试");
        }
        return toSeq;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueReassignVO reassignQueue(Long queueId, Long doctorId, Long roomId) {
        Queue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        if (queue.getQueueStatus() == null
                || !queue.getQueueStatus().equals(QueueStatusEnum.WAITING.getCode())) {
            throw new BusinessException("只能对候诊中的患者改派");
        }
        if (doctorId == null) {
            throw new BusinessException("目标医生不能为空");
        }
        if (doctorId.equals(queue.getDoctorId())
                && (roomId == null || roomId.equals(queue.getRoomId()))) {
            throw new BusinessException("目标医生/诊室与当前一致，无需改派");
        }
        EmployeeBriefVO doctor = systemFacadeService.findEmployee(doctorId);
        if (doctor == null) {
            throw new BusinessException("目标医生不存在");
        }
        LocalDate visitDate = queue.getVisitDate() != null ? queue.getVisitDate() : LocalDate.now();
        // 换诊室 → 按目标诊室前缀重排队号（排队号前缀 = 诊室编号，患者按号找诊室）；
        // 同诊室换医生 → 号不变、诊室快照不动
        if (roomId != null && !roomId.equals(queue.getRoomId())) {
            ClinicRoomBriefVO room = systemFacadeService.findClinicRoom(roomId);
            if (room == null) {
                throw new BusinessException("目标诊室不存在");
            }
            queue.setRoomId(roomId);
            queue.setRoomName(room.getName());
            queue.setQueueNo(buildQueueNoByRoom(queue.getDeptId(), roomId));
        }
        // 目标医生候诊队尾
        int tailSeq = 0;
        Queue tail = baseMapper.selectOne(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getDeptId, queue.getDeptId())
                .eq(Queue::getDoctorId, doctorId)
                .eq(Queue::getVisitDate, visitDate)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .select(Queue::getSequenceNo)
                .orderByDesc(Queue::getSequenceNo)
                .last("LIMIT 1"));
        if (tail != null && tail.getSequenceNo() != null) {
            tailSeq = tail.getSequenceNo();
        }
        // 原医生队列中排在后面的候诊行整体前移一位（保持序号连续、无空洞）
        if (queue.getDoctorId() != null && queue.getSequenceNo() != null) {
            baseMapper.update(null, new LambdaUpdateWrapper<Queue>()
                    .eq(Queue::getDeptId, queue.getDeptId())
                    .eq(Queue::getDoctorId, queue.getDoctorId())
                    .eq(Queue::getVisitDate, visitDate)
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                    .gt(Queue::getSequenceNo, queue.getSequenceNo())
                    .setSql("sequence_no = sequence_no - 1"));
        }
        queue.setDoctorId(doctorId);
        queue.setDoctorName(doctor.getEmpName());
        queue.setSequenceNo(tailSeq + 1);
        // 改派后按普通队列排（不再保留原优先标记，避免旧状态带到新医生队列）
        queue.setQueueType(QueueTypeEnum.NORMAL.getCode());
        int updated = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
        if (updated == 0) {
            throw new BusinessException("该队列状态已变更，改派失败，请刷新后重试");
        }
        // 挂号快照医生同步：医生站/结算按挂号取接诊医生，队列改派后必须跟着改，否则
        // 「医生站里是 A，收费单上写 B」的错位。号源不随改派转移（如需换号源走改约）。
        if (queue.getRegistId() != null) {
            AppointInfo regist = appointInfoMapper.selectById(queue.getRegistId());
            if (regist != null && !doctorId.equals(regist.getDoctorId())) {
                regist.setDoctorId(doctorId);
                regist.setDoctorName(doctor.getEmpName());
                appointInfoMapper.updateById(regist);
            }
        }
        QueueReassignVO vo = new QueueReassignVO();
        vo.setQueueId(queue.getId());
        vo.setQueueNo(queue.getQueueNo());
        vo.setSequenceNo(queue.getSequenceNo());
        vo.setDoctorId(doctorId);
        vo.setDoctorName(doctor.getEmpName());
        vo.setRoomId(queue.getRoomId());
        vo.setRoomName(queue.getRoomName());
        return vo;
    }

    @Override
    public List<DoctorConsultingVO> getDoctorConsultingInfo(Long deptId) {
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        if (scoped == null || scoped.isEmpty()) {
            return new ArrayList<>();
        }
        // 今日出诊号源医生名单来自号源端口；接诊状态仍由本地 Redis 缓存叠加，不编造名单
        List<ClinicSourceBriefVO> sources = clinicSourceFacadeService.listTodayClinicSources(scoped, LocalDate.now());
        Map<Long, DoctorConsultingVO> doctorMap = new LinkedHashMap<>();
        for (ClinicSourceBriefVO source : sources) {
            Long doctorId = source.getDoctorId();
            if (doctorId == null) {
                continue;
            }
            DoctorConsultingVO vo = doctorMap.computeIfAbsent(doctorId, k -> {
                DoctorConsultingVO nv = new DoctorConsultingVO();
                nv.setDoctorId(doctorId);
                nv.setDoctorName(source.getDoctorName());
                nv.setRoomId(source.getRoomId());
                nv.setRoomName(source.getRoomName());
                nv.setScheduleId(source.getId());
                return nv;
            });
            vo.setConsultStatus(doctorStatusCacheService.getStatus(doctorId));
        }
        return new ArrayList<>(doctorMap.values());
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
        long waiting = this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
        long consulting = this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode()));
        long completed = this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode()));
        long overdue = this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.OVERDUE.getCode()));

        // 待签到数量
        long unchecked = countPaidUncheckedAppoints(deptId);

        stats.setWaiting(waiting);
        stats.setConsulting(consulting);
        stats.setCompleted(completed);
        stats.setOverdue(overdue);
        stats.setUnchecked(unchecked);

        //挂号总数
        stats.setTotal(appointInfoMapper.selectCount(new LambdaQueryWrapper<AppointInfo>()
                .in(scoped != null, AppointInfo::getDeptId, scoped)));

        // 坐诊医生数量：今日号源医生去重计数（接诊状态不影响人数），无号源时为 0
        long doctorCount = clinicSourceFacadeService.listTodayClinicSources(scoped, LocalDate.now())
                .stream()
                .map(ClinicSourceBriefVO::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        stats.setDoctorCount((int) doctorCount);

        return stats;
    }

    @Override
    public List<DoctorConsultingVO> getConsultingPatients(Long deptId) {
        // 查询当前就诊中的患者（按医生分组）
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .orderByAsc(Queue::getDoctorId);

        List<Queue> queues = baseMapper.selectList(wrapper);

        // 按医生ID分组
        Map<Long, DoctorConsultingVO> doctorMap = new LinkedHashMap<>();
        for (Queue queue : queues) {
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
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        // 「今天」= visit_date（与列表/叫号同口径）。漏了它，昨天入队后一直没人收摊的
        // 「候诊中」会永久挂在这个面板上 —— 护士看到的是昨天的患者，还标着「等候 20时13分」。
        wrapper.in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getVisitDate, LocalDate.now())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .isNotNull(Queue::getArriveTime)
                .le(Queue::getArriveTime, LocalDateTime.now().minusHours(1))
                .orderByAsc(Queue::getArriveTime);

        List<Queue> queues = baseMapper.selectList(wrapper);

        // 过滤出复诊患者
        List<BizQueueListVO> result = new ArrayList<>();
        for (Queue queue : queues) {
            // 查询挂号记录，判断是否复诊
            AppointInfo appointInfo = appointInfoMapper.selectById(queue.getRegistId());
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
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scoped != null, Queue::getDeptId, scoped);
        List<Queue> queues = baseMapper.selectList(wrapper);

        // 按医生ID分组统计
        Map<Long, DoctorStatsVO> doctorMap = new LinkedHashMap<>();
        for (Queue queue : queues) {
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
    public PageResult<OpdLogListVO> listPage(OpdLogPageQueryDTO queryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        Page<OpdLogListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OpdLogListVO> result = clincOpdLogMapper.selectOpdLogPage(page, queryDTO);
        List<OpdLogListVO> records = result.getRecords();
        for (OpdLogListVO vo : records) {
            fillLogStatus(vo);
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(), records);
    }

    @Override
    public OpdLogStatsVO opdLogStats(OpdLogPageQueryDTO queryDTO) {
        OpdLogStatsVO stats = clincOpdLogMapper.selectOpdLogStats(queryDTO);
        if (stats == null) {
            return new OpdLogStatsVO();
        }
        return stats;
    }

    /**
     * 就诊状态文案由后端给，避免前端各写一份映射。
     */
    private void fillLogStatus(OpdLogListVO vo) {
        vo.setLogStatusLabel(OpdLogStatusEnum.getLabel(vo.getLogStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TriageRecordVO saveTriage(TriageUpsertDTO dto) {
        Queue queue = baseMapper.selectById(dto.getQueueId());
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        // 分诊等级码值由 TriageUpsertDTO.triageLevel 的 @AllowedValues 在边界校验
        // 已就诊/已过号/已退号的患者再分诊没有意义，反而会把队列上的「当前生效值」改乱。
        Integer status = queue.getQueueStatus();
        boolean changeable = status != null
                && (status.equals(QueueStatusEnum.WAITING.getCode())
                || status.equals(QueueStatusEnum.CONSULTING.getCode()));
        if (!changeable) {
            throw new BusinessException("该患者本次就诊已结束，不能再分诊");
        }

        Long roomId = queue.getRoomId();
        String roomName = queue.getRoomName();
        // 队列无诊室快照时，按挂号 scheduleId 回查号源补齐 roomId/roomName；再按 roomId 补诊室名
        if (roomId == null) {
            AppointInfo regist = queue.getRegistId() == null
                    ? null : appointInfoMapper.selectById(queue.getRegistId());
            if (regist != null && regist.getScheduleId() != null) {
                ClinicSourceBriefVO source = clinicSourceFacadeService.findClinicSource(regist.getScheduleId());
                if (source != null) {
                    roomId = source.getRoomId();
                    roomName = source.getRoomName();
                }
            }
        }
        if (roomId != null && !TextUtil.hasText(roomName)) {
            ClinicRoomBriefVO room = systemFacadeService.findClinicRoom(roomId);
            roomName = room != null ? room.getName() : null;
        }

        // BMI 由后端算：前端算一遍后端再信一遍，迟早两边不一致。
        BigDecimal bmi = null;
        if (dto.getHeight() != null && dto.getWeight() != null
                && dto.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal h = dto.getHeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            bmi = dto.getWeight().divide(h.multiply(h), 1, RoundingMode.HALF_UP);
        }

        LocalDateTime now = LocalDateTime.now();

        TriageRecord record = new TriageRecord();
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
        // 按科室 + 护理岗取当班责任人（优先本人）；该科室此刻无排班则留空，身份凭证 ≠ 当班责任，宁缺勿假。
        CurrentUser currentUser = UserUtils.getCurrentUser();
        DutyStaffBriefVO dutyNurse = dutyRosterFacadeService.pickDutyStaff(
                queue.getDeptId(), StaffTypeEnum.NURSE.getCode(),
                currentUser == null ? null : currentUser.getEmployeeId());
        if (dutyNurse != null) {
            record.setTriageNurseId(dutyNurse.getStaffId());
            record.setTriageNurseName(dutyNurse.getStaffName());
        }
        record.setTriageTime(now);
        record.setRemark(dto.getRemark());
        // 只增不改：重测体温/重新定级是**追加**一条，历史留痕不动
        triageRecordMapper.insert(record);

        // 队列上存「当前生效值」快照，供列表与叫号排序使用。
        // 门诊版：分级已从分诊卡移除（triage_level 可空），非空才回写，避免把
        // 急诊直录行已有的 I~IV 级清成 null；triage_status 置 1=护士已处理。
        queue.setTriageStatus(1);
        if (dto.getTriageLevel() != null) {
            queue.setTriageLevel(dto.getTriageLevel());
        }
        queue.setRoomId(roomId);
        queue.setRoomName(roomName);
        // 守卫下沉到 WHERE：分诊快照仅回写「候诊中/就诊中」队列，防把并发已结束的队列复活
        int snapshot = baseMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                .eq(Queue::getId, queue.getId())
                .in(Queue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(), QueueStatusEnum.CONSULTING.getCode()));
        if (snapshot == 0) {
            throw new BusinessException("该患者本次就诊已结束，不能再分诊，请刷新后重试");
        }

        return toTriageVO(record);
    }

    @Override
    public TriageDetailVO getTriageByQueueId(Long queueId) {
        TriageDetailVO detail = new TriageDetailVO();
        if (queueId == null) {
            return detail;
        }
        Queue queue = baseMapper.selectById(queueId);
        if (queue == null) {
            return detail;
        }
        deptScopeService.assertDeptAccessible(queue.getDeptId());
        List<TriageRecord> list = triageRecordMapper.selectList(
                new LambdaQueryWrapper<TriageRecord>()
                        .eq(TriageRecord::getQueueId, queueId)
                        .orderByDesc(TriageRecord::getTriageTime)
                        .orderByDesc(TriageRecord::getId));
        if (list.isEmpty()) {
            return detail;
        }
        List<TriageRecordVO> history = new ArrayList<>(list.size());
        for (TriageRecord record : list) {
            history.add(toTriageVO(record));
        }
        detail.setHistory(history);
        detail.setLatest(history.get(0));
        return detail;
    }

    private TriageRecordVO toTriageVO(TriageRecord record) {
        TriageRecordVO vo = BeanUtil.copyProperties(record, TriageRecordVO.class);
        vo.setTriageLevelText(TriageLevelEnum.getLabel(record.getTriageLevel()));
        return vo;
    }

    @Override
    public List<BizQueueListVO> listPaidUnchecked(Long deptId, LocalDate visitDate) {
        List<Long> scoped = deptScopeService.scopedDeptIds(deptId);
        LambdaQueryWrapper<AppointInfo> appointWrapper = new LambdaQueryWrapper<>();
        appointWrapper.in(scoped != null, AppointInfo::getDeptId, scoped)
                .eq(AppointInfo::getRegistStatus, AppointStatusEnum.REGISTERED.getCode())
                .eq(visitDate != null, AppointInfo::getVisitDate, visitDate);
        List<AppointInfo> appoints = appointInfoMapper.selectList(appointWrapper);
        List<BizQueueListVO> result = new ArrayList<>();
        for (AppointInfo appoint : appoints) {
            // 挂号状态「已挂号」只说明还没签到，还得确认钱真的收了 ——
            // 未缴费也能挂号（挂完去收费处），把没交钱的人放进「待签到」会让护士白叫。
            // 免收的号没有账单，属于"没有欠账"，必须照常放进来（旧口径用 isNotNull(charge_id) 把他们整体筛掉了）。
            if (!isRegistFeeSettled(chargeFacadeService, appoint)) {
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
        List<AppointInfo> appoints = appointInfoMapper.selectList(
                new LambdaQueryWrapper<AppointInfo>()
                        .eq(AppointInfo::getPatientId, patientId)
                        .ge(AppointInfo::getVisitDate, from)
                        .ne(AppointInfo::getRegistStatus, AppointStatusEnum.CANCELLED.getCode())
                        .orderByDesc(AppointInfo::getVisitDate)
                        .orderByDesc(AppointInfo::getId));
        if (appoints.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> registIds = appoints.stream().map(AppointInfo::getId).collect(Collectors.toList());
        Map<Long, Queue> queueByRegist = this.list(new LambdaQueryWrapper<Queue>()
                        .in(Queue::getRegistId, registIds))
                .stream()
                .collect(Collectors.toMap(Queue::getRegistId, q -> q, (a, b) -> a));

        List<PatientQueueVO> list = new ArrayList<>();
        for (AppointInfo appoint : appoints) {
            Queue queue = queueByRegist.get(appoint.getId());
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
                vo.setQueueStatusText(QueueStatusEnum.getLabel(queue.getQueueStatus()));
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
    private int countAhead(Queue queue) {
        return (int) this.count(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getDeptId, queue.getDeptId())
                .eq(queue.getDoctorId() != null, Queue::getDoctorId, queue.getDoctorId())
                .eq(Queue::getVisitDate, queue.getVisitDate())
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .lt(queue.getArriveTime() != null, Queue::getArriveTime, queue.getArriveTime()));
    }

    /**
     * 当前已叫到的序号：同口径队列里已被叫过（就诊中/已就诊/过号）的最大顺序号
     */
    private Integer currentCalledNo(Queue queue) {
        List<Queue> called = this.list(new LambdaQueryWrapper<Queue>()
                .eq(Queue::getDeptId, queue.getDeptId())
                .eq(queue.getDoctorId() != null, Queue::getDoctorId, queue.getDoctorId())
                .eq(Queue::getVisitDate, queue.getVisitDate())
                .in(Queue::getQueueStatus,
                        QueueStatusEnum.CONSULTING.getCode(),
                        QueueStatusEnum.COMPLETED.getCode(),
                        QueueStatusEnum.OVERDUE.getCode()));
        return called.stream()
                .map(Queue::getSequenceNo)
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
        if (!Objects.equals(dto.getStatus(), DoctorConsultStatusEnum.IDLE.getCode())
                && !Objects.equals(dto.getStatus(), DoctorConsultStatusEnum.PAUSED.getCode())) {
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
        queueStatsVO.setWaiting(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .ge(Queue::getArriveTime, dateStart)
                .le(Queue::getArriveTime, dateEnd)));
        queueStatsVO.setConsulting(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.CONSULTING.getCode())
                .ge(Queue::getArriveTime, dateStart)
                .le(Queue::getArriveTime, dateEnd)));
        queueStatsVO.setCompleted(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.COMPLETED.getCode())
                .ge(Queue::getArriveTime, dateStart)
                .le(Queue::getArriveTime, dateEnd)));
        queueStatsVO.setOverdue(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.OVERDUE.getCode())
                .ge(Queue::getArriveTime, dateStart)
                .le(Queue::getArriveTime, dateEnd)));
        queueStatsVO.setUnchecked(countPaidUncheckedAppoints(queueQueryDTO.getDeptId()));
        queueStatsVO.setTotal(queueStatsVO.getWaiting() + queueStatsVO.getConsulting()
                + queueStatsVO.getCompleted() + queueStatsVO.getOverdue());

        // 未核验分级：2026-10-22 门诊收口后，签到/分诊一律写 triage_status=1，
        // 已没有入口产生 triage_status=0 的行，这个数恒为 0 —— 保留字段仅为接口兼容，
        // 门诊分诊台不再展示。
        queueStatsVO.setUnTriage(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(Queue::getVisitDate, LocalDate.now())
                .eq(Queue::getTriageStatus, 0)));

        // 危重待接诊：今日候诊中 1/2 级的人数，只可能是急诊科直录的行（门诊行 triage_level
        // 为 null 不参与）。callSpecific 会拒绝越过他们去叫普通患者，急诊分诊台展示用。
        queueStatsVO.setCriticalWaiting(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(Queue::getVisitDate, LocalDate.now())
                .le(Queue::getTriageLevel, TriageLevelEnum.EMERGENCY.getCode())));

        // 候诊超时：现算，不落状态列
        queueStatsVO.setTimeout(this.count(new LambdaQueryWrapper<Queue>()
                .in(scoped != null, Queue::getDeptId, scoped)
                .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode())
                .eq(Queue::getVisitDate, LocalDate.now())
                .le(Queue::getArriveTime, LocalDateTime.now().minusMinutes(30))));

        return queueStatsVO;
    }

    @Override
    public void checkInByRegist(AppointCheckInUpdateDTO updateDTO) {
        // 分诊台/自助机用的接口，患者 token 也调得到 —— 不校验归属就等于能替别人签到
        if (patientFacadeService.patientScopeViolated(patientIdOfRegist(updateDTO.getRegistId()))) {
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