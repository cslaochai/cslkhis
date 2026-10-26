package com.his.outpatient.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.outpatient.dto.AppointBoardQueryDTO;
import com.his.outpatient.dto.AppointCancelDTO;
import com.his.outpatient.entity.AppointInfo;
import com.his.outpatient.entity.BizClinicSource;
import com.his.outpatient.entity.BizClinicSourceSlot;
import com.his.outpatient.entity.Queue;
import com.his.outpatient.mapper.AppointInfoMapper;
import com.his.outpatient.mapper.BizClinicSourceMapper;
import com.his.outpatient.mapper.BizClinicSourceSlotMapper;
import com.his.outpatient.mapper.QueueMapper;
import com.his.outpatient.service.AppointService;
import com.his.outpatient.service.RevisitFeePolicyService;
import com.his.outpatient.trigger.DayEndSettleTrigger;
import com.his.outpatient.vo.AppointStatusCountVO;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.dto.AppointUpsertDTO;
import com.his.spi.dto.RevisitFeePreviewDTO;
import com.his.spi.service.*;
import com.his.spi.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 挂号服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointServiceImpl extends ServiceImpl<AppointInfoMapper, AppointInfo> implements AppointService {

    private final DeptScopeService deptScopeService;

    private final QueueMapper queueMapper;

    private final RedisSequenceService redisSequenceService;

    private final RevisitFeePolicyService revisitFeePolicyService;

    private final DayEndSettleTrigger dayEndSettleTrigger;

    private final PatientFacadeService patientFacadeService;

    private final ChargeFacadeService chargeFacadeService;

    private final BizClinicSourceMapper bizClinicSourceMapper;

    private final BizClinicSourceSlotMapper bizClinicSourceSlotMapper;

    private final EmrFacadeService emrFacadeService;

    private static String visitDatePastReason(AppointInfo regist, String action) {
        LocalDate visitDate = regist.getVisitDate();
        if (visitDate == null || !visitDate.isBefore(LocalDate.now())) {
            return null;
        }
        return "就诊日 " + visitDate + " 已过，不允许" + action
                + "：患者未到诊需退挂号费的，请到收费处「退费管理」发起退费申请。";
    }

    private static String notAllowedReason(Integer registStatus, String action) {
        String label = AppointStatusEnum.getLabel(registStatus);
        String who = label == null ? "当前状态(" + registStatus + ")" : label;
        if (AppointStatusEnum.isFinal(registStatus)) {
            return who + " 不允许" + action + "：诊疗已结束，号源属过去（或已作废），不可再变更";
        }
        return who + " 不允许" + action
                + "（仅「已挂号」可调整/变更号源，「已挂号/已签到」可退号；已接诊/已就诊为诊疗事实，锁死）";
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
                .in(StringUtils.isNoneBlank(queryDTO.getRegistTypes()), AppointInfo::getRegistType,
                        StringUtils.split(queryDTO.getRegistTypes(), ","))
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
        scoped.setRegistTypes(queryDTO.getRegistTypes());
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
                .in(StringUtils.isNoneBlank(queryDTO.getRegistTypes()), AppointInfo::getRegistType, StringUtils.split(queryDTO.getRegistTypes(), ","))
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

        // 批量查询账单信息
        Map<Long, BillBriefVO> billMap = new HashMap<>();
        if (!billIds.isEmpty()) {
            billMap = chargeFacadeService.mapBillsByIds(billIds);
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

        PatientMasterVO bizPatient = patientFacadeService.getPatient(upsertDTO.getPatientId());
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
        appointInfo.setVisitType(upsertDTO.getVisitType());
        appointInfo.setRevisitSource(revisit ? upsertDTO.getRevisitSource() : null);

        boolean noSchedule = revisit && RevisitSourceEnum.needsNoSchedule(upsertDTO.getRevisitSource());
        if (appointInfo.getScheduleId() != null) {
            if (noSchedule) {
                throw new BusinessException("当日回诊不占号源，请勿选择排班");
            }
            // 号源快照回填见下方 !noSchedule 分支（同模块直读 BizClinicSource）。
            // 待恢复：医生岗位校验/剩余号校验/段归属与停用校验/就诊日与开诊截止时间闸
            // （releasesClinicSource/scheduleTypeOf）与号源原子扣减（deductScheduleSource/deductSlotSource）。
            if (upsertDTO.getSlotId() != null) {
                appointInfo.setSlotId(upsertDTO.getSlotId());
            }
            if (TextUtil.hasText(upsertDTO.getSlotTime())) {
                validateSlotTime(upsertDTO.getSlotTime(), upsertDTO.getScheduleId());
                appointInfo.setSlotTime(upsertDTO.getSlotTime());
            }
        } else if (!noSchedule) {
            // B类（条件必填）：只有「当日回诊」免号源，其余来源必须选排班，注解无法按来源分支
            throw new BusinessException("请选择号源");
        }

        // 查询患者信息，自动填充挂号记录
        PatientMasterVO patient = patientFacadeService.getPatient(appointInfo.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 挂号时若显式传了费别/医保号且主档尚缺（录入新患者未补全等），先补全主档，保证口径单一
        patientFacadeService.fillInsuranceIfAbsent(patient.getId(), upsertDTO.getMedicalInsuranceType(),
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

        // 生成挂号单号：复诊以 FZ 开头（就诊号在挂号单上按初诊/复诊可一眼区分，号段独立不撞唯一索引）
        String appointNo = revisit ? redisSequenceService.generateRevisitNo() : redisSequenceService.generateAppointNo();
        appointInfo.setRegistNo(appointNo);

        // 科室/医生/就诊日快照：当日回诊(noSchedule)取当前登录人（医生站「建复诊」就是医生本人点的）；
        if (noSchedule) {
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
        } else if (appointInfo.getScheduleId() != null) {
            BizClinicSource source = bizClinicSourceMapper.selectById(appointInfo.getScheduleId());
            if (source == null) {
                throw new BusinessException("所选号源不存在或已停诊，请重新选择号源");
            }
            // 段归属校验 + 段快照（slotStart/slotEnd）：看板按快照归位，缺了会兜底到班次首档，
            // 表现为「选 11:30 却挂到 08:00 档」。
            BizClinicSourceSlot slot = null;
            if (appointInfo.getSlotId() != null) {
                slot = bizClinicSourceSlotMapper.selectById(appointInfo.getSlotId());
                if (slot == null || !appointInfo.getScheduleId().equals(slot.getScheduleId())
                        || slot.getStatus() == null || slot.getStatus() != 1) {
                    throw new BusinessException("所选时段不存在、已停用或不属于该号源，请重新选择");
                }
                appointInfo.setSlotStart(slot.getStartTime());
                appointInfo.setSlotEnd(slot.getEndTime());
            }
            appointInfo.setDeptId(source.getDeptId());
            appointInfo.setDeptName(source.getDeptName());
            appointInfo.setDoctorId(source.getDoctorId());
            appointInfo.setDoctorName(source.getDoctorName());
            appointInfo.setVisitDate(source.getScheduleDate());
            // 号别：号源是否专家号 → 专家号(2)，否则普通号(1)
            appointInfo.setRegistType(source.getIsExpert() != null && source.getIsExpert() == 1
                    ? RegistTypeEnum.EXPERT.getCode() : RegistTypeEnum.NORMAL.getCode());

            // 原子扣减（先段后主表，任一 0 行 = 号满，抛异常整事务回滚，不留半截扣减）。
            // 预约渠道(4)从预约池扣；现场渠道(1/2/3)当日及以前直接扣总余号，提前日受预约池限制。
            boolean sameDayOrPast = isSameDayOrPast(source.getScheduleDate());
            if (slot != null && deductSlot(slot.getId(), registSource, sameDayOrPast) == 0) {
                throw new BusinessException("该时段号源已约满，请选择其他时段");
            }
            if (deductSchedule(source.getId(), registSource, sameDayOrPast) == 0) {
                throw new BusinessException("该号源已约满，请选择其他号源");
            }
        }
        appointInfo.setRegistTime(LocalDateTime.now());
        appointInfo.setRegistStatus(AppointStatusEnum.REGISTERED.getCode()); // 待支付

        // 保存挂号记录（排队号在签到时生成，以诊室编号为前缀）
        this.save(appointInfo);

        // 挂号费记账 + 出账
        RevisitFeePolicyService.RevisitFeeDecision feeDecision = revisitFeePolicyService.decide(
                buildRevisitFeeContext(revisit, upsertDTO.getRevisitSource(), origin,
                        appointInfo.getDeptId(), appointInfo.getDoctorId(), appointInfo.getVisitDate(),
                        patient.getBirthDate(), upsertDTO.getScheduleId()));

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
            command.setDiagnosisTier(feeDecision.getDiagnosisTier());
            command.setBaseFee(feeDecision.getBaseFee());
            command.setSurchargeFee(feeDecision.getSurchargeFee());
            command.setWaived(feeDecision.isWaived());
            command.setWaiveReason(feeDecision.getReason());
            // 本单结算方式（1-全自费 2-医保联网）：不再单独存挂号单字段，出账时按费别+医保号现算
            MedicalInsuranceTypeEnum feeEnum = MedicalInsuranceTypeEnum.fromCode(appointInfo.getMedicalInsuranceType());
            boolean insuranceChannel = feeEnum != null && feeEnum.isPoolCovered()
                    && TextUtil.hasText(appointInfo.getMedicalInsuranceNo());
            command.setSettlementMode(insuranceChannel ? 2 : 1);

            BillBriefVO bill = chargeFacadeService.createRegistCharge(command);
            if (bill != null) {
                appointInfo.setBillId(bill.getBillId());
                appointInfo.setBillNo(bill.getBillNo());
                appointInfo.setInsuranceWarning(bill.getInsuranceWarning());
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
        RecordBriefVO origin = emrFacadeService.getRecord(previewDTO.getRevisitRecordId());
        if (origin == null) {
            throw new BusinessException("原病历不存在，无法预估复诊费用");
        }
        if (origin.getPatientId() == null || !origin.getPatientId().equals(previewDTO.getPatientId())) {
            throw new BusinessException("原病历不属于当前患者，无法预估复诊费用");
        }

        RevisitFeePreviewVO vo = new RevisitFeePreviewVO();
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
            if (previewDTO.getScheduleId() == null) {
                throw new BusinessException("请选择号源");
            }
            // 号源表在本模块，直读裸 Mapper 取就诊日/科室/医生快照（同 461 行先例，不引 SPI 端口）
            BizClinicSource source = bizClinicSourceMapper.selectById(previewDTO.getScheduleId());
            if (source == null) {
                throw new BusinessException("所选号源不存在或已停诊，请重新选择");
            }
            vo.setVisitDate(source.getScheduleDate());
            vo.setDeptId(source.getDeptId());
            vo.setDeptName(source.getDeptName());
            vo.setDoctorId(source.getDoctorId());
            vo.setDoctorName(source.getDoctorName());
        }

        // 🔑 儿童加收要读主档出生日期，与 addAppoint 取同一份数据 ——
        //   预估和实收一旦各写一份判定，患者在小程序上确认了一个数、到窗口被告知另一个数，
        //   是本项目反复踩过的坑（见 revisitFeePreview 的方法注释）。取不到主档按 null 传，
        //   与实收同样表现成「不加收」，不会一边加一边不加。
        PatientMasterVO previewPatient = patientFacadeService.getPatient(previewDTO.getPatientId());
        RevisitFeePolicyService.RevisitFeeDecision decision = revisitFeePolicyService.decide(
                buildRevisitFeeContext(true, previewDTO.getRevisitSource(), origin,
                        vo.getDeptId(), vo.getDoctorId(), vo.getVisitDate(),
                        previewPatient == null ? null : previewPatient.getBirthDate(),
                        previewDTO.getScheduleId()));
        vo.setRegistFee(decision.getRegistFee());
        vo.setDiagnosisFee(decision.getDiagnosisFee());
        vo.setTotalFee(decision.totalFee());
        vo.setWaived(decision.isWaived());
        vo.setChargeMode(decision.getChargeMode());
        vo.setPolicyName(decision.getPolicyName());
        vo.setReason(decision.getReason());
        vo.setBaseDiagnosisFee(decision.getBaseDiagnosisFee());
        vo.setChildSurcharged(decision.isChildSurcharged());
        vo.setChildSurchargeAmount(decision.getChildSurchargeAmount());
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
        for (RecordBriefVO brief : emrFacadeService.listRecentByPatient(patientId, limit)) {
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
            Long targetDeptId, Long targetDoctorId, LocalDate targetVisitDate,
            LocalDate patientBirthDate, Long sourceScheduleId) {
        RevisitFeePolicyService.RevisitFeeContext context = new RevisitFeePolicyService.RevisitFeeContext();
        context.setRevisitSource(revisit ? revisitSource : null);
        // registFee（挂号费）58号文后已废止恒为 0，计价只读 diagnosisFee/diagnosisTier
        context.setRegistFee(BigDecimal.ZERO);
        // 号源价格快照（诊查费/职级档）：号源表在本模块，直读裸 Mapper；查不到按 0/null 兜底
        if (sourceScheduleId != null) {
            BizClinicSource source = bizClinicSourceMapper.selectById(sourceScheduleId);
            if (source != null) {
                context.setDiagnosisFee(NumUtil.orZero(source.getDiagnosisFee()));
                context.setDiagnosisTier(source.getDiagnosisTier());
                return context;
            }
        }
        context.setDiagnosisFee(BigDecimal.ZERO);
        // 🔑 儿童加收 30% 的两个判定输入（湘医保发〔2025〕58号附件1 计价说明）：
        //   birthDate 判是否六周岁以内；缺失则不加收（判不出来不猜，多收会被稽核抓）
        //   visitDate 取「拟就诊日」而非今天 —— 预约可提前数周，一个今天 6 岁半的孩子
        //   预约到两周后就诊时已满 7 周岁，按挂号日算会多收
        context.setBirthDate(patientBirthDate);
        context.setVisitDate(targetVisitDate);
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
     * 是否预约渠道（4-预约挂号）；其余 1窗口/2自助/3网上 均为现场渠道。
     */
    private boolean isAppointmentChannel(Integer registSource) {
        return registSource != null && registSource == AppointSourceEnum.APPOINTMENT.getCode();
    }

    /**
     * 就诊日在今天及以前：预约池不再被远程预约占用，现场可直接扣总余号；提前日才受预约池限制。
     */
    private boolean isSameDayOrPast(LocalDate scheduleDate) {
        return scheduleDate == null || !scheduleDate.isAfter(LocalDate.now());
    }

    /**
     * 按渠道原子扣主表；返回受影响行数，0 = 号满。
     */
    private int deductSchedule(Long scheduleId, Integer registSource, boolean sameDayOrPast) {
        if (isAppointmentChannel(registSource)) {
            return bizClinicSourceMapper.deductScheduleSourceForAppointment(scheduleId);
        }
        return sameDayOrPast
                ? bizClinicSourceMapper.deductScheduleSourceForWalkinSameDay(scheduleId)
                : bizClinicSourceMapper.deductScheduleSourceForWalkin(scheduleId);
    }

    /**
     * 按渠道原子扣段；返回受影响行数，0 = 该时段已满。
     */
    private int deductSlot(Long slotId, Integer registSource, boolean sameDayOrPast) {
        if (isAppointmentChannel(registSource)) {
            return bizClinicSourceSlotMapper.deductSlotSourceForAppointment(slotId);
        }
        return sameDayOrPast
                ? bizClinicSourceSlotMapper.deductSlotSourceForWalkinSameDay(slotId)
                : bizClinicSourceSlotMapper.deductSlotSourceForWalkin(slotId);
    }

    /**
     * 退号/换号归还主表（按渠道还预约池或总池）；SQL 自带 used_source>0 守卫，重复归还不会减成负数。
     */
    private void releaseSchedule(Long scheduleId, Integer registSource) {
        if (scheduleId == null) {
            return;
        }
        if (isAppointmentChannel(registSource)) {
            bizClinicSourceMapper.releaseScheduleSourceForAppointment(scheduleId);
        } else {
            bizClinicSourceMapper.releaseScheduleSourceForWalkin(scheduleId);
        }
    }

    /**
     * 退号/换号归还段（按渠道还预约池或总池）。
     */
    private void releaseSlot(Long slotId, Integer registSource) {
        if (slotId == null) {
            return;
        }
        if (isAppointmentChannel(registSource)) {
            bizClinicSourceSlotMapper.releaseSlotSourceForAppointment(slotId);
        } else {
            bizClinicSourceSlotMapper.releaseSlotSourceForWalkin(slotId);
        }
    }

    /**
     * 就诊时段校验：HH:mm 格式、30 分钟粒度，且落在所选号源班次开诊时段内。
     *
     * <p>号源表在本模块，直读裸 Mapper 取开诊 start/end 比对（同 461 行先例，不引 SPI 端口）；
     * scheduleId 缺失或号源查不到时只保留格式/粒度校验。
     */
    private void validateSlotTime(String slotTime, Long scheduleId) {
        if (slotTime == null || !slotTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new BusinessException("就诊时段格式不合法");
        }
        if (Integer.parseInt(slotTime.substring(3)) % 30 != 0) {
            throw new BusinessException("就诊时段必须为 30 分钟粒度（:00 或 :30）");
        }
        if (scheduleId != null) {
            BizClinicSource source = bizClinicSourceMapper.selectById(scheduleId);
            if (source != null
                    && TextUtil.hasText(source.getStartTime())
                    && TextUtil.hasText(source.getEndTime())
                    && (slotTime.compareTo(source.getStartTime()) < 0
                    || slotTime.compareTo(source.getEndTime()) > 0)) {
                throw new BusinessException("就诊时段不在所选号源的开诊时间内，请重新选择");
            }
        }
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
        RecordBriefVO origin = emrFacadeService.getRecord(upsertDTO.getRevisitRecordId());
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

        // 原子归还号源（按渠道：预约渠道退号同步递减预约池，现场渠道还总池；持有 slot_id → 段与主表双还）。
        // 当日回诊不占号源，scheduleId/slotId 均为 null，内部判空直接跳过。
        releaseSchedule(registInfo.getScheduleId(), registInfo.getRegistSource());
        releaseSlot(registInfo.getSlotId(), registInfo.getRegistSource());

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
     * ③ <b>复诊号</b> —— 费用由复诊收费策略按「与原就诊的间隔天数」现算，改期会让免收口径失效。
     *
     * <p>账单实收与复诊号闸门在本方法内；同科室/同医生/同诊查价核对号源表（号源在本模块，直读裸 Mapper）。
     */
    private void assertPaidRescheduleSamePrice(AppointInfo existing, BizClinicSource newSource) {
        if (existing.getBillId() == null) {
            return;
        }
        BillBriefVO bill = chargeFacadeService.getBill(existing.getBillId());
        BigDecimal paid = bill == null ? BigDecimal.ZERO : NumUtil.orZero(bill.getPaidAmount());
        if (paid.signum() <= 0) {
            return;
        }
        String billText = "该挂号已收费（账单 " + bill.getBillNo() + "，实收 ¥" + paid.toPlainString() + "）";
        if (existing.getRevisitSource() != null) {
            throw new BusinessException(billText + "是复诊号，费用按复诊收费策略现算，改期会使免收口径失效；"
                    + "请退号退费（按原支付渠道退回）后重新挂号");
        }
        // 同科室、同医生、同诊查价三项比对；旧号源直读本模块号源表，任一不一致即退号重挂
        BizClinicSource oldSource = existing.getScheduleId() == null
                ? null : bizClinicSourceMapper.selectById(existing.getScheduleId());
        if (oldSource == null || newSource == null) {
            return;
        }
        boolean sameDept = Objects.equals(oldSource.getDeptId(), newSource.getDeptId());
        boolean sameDoctor = Objects.equals(oldSource.getDoctorId(), newSource.getDoctorId());
        boolean samePrice = NumUtil.orZero(oldSource.getDiagnosisFee())
                .compareTo(NumUtil.orZero(newSource.getDiagnosisFee())) == 0;
        if (!sameDept || !sameDoctor || !samePrice) {
            throw new BusinessException("换号源需同科室、同医生、同诊查价；"
                    + billText + "，请退号退费（按原支付渠道退回）后重新挂号");
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
        boolean closed = chargeFacadeService.cancelRegistCharge(command);
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

        // 换号源/换段判定：scheduleId/slotId 为入参携带的身份字段，可直接比较。
        // 号源/段都在本模块，直读裸 Mapper 完成校验、扣新还旧与快照跟随（同 461 行先例，不引 SPI 端口）。
        boolean sourceChanging = registInfo.getScheduleId() != null
                && !registInfo.getScheduleId().equals(existing.getScheduleId());
        // 渠道沿用原挂号（改号不改来源）：还旧扣新都按它择池
        Integer channel = existing.getRegistSource();

        BizClinicSource newSource = null;
        if (sourceChanging) {
            // 先扣新成功再还旧：新号满时抛异常，旧号不动，事务整体回滚。
            newSource = bizClinicSourceMapper.selectById(registInfo.getScheduleId());
            if (newSource == null) {
                throw new BusinessException("所选号源不存在或已停诊，请重新选择");
            }
            // 已收费改期：仅允许同科室、同医生、同诊查价，否则退号重挂
            assertPaidRescheduleSamePrice(existing, newSource);
            boolean newSameDayOrPast = isSameDayOrPast(newSource.getScheduleDate());
            if (deductSchedule(newSource.getId(), channel, newSameDayOrPast) == 0) {
                throw new BusinessException("所选新号源已约满，请选择其他号源");
            }
            releaseSchedule(existing.getScheduleId(), channel);
            // 跟随新号刷新主快照（科室/医生/就诊日/号别）
            existing.setDeptId(newSource.getDeptId());
            existing.setDeptName(newSource.getDeptName());
            existing.setDoctorId(newSource.getDoctorId());
            existing.setDoctorName(newSource.getDoctorName());
            existing.setVisitDate(newSource.getScheduleDate());
            existing.setRegistType(newSource.getIsExpert() != null && newSource.getIsExpert() == 1
                    ? RegistTypeEnum.EXPERT.getCode() : RegistTypeEnum.NORMAL.getCode());
            existing.setScheduleId(registInfo.getScheduleId());
        }

        // 换段（同号源换段，或换新号源同时带新段）：扣新段 → 还旧段 → 刷新段快照
        Long newSlotId = registInfo.getSlotId();
        boolean slotChanged = newSlotId != null && !newSlotId.equals(existing.getSlotId());
        if (slotChanged) {
            BizClinicSourceSlot newSlot = bizClinicSourceSlotMapper.selectById(newSlotId);
            Long ownerScheduleId = sourceChanging ? registInfo.getScheduleId() : existing.getScheduleId();
            if (newSlot == null || !ownerScheduleId.equals(newSlot.getScheduleId())
                    || newSlot.getStatus() == null || newSlot.getStatus() != 1) {
                throw new BusinessException("所选时段不存在、已停用或不属于该号源，请重新选择");
            }
            boolean slotSameDayOrPast = sourceChanging
                    ? isSameDayOrPast(newSource.getScheduleDate())
                    : isSameDayOrPast(existing.getVisitDate());
            if (deductSlot(newSlotId, channel, slotSameDayOrPast) == 0) {
                throw new BusinessException("所选新时段已约满，请选择其他时段");
            }
            releaseSlot(existing.getSlotId(), channel);
            existing.setSlotId(newSlotId);
            existing.setSlotStart(newSlot.getStartTime());
            existing.setSlotEnd(newSlot.getEndTime());
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
        if (sourceChanging && registInfo.getSlotId() == null) {
            // 换新号源且未带新段：updateById 跳过 null 字段，旧段快照必须显式清空（防残留旧段）
            this.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AppointInfo>()
                    .eq(AppointInfo::getId, existing.getId())
                    .set(AppointInfo::getSlotId, null)
                    .set(AppointInfo::getSlotStart, null)
                    .set(AppointInfo::getSlotEnd, null));
        }
        return saved;
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
            patientFacadeService.markLastVisit(regist.getPatientId(), LocalDateTime.now(),
                    regist.getDeptId(), regist.getDeptName(),
                    regist.getDoctorId(), regist.getDoctorName());
            patientFacadeService.markFirstVisit(regist.getPatientId(), LocalDateTime.now(),
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
        if (patientFacadeService.patientScopeViolated(previewDTO.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的费用信息");
        }
        return revisitFeePreview(previewDTO);
    }

    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectListScoped(Long patientId) {
        if (patientFacadeService.patientScopeViolated(patientId)) {
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
        vo.setInsuranceWarning(entity.getInsuranceWarning());
        return vo;
    }

}
