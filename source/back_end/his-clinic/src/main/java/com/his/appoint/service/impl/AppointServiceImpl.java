package com.his.appoint.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.provider.dto.AppointCancelDTO;
import com.his.provider.dto.AppointQueryDTO;
import com.his.provider.dto.AppointUpsertDTO;
import com.his.provider.dto.RevisitFeePreviewDTO;
import com.his.provider.service.ChargeProvider;
import com.his.provider.service.EmrProvider;
import com.his.provider.service.PatientProvider;
import com.his.provider.vo.*;
import com.his.appoint.dto.AppointBoardQueryDTO;
import com.his.appoint.entity.AppointInfo;
import com.his.appoint.entity.Queue;
import com.his.common.enums.QueueStatusEnum;
import com.his.common.enums.RegistTypeEnum;
import com.his.appoint.mapper.AppointInfoMapper;
import com.his.appoint.mapper.QueueMapper;
import com.his.appoint.service.AppointService;
import com.his.appoint.service.RevisitFeePolicyService;
import com.his.appoint.trigger.DayEndSettleTrigger;
import com.his.appoint.vo.AppointStatusCountVO;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.system.entity.BizClinicSource;
import com.his.system.entity.BizClinicSourceSlot;
import com.his.system.entity.BizSchedule;
import com.his.common.entity.CurrentUser;
import com.his.system.mapper.BizClinicSourceMapper;
import com.his.system.mapper.BizClinicSourceSlotMapper;
import com.his.provider.service.DeptScopeService;
import com.his.system.service.RedisSequenceService;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffScheduleService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 挂号服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointServiceImpl extends ServiceImpl<AppointInfoMapper, AppointInfo> implements AppointService {

    private static final int REGIST_CUTOFF_MINUTES = 10;

    private final DeptScopeService deptScopeService;

    private final BizClinicSourceMapper bizScheduleMapper;

    private final BizClinicSourceSlotMapper bizScheduleSlotMapper;

    private final QueueMapper queueMapper;

    private final RedisSequenceService redisSequenceService;

    private final ShiftService shiftService;

    private final StaffScheduleService staffScheduleService;

    private final RevisitFeePolicyService revisitFeePolicyService;

    private final DayEndSettleTrigger dayEndSettleTrigger;

    private final PatientProvider patientProvider;

    private final ChargeProvider chargeProvider;

    private final EmrProvider emrProvider;

    private static String visitDatePastReason(AppointInfo regist, String action) {
        LocalDate visitDate = regist.getVisitDate();
        if (visitDate == null || !visitDate.isBefore(LocalDate.now())) {
            return null;
        }
        return "就诊日 " + visitDate + " 已过，不允许" + action
                + "：患者未到诊需退挂号费的，请到收费处「退费管理」发起退费申请。";
    }

    /**
     * 分时段挂号时间闸（新增/改约共用，后端兜底）：
     */
    private static void assertRegistTimeNotPassed(LocalDate schedDate, String startText, String action) {
        LocalDate today = LocalDate.now();
        if (schedDate == null || !TextUtil.hasText(startText)) {
            return;
        }
        if (schedDate.isBefore(today)) {
            throw new BusinessException("该号源就诊日期（" + schedDate + "）已过，不允许" + action);
        }
        if (!schedDate.equals(today)) {
            return;
        }
        String hm = startText.length() >= 5 ? startText.substring(0, 5) : startText;
        LocalTime start;
        try {
            start = LocalTime.parse(hm);
        } catch (Exception e) {
            return;
        }
        if (!LocalTime.now().isBefore(start.minusMinutes(REGIST_CUTOFF_MINUTES))) {
            throw new BusinessException("该时段（" + startText + "）已过挂号截止时间"
                    + "（开诊前 " + REGIST_CUTOFF_MINUTES + " 分钟截止），不允许" + action);
        }
    }

    private static String notAllowedReason(Integer registStatus, String action) {
        String label = AppointStatusEnum.getText(registStatus);
        String who = label == null ? "当前状态(" + registStatus + ")" : label;
        if (AppointStatusEnum.isFinal(registStatus)) {
            return who + " 不允许" + action + "：诊疗已结束，号源属过去（或已作废），不可再变更";
        }
        return who + " 不允许" + action
                + "（仅「已挂号」可调整/变更号源，「已挂号/已签到」可退号；已接诊/已就诊为诊疗事实，锁死）";
    }

    /**
     * 号源原价 = 挂号费 + 诊查费，与出账口径一致（专家号的高低体现在挂号费上，不另列）
     */
    private static BigDecimal scheduleFeeTotal(BizClinicSource schedule) {
        return NumUtil.orZero(schedule.getRegistFee()).add(NumUtil.orZero(schedule.getDiagnosisFee()));
    }

    /**
     * 现场扣号是否放宽预约池限制：预约渠道自己就是从池里扣，不涉及放宽；
     * 现场渠道仅在「就诊日不晚于今天」时放宽——池是给提前日远程预约留的额度，
     * 到了就诊日还锁着池，现场就会出现「界面显示有余号却挂不上」。
     */
    private static boolean isSameDayWalkin(boolean apptChannel, LocalDate visitDate) {
        return !apptChannel && visitDate != null && !visitDate.isAfter(LocalDate.now());
    }

    @Override
    public PageResult<BizAppointInfoListVO> listPage(AppointQueryDTO queryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        LambdaQueryWrapper<AppointInfo> wrapper = buildFilter(queryDTO);
        wrapper.orderByDesc(AppointInfo::getCreateTime)
                .orderByDesc(AppointInfo::getId);

        Page<AppointInfo> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PageResult<>();
        }

        List<BizAppointInfoListVO> voList = toVOList(page.getRecords());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    /**
     * 挂号列表与状态统计**共用**的筛选条件（不含分页、不含排序）。
     *
     * <p>抽出来是为了让「列表显示的条数」与「卡片上的条数」不可能各写一遍再慢慢跑偏 ——
     * 这两处口径一旦分叉，页面就会出现「筛选后列表 3 条、卡片还是全院总数」这种没人报错的错。
     */
    private LambdaQueryWrapper<AppointInfo> buildFilter(AppointQueryDTO queryDTO) {
        LambdaQueryWrapper<AppointInfo> wrapper = new LambdaQueryWrapper<AppointInfo>()
                .eq(queryDTO.getPatientId() != null, AppointInfo::getPatientId, queryDTO.getPatientId())
                .eq(queryDTO.getDoctorId() != null, AppointInfo::getDoctorId, queryDTO.getDoctorId())
                .eq(queryDTO.getVisitDate() != null, AppointInfo::getVisitDate, queryDTO.getVisitDate())
                .eq(queryDTO.getRegistStatus() != null, AppointInfo::getRegistStatus, queryDTO.getRegistStatus())
                .ge(queryDTO.getBeginTime() != null, AppointInfo::getCreateTime, queryDTO.getBeginTime())
                .le(queryDTO.getEndTime() != null, AppointInfo::getCreateTime, queryDTO.getEndTime());
        applyDeptScope(wrapper, queryDTO.getDeptId());
        return wrapper;
    }

    /**
     * 科室数据权限收口 —— 挂号/预约域对科室ID 的唯一写入口。
     *
     * <p>此前 deptId 是「可选过滤」：不传就是全院全时段，任何登录用户都能看全院号源
     * （角色.data_scope 有数据但后端零消费）。
     *
     * <p>三种情形，**必须区分开**，否则会得出相反结论：
     * <ul>
     *   <li>不限权（超管 / {@code data_scope=1}）→ 不追加任何条件，保持原语义</li>
     *   <li>限权 + 传了 deptId → 越权直接拒绝（{@code resolveDeptId} 抛业务异常），授权内则等值过滤</li>
     *   <li>限权 + 没传 deptId → 收敛到「授权科室集合」。<b>注意这里是收敛而不是放行</b>：
     *       旧的 {@code eq(deptId != null, ...)} 在不传时等于全放行，正是本次要堵的洞</li>
     * </ul>
     */
    private void applyDeptScope(LambdaQueryWrapper<AppointInfo> wrapper, Long requestedDeptId) {
        Long scopedDeptId = deptScopeService.resolveDeptId(requestedDeptId);
        if (scopedDeptId != null) {
            wrapper.eq(AppointInfo::getDeptId, scopedDeptId);
        } else if (deptScopeService.isScoped()) {
            wrapper.in(AppointInfo::getDeptId, deptScopeService.allowedDeptIds());
        }
    }

    @Override
    public AppointStatusCountVO statusCount(AppointQueryDTO queryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        // 状态口径必须与 AppointStatusEnum 对齐：1已挂号 2已签到 4已就诊 5已退号 6已过号。
        // 注意 3（已接诊）是流程中间态、7（爽约）/8（未就诊）是收尾态：
        // 六格卡片里没有它们的位置，所以**只统计这五个 + 总数**，不要拿枚举全量去凑。
        AppointStatusCountVO vo = new AppointStatusCountVO();
        vo.setTotal(countByStatus(queryDTO, null));
        vo.setWaiting(countByStatus(queryDTO, AppointStatusEnum.REGISTERED.getCode()));
        vo.setCheckedIn(countByStatus(queryDTO, AppointStatusEnum.CHECKED_IN.getCode()));
        vo.setCompleted(countByStatus(queryDTO, AppointStatusEnum.COMPLETED.getCode()));
        vo.setRefunded(countByStatus(queryDTO, AppointStatusEnum.CANCELLED.getCode()));
        vo.setOverdue(countByStatus(queryDTO, AppointStatusEnum.OVERDUE.getCode()));
        vo.setScopeLabel(buildScopeLabel(queryDTO));
        vo.setStatsTime(TimeUtil.nowSeconds());
        return vo;
    }

    /**
     * 按状态取条数。{@code status == null} 表示不按状态过滤（总数）。
     */
    private Long countByStatus(AppointQueryDTO queryDTO, Integer status) {
        AppointQueryDTO scoped = new AppointQueryDTO();
        // 复制筛选条件后**覆盖** registStatus：总数那格不能被传进来的 status 影响
        scoped.setPatientId(queryDTO.getPatientId());
        scoped.setDeptId(queryDTO.getDeptId());
        scoped.setDoctorId(queryDTO.getDoctorId());
        scoped.setVisitDate(queryDTO.getVisitDate());
        scoped.setBeginTime(queryDTO.getBeginTime());
        scoped.setEndTime(queryDTO.getEndTime());
        scoped.setRegistStatus(status);
        return this.count(buildFilter(scoped));
    }

    /**
     * 统计范围的人话描述 —— 页面要能一眼看出这六个数是「哪一段」的，
     * 否则用户会把「当前筛选结果」误读成「全院总数」。
     *
     * <p><b>各条件是「且」的关系，所以必须逐个列全，不能写成 if / else if。</b>
     * 页面默认同时下发 {@code visitDate}（就诊日，默认今天）与 {@code beginTime/endTime}
     * （挂号时间，默认为本周），原先的 else-if 只报「就诊日 2026-09-21」，
     * 把「挂号时间限定在本周」这个同样在生效的过滤条件藏掉了 ——
     * 实测同一时刻接口不带参返回 621、只按就诊日返回 434、页面（两个条件都带）返回 105，
     * 而标签只写「就诊日 2026-09-21」，用户按标签推算只能算出 434，无法解释 105 的去向。
     * 这正是「展示的范围 ≠ 实际查询的范围」，与之前挂号页踩过的坑同源。
     */
    private String buildScopeLabel(AppointQueryDTO queryDTO) {
        List<String> parts = new ArrayList<>();
        if (queryDTO.getVisitDate() != null) {
            parts.add("就诊日 " + queryDTO.getVisitDate());
        }
        if (queryDTO.getBeginTime() != null && queryDTO.getEndTime() != null) {
            parts.add("挂号时间 " + queryDTO.getBeginTime().toLocalDate()
                    + " ~ " + queryDTO.getEndTime().toLocalDate());
        }
        if (parts.isEmpty()) {
            parts.add("全部时间");
        }
        if (queryDTO.getDeptId() != null) {
            parts.add("指定科室");
        }
        if (queryDTO.getDoctorId() != null) {
            parts.add("指定医生");
        }
        if (queryDTO.getPatientId() != null) {
            parts.add("指定患者");
        }
        return String.join(" · ", parts);
    }

    /**
     * 预约看板：一次取整段区间的全部挂号，**不分页**。
     *
     * <p>为什么不在 {@link #listPage} 上加个日期区间了事：看板要按「日期 × 班次 × 医生」铺格子，
     * 每格要算「已挂 N / 总号源」和「还有 M 人」，拿到某一页没有意义；按天拆成 7 次分页查询
     * 会变成 7×页数个请求，且任一分页被截断时看板只是「就这么几张」——**不报错**。
     */
    @Override
    public List<BizAppointInfoListVO> boardList(AppointBoardQueryDTO queryDTO) {
        dayEndSettleTrigger.ensureSettledUpToYesterday();
        LambdaQueryWrapper<AppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AppointInfo::getVisitDate, queryDTO.getStartDate(), queryDTO.getEndDate())
                .eq(queryDTO.getDeptId() != null, AppointInfo::getDeptId, queryDTO.getDeptId())
                .eq(queryDTO.getDoctorId() != null, AppointInfo::getDoctorId, queryDTO.getDoctorId())
                // 排序要**全序**（同一格内患者顺序稳定）：就诊日 → 班次 → 挂号时间 → id。
                // 最后一项是唯一键，同秒并列行才不会在两次请求间换位置（listPage 同样吃过这个亏）。
                .orderByAsc(AppointInfo::getVisitDate)
                .orderByAsc(AppointInfo::getScheduleId)
                .orderByAsc(AppointInfo::getCreateTime)
                .orderByAsc(AppointInfo::getId);
        return toVOList(this.list(wrapper));
    }

    /**
     * 批量装配挂号 VO —— 收费状态（SPI 到 his-charge）与队列号（候诊队列）都必须**批量**取。
     *
     * <p>看板一次要装几千条，循环里单条查会直接打穿数据库（N+1）；
     * 分页查询和看板全量查询共用这一段，避免两处口径各写一遍再慢慢跑偏。
     */
    private List<BizAppointInfoListVO> toVOList(List<AppointInfo> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        // 批量收集关联的账单ID / 挂号ID
        List<Long> billIds = new ArrayList<>();
        List<Long> registIds = new ArrayList<>();
        for (AppointInfo appoint : records) {
            if (appoint.getBillId() != null) {
                billIds.add(appoint.getBillId());
            }
            registIds.add(appoint.getId());
        }

        // 批量查询账单信息（经 SPI 委派至 his-charge）
        Map<Long, BillBriefVO> billMap = new HashMap<>();
        if (!billIds.isEmpty()) {
            billMap = chargeProvider.mapBillsByIds(billIds);
        }

        // 批量查询队列记录：挂号表本身没有排队号与队列状态，
        // 分诊台要显示真实排队状态就必须关联候诊队列（历史上前端拿 registStatus 顶替，状态列整体串位）
        Map<Long, Queue> queueMap = new HashMap<>();
        List<Queue> queues = queueMapper.selectList(
                new LambdaQueryWrapper<Queue>().in(Queue::getRegistId, registIds));
        for (Queue queue : queues) {
            // 同一挂号可能对应多条队列记录（过号后重入队），取 id 最大的一条作为当前队列
            Queue exist = queueMap.get(queue.getRegistId());
            if (exist == null || (queue.getId() != null && exist.getId() != null && queue.getId() > exist.getId())) {
                queueMap.put(queue.getRegistId(), queue);
            }
        }

        // 转换为VO并填充支付状态
        List<BizAppointInfoListVO> voList = new ArrayList<>();
        for (AppointInfo appoint : records) {
            voList.add(convertToVO(appoint, billMap, queueMap.get(appoint.getId())));
        }
        return voList;
    }

    /**
     * 将挂号实体转换为VO，包含账单状态与队列状态
     *
     * @param queue 该挂号当前的队列记录，可能为空（未签到）
     */
    private BizAppointInfoListVO convertToVO(AppointInfo appoint,
                                             Map<Long, BillBriefVO> billMap,
                                             Queue queue) {
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        vo.setId(appoint.getId());
        vo.setRegistNo(appoint.getRegistNo());
        vo.setPatientId(appoint.getPatientId());
        vo.setPatientNo(appoint.getPatientNo());
        vo.setPatientName(appoint.getPatientName());
        vo.setGender(appoint.getGender());
        vo.setAge(appoint.getAge());
        vo.setPhone(appoint.getPhone());
        vo.setDeptId(appoint.getDeptId());
        vo.setDeptName(appoint.getDeptName());
        vo.setDoctorId(appoint.getDoctorId());
        vo.setDoctorName(appoint.getDoctorName());
        vo.setScheduleId(appoint.getScheduleId());
        vo.setSlotId(appoint.getSlotId());
        vo.setSlotStart(appoint.getSlotStart());
        vo.setSlotEnd(appoint.getSlotEnd());
        vo.setRegistType(appoint.getRegistType());
        vo.setRegistSource(appoint.getRegistSource());
        vo.setRegistTime(appoint.getRegistTime());
        vo.setVisitType(appoint.getVisitType());
        vo.setRevisitSource(appoint.getRevisitSource());
        vo.setVisitDate(appoint.getVisitDate());
        vo.setArriveTime(appoint.getArriveTime());
        vo.setScheduleType(appoint.getScheduleType());
        vo.setSettlementType(appoint.getSettlementType());
        vo.setMedicalInsuranceType(appoint.getMedicalInsuranceType());
        vo.setMedicalInsuranceNo(appoint.getMedicalInsuranceNo());
        vo.setRegistStatus(appoint.getRegistStatus());
        vo.setRefundTime(appoint.getRefundTime());
        vo.setRefundReason(appoint.getRefundReason());
        vo.setBillId(appoint.getBillId());
        vo.setBillNo(appoint.getBillNo());

        // 从缓存的账单信息中取缴费状态
        if (appoint.getBillId() != null && billMap.containsKey(appoint.getBillId())) {
            BillBriefVO bill = billMap.get(appoint.getBillId());
            vo.setBillStatus(bill.getBillStatus());
            vo.setTotalFee(bill.getPayableAmount());
        }

        // 队列信息（未签到时整组为空，前端据此显示「待签到」）
        if (queue != null) {
            vo.setQueueId(queue.getId());
            vo.setQueueNo(queue.getQueueNo());
            vo.setQueueStatus(queue.getQueueStatus());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointInfo addAppoint(AppointUpsertDTO upsertDTO) {
        boolean revisit = upsertDTO.getVisitType() != null && upsertDTO.getVisitType() == VisitTypeEnum.REVISIT.getCode();
        RecordBriefVO origin = validateRevisit(upsertDTO, revisit);
        Long revisitRecordId = origin == null ? null : origin.getId();

        // 幂等性校验：检查是否已存在相同挂号记录
        checkIdempotent(upsertDTO, revisit);

        PatientMasterVO bizPatient = patientProvider.getPatient(upsertDTO.getPatientId());
        if (bizPatient == null) {
            throw new BusinessException("患者信息为空，请检查后重试");
        }

        AppointInfo appointInfo = new AppointInfo();
        appointInfo.setPatientId(upsertDTO.getPatientId());
        appointInfo.setScheduleId(upsertDTO.getScheduleId());
        appointInfo.setRevisitRecordId(revisitRecordId);
        // 必填项由 @NotNull 兜底，业务层不再静默按「窗口挂号」处理
        Integer registSource = upsertDTO.getRegistSource();
        appointInfo.setRegistSource(registSource);
        appointInfo.setPatientName(bizPatient.getPatientName());
        appointInfo.setSettlementType(upsertDTO.getSettlementType());
        appointInfo.setVisitType(upsertDTO.getVisitType());
        appointInfo.setRevisitSource(revisit ? upsertDTO.getRevisitSource() : null);

        boolean noSchedule = revisit && RevisitSourceEnum.needsNoSchedule(upsertDTO.getRevisitSource());
        BizClinicSource schedule = null;
        BizClinicSourceSlot slot = null;
        if (appointInfo.getScheduleId() != null) {
            if (noSchedule) {
                throw new BusinessException("当日回诊不占号源，请勿选择排班");
            }
            schedule = bizScheduleMapper.selectById(appointInfo.getScheduleId());
            if (schedule == null) {
                throw new BusinessException("排班信息不存在");
            }
            if (!StaffTypeEnum.isDoctor(schedule.getStaffType())) {
                throw new BusinessException("只能挂医生出诊号源：该排班是「"
                        + StaffTypeEnum.getText(schedule.getStaffType()) + "」岗位出勤，不对外放号");
            }
            if (schedule.getStaffScheduleId() != null) {
                BizSchedule core = staffScheduleService.getById(schedule.getStaffScheduleId());
                if (!staffScheduleService.releasesClinicSource(core)) {
                    throw new BusinessException(core == null
                            ? "该号源对应的岗位排班已不存在，请到「全院排班」重新排班后再挂号"
                            : "「" + core.getEmployeeName() + "」在 " + core.getScheduleDate() + " 的排班是"
                            + StaffDutyStatusEnum.getText(core.getDutyStatus()) + "·"
                            + AttendModeEnum.getText(core.getAttendMode()) + "，不出诊、不对外放号");
                }
            }
            if (schedule.getAvailableSource() <= 0) {
                throw new BusinessException("号源已满");
            }
            appointInfo.setScheduleType(shiftService.scheduleTypeOf(schedule.getShiftId()));
            if (upsertDTO.getSlotId() != null) {
                slot = bizScheduleSlotMapper.selectById(upsertDTO.getSlotId());
                if (slot == null || !schedule.getId().equals(slot.getScheduleId())) {
                    throw new BusinessException("所选时间段不存在或已调整，请重新选择");
                }
                if (slot.getStatus() == null || slot.getStatus() != 1) {
                    throw new BusinessException("所选时间段已停用，请重新选择");
                }
                appointInfo.setSlotId(slot.getId());
                appointInfo.setSlotStart(slot.getStartTime());
                appointInfo.setSlotEnd(slot.getEndTime());
                // slotTime（旧轻量分时字段）与段开始保持同一口径
                appointInfo.setSlotTime(slot.getStartTime());
            } else if (TextUtil.hasText(upsertDTO.getSlotTime())) {
                // 旧路径：只传就诊时段，不做段级扣减（历史挂号无段）
                validateSlotTime(upsertDTO.getSlotTime(), schedule);
                appointInfo.setSlotTime(upsertDTO.getSlotTime());
            }
            // 时间闸（后端兜底，前端已置灰）：就诊日已过 / 当天已过「开诊前 10 分钟」截止
            assertRegistTimeNotPassed(schedule.getScheduleDate(),
                    slot != null ? slot.getStartTime() : schedule.getStartTime(), "新增挂号");
        } else if (!noSchedule) {
            // B类（条件必填）：只有「当日回诊」免号源，其余来源必须选排班，注解无法按来源分支
            throw new BusinessException("请选择号源");
        }

        // 查询患者信息，自动填充挂号记录
        PatientMasterVO patient = patientProvider.getPatient(appointInfo.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 挂号时若显式传了费别/医保号且主档尚缺（录入新患者未补全等），先补全主档，保证口径单一
        patientProvider.fillInsuranceIfAbsent(patient.getId(), upsertDTO.getMedicalInsuranceType(),
                upsertDTO.getMedicalInsuranceNo());
        if (TextUtil.hasText(upsertDTO.getMedicalInsuranceType()) && !TextUtil.hasText(patient.getMedicalInsuranceType())) {
            patient.setMedicalInsuranceType(upsertDTO.getMedicalInsuranceType());
        }
        if (TextUtil.hasText(upsertDTO.getMedicalInsuranceNo()) && !TextUtil.hasText(patient.getMedicalInsuranceNo())) {
            patient.setMedicalInsuranceNo(upsertDTO.getMedicalInsuranceNo());
        }

        appointInfo.setPatientNo(patient.getPatientNo());
        appointInfo.setPatientName(patient.getPatientName());
        appointInfo.setGender(patient.getGender());
        appointInfo.setAge(patient.getAge());
        appointInfo.setPhone(patient.getPhone());
        appointInfo.setMedicalInsuranceType(
                TextUtil.hasText(upsertDTO.getMedicalInsuranceType())
                        ? upsertDTO.getMedicalInsuranceType()
                        : patient.getMedicalInsuranceType());
        appointInfo.setMedicalInsuranceNo(patient.getMedicalInsuranceNo());

        if (SettlementModeEnum.INSURANCE.getCode().equals(appointInfo.getSettlementType())) {
            // 医保联网结算总开关：关闭时不支持医保结算，挂号侧直接拒（不查任何参保库）
            if (!chargeProvider.insuranceEnabled()) {
                throw new BusinessException("医保联网结算未启用，请改按全自费结算");
            }
            MedicalInsuranceTypeEnum feeType = MedicalInsuranceTypeEnum.parse(appointInfo.getMedicalInsuranceType());
            if (feeType == null || !feeType.isPoolCovered()) {
                throw new BusinessException("患者费别为「"
                        + TextUtil.blankToDefault(appointInfo.getMedicalInsuranceType(), "未设置")
                        + "」，不在医保统筹范围，不能联网结算，请改为全自费");
            }
            if (!TextUtil.hasText(appointInfo.getMedicalInsuranceNo())) {
                throw new BusinessException("医保联网结算需要医保卡号：患者主档未登记，请先补全后再提交");
            }
        }

        // 生成挂号单号
        String appointNo = redisSequenceService.generateAppointNo();
        appointInfo.setRegistNo(appointNo);

        // 根据是否有排班信息设置科室、医生、日期等
        if (schedule != null) {
            appointInfo.setDeptId(schedule.getDeptId());
            appointInfo.setDeptName(schedule.getDeptName());
            appointInfo.setDoctorId(schedule.getDoctorId());
            appointInfo.setDoctorName(schedule.getDoctorName());
            appointInfo.setVisitDate(schedule.getScheduleDate());
            appointInfo.setRegistType(registTypeFromSchedule(schedule));
        } else {
            // 当日回诊：不占号源，医生/科室取当前登录人（医生站「建复诊」就是医生本人点的）
            CurrentUser currentUser = UserUtils.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException("未获取到当前用户信息");
            }
            appointInfo.setDeptId(currentUser.getDeptId());
            appointInfo.setDeptName(currentUser.getDeptName());
            appointInfo.setDoctorId(currentUser.getEmployeeId());
            appointInfo.setDoctorName(currentUser.getRealName());
            appointInfo.setVisitDate(LocalDate.now());
            // 当日回诊不占号源、无号别可推导：归为普通号(1)
            appointInfo.setRegistType(RegistTypeEnum.NORMAL.getCode());
        }
        appointInfo.setRegistTime(LocalDateTime.now());
        appointInfo.setRegistStatus(AppointStatusEnum.REGISTERED.getCode()); // 待支付

        // 保存挂号记录（排队号在签到时生成，以诊室编号为前缀）
        this.save(appointInfo);

        // 原子扣减号源（按渠道分池：预约渠道必须从池内扣；现场渠道提前日不许占用预约池剩余，
        if (schedule != null) {
            boolean appt = registSource == 4;
            // 就诊当日现场可挂走预约池剩余（提前日不许）：池是给远程预约留的额度，
            // 到了就诊日再锁着池，就会出现「显示有余号却挂不上」。
            boolean sameDayWalkin = isSameDayWalkin(appt, schedule.getScheduleDate());
            if (slot != null) {
                int deductedSlot = deductSlotSource(slot, appt, sameDayWalkin);
                if (deductedSlot == 0) {
                    throw new BusinessException(appt ? "该时间段的预约号源已满" : "该时间段号源已满");
                }
                int deductedMain = deductScheduleSource(schedule.getId(), appt, sameDayWalkin);
                if (deductedMain == 0) {
                    // 段有余但主表 Σ 扣不动（数据不一致）：抛异常回滚整个事务，不做静默半扣
                    throw new BusinessException("号源状态异常（排班汇总与时间段不一致），请稍后重试");
                }
            } else {
                int deducted = deductScheduleSource(schedule.getId(), appt, sameDayWalkin);
                if (deducted == 0) {
                    throw new BusinessException(appt ? "预约号源已满" : "号源已满");
                }
            }
        }

        // 挂号费记账 + 出账，经 SPI 委派至 his-charge。
        RevisitFeePolicyService.RevisitFeeDecision feeDecision = revisitFeePolicyService.decide(
                buildRevisitFeeContext(revisit, upsertDTO.getRevisitSource(), origin, schedule,
                        appointInfo.getDeptId(), appointInfo.getDoctorId(), appointInfo.getVisitDate()));

        if (!feeDecision.isWaived() && feeDecision.totalFee().compareTo(BigDecimal.ZERO) > 0) {
            RegistChargeCommandVO command = new RegistChargeCommandVO();
            command.setRegistId(appointInfo.getId());
            command.setRegistNo(appointInfo.getRegistNo());
            command.setPatientId(appointInfo.getPatientId());
            command.setPatientNo(appointInfo.getPatientNo());
            command.setPatientName(appointInfo.getPatientName());
            command.setDeptId(appointInfo.getDeptId());
            command.setDeptName(appointInfo.getDeptName());
            command.setDoctorId(appointInfo.getDoctorId());
            command.setDoctorName(appointInfo.getDoctorName());
            command.setRegistFee(feeDecision.getRegistFee());
            command.setDiagnosisFee(feeDecision.getDiagnosisFee());
            command.setWaived(feeDecision.isWaived());
            command.setWaiveReason(feeDecision.getReason());
            // 本单结算方式（1-全自费 2-医保联网）：挂号单选什么就透什么，支持医保患者本单改自费
            command.setSettlementMode(appointInfo.getSettlementType());

            BillBriefVO bill = chargeProvider.createRegistCharge(command);
            if (bill != null) {
                appointInfo.setBillId(bill.getBillId());
                appointInfo.setBillNo(bill.getBillNo());
                this.updateById(appointInfo);
            }
        }

        return appointInfo;
    }

    /**
     * 复诊费用预估：提交挂号<b>之前</b>问一次「这张号要交多少钱」。
     *
     * <p>与 {@link #addAppoint} 复用同一套 {@code validateRevisit → buildRevisitFeeContext → decide}，
     * 因为预览和实收一旦各写一份判定，迟早漂移；漂移的后果是患者在小程序上确认了 0 元、
     * 到窗口被告知要交 12 元（或反过来，白跑一趟）。
     *
     * <p>本方法<b>只读</b>：不落挂号记录、不扣号源、不建收费单。
     */
    @Override
    public RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO previewDTO) {
        boolean noSchedule = RevisitSourceEnum.needsNoSchedule(previewDTO.getRevisitSource());
        RecordBriefVO origin = emrProvider.getRecord(previewDTO.getRevisitRecordId());
        if (origin == null) {
            throw new BusinessException("原病历不存在，无法预估复诊费用");
        }
        if (origin.getPatientId() == null || !origin.getPatientId().equals(previewDTO.getPatientId())) {
            throw new BusinessException("原病历不属于当前患者，无法预估复诊费用");
        }

        RevisitFeePreviewVO vo = new RevisitFeePreviewVO();
        BizClinicSource schedule = null;
        if (noSchedule) {
            // 与 addAppoint 里「scheduleId 为空」那条分支同口径：不占号源，科室/医生取当前登录人
            CurrentUser currentUser = UserUtils.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException("未获取到当前用户信息");
            }
            vo.setVisitDate(LocalDate.now());
            vo.setDeptId(currentUser.getDeptId());
            vo.setDeptName(currentUser.getDeptName());
            vo.setDoctorId(currentUser.getEmployeeId());
            vo.setDoctorName(currentUser.getRealName());
        } else {
            // B-条件必填：仅「当日回诊」不占号源可免填，必填性取决于同一请求的复诊来源，DTO 注解无法表达，保留
            if (previewDTO.getScheduleId() == null) {
                throw new BusinessException("请选择号源");
            }
            schedule = bizScheduleMapper.selectById(previewDTO.getScheduleId());
            if (schedule == null) {
                throw new BusinessException("排班信息不存在");
            }
            vo.setVisitDate(schedule.getScheduleDate());
            vo.setDeptId(schedule.getDeptId());
            vo.setDeptName(schedule.getDeptName());
            vo.setDoctorId(schedule.getDoctorId());
            vo.setDoctorName(schedule.getDoctorName());
        }

        RevisitFeePolicyService.RevisitFeeDecision decision = revisitFeePolicyService.decide(
                buildRevisitFeeContext(true, previewDTO.getRevisitSource(), origin, schedule,
                        vo.getDeptId(), vo.getDoctorId(), vo.getVisitDate()));
        vo.setRegistFee(decision.getRegistFee());
        vo.setDiagnosisFee(decision.getDiagnosisFee());
        vo.setTotalFee(decision.totalFee());
        vo.setWaived(decision.isWaived());
        vo.setChargeMode(decision.getChargeMode());
        vo.setPolicyName(decision.getPolicyName());
        vo.setReason(decision.getReason());
        return vo;
    }

    /**
     * 复诊「原病历」候选列表。
     */
    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit) {
        if (patientId == null) {
            return new ArrayList<>();
        }
        List<RevisitRecordSelectVO> options = new ArrayList<>();
        for (RecordBriefVO brief : emrProvider.listRecentByPatient(patientId, limit)) {
            RevisitRecordSelectVO option = new RevisitRecordSelectVO();
            option.setId(brief.getId());
            option.setRecordNo(brief.getRecordNo());
            option.setVisitDate(brief.getVisitDate());
            option.setVisitType(brief.getVisitType());
            option.setDeptId(brief.getDeptId());
            option.setDeptName(brief.getDeptName());
            option.setDoctorId(brief.getDoctorId());
            option.setDoctorName(brief.getDoctorName());
            option.setDiagnosisName(brief.getDiagnosisName());
            options.add(option);
        }
        return options;
    }

    /**
     * 组装复诊收费判定上下文。
     *
     * <p>初诊也走这里（revisit=false → 来源 null → 只有「不限来源」的策略可能命中），
     * 目的是让「原价 0 的号也建单并置已收费」这条口径只有一处实现。
     */
    private RevisitFeePolicyService.RevisitFeeContext buildRevisitFeeContext(
            boolean revisit, Integer revisitSource, RecordBriefVO origin,
            BizClinicSource schedule, Long targetDeptId, Long targetDoctorId, LocalDate targetVisitDate) {
        RevisitFeePolicyService.RevisitFeeContext context = new RevisitFeePolicyService.RevisitFeeContext();
        context.setRevisitSource(revisit ? revisitSource : null);
        context.setRegistFee(schedule == null || schedule.getRegistFee() == null
                ? BigDecimal.ZERO : schedule.getRegistFee());
        context.setDiagnosisFee(schedule == null || schedule.getDiagnosisFee() == null
                ? BigDecimal.ZERO : schedule.getDiagnosisFee());
        if (!revisit || origin == null) {
            return context;
        }
        context.setSameDoctor(compareId(origin.getDoctorId(), targetDoctorId));
        context.setSameDept(compareId(origin.getDeptId(), targetDeptId));
        if (origin.getVisitDate() != null && targetVisitDate != null) {
            context.setDaysSinceOrigin(ChronoUnit.DAYS.between(origin.getVisitDate(), targetVisitDate));
        }
        return context;
    }

    /**
     * 比对两个ID是否相同；任一侧缺失就返回 null（判不出来），由策略层按「不命中带此条件的策略」处理。
     */
    private Boolean compareId(Long originId, Long targetId) {
        if (originId == null || targetId == null) {
            return null;
        }
        return originId.equals(targetId);
    }

    /**
     * 就诊时段校验：HH:mm 格式、30 分钟粒度、必须落在所选班次时段内
     */
    private void validateSlotTime(String slotTime, BizClinicSource schedule) {
        if (slotTime == null || !slotTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new BusinessException("就诊时段格式不合法");
        }
        if (Integer.parseInt(slotTime.substring(3)) % 30 != 0) {
            throw new BusinessException("就诊时段必须为 30 分钟粒度（:00 或 :30）");
        }
        String start = schedule.getStartTime();
        String end = schedule.getEndTime();
        if (TextUtil.hasText(start) && TextUtil.hasText(end)
                && (slotTime.compareTo(start) < 0 || slotTime.compareTo(end) >= 0)) {
            throw new BusinessException("就诊时段不在班次时间范围内（" + start + "-" + end + "）");
        }
    }

    /**
     * 从号源推导挂号类型（号别）：号源标了「专家号」→ 专家号(2)，否则普通号(1)。
     *
     * <p>{@code regist_type} 是 NOT NULL 且无默认值，必须由后端根据号源填充，
     * 不能依赖前端传（DTO 里也没有这个字段）。新增挂号、换号源两处共用，避免各写一遍再漂移。
     */
    private Integer registTypeFromSchedule(BizClinicSource schedule) {
        if (schedule != null && schedule.getIsExpert() != null && schedule.getIsExpert() == 1) {
            return RegistTypeEnum.EXPERT.getCode();
        }
        return RegistTypeEnum.NORMAL.getCode();
    }

    /**
     * 复诊入参的**校验**（只校验，不写库）：来源、原病历归属、当日回诊的时效。
     *
     * <p>为什么把「来源」和「原病历」都做成必填：收费策略要按来源匹配，间隔天数要从原病历的
     * 就诊日算出来 —— 两者缺任一项，「该不该免钱」就无从判定。判不准时的方向是**收费**，
     * 不是免钱，所以宁可在这里把请求挡下来。
     *
     * @return 复诊时返回通过校验的原病历；初诊返回 null
     */
    private RecordBriefVO validateRevisit(AppointUpsertDTO upsertDTO, boolean revisit) {
        if (!revisit) {
            if (upsertDTO.getRevisitSource() != null || upsertDTO.getRevisitRecordId() != null) {
                throw new BusinessException("只有复诊挂号才能指定复诊来源/关联原病历");
            }
            return null;
        }
        // B类（条件必填）：只在复诊分支要求，初诊传了反而报错，一刀切注解会把合法请求挡成 400
        if (RevisitSourceEnum.fromCode(upsertDTO.getRevisitSource()) == RevisitSourceEnum.UNKNOWN) {
            throw new BusinessException("复诊挂号必须选择复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）");
        }
        // B类（条件必填）：同上，原病历仅在复诊分支必填
        if (upsertDTO.getRevisitRecordId() == null) {
            throw new BusinessException("复诊挂号必须关联原病历（收费策略与间隔天数都按原就诊记录判定）");
        }
        RecordBriefVO origin = emrProvider.getRecord(upsertDTO.getRevisitRecordId());
        if (origin == null) {
            throw new BusinessException("原病历不存在，无法创建复诊挂号");
        }
        if (origin.getPatientId() == null || !origin.getPatientId().equals(upsertDTO.getPatientId())) {
            throw new BusinessException("原病历不属于当前患者，无法创建复诊挂号");
        }
        // 「当日回诊」免钱的正当性来自"这还是一次就诊"，跨了天就不成立：
        // 不挡的话，拿一份上个月的病历就能反复挂 0 元号，等于给免费开口子。
        if (RevisitSourceEnum.needsNoSchedule(upsertDTO.getRevisitSource())
                && origin.getVisitDate() != null && !LocalDate.now().equals(origin.getVisitDate())) {
            throw new BusinessException("当日回诊只能关联当天（" + LocalDate.now() + "）的就诊病历，"
                    + "原病历就诊日为 " + origin.getVisitDate() + "；隔日复查请改用「医嘱复诊预约」并选择号源");
        }
        return origin;
    }

    /**
     * 幂等性校验：检查是否已存在相同挂号记录
     * <p>
     * 判重键按复诊来源分两套，不能一把套：
     * <ul>
     *   <li><b>当日回诊</b>（不占号源，scheduleId 为空）：唯一性是「同一份原病历只能建一个复诊号」。
     *       套用初诊那套会退化成「该患者只要有一条未取消的挂号就拒」—— 刚看完病的患者
     *       （regist_status=4 已就诊）正好命中，复诊号永远建不出来。</li>
     *   <li><b>医嘱/自助/随访复诊</b>（占号源）：是新的一次就诊，同一份原病历后续可能要复诊好几次，
     *       按病历判重会把第二次复查拦死；唯一性只能是「同一患者 + 同一排班 + 同一就诊类型」。</li>
     * </ul>
     * <p>
     * 判重一律用 count：命中多行只是「确实重复」，不是系统故障 ——
     * `getOne` 在命中多行时抛 TooManyResultsException，会被兜成 500「系统内部错误」。
     */
    private void checkIdempotent(AppointUpsertDTO upsertDTO, boolean revisit) {
        if (revisit && RevisitSourceEnum.needsNoSchedule(upsertDTO.getRevisitSource())) {
            LambdaQueryWrapper<AppointInfo> revisitWrapper = new LambdaQueryWrapper<>();
            revisitWrapper.eq(AppointInfo::getRevisitRecordId, upsertDTO.getRevisitRecordId())
                    .notIn(AppointInfo::getRegistStatus, AppointStatusEnum.OVERDUE.getCode(), AppointStatusEnum.CANCELLED.getCode());
            if (this.count(revisitWrapper) > 0) {
                throw new BusinessException("该病历已创建过复诊号，不可重复创建");
            }
            return;
        }
        if (upsertDTO.getScheduleId() == null) {
            return;
        }
        LambdaQueryWrapper<AppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointInfo::getPatientId, upsertDTO.getPatientId())
                .eq(AppointInfo::getScheduleId, upsertDTO.getScheduleId())
                .eq(AppointInfo::getVisitType, upsertDTO.getVisitType())
                .notIn(AppointInfo::getRegistStatus, AppointStatusEnum.OVERDUE.getCode(), AppointStatusEnum.CANCELLED.getCode()); // 排除已取消的记录
        if (this.count(wrapper) > 0) {
            throw new BusinessException(revisit ? "该患者已在此排班预约过复诊，不可重复预约" : "该患者已在此排班挂号，不可重复挂号");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegist(Long registId, String reason) {
        AppointInfo registInfo = this.getById(registId);
        if (registInfo == null) {
            throw new BusinessException("挂号记录不存在");
        }
        if (!AppointStatusEnum.isCancelable(registInfo.getRegistStatus())) {
            // 口径：已挂号(1) / 已签到(2) 可退；已接诊(3) 之后锁死。
            // 「已签到也能退」是补的一个出口：签到后才发现不看了，以前窗口退不掉、日终结转也没有，
            // 患者只能等着被自动收成「未就诊」。
            throw new BusinessException(notAllowedReason(registInfo.getRegistStatus(), "退号"));
        }
        String past = visitDatePastReason(registInfo, "退号");
        if (past != null) {
            throw new BusinessException(past);
        }

        // 更新挂号状态（退号=CANCELLED(5)，之前误写成 4「已就诊」）
        registInfo.setRegistStatus(AppointStatusEnum.CANCELLED.getCode());
        registInfo.setRefundTime(LocalDateTime.now());
        registInfo.setRefundReason(reason);
        // 守卫下沉到 WHERE：退号仅限「已挂号/已签到」，并发签到/接诊/日终结转时 0 行失败，防整行快照覆盖
        boolean cancelled = this.update(registInfo, new LambdaUpdateWrapper<AppointInfo>()
                .eq(AppointInfo::getId, registInfo.getId())
                .in(AppointInfo::getRegistStatus,
                        AppointStatusEnum.REGISTERED.getCode(), AppointStatusEnum.CHECKED_IN.getCode()));
        if (!cancelled) {
            throw new BusinessException("该挂号状态已变更，不能退号，请刷新后重试");
        }

        // 更新排队状态：退号≠过号，队列同样置「已退号」
        LambdaQueryWrapper<Queue> queueWrapper = new LambdaQueryWrapper<>();
        queueWrapper.eq(Queue::getRegistId, registId);
        Queue queue = queueMapper.selectOne(queueWrapper);
        if (queue != null) {
            queue.setQueueStatus(QueueStatusEnum.CANCELLED.getCode());
            // 守卫下沉到 WHERE：队列同步退号仅限「候诊中」，并发叫号/过号时 0 行失败，防陈旧快照复活
            int queueRows = queueMapper.update(queue, new LambdaUpdateWrapper<Queue>()
                    .eq(Queue::getId, queue.getId())
                    .eq(Queue::getQueueStatus, QueueStatusEnum.WAITING.getCode()));
            if (queueRows == 0) {
                throw new BusinessException("该队列状态已变更，请刷新后重试");
            }
        }

        // 原子释放号源（按挂号渠道还池：预约渠道退号时预约池已用同步递减；
        // 池计数已被修正（=0）时兜底走总池释放，保证总号正确）
        // 段级还号：新挂号持有 slot_id → 段与主表双还；段可能已被物理删除（排班重建），
        // released == 0 容错跳过——挂号上的 dept/doctor/时段快照自证，不阻塞退号。
        if (registInfo.getScheduleId() != null) {
            boolean appt = registInfo.getRegistSource() != null && registInfo.getRegistSource() == 4;
            if (registInfo.getSlotId() != null) {
                int releasedSlot = appt
                        ? bizScheduleSlotMapper.releaseSlotSourceForAppointment(registInfo.getSlotId())
                        : bizScheduleSlotMapper.releaseSlotSourceForWalkin(registInfo.getSlotId());
                if (releasedSlot == 0 && appt) {
                    bizScheduleSlotMapper.releaseSlotSourceForWalkin(registInfo.getSlotId());
                }
            }
            int released = appt
                    ? bizScheduleMapper.releaseScheduleSourceForAppointment(registInfo.getScheduleId())
                    : bizScheduleMapper.releaseScheduleSourceForWalkin(registInfo.getScheduleId());
            if (released == 0 && appt) {
                bizScheduleMapper.releaseScheduleSourceForWalkin(registInfo.getScheduleId());
            }
        }

        // 退号必须同步处理钱：已收费 → 退费，待收费 → 作废。
        // 只改挂号状态不碰收费单，会留下「已退号 + 收了钱」的对账缺口（钱退不回去、日报还挂着应收）。
        settleChargeOnRegistCancel(registInfo, reason);

        return true;
    }

    /**
     * 已收费的挂号换号源 —— 只放开「同医生 + 同费用」这一档，其余一律退回「退号退费 → 重新挂号」。
     *
     * <p>为什么已收费还允许改：患者临时有事往后挪一档时间是最高频的诉求，而钱已经在账上 ——
     * 只要新号源一分钱不差，这笔实收就原样跟着人走，不需要任何资金动作（不补差、不退差、不冲正）。
     *
     * <p>为什么只放这一档，三种情形必须退号重挂：
     * ① <b>价差</b> —— 普通号 ↔ 专家号换过去，已收的金额既不够补差也无需退差，两边台账都不认；
     * ② <b>收入归属</b> —— 记账行的医生快照一经出账不再改写，换医生等于把这笔挂号费记到别人头上；
     * ③ <b>复诊号</b> —— 费用由复诊收费策略按「与原就诊的间隔天数」现算，改期会让免收口径失效
     * （免收的号改出策略窗口，等于该收的钱没收，且没有任何地方会报错）。
     *
     * <p>判费用用的是<b>号源原价</b>（挂号费 + 诊查费）而不是账单实收：实收可能已被策略减免，
     * 拿它跟原价比会让「原价相同、都被免收」的两条号源误判成不同价。
     */
    private void assertPaidRescheduleSamePrice(AppointInfo existing, BizClinicSource newSchedule) {
        if (existing.getBillId() == null) {
            return;
        }
        BillBriefVO bill = chargeProvider.getBill(existing.getBillId());
        BigDecimal paid = bill == null ? BigDecimal.ZERO : NumUtil.orZero(bill.getPaidAmount());
        if (paid.signum() <= 0) {
            return;
        }
        String billText = "该挂号已收费（账单 " + bill.getBillNo() + "，实收 ¥" + paid.toPlainString() + "）";
        if (existing.getRevisitSource() != null) {
            throw new BusinessException(billText + "是复诊号，费用按复诊收费策略现算，改期会使免收口径失效；"
                    + "请退号退费（按原支付渠道退回）后重新挂号");
        }
        if (!Objects.equals(existing.getDeptId(), newSchedule.getDeptId())) {
            throw new BusinessException(billText + "改约不允许换科室；请退号退费（按原支付渠道退回）后重新挂号");
        }
        if (!Objects.equals(existing.getDoctorId(), newSchedule.getDoctorId())) {
            throw new BusinessException(billText + "改约不允许换医生（挂号费收入按医生记账，出账后不改写归属）；"
                    + "请退号退费（按原支付渠道退回）后重新挂号");
        }
        BizClinicSource oldSchedule = existing.getScheduleId() == null ? null : bizScheduleMapper.selectById(existing.getScheduleId());
        if (oldSchedule == null) {
            throw new BusinessException(billText + "原号源已不存在，无法核对费用是否一致；"
                    + "请退号退费（按原支付渠道退回）后重新挂号");
        }
        BigDecimal oldFee = scheduleFeeTotal(oldSchedule);
        BigDecimal newFee = scheduleFeeTotal(newSchedule);
        if (oldFee.compareTo(newFee) != 0) {
            throw new BusinessException(billText + "改约只放同费用号源：新号源 ¥" + newFee.toPlainString()
                    + "、原号源 ¥" + oldFee.toPlainString() + "；请退号退费（按原支付渠道退回）后重新挂号");
        }
    }

    /**
     * 退号时联动账单（走 SPI 到 his-charge）：整单撤销 = 钱原路退回 + 应收红冲 + 账单作废。
     *
     * <p>与退号在<b>同一个事务</b>里：只退号不处理钱，患者钱退不回去、日报还挂着这笔应收。
     */
    private void settleChargeOnRegistCancel(AppointInfo registInfo, String reason) {
        if (registInfo.getBillId() == null) {
            return;
        }
        CancelCommandVO command = new CancelCommandVO();
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        command.setBillId(registInfo.getBillId());
        command.setReason(TextUtil.hasText(reason) ? reason : "退号");
        command.setOperator(operatorUser.getRealName());
        boolean closed = chargeProvider.cancelRegistCharge(command);
        log.info("退号联动账单：registId={}，billId={}，撤销={}",
                registInfo.getId(), registInfo.getBillId(), closed);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRegist(AppointInfo registInfo) {
        AppointInfo existing = this.getById(registInfo.getId());
        if (existing == null) {
            throw new BusinessException("挂号记录不存在");
        }
        if (!AppointStatusEnum.isSourceChangeAllowed(existing.getRegistStatus())) {
            throw new BusinessException(notAllowedReason(existing.getRegistStatus(), "调整/变更号源"));
        }
        String past = visitDatePastReason(existing, "变更号源/改约");
        if (past != null) {
            throw new BusinessException(past);
        }
        if (existing.getRegistSource() == null) {
            throw new BusinessException("挂号记录缺少渠道信息，不允许修改");
        }
        // 渠道跟随原挂号记录：窗口(1/2/3)走现场池，预约(4)走预约池（扣新号双列同扣、还旧号按渠道回池）
        boolean apptChannel = existing.getRegistSource() == 4;

        // 换号源：先取到新排班再过闸（闸要看新号源的费用，不能先还旧号再发现不让改）
        boolean sourceChanging = registInfo.getScheduleId() != null
                && !registInfo.getScheduleId().equals(existing.getScheduleId());
        BizClinicSource newSchedule = null;
        if (sourceChanging) {
            newSchedule = bizScheduleMapper.selectById(registInfo.getScheduleId());
            if (newSchedule == null) {
                throw new BusinessException("新排班信息不存在");
            }
            // 时间闸：新号源就诊日已过 / 当天已过「开诊前 10 分钟」截止，不允许改约
            assertRegistTimeNotPassed(newSchedule.getScheduleDate(), newSchedule.getStartTime(), "变更号源/改约");
            assertPaidRescheduleSamePrice(existing, newSchedule);
        }

        Long oldSlotId = existing.getSlotId();
        boolean slotChanging = !sourceChanging
                && registInfo.getSlotId() != null
                && !registInfo.getSlotId().equals(oldSlotId);
        if (slotChanging) {
            BizClinicSourceSlot newSlot = bizScheduleSlotMapper.selectById(registInfo.getSlotId());
            if (newSlot == null || !existing.getScheduleId().equals(newSlot.getScheduleId())) {
                throw new BusinessException("新时间段不存在或已调整，请重新选择");
            }
            if (newSlot.getStatus() == null || newSlot.getStatus() != 1) {
                throw new BusinessException("新时间段已停用，请重新选择");
            }
            // 时间闸：当天改段，新段已过「开诊前 10 分钟」截止同样不允许
            assertRegistTimeNotPassed(existing.getVisitDate(), newSlot.getStartTime(), "变更时间段/改约");
            if (oldSlotId != null) {
                int releasedSlot = apptChannel
                        ? bizScheduleSlotMapper.releaseSlotSourceForAppointment(oldSlotId)
                        : bizScheduleSlotMapper.releaseSlotSourceForWalkin(oldSlotId);
                if (releasedSlot == 0 && apptChannel) {
                    bizScheduleSlotMapper.releaseSlotSourceForWalkin(oldSlotId);
                }
            }
            int deductedSlot = deductSlotSource(newSlot, apptChannel,
                    isSameDayWalkin(apptChannel, existing.getVisitDate()));
            if (deductedSlot == 0) {
                throw new BusinessException(apptChannel ? "新时间段的预约号已满" : "新时间段号源已满");
            }
            existing.setSlotId(newSlot.getId());
            existing.setSlotStart(newSlot.getStartTime());
            existing.setSlotEnd(newSlot.getEndTime());
            existing.setSlotTime(newSlot.getStartTime());
        }

        // 换号源：按原渠道原子释放旧号源、原子占用新号源
        if (sourceChanging) {
            if (existing.getScheduleId() != null) {
                if (oldSlotId != null) {
                    int releasedSlot = apptChannel
                            ? bizScheduleSlotMapper.releaseSlotSourceForAppointment(oldSlotId)
                            : bizScheduleSlotMapper.releaseSlotSourceForWalkin(oldSlotId);
                    if (releasedSlot == 0 && apptChannel) {
                        bizScheduleSlotMapper.releaseSlotSourceForWalkin(oldSlotId);
                    }
                }
                int released = apptChannel
                        ? bizScheduleMapper.releaseScheduleSourceForAppointment(existing.getScheduleId())
                        : bizScheduleMapper.releaseScheduleSourceForWalkin(existing.getScheduleId());
                if (released == 0 && apptChannel) {
                    bizScheduleMapper.releaseScheduleSourceForWalkin(existing.getScheduleId());
                }
            }

            boolean sameDayWalkin = isSameDayWalkin(apptChannel, newSchedule.getScheduleDate());
            BizClinicSourceSlot newSlot = null;
            if (registInfo.getSlotId() != null) {
                newSlot = bizScheduleSlotMapper.selectById(registInfo.getSlotId());
                if (newSlot == null || !newSchedule.getId().equals(newSlot.getScheduleId())) {
                    throw new BusinessException("新时间段不存在或已调整，请重新选择");
                }
                if (newSlot.getStatus() == null || newSlot.getStatus() != 1) {
                    throw new BusinessException("新时间段已停用，请重新选择");
                }
                int deductedSlot = deductSlotSource(newSlot, apptChannel, sameDayWalkin);
                if (deductedSlot == 0) {
                    throw new BusinessException(apptChannel ? "新时间段的预约号已满" : "新时间段号源已满");
                }
            }
            int deducted = deductScheduleSource(newSchedule.getId(), apptChannel, sameDayWalkin);
            if (deducted == 0) {
                throw new BusinessException(apptChannel ? "新号源的预约号已满" : "新号源已满");
            }

            existing.setDeptId(newSchedule.getDeptId());
            existing.setDeptName(newSchedule.getDeptName());
            existing.setDoctorId(newSchedule.getDoctorId());
            existing.setDoctorName(newSchedule.getDoctorName());
            existing.setVisitDate(newSchedule.getScheduleDate());
            existing.setScheduleId(newSchedule.getId());
            existing.setScheduleType(shiftService.scheduleTypeOf(newSchedule.getShiftId()));
            // 换号源后号别必须跟随新号源重算：普通号源 ↔ 专家号源互换时不能残留旧号别
            existing.setRegistType(registTypeFromSchedule(newSchedule));
            // 段快照跟随新号：新号有段写新值；新号没段（异常场景）时 updateById 跳过 null 字段清不掉，
            // 走 LambdaUpdateWrapper 显式置空，避免残留旧段的快照误导前端
            existing.setSlotId(newSlot != null ? newSlot.getId() : null);
            existing.setSlotStart(newSlot != null ? newSlot.getStartTime() : null);
            existing.setSlotEnd(newSlot != null ? newSlot.getEndTime() : null);
            existing.setSlotTime(newSlot != null ? newSlot.getStartTime() : null);
        }

        if (registInfo.getVisitType() != null) {
            existing.setVisitType(registInfo.getVisitType());
        }

        // 守卫下沉到 WHERE：变更号源仅限「已挂号」，并发签到/退号时 0 行失败，防陈旧快照把状态写回
        boolean saved = this.update(existing, new LambdaUpdateWrapper<AppointInfo>()
                .eq(AppointInfo::getId, existing.getId())
                .eq(AppointInfo::getRegistStatus, AppointStatusEnum.REGISTERED.getCode()));
        if (!saved) {
            throw new BusinessException("该挂号状态已变更，不能调整/变更号源，请刷新后重试");
        }
        if (sourceChanging && registInfo.getSlotId() == null && existing.getSlotId() == null) {
            // 新号没段：updateById 跳过 null 字段，段快照必须显式清空（防残留旧段）
            this.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AppointInfo>()
                    .eq(AppointInfo::getId, existing.getId())
                    .set(AppointInfo::getSlotId, null)
                    .set(AppointInfo::getSlotStart, null)
                    .set(AppointInfo::getSlotEnd, null));
        }
        return saved;
    }

    /**
     * 挂号扣段：预约渠道从段池扣，现场渠道提前日不许吃池剩余、就诊当日放宽（口径见 isSameDayWalkin）
     */
    private int deductSlotSource(BizClinicSourceSlot slot, boolean apptChannel, boolean sameDayWalkin) {
        if (apptChannel) {
            return bizScheduleSlotMapper.deductSlotSourceForAppointment(slot.getId());
        }
        if (sameDayWalkin) {
            return bizScheduleSlotMapper.deductSlotSourceForWalkinSameDay(slot.getId());
        }
        return bizScheduleSlotMapper.deductSlotSourceForWalkin(slot.getId());
    }

    /**
     * 挂号扣主表号源：择池口径必须与 deductSlotSource 一致，否则段与主表汇总对不上
     */
    private int deductScheduleSource(Long scheduleId, boolean apptChannel, boolean sameDayWalkin) {
        if (apptChannel) {
            return bizScheduleMapper.deductScheduleSourceForAppointment(scheduleId);
        }
        if (sameDayWalkin) {
            return bizScheduleMapper.deductScheduleSourceForWalkinSameDay(scheduleId);
        }
        return bizScheduleMapper.deductScheduleSourceForWalkin(scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long registId, Integer targetStatus) {
        AppointInfo regist = this.getById(registId);
        if (regist == null) {
            throw new BusinessException("挂号记录不存在");
        }
        // D类（业务规则）：码值合法性闸门，非空只是它的前置条件
        if (targetStatus == null || AppointStatusEnum.fromCode(targetStatus).isFallback()) {
            // 之前这个接口是「把入参直接 set 进实体」，status 传 0/99 这类枚举外的值也照存不误，
            // 脏码值从此在列表里渲染成「未知(n)」而没人知道是谁写进去的。
            throw new BusinessException("未知的挂号状态：" + targetStatus);
        }
        if (targetStatus == AppointStatusEnum.CANCELLED.getCode()) {
            // 退号是「一条链」不是「一个字段」：还号源 + 同步队列 + 记退号时间/原因。
            throw new BusinessException("退号请走退号接口（会同步归还号源与候诊队列），不能直接改状态");
        }
        Integer current = regist.getRegistStatus();
        if (AppointStatusEnum.isFinal(current)) {
            throw new BusinessException(notAllowedReason(current, "变更状态"));
        }
        regist.setRegistStatus(targetStatus);
        // 守卫下沉到 WHERE：终态（已就诊/已退号/已过号/爽约/未就诊）不可再变更，并发变更时 0 行失败
        boolean saved = this.update(regist, new LambdaUpdateWrapper<AppointInfo>()
                .eq(AppointInfo::getId, regist.getId())
                .notIn(AppointInfo::getRegistStatus,
                        AppointStatusEnum.COMPLETED.getCode(), AppointStatusEnum.CANCELLED.getCode(),
                        AppointStatusEnum.OVERDUE.getCode(), AppointStatusEnum.NO_SHOW.getCode(),
                        AppointStatusEnum.UNVISITED.getCode()));
        if (!saved) {
            throw new BusinessException("该挂号已是终态或状态已变更，不能变更状态，请刷新后重试");
        }
        // 操作台把状态改成「已就诊」也是结诊事实，与队列结诊（completeQueue）走同一份回写：
        // 漏掉这条路径，从挂号管理里手工完结的挂号永远不会出现在患者的「最近/首次就诊」上。
        if (targetStatus == AppointStatusEnum.COMPLETED.getCode()) {
            patientProvider.markLastVisit(regist.getPatientId(), LocalDateTime.now(),
                    regist.getDeptId(), regist.getDeptName(),
                    regist.getDoctorId(), regist.getDoctorName());
            patientProvider.markFirstVisit(regist.getPatientId(), LocalDateTime.now(),
                    regist.getDeptId(), regist.getDeptName(),
                    regist.getDoctorId(), regist.getDoctorName());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizAppointInfoListVO appointUpsert(AppointUpsertDTO upsertDTO) {
        if (upsertDTO.getId() == null) {
            return toRegistVO(addAppoint(upsertDTO));
        }
        AppointInfo registInfo = new AppointInfo();
        registInfo.setId(upsertDTO.getId());
        registInfo.setScheduleId(upsertDTO.getScheduleId());
        registInfo.setVisitType(upsertDTO.getVisitType());
        registInfo.setSlotId(upsertDTO.getSlotId());
        if (!updateRegist(registInfo)) {
            throw new BusinessException("修改失败");
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizAppointInfoListVO revisitUpsert(AppointUpsertDTO upsertDTO) {
        if (upsertDTO.getId() != null) {
            throw new BusinessException("医生站只能新建复诊号，改号请走挂号窗口");
        }
        int source = upsertDTO.getRevisitSource() == null ? 0 : upsertDTO.getRevisitSource();
        if (source != RevisitSourceEnum.SAME_DAY_RETURN.getCode()
                && source != RevisitSourceEnum.DOCTOR_ORDERED.getCode()) {
            throw new BusinessException("医生站只能建「当日回诊」或「医嘱复诊预约」两种复诊号");
        }
        upsertDTO.setVisitType(2);
        return toRegistVO(addAppoint(upsertDTO));
    }

    @Override
    public RevisitFeePreviewVO revisitFeePreviewScoped(RevisitFeePreviewDTO previewDTO) {
        // 只读、不涉敏，越权由 patientScopeViolated 收口（员工放行、患者只碰自己绑定的就诊人）
        if (patientProvider.patientScopeViolated(previewDTO.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的费用信息");
        }
        return revisitFeePreview(previewDTO);
    }

    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectListScoped(Long patientId) {
        if (patientProvider.patientScopeViolated(patientId)) {
            throw new BusinessException("无权查询该就诊人的病历");
        }
        return revisitRecordSelectList(patientId, 20);
    }

    @Override
    public void cancelRegist(AppointCancelDTO appointCancelDTO) {
        String reason = TextUtil.hasText(appointCancelDTO.getReason())
                ? appointCancelDTO.getReason() : "患者主动退号";
        if (!cancelRegist(appointCancelDTO.getRegistId(), reason)) {
            throw new BusinessException("退号失败");
        }
    }

    @Override
    public BizAppointInfoListVO getDetail(AppointQueryDTO appointQueryDTO) {
        return toRegistVO(this.getById(appointQueryDTO.getPatientId()));
    }

    /**
     * 单条挂号的出参装配：不带账单与队列扩展（那两个口径只服务列表/看板）。
     */
    private BizAppointInfoListVO toRegistVO(AppointInfo entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        vo.setId(entity.getId());
        vo.setRegistNo(entity.getRegistNo());
        vo.setPatientId(entity.getPatientId());
        vo.setPatientNo(entity.getPatientNo());
        vo.setPatientName(entity.getPatientName());
        vo.setGender(entity.getGender());
        vo.setAge(entity.getAge());
        vo.setPhone(entity.getPhone());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(entity.getDoctorName());
        vo.setScheduleId(entity.getScheduleId());
        vo.setSlotId(entity.getSlotId());
        vo.setSlotStart(entity.getSlotStart());
        vo.setSlotEnd(entity.getSlotEnd());
        vo.setRegistType(entity.getRegistType());
        vo.setRegistSource(entity.getRegistSource());
        vo.setRegistTime(entity.getRegistTime());
        vo.setVisitDate(entity.getVisitDate());
        vo.setSettlementType(entity.getSettlementType());
        vo.setMedicalInsuranceType(entity.getMedicalInsuranceType());
        vo.setMedicalInsuranceNo(entity.getMedicalInsuranceNo());
        vo.setRegistStatus(entity.getRegistStatus());
        vo.setVisitType(entity.getVisitType());
        vo.setRevisitSource(entity.getRevisitSource());
        // 批次E/E6：复诊关联原病历（只回引用ID，原病历内容由病历侧按需取）
        vo.setRevisitRecordId(entity.getRevisitRecordId());
        vo.setRefundTime(entity.getRefundTime());
        vo.setRefundReason(entity.getRefundReason());
        vo.setBillId(entity.getBillId());
        vo.setBillNo(entity.getBillNo());
        return vo;
    }

}
