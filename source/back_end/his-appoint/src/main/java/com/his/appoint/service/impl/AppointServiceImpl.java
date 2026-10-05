package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.*;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.entity.BizScheduleSlot;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.appoint.enums.RevisitSourceEnum;
import com.his.appoint.enums.VisitTypeEnum;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.mapper.BizScheduleSlotMapper;
import com.his.appoint.service.AppointChargeGateway;
import com.his.appoint.service.AppointService;
import com.his.appoint.service.MedicalRecordRefGateway;
import com.his.appoint.service.RevisitFeePolicyService;
import com.his.appoint.trigger.DayEndSettleTrigger;
import com.his.appoint.support.PatientVisitSummaryUpdater;
import com.his.appoint.vo.AppointStatusCountVO;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.appoint.vo.RevisitFeePreviewVO;
import com.his.appoint.vo.RevisitRecordSelectVO;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.enums.AttendModeEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.PatientGuardianService;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import com.his.security.entity.CurrentUser;
import com.his.system.entity.BizStaffSchedule;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂号服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointServiceImpl extends ServiceImpl<BizAppointInfoMapper, BizAppointInfo> implements AppointService {

    private final BizScheduleMapper scheduleMapper;
    private final BizScheduleSlotMapper slotMapper;
    private final BizQueueMapper queueMapper;
    private final RedisSequenceService redisSequenceService;
    private final BizPatientMapper patientMapper;
    private final PatientGuardianService patientGuardianService;
    /**
     * 挂号单上的「班别」是历史快照：排班表已不存班别，只能按 shift_id 从班次字典取
     */
    private final ShiftService shiftService;
    /**
     * 岗位排班事实：号源放不放由它决定（出诊计划只是「这个班放号」，不代表人今天在场）
     */
    private final StaffScheduleService staffScheduleService;
    /**
     * 收费能力通过 SPI 获取，实现位于 his-charge
     */
    private final ObjectProvider<AppointChargeGateway> appointChargeGateway;

    /**
     * 病历引用能力通过 SPI 获取，实现位于 his-emr（复诊关联原病历校验）
     */
    private final ObjectProvider<MedicalRecordRefGateway> medicalRecordRefGateway;

    /**
     * 复诊号收多少钱的唯一判定入口（原来写死 {@code waived = revisit}）
     */
    private final RevisitFeePolicyService revisitFeePolicyService;

    /**
     * 结诊后回写患者主档「首次/最近就诊」冗余字段（患者中心列表展示用）
     */
    private final PatientVisitSummaryUpdater patientVisitSummaryUpdater;

    /**
     * 日终结转的懒触发：挂号的三个读接口（列表/统计/看板）在取数前顺手把「昨天及更早」的遗留收掉。
     */
    private final DayEndSettleTrigger dayEndSettleTrigger;

    private static BigDecimal nzAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 就诊日已过 → 返回拒绝理由，未过期返回 null。
     *
     * <p>状态闸门（{@link AppointStatusEnum#isCancelable}）只管「诊疗发没发生」，管不到「日期过没过去」：
     * 日终结转没跑的那天（夜里关机、停诊、导数据），昨天的号还停在 1/2，于是既能退又能改约。
     * 而这两件事在业务上都不成立 —— 号源属于过去的日期，退了也还不回池（见 {@code DayEndSettleMapper}
     * 类注释「为什么不释放号源」），改了等于把昨天的就诊挪到今天。
     *
     * <p>刻意<b>只到「日」不到「时段」</b>：当天下午的号没来看，傍晚来窗口退钱是合理诉求，
     * 用 slotEnd 卡死会把这种正常操作变成 400。日终结转同样是次日 00:10 才收，两边口径一致。
     *
     * <p>患者昨天没来、今天要把挂号费退掉，走的不是退号而是<b>退费申请</b>
     * （{@code /charge/refund/applyRefund}：申请 → 审核 → 执行，全程留痕、允许跨期），
     * 挂号行保持「爽约/未就诊」不动 —— 那是就诊事实，改了医保和报表都对不上。
     */
    private static String visitDatePastReason(BizAppointInfo regist, String action) {
        LocalDate visitDate = regist.getVisitDate();
        if (visitDate == null || !visitDate.isBefore(LocalDate.now())) {
            return null;
        }
        return "就诊日 " + visitDate + " 已过，不允许" + action
                + "：患者未到诊需退挂号费的，请到收费处「退费管理」发起退费申请。";
    }

    /**
     * 「某某状态不允许做某动作」的人话文案。
     *
     * <p>为什么不在各处写死一句「当前状态不允许修改」：操作台的人看到这句话不知道该去干什么。
     * 说清「现在是已就诊」+「为什么不能改」+「该走哪条路」，才是能自解释的错误。
     * （与前端 {@code AppointmentsView.sourceLockReason} 是同一套口径，两边一起改。）
     */
    private static String notAllowedReason(Integer registStatus, String action) {
        String label = AppointStatusEnum.getText(registStatus);
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
        LambdaQueryWrapper<BizAppointInfo> wrapper = buildFilter(queryDTO);
        // create_time 是 DATETIME(0)：只到秒。批量/同秒落库的行 create_time 完全并列，
        // 而「并列行之间」的返回顺序 SQL 不保证稳定 → 翻页不是集合的划分：
        // 实测（2026-09-21）单日 432 条、其中 92 条同一秒，3 页翻下来只取到 430 个唯一 id
        // —— 2 个挂号出现两次、另 2 个永远取不到，**接口不报错**。
        // 看板/收件箱这类「翻页取全」的调用都会因此凭空多一个人、少一个人。
        // 必须补唯一二级键（id），否则 ORDER BY 只定义了"大致顺序"。
        wrapper.orderByDesc(BizAppointInfo::getCreateTime)
                .orderByDesc(BizAppointInfo::getId);

        Page<BizAppointInfo> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
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
    private LambdaQueryWrapper<BizAppointInfo> buildFilter(AppointQueryDTO queryDTO) {
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<BizAppointInfo>()
                .eq(queryDTO.getPatientId() != null, BizAppointInfo::getPatientId, queryDTO.getPatientId())
                .eq(queryDTO.getDoctorId() != null, BizAppointInfo::getDoctorId, queryDTO.getDoctorId())
                .eq(queryDTO.getVisitDate() != null, BizAppointInfo::getVisitDate, queryDTO.getVisitDate())
                .eq(queryDTO.getRegistStatus() != null, BizAppointInfo::getRegistStatus, queryDTO.getRegistStatus())
                .ge(queryDTO.getBeginTime() != null, BizAppointInfo::getCreateTime, queryDTO.getBeginTime())
                .le(queryDTO.getEndTime() != null, BizAppointInfo::getCreateTime, queryDTO.getEndTime());
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
    private void applyDeptScope(LambdaQueryWrapper<BizAppointInfo> wrapper, Long requestedDeptId) {
        Long scopedDeptId = DeptScopeGuard.resolveDeptId(requestedDeptId);
        if (scopedDeptId != null) {
            wrapper.eq(BizAppointInfo::getDeptId, scopedDeptId);
        } else if (DeptScopeGuard.isScoped()) {
            wrapper.in(BizAppointInfo::getDeptId, DeptScopeGuard.allowedDeptIds());
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
        vo.setStatsTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
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
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(BizAppointInfo::getVisitDate, queryDTO.getStartDate(), queryDTO.getEndDate())
                .eq(queryDTO.getDeptId() != null, BizAppointInfo::getDeptId, queryDTO.getDeptId())
                .eq(queryDTO.getDoctorId() != null, BizAppointInfo::getDoctorId, queryDTO.getDoctorId())
                // 排序要**全序**（同一格内患者顺序稳定）：就诊日 → 班次 → 挂号时间 → id。
                // 最后一项是唯一键，同秒并列行才不会在两次请求间换位置（listPage 同样吃过这个亏）。
                .orderByAsc(BizAppointInfo::getVisitDate)
                .orderByAsc(BizAppointInfo::getScheduleId)
                .orderByAsc(BizAppointInfo::getCreateTime)
                .orderByAsc(BizAppointInfo::getId);
        return toVOList(this.list(wrapper));
    }

    /**
     * 批量装配挂号 VO —— 收费状态（SPI 到 his-charge）与队列号（候诊队列）都必须**批量**取。
     *
     * <p>看板一次要装几千条，循环里单条查会直接打穿数据库（N+1）；
     * 分页查询和看板全量查询共用这一段，避免两处口径各写一遍再慢慢跑偏。
     */
    private List<BizAppointInfoListVO> toVOList(List<BizAppointInfo> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        // 批量收集关联的账单ID / 挂号ID
        List<Long> billIds = new ArrayList<>();
        List<Long> registIds = new ArrayList<>();
        for (BizAppointInfo appoint : records) {
            if (appoint.getBillId() != null) {
                billIds.add(appoint.getBillId());
            }
            registIds.add(appoint.getId());
        }

        // 批量查询账单信息（经 SPI 委派至 his-charge）
        Map<Long, AppointChargeGateway.BillBrief> billMap = new HashMap<>();
        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        if (gateway != null && !billIds.isEmpty()) {
            billMap = gateway.mapBillsByIds(billIds);
        }

        // 批量查询队列记录：挂号表本身没有排队号与队列状态，
        // 分诊台要显示真实排队状态就必须关联候诊队列（历史上前端拿 registStatus 顶替，状态列整体串位）
        Map<Long, BizQueue> queueMap = new HashMap<>();
        List<BizQueue> queues = queueMapper.selectList(
                new LambdaQueryWrapper<BizQueue>().in(BizQueue::getRegistId, registIds));
        for (BizQueue queue : queues) {
            // 同一挂号可能对应多条队列记录（过号后重入队），取 id 最大的一条作为当前队列
            BizQueue exist = queueMap.get(queue.getRegistId());
            if (exist == null || (queue.getId() != null && exist.getId() != null && queue.getId() > exist.getId())) {
                queueMap.put(queue.getRegistId(), queue);
            }
        }

        // 转换为VO并填充支付状态
        List<BizAppointInfoListVO> voList = new ArrayList<>();
        for (BizAppointInfo appoint : records) {
            voList.add(convertToVO(appoint, billMap, queueMap.get(appoint.getId())));
        }
        return voList;
    }

    /**
     * 将挂号实体转换为VO，包含账单状态与队列状态
     *
     * @param queue 该挂号当前的队列记录，可能为空（未签到）
     */
    private BizAppointInfoListVO convertToVO(BizAppointInfo appoint,
                                             Map<Long, AppointChargeGateway.BillBrief> billMap,
                                             BizQueue queue) {
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
            AppointChargeGateway.BillBrief bill = billMap.get(appoint.getBillId());
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
    public BizAppointInfo addAppoint(AppointUpsertDTO upsertDTO) {
        // 复诊收口：来源必须显式传，来源决定「占不占号源」和「按哪条策略收钱」
        // 顺序很重要：入参本身就是错的（初诊却要关联病历 / 指向不存在的病历 /
        // 病历不是这个患者的），不该被「已存在挂号记录」这类状态冲突抢先报出来 ——
        // 那样调用方会对着一个不是原因的错去改。
        boolean revisit = upsertDTO.getVisitType() != null
                && upsertDTO.getVisitType() == VisitTypeEnum.REVISIT.getCode();
        MedicalRecordRefGateway.RecordBrief origin = validateRevisit(upsertDTO, revisit);
        Long revisitRecordId = origin == null ? null : origin.getId();

        // 幂等性校验：检查是否已存在相同挂号记录
        checkIdempotent(upsertDTO, revisit);

        BizPatient bizPatient = patientMapper.selectById(upsertDTO.getPatientId());
        // D类（业务规则）：查库后的关联实体存在性判定，不是字段填没填
        if (bizPatient == null) {
            throw new BusinessException("患者信息为空，请检查后重试");
        }

        BizAppointInfo appointInfo = new BizAppointInfo();
        appointInfo.setPatientId(upsertDTO.getPatientId());
        appointInfo.setScheduleId(upsertDTO.getScheduleId());
        appointInfo.setRevisitRecordId(revisitRecordId);
        // 挂号渠道（1-窗口 2-自助机 3-网上 4-预约；空默认窗口），决定扣哪池：
        // 1/2/3 现场可用号不可吃预约池剩余，4 必须从预约池内扣
        Integer registSource = upsertDTO.getRegistSource() == null ? 1 : upsertDTO.getRegistSource();
        if (registSource < 1 || registSource > 4) {
            throw new BusinessException("挂号来源不合法（1-窗口 2-自助机 3-网上 4-预约）");
        }
        appointInfo.setRegistSource(registSource);
        appointInfo.setPatientName(bizPatient.getPatientName());
        appointInfo.setSettlementType(upsertDTO.getSettlementType());
        appointInfo.setMedicalInsuranceType(upsertDTO.getMedicalInsuranceType());
        appointInfo.setMedicalInsuranceNo(upsertDTO.getMedicalInsuranceNo());
        appointInfo.setVisitType(upsertDTO.getVisitType());
        appointInfo.setRevisitSource(revisit ? upsertDTO.getRevisitSource() : null);

        // 复诊关联原病历
        // （入参校验已在方法开头做掉，这里只剩「号源校验要在归属校验之后」这个约束）
        // 复诊号必须指向一张真实存在、且属于同一患者的原病历；
        // 只落「引用关系」，**原病历一律不改**（不复制、不回写、不改状态）。

        // 查询排班信息：只有「当日回诊」不占号源（同一次就诊的延续），
        // 医嘱复诊/自助复诊/随访复诊都是新的一次就诊，必须选排班、扣号源。
        boolean noSchedule = revisit && RevisitSourceEnum.needsNoSchedule(upsertDTO.getRevisitSource());
        BizSchedule schedule = null;
        BizScheduleSlot slot = null;
        if (appointInfo.getScheduleId() != null) {
            if (noSchedule) {
                throw new BusinessException("当日回诊不占号源，请勿选择排班");
            }
            schedule = scheduleMapper.selectById(appointInfo.getScheduleId());
            if (schedule == null) {
                throw new BusinessException("排班信息不存在");
            }
            // 只能挂医生出诊号源（sql/195）：护士/技师/收费员这些岗位是出勤排班，号源恒 0、没有诊室，
            // 挂到他们名下等于开出一张「有医生姓名、没有医生在场」的号。
            if (!StaffTypeEnum.isDoctor(schedule.getStaffType())) {
                throw new BusinessException("只能挂医生出诊号源：该排班是「"
                        + StaffTypeEnum.getText(schedule.getStaffType()) + "」岗位出勤，不对外放号");
            }
            // 号源闸：投影行说「这个班放号」，人在不在场由岗位排班事实说了算。
            // 事实被删或改成请假/停班/听班而投影还活着时，必须停挂——否则开出一张「有医生姓名、医生不在场」的号。
            // 引用为空 = 写入口收敛之前建的历史排班，没有事实可核，按原口径放行
            if (schedule.getStaffScheduleId() != null) {
                BizStaffSchedule core = staffScheduleService.getById(schedule.getStaffScheduleId());
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
            // 时间片段：传了 slotId 必须命中该排班下的正常段
            // （段余量够不够由扣减 SQL 原子兜底，这里只挡「段不存在/不属于该排班/已停用」）
            if (upsertDTO.getSlotId() != null) {
                slot = slotMapper.selectById(upsertDTO.getSlotId());
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
            } else if (StringUtils.hasText(upsertDTO.getSlotTime())) {
                // 旧路径：只传就诊时段，不做段级扣减（历史挂号无段）
                validateSlotTime(upsertDTO.getSlotTime(), schedule);
                appointInfo.setSlotTime(upsertDTO.getSlotTime());
            }
        } else if (!noSchedule) {
            // B类（条件必填）：只有「当日回诊」免号源，其余来源必须选排班，注解无法按来源分支
            throw new BusinessException("请选择号源");
        }

        // 查询患者信息，自动填充挂号记录
        BizPatient patient = patientMapper.selectById(appointInfo.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        appointInfo.setPatientNo(patient.getPatientNo());
        appointInfo.setPatientName(patient.getPatientName());
        appointInfo.setGender(patient.getGender());
        appointInfo.setAge(patient.getAge());
        appointInfo.setPhone(patient.getPhone());

        // 如果挂号时设置了医保信息且患者之前没有，回填到患者信息
        if (StringUtils.hasText(appointInfo.getMedicalInsuranceType()) || StringUtils.hasText(appointInfo.getMedicalInsuranceNo())) {
            boolean needUpdate = false;
            if (StringUtils.hasText(appointInfo.getMedicalInsuranceType()) && !StringUtils.hasText(patient.getMedicalInsuranceType())) {
                patient.setMedicalInsuranceType(appointInfo.getMedicalInsuranceType());
                needUpdate = true;
            }
            if (StringUtils.hasText(appointInfo.getMedicalInsuranceNo()) && !StringUtils.hasText(patient.getMedicalInsuranceNo())) {
                patient.setMedicalInsuranceNo(appointInfo.getMedicalInsuranceNo());
                needUpdate = true;
            }
            if (needUpdate) {
                patientMapper.updateById(patient);
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
        }
        appointInfo.setRegistTime(LocalDateTime.now());
        appointInfo.setRegistStatus(AppointStatusEnum.REGISTERED.getCode()); // 待支付

        // 保存挂号记录（排队号在签到时生成，以诊室编号为前缀）
        this.save(appointInfo);

        // 原子扣减号源（按渠道分池：现场渠道不许占用预约池剩余，预约渠道必须从池内扣；
        // WHERE 条件兜住并发抢号，返回 0 表示对应池已空）
        // 号源事实在段上：选了段 → 先扣段再扣主表（同事务双写，读路径仍走主表）；
        // 未选段（历史入口）→ 只扣主表。
        if (schedule != null) {
            boolean appt = registSource == 4;
            if (slot != null) {
                int deductedSlot = appt
                        ? slotMapper.deductSlotSourceForAppointment(slot.getId())
                        : slotMapper.deductSlotSourceForWalkin(slot.getId());
                if (deductedSlot == 0) {
                    throw new BusinessException(appt ? "该时间段的预约号源已满" : "该时间段号源已满");
                }
                int deductedMain = appt
                        ? scheduleMapper.deductScheduleSourceForAppointment(schedule.getId())
                        : scheduleMapper.deductScheduleSourceForWalkin(schedule.getId());
                if (deductedMain == 0) {
                    // 段有余但主表 Σ 扣不动（数据不一致）：抛异常回滚整个事务，不做静默半扣
                    throw new BusinessException("号源状态异常（排班汇总与时间段不一致），请稍后重试");
                }
            } else {
                int deducted = appt
                        ? scheduleMapper.deductScheduleSourceForAppointment(schedule.getId())
                        : scheduleMapper.deductScheduleSourceForWalkin(schedule.getId());
                if (deducted == 0) {
                    throw new BusinessException(appt ? "预约号源已满" : "号源已满");
                }
            }
        }

        // 挂号费记账 + 出账，经 SPI 委派至 his-charge。
        // 复诊收哪几项费由「复诊收费策略」判定（原来这里写死 waived = revisit，见 sql/121）。
        // 免收时不记账也不出账（返回 null）：签到门禁认的是「这张号没有未结清的挂号账单」，
        // 旧模型为了过门禁造 0 元收费单那套已经随收费单一起退役。
        RevisitFeePolicyService.RevisitFeeDecision feeDecision = revisitFeePolicyService.decide(
                buildRevisitFeeContext(revisit, upsertDTO.getRevisitSource(), origin, schedule,
                        appointInfo.getDeptId(), appointInfo.getDoctorId(), appointInfo.getVisitDate()));

        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        if (gateway != null && !feeDecision.isWaived() && feeDecision.totalFee().compareTo(BigDecimal.ZERO) > 0) {
            AppointChargeGateway.RegistChargeCommand command = new AppointChargeGateway.RegistChargeCommand();
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

            AppointChargeGateway.BillBrief bill = gateway.createRegistCharge(command);
            if (bill != null) {
                appointInfo.setBillId(bill.getBillId());
                appointInfo.setBillNo(bill.getBillNo());
                this.updateById(appointInfo);
            }
        } else if (gateway == null) {
            // 收费模块缺席：号照挂，但这笔挂号费从来没进过应收，台账上得留一句解释
            log.warn("收费能力不可用，挂号费未记账（号已挂，请人工补收）：registNo={} 应收 ¥{}",
                    appointInfo.getRegistNo(), feeDecision.totalFee().toPlainString());
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
        MedicalRecordRefGateway refGateway = medicalRecordRefGateway.getIfAvailable();
        if (refGateway == null) {
            throw new BusinessException("病历服务不可用，无法关联原病历");
        }
        MedicalRecordRefGateway.RecordBrief origin = refGateway.getRecord(previewDTO.getRevisitRecordId());
        if (origin == null) {
            throw new BusinessException("原病历不存在，无法预估复诊费用");
        }
        if (origin.getPatientId() == null || !origin.getPatientId().equals(previewDTO.getPatientId())) {
            throw new BusinessException("原病历不属于当前患者，无法预估复诊费用");
        }

        RevisitFeePreviewVO vo = new RevisitFeePreviewVO();
        BizSchedule schedule = null;
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
            // B类（条件必填）：仅「当日回诊」不占号源可免填，必填性取决于同一请求的复诊来源
            if (previewDTO.getScheduleId() == null) {
                throw new BusinessException("请选择号源");
            }
            schedule = scheduleMapper.selectById(previewDTO.getScheduleId());
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
     *
     * <p>走 SPI 而不是直接 join 病历表：his-appoint 不依赖 his-emr 的 mapper（依赖方向单向）。
     * 实现缺失（未引入 his-emr）时返回空集合，让前端下拉显示「暂无就诊记录」而不是 500。
     */
    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit) {
        MedicalRecordRefGateway refGateway = medicalRecordRefGateway.getIfAvailable();
        if (refGateway == null || patientId == null) {
            return new ArrayList<>();
        }
        List<RevisitRecordSelectVO> options = new ArrayList<>();
        for (MedicalRecordRefGateway.RecordBrief brief : refGateway.listRecentByPatient(patientId, limit)) {
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
            boolean revisit, Integer revisitSource, MedicalRecordRefGateway.RecordBrief origin,
            BizSchedule schedule, Long targetDeptId, Long targetDoctorId, LocalDate targetVisitDate) {
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
    private void validateSlotTime(String slotTime, BizSchedule schedule) {
        if (slotTime == null || !slotTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new BusinessException("就诊时段格式不合法（HH:mm）");
        }
        if (Integer.parseInt(slotTime.substring(3)) % 30 != 0) {
            throw new BusinessException("就诊时段必须为 30 分钟粒度（:00 或 :30）");
        }
        String start = schedule.getStartTime();
        String end = schedule.getEndTime();
        if (StringUtils.hasText(start) && StringUtils.hasText(end)
                && (slotTime.compareTo(start) < 0 || slotTime.compareTo(end) >= 0)) {
            throw new BusinessException("就诊时段不在班次时间范围内（" + start + "-" + end + "）");
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
    private MedicalRecordRefGateway.RecordBrief validateRevisit(AppointUpsertDTO upsertDTO, boolean revisit) {
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
        MedicalRecordRefGateway refGateway = medicalRecordRefGateway.getIfAvailable();
        if (refGateway == null) {
            throw new BusinessException("病历服务不可用，无法关联原病历");
        }
        MedicalRecordRefGateway.RecordBrief origin = refGateway.getRecord(upsertDTO.getRevisitRecordId());
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
            LambdaQueryWrapper<BizAppointInfo> revisitWrapper = new LambdaQueryWrapper<>();
            revisitWrapper.eq(BizAppointInfo::getRevisitRecordId, upsertDTO.getRevisitRecordId())
                    .notIn(BizAppointInfo::getRegistStatus,
                            AppointStatusEnum.OVERDUE.getCode(), AppointStatusEnum.CANCELLED.getCode());
            if (this.count(revisitWrapper) > 0) {
                throw new BusinessException("该病历已创建过复诊号，不可重复创建");
            }
            return;
        }
        if (upsertDTO.getScheduleId() == null) {
            // 占号源的挂号（初诊与 2/3/4 类复诊）没选排班时不做判重，
            // 否则条件会退化成人级全表比对，报出「已在此排班挂号」这种对不上号的原因；
            // 缺排班由后面的「请选择号源」拦下。
            return;
        }
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAppointInfo::getPatientId, upsertDTO.getPatientId())
                .eq(BizAppointInfo::getScheduleId, upsertDTO.getScheduleId())
                // visit_type 必须进判重键：同一排班上午看完初诊、下午接着挂复诊是常态
                .eq(BizAppointInfo::getVisitType, upsertDTO.getVisitType())
                .notIn(BizAppointInfo::getRegistStatus, AppointStatusEnum.OVERDUE.getCode(), AppointStatusEnum.CANCELLED.getCode()); // 排除已取消的记录
        if (this.count(wrapper) > 0) {
            throw new BusinessException(revisit
                    ? "该患者已在此排班预约过复诊，不可重复预约"
                    : "该患者已在此排班挂号，不可重复挂号");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegist(Long registId, String reason) {
        BizAppointInfo registInfo = this.getById(registId);
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
        this.updateById(registInfo);

        // 更新排队状态：退号≠过号，队列同样置「已退号」
        LambdaQueryWrapper<BizQueue> queueWrapper = new LambdaQueryWrapper<>();
        queueWrapper.eq(BizQueue::getRegistId, registId);
        BizQueue queue = queueMapper.selectOne(queueWrapper);
        if (queue != null) {
            queue.setQueueStatus(QueueStatusEnum.CANCELLED.getCode());
            queueMapper.updateById(queue);
        }

        // 原子释放号源（按挂号渠道还池：预约渠道退号时预约池已用同步递减；
        // 池计数已被修正（=0）时兜底走总池释放，保证总号正确）
        // 段级还号：新挂号持有 slot_id → 段与主表双还；段可能已被物理删除（排班重建），
        // released == 0 容错跳过——挂号上的 dept/doctor/时段快照自证，不阻塞退号。
        if (registInfo.getScheduleId() != null) {
            boolean appt = registInfo.getRegistSource() != null && registInfo.getRegistSource() == 4;
            if (registInfo.getSlotId() != null) {
                int releasedSlot = appt
                        ? slotMapper.releaseSlotSourceForAppointment(registInfo.getSlotId())
                        : slotMapper.releaseSlotSourceForWalkin(registInfo.getSlotId());
                if (releasedSlot == 0 && appt) {
                    slotMapper.releaseSlotSourceForWalkin(registInfo.getSlotId());
                }
            }
            int released = appt
                    ? scheduleMapper.releaseScheduleSourceForAppointment(registInfo.getScheduleId())
                    : scheduleMapper.releaseScheduleSourceForWalkin(registInfo.getScheduleId());
            if (released == 0 && appt) {
                scheduleMapper.releaseScheduleSourceForWalkin(registInfo.getScheduleId());
            }
        }

        // 退号必须同步处理钱：已收费 → 退费，待收费 → 作废。
        // 只改挂号状态不碰收费单，会留下「已退号 + 收了钱」的对账缺口（钱退不回去、日报还挂着应收）。
        settleChargeOnRegistCancel(registInfo, reason);

        return true;
    }

    /**
     * 换号源前校验「这笔挂号还没收钱」。
     *
     * <p>为什么要拦：账单是挂在挂号单上的（挂号信息的账单ID）。挂号记录一改，
     * 科室/医生/日期/号别全变，账单却还指着原来的事实 —— 财务对账、科室日报、医保对账全部错位；
     * 而且普通号 ↔ 专家号有价差，改号源时没有人处理这笔差额。
     *
     * <p>手机端/网上预约付过款的尤其如此：钱已经进了微信/支付宝，挂号台把号源一换，
     * 没有任何人能把这笔钱挪到新号源上。真实窗口的做法也是这一条：
     * 已缴费患者换号走「退号退费（按原支付渠道退回）→ 重新挂号」。
     */
    private void assertSourceChangeUnpaid(BizAppointInfo existing) {
        if (existing.getBillId() == null) {
            return;
        }
        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        if (gateway == null) {
            // 收费能力缺失（未引入 his-charge）时不阻断挂号业务，但要留下痕迹便于排查
            log.warn("收费能力不可用，跳过改约的收费校验：registId={}", existing.getId());
            return;
        }
        AppointChargeGateway.BillBrief bill = gateway.getBill(existing.getBillId());
        if (bill == null) {
            return;
        }
        BigDecimal paid = nzAmount(bill.getPaidAmount());
        if (paid.signum() <= 0) {
            return;
        }
        throw new BusinessException("该挂号已收费（账单 " + bill.getBillNo()
                + "，实收 ¥" + paid.toPlainString()
                + "），不能直接变更号源；请先退号退费（按原支付渠道退回）后重新挂号");
    }

    /**
     * 退号时联动账单（走 SPI 到 his-charge）：整单撤销 = 钱原路退回 + 应收红冲 + 账单作废。
     *
     * <p>与退号在<b>同一个事务</b>里：只退号不处理钱，患者钱退不回去、日报还挂着这笔应收。
     */
    private void settleChargeOnRegistCancel(BizAppointInfo registInfo, String reason) {
        if (registInfo.getBillId() == null) {
            return;
        }
        AppointChargeGateway gateway = appointChargeGateway.getIfAvailable();
        if (gateway == null) {
            log.warn("收费能力不可用，退号未联动账单：registId={}，billId={}",
                    registInfo.getId(), registInfo.getBillId());
            return;
        }
        AppointChargeGateway.CancelCommand command = new AppointChargeGateway.CancelCommand();
        command.setBillId(registInfo.getBillId());
        command.setReason(StringUtils.hasText(reason) ? reason : "退号");
        command.setOperator(currentOperatorName());
        boolean closed = gateway.cancelRegistCharge(command);
        log.info("退号联动账单：registId={}，billId={}，撤销={}",
                registInfo.getId(), registInfo.getBillId(), closed);
    }

    /**
     * 当前操作人姓名 —— 一律服务端取，不信前端传的身份。
     */
    private String currentOperatorName() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            return "系统";
        }
        if (StringUtils.hasText(currentUser.getEmployeeName())) {
            return currentUser.getEmployeeName();
        }
        if (StringUtils.hasText(currentUser.getRealName())) {
            return currentUser.getRealName();
        }
        return currentUser.getUsername();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRegist(BizAppointInfo registInfo) {
        BizAppointInfo existing = this.getById(registInfo.getId());
        if (existing == null) {
            throw new BusinessException("挂号记录不存在");
        }
        if (!AppointStatusEnum.isSourceChangeAllowed(existing.getRegistStatus())) {
            // 换号源只允许「已挂号」：已签到号源已消耗且患者已在队列里（换号得连队列一起搬），
            // 已接诊/已就诊是诊疗事实 —— 改了会让病历、队列号前缀、诊室快照全对不上。
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

        // 换号源前先看钱：已收费的挂号不能直接改号源（价差、收费单归属都会错位）
        boolean sourceChanging = registInfo.getScheduleId() != null
                && !registInfo.getScheduleId().equals(existing.getScheduleId());
        if (sourceChanging) {
            assertSourceChangeUnpaid(existing);
        }

        // 同排班内换时间段（排班不变、只挪段）：主表 Σ 还了又扣、净零不动，
        // 只做段级释放/占用与快照更新。不校验已收费——钱挂在挂号上，同排班挪段无财务错位。
        Long oldSlotId = existing.getSlotId();
        boolean slotChanging = !sourceChanging
                && registInfo.getSlotId() != null
                && !registInfo.getSlotId().equals(oldSlotId);
        if (slotChanging) {
            BizScheduleSlot newSlot = slotMapper.selectById(registInfo.getSlotId());
            if (newSlot == null || !existing.getScheduleId().equals(newSlot.getScheduleId())) {
                throw new BusinessException("新时间段不存在或已调整，请重新选择");
            }
            if (newSlot.getStatus() == null || newSlot.getStatus() != 1) {
                throw new BusinessException("新时间段已停用，请重新选择");
            }
            if (oldSlotId != null) {
                int releasedSlot = apptChannel
                        ? slotMapper.releaseSlotSourceForAppointment(oldSlotId)
                        : slotMapper.releaseSlotSourceForWalkin(oldSlotId);
                if (releasedSlot == 0 && apptChannel) {
                    slotMapper.releaseSlotSourceForWalkin(oldSlotId);
                }
            }
            int deductedSlot = apptChannel
                    ? slotMapper.deductSlotSourceForAppointment(newSlot.getId())
                    : slotMapper.deductSlotSourceForWalkin(newSlot.getId());
            if (deductedSlot == 0) {
                throw new BusinessException(apptChannel ? "新时间段的预约号已满" : "新时间段号源已满");
            }
            existing.setSlotId(newSlot.getId());
            existing.setSlotStart(newSlot.getStartTime());
            existing.setSlotEnd(newSlot.getEndTime());
            existing.setSlotTime(newSlot.getStartTime());
        }

        // 换号源：按原渠道原子释放旧号源、原子占用新号源
        // 号源事实在段上：旧挂号有段 → 段与主表双还；新号选了段 → 段与主表双扣并更新段快照
        if (sourceChanging) {
            if (existing.getScheduleId() != null) {
                if (oldSlotId != null) {
                    int releasedSlot = apptChannel
                            ? slotMapper.releaseSlotSourceForAppointment(oldSlotId)
                            : slotMapper.releaseSlotSourceForWalkin(oldSlotId);
                    if (releasedSlot == 0 && apptChannel) {
                        slotMapper.releaseSlotSourceForWalkin(oldSlotId);
                    }
                }
                int released = apptChannel
                        ? scheduleMapper.releaseScheduleSourceForAppointment(existing.getScheduleId())
                        : scheduleMapper.releaseScheduleSourceForWalkin(existing.getScheduleId());
                if (released == 0 && apptChannel) {
                    scheduleMapper.releaseScheduleSourceForWalkin(existing.getScheduleId());
                }
            }

            BizSchedule newSchedule = scheduleMapper.selectById(registInfo.getScheduleId());
            if (newSchedule == null) {
                throw new BusinessException("新排班信息不存在");
            }
            BizScheduleSlot newSlot = null;
            if (registInfo.getSlotId() != null) {
                newSlot = slotMapper.selectById(registInfo.getSlotId());
                if (newSlot == null || !newSchedule.getId().equals(newSlot.getScheduleId())) {
                    throw new BusinessException("新时间段不存在或已调整，请重新选择");
                }
                if (newSlot.getStatus() == null || newSlot.getStatus() != 1) {
                    throw new BusinessException("新时间段已停用，请重新选择");
                }
                int deductedSlot = apptChannel
                        ? slotMapper.deductSlotSourceForAppointment(newSlot.getId())
                        : slotMapper.deductSlotSourceForWalkin(newSlot.getId());
                if (deductedSlot == 0) {
                    throw new BusinessException(apptChannel ? "新时间段的预约号已满" : "新时间段号源已满");
                }
            }
            int deducted = apptChannel
                    ? scheduleMapper.deductScheduleSourceForAppointment(newSchedule.getId())
                    : scheduleMapper.deductScheduleSourceForWalkin(newSchedule.getId());
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

        boolean saved = this.updateById(existing);
        if (saved && sourceChanging && registInfo.getSlotId() == null && existing.getSlotId() == null) {
            // 新号没段：updateById 跳过 null 字段，段快照必须显式清空（防残留旧段）
            this.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BizAppointInfo>()
                    .eq(BizAppointInfo::getId, existing.getId())
                    .set(BizAppointInfo::getSlotId, null)
                    .set(BizAppointInfo::getSlotStart, null)
                    .set(BizAppointInfo::getSlotEnd, null));
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long registId, Integer targetStatus) {
        BizAppointInfo regist = this.getById(registId);
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
        boolean saved = this.updateById(regist);
        // 操作台把状态改成「已就诊」也是结诊事实，与队列结诊（completeQueue）走同一份回写：
        // 漏掉这条路径，从挂号管理里手工完结的挂号永远不会出现在患者的「最近/首次就诊」上。
        if (saved && targetStatus == AppointStatusEnum.COMPLETED.getCode()) {
            patientVisitSummaryUpdater.onVisitCompleted(regist.getPatientId(), LocalDateTime.now(),
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
        BizAppointInfo registInfo = new BizAppointInfo();
        registInfo.setId(upsertDTO.getId());
        registInfo.setPatientId(upsertDTO.getPatientId());
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
        if (patientGuardianService.patientScopeViolated(previewDTO.getPatientId())) {
            throw new BusinessException("无权查询该就诊人的费用信息");
        }
        return revisitFeePreview(previewDTO);
    }

    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectListScoped(Long patientId) {
        if (patientGuardianService.patientScopeViolated(patientId)) {
            throw new BusinessException("无权查询该就诊人的病历");
        }
        return revisitRecordSelectList(patientId, 20);
    }

    @Override
    public void cancelRegist(AppointCancelDTO appointCancelDTO) {
        String reason = StringUtils.hasText(appointCancelDTO.getReason())
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
    private BizAppointInfoListVO toRegistVO(BizAppointInfo entity) {
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
