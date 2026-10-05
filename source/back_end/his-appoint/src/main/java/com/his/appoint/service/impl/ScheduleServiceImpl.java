package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.OnDutyQueryDTO;
import com.his.appoint.dto.ScheduleQueryDTO;
import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.dto.ScheduleUpsertDTO;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizSchedule;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizStaffSchedule;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.service.AppointService;
import com.his.appoint.service.ScheduleService;
import com.his.appoint.service.ScheduleSlotService;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffScheduleService;
import com.his.appoint.vo.OnDutyStaffVO;
import com.his.appoint.vo.ScheduleDetailVO;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.appoint.vo.StopImpactItemVO;
import com.his.common.enums.AttendModeEnum;
import com.his.common.enums.ConsultStatusEnum;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.ScheduleStatusEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.ShiftCoverUtil;
import com.his.security.entity.CurrentUser;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import com.his.system.service.SysClinicRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 挂号服务实现
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<BizScheduleMapper, BizSchedule> implements ScheduleService {

    private final BizScheduleMapper scheduleMapper;
    private final BizAppointInfoMapper appointInfoMapper;
    private final AppointService appointService;
    /**
     * 时间片段：号源与占用的事实落在段上，段生命周期（生成/重算/摊加号/Σ写回）单点收口
     */
    private final ScheduleSlotService slotService;
    /**
     * 换诊室时要同步队列上的诊室快照（队列属本模块，直接注入；不经 QueueService 以免与服务层互相依赖）
     */
    private final BizQueueMapper queueMapper;
    /**
     * 排班只填了诊室ID没填名称时补一次名称（诊室属 his-system）
     */
    private final SysClinicRoomService clinicRoomService;
    /**
     * 班次字典：排班的时间段与班别的唯一来源（写入口都经它覆盖）
     */
    private final ShiftService shiftService;
    /**
     * 全院岗位排班（出勤事实）：这条出诊计划「谁、哪天、什么班、出不出勤」的唯一来源
     */
    private final StaffScheduleService staffScheduleService;

    @Override
    public List<BizSchedule> listPage(ScheduleQueryDTO queryDTO) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        // 科室数据权限收口（2026-09-21）：以前 deptId 是"可选过滤"，不传就看全院排班。
        // 现在：
        //   · 传了 deptId → 必须在被授权范围内，越权直接拒绝（不静默改写条件）；
        //   · 没传但用户受限 → 收口到他被授权的科室集合（而不是全院）；
        //   · data_scope=1（全部数据）角色 → 维持原行为，不加条件。
        Long scopedDeptId = DeptScopeGuard.resolveDeptId(queryDTO.getDeptId());
        if (scopedDeptId != null) {
            wrapper.eq(BizSchedule::getDeptId, scopedDeptId);
        } else if (DeptScopeGuard.isScoped()) {
            wrapper.in(BizSchedule::getDeptId, DeptScopeGuard.allowedDeptIds());
        }
        // 岗位类别（sql/195）：空=全部岗位，传了就只看该岗位。
        // 排班面板是「按岗位分开看」的——「今天内科几个医生出诊」和「今天窗口几个收费员在岗」是两张表，
        // 混在一张列表里既看不清人力，也会让号源列在出勤岗上显示成一串没有意义的 0。
        wrapper.eq(queryDTO.getStaffType() != null, BizSchedule::getStaffType, queryDTO.getStaffType())
                .eq(queryDTO.getDoctorId() != null, BizSchedule::getDoctorId, queryDTO.getDoctorId())
                .ge(queryDTO.getStartDate() != null, BizSchedule::getScheduleDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, BizSchedule::getScheduleDate, queryDTO.getEndDate());
        // 2026-09-22：同一天同时段再按 **doctor_id 升序**（原来是「日期 + 开始时间」两级）。
        // 这个接口的返回同时喂给预约看板（日视图医生列 / 周视图医生行）与排班面板，
        // 少了这一级排序时，同一天同一时段的几位医生谁先谁后取决于 id 插入先后 ——
        // 而 id 又是雪花/夹具混着来的，翻一次页面顺序就可能变一次。
        // 按 doctor_id 排的好处是**稳定**：同一个 id 永远落在同一个位置，今天排第一明天还排第一，
        // 不依赖也不承诺姓氏 — 挂号员熟了之后是记位置而不是记名字。
        // ⚠️ 用了 last() 就不要再叠 orderByAsc：两个都会生成 ORDER BY，拼成两段是语法错误。
        wrapper.last("ORDER BY schedule_date ASC, start_time ASC, doctor_id ASC, id ASC");
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 查询可挂号源。
     *
     * <p><b>deptId 可空 = 不限科室</b>（2026-09-21 修）：以前这里是
     * {@code wrapper.eq(BizSchedule::getDeptId, dto.getDeptId())} 无条件拼接，
     * 于是「不传 deptId」变成 {@code dept_id = NULL} → **恒返回 0 条**，
     * 而调用方（号源面板的「全部科室」、以及数据权限放开后的看板）以为拿到的是全院号源。
     * 「查不到」和「没这个科室」在界面上长得一模一样，静默错。
     *
     * <p>同时补上科室数据权限收口（与 {@link #listPage} 同一口径）：
     * 传了越权 deptId → 直接拒绝；没传但用户受限 → 收口到被授权科室集合。
     */
    @Override
    public List<BizSchedule> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        Long scopedDeptId = DeptScopeGuard.resolveDeptId(scheduleQueryDTO.getDeptId());
        if (scopedDeptId != null) {
            wrapper.eq(BizSchedule::getDeptId, scopedDeptId);
        } else if (DeptScopeGuard.isScoped()) {
            wrapper.in(BizSchedule::getDeptId, DeptScopeGuard.allowedDeptIds());
        }
        // 号源下拉只认医生出诊排班（sql/195）：护士/技师/收费员这些岗位是**出勤排班**，
        // 号源恒 0 且没有诊室，一旦混进这个下拉，挂号员能选到「张三（收费员）」并挂出一个没有号源的号。
        // 这里硬编码 1-医生而不是透传入参：能挂号的只有医生，这不是筛选条件，是业务前提。
        wrapper.eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(scheduleQueryDTO.getVisitDate() != null,
                        BizSchedule::getScheduleDate, scheduleQueryDTO.getVisitDate())
                .eq(scheduleQueryDTO.getDoctorId() != null,
                        BizSchedule::getDoctorId, scheduleQueryDTO.getDoctorId())
                .gt(BizSchedule::getAvailableSource, 0)
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizSchedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSchedule(BizSchedule schedule) {
        applyShift(schedule);
        assertNotPast(schedule.getScheduleDate(), "新增排班");
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, null);

        schedule.setUsedSource(0);
        schedule.setAvailableSource(schedule.getTotalSource());
        if (schedule.getStatus() == null) {
            schedule.setStatus(ScheduleStatusEnum.NORMAL.getCode());
        }
        boolean ok = scheduleMapper.insert(schedule) > 0;
        if (ok && StaffTypeEnum.hasSource(schedule.getStaffType())) {
            // 时间片段：按半小时切分均分号源落段（余数给前面的段），Σ段写回主表 6 个号源字段。
            // 只有医生岗（有号源）才切片：出勤岗号源恒 0，切出来的段全是空段，
            // 反而会在「号源明细」里挂出一堆永远挂不上的时间段。
            slotService.generateSlots(schedule.getId(), schedule.getStartTime(), schedule.getEndTime(),
                    schedule.getTotalSource(), schedule.getAppointmentSource());
        }
        return ok;
    }

    /**
     * <b>为什么岗位类别要反过来覆盖投影行</b>：前端传来的 staffType 是「这次想排成什么岗」，
     * 而事实层按「人 × 科室 × 角色」现算出的是这个人在人事上到底是什么岗；两者不一致时必须以人事为准，
     * 否则同一个人能在门诊表里是医生、在岗位排班表里是收费员，号源池的开关就成了一句前端传参。
     */
    @Override
    public void bindCoreSchedule(BizSchedule schedule, StaffScheduleSourceEnum source) {
        Integer submittedStaffType = schedule.getStaffType();
        StaffScheduleUpsertDTO dto = new StaffScheduleUpsertDTO();
        dto.setId(schedule.getStaffScheduleId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setOrgType(OrgUnitTypeEnum.DEPT.getCode());
        dto.setOrgId(schedule.getDeptId());
        dto.setEmployeeId(schedule.getDoctorId());
        dto.setShiftId(schedule.getShiftId());
        // 停诊 ≠ 停班：号源池的状态与人今天上不上班是两件事，出诊计划只按「上班」派生事实
        dto.setDutyStatus(StaffDutyStatusEnum.WORK.getCode());
        dto.setAttendMode(AttendModeEnum.ON_SITE.getCode());
        dto.setClinicFlag(YesOrNoEnum.YES.getCode());
        dto.setRemark(schedule.getRemark());
        BizStaffSchedule core = staffScheduleService.ensureForClinic(dto, source);
        if (submittedStaffType != null && !Objects.equals(submittedStaffType, core.getStaffType())) {
            throw new BusinessException("「" + core.getEmployeeName() + "」在"
                    + OrgUnitTypeEnum.labelOf(core.getOrgType()) + "「" + core.getDeptName()
                    + "」的岗位类别是" + StaffTypeEnum.labelOf(core.getStaffType())
                    + "，不是" + StaffTypeEnum.labelOf(submittedStaffType) + "：请先修正这个人的岗位配置，排班表不接受手填类别");
        }
        schedule.setStaffScheduleId(core.getId());
        schedule.setWeekDay(core.getWeekDay());
        schedule.setStaffType(core.getStaffType());
        schedule.setDoctorName(core.getEmployeeName());
        schedule.setDeptName(core.getDeptName());
        schedule.setStartTime(core.getStartTime());
        schedule.setEndTime(core.getEndTime());
    }

    /**
     * 投影行不再引用这条出勤事实时，把事实一起收掉。
     *
     * <p>删门诊排班删的是「这个班放不放号」，但「这个人今天上不上班」是事实层的事，
     * 只有当没有任何出诊计划再挂在它上面时它才成为孤儿。一条事实可以挂多条出诊计划
     * （同一人同班次在两个科室各开号源池），所以不能「删一条投影就删一个事实」。
     */
    private void releaseCoreSchedule(Long staffScheduleId) {
        if (staffScheduleId == null) {
            return;
        }
        Long holding = scheduleMapper.selectCount(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getStaffScheduleId, staffScheduleId));
        if (holding == null || holding == 0) {
            staffScheduleService.deleteById(staffScheduleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(BizSchedule schedule) {
        BizSchedule existing = scheduleMapper.selectById(schedule.getId());
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        // 已过去的排班一律只读：历史班次的真实就诊结果以门诊日志/挂号记录为准，改排班表等于改历史。
        // 既禁"改一条过去的"，也禁"把未来的改到过去"。
        assertNotPast(existing.getScheduleDate(), "修改");
        assertNotPast(schedule.getScheduleDate(), "修改");
        // 已挂过号的排班不允许换岗位：医生岗改出勤岗会把「能挂号的号源池」直接抹掉，
        // 出勤岗改医生岗则让一条没有号源/诊室配置的排班突然出现在挂号下拉里。
        if (!Objects.equals(existing.getStaffType(), schedule.getStaffType())
                && existing.getUsedSource() != null && existing.getUsedSource() > 0) {
            throw new BusinessException("该排班已有 " + existing.getUsedSource() + " 条挂号记录，不能变更岗位类别");
        }
        applyShift(schedule);
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, schedule.getId());

        int delta = schedule.getTotalSource() - existing.getTotalSource();
        schedule.setAvailableSource(existing.getAvailableSource() + delta);
        if (schedule.getAvailableSource() < 0) {
            throw new BusinessException("号源数量不能小于已使用数量");
        }
        // 诊室变了要单独收口：诊室是**排班级**属性，改一处必须让今天该班次的人一起搬，
        // 否则「排班说 305、队列/门诊日志还说 302」——患者照旧走错房间。
        boolean roomChanged = !Objects.equals(existing.getRoomId(), schedule.getRoomId())
                || !Objects.equals(existing.getRoomName(), schedule.getRoomName());
        boolean ok = scheduleMapper.updateById(schedule) > 0;
        if (ok) {
            if (StaffTypeEnum.hasSource(schedule.getStaffType())) {
                // 段重算：时间窗变化且无挂号 → 物理删段重建；有挂号 → 拒绝改窗；
                // 仅号源变化 → 重摊（已用的段保底），Σ段写回主表
                slotService.regenerateForSchedule(schedule, schedule.getTotalSource(), schedule.getAppointmentSource());
            } else {
                // 改成出勤岗：号源没了，段也就没有意义——留着就是一堆挂不上的空段
                slotService.physicalDeleteByScheduleId(schedule.getId());
            }
            if (roomChanged) {
                syncRoomToTodayWorklist(schedule);
            }
            // 换人/换班会把这条投影挪到另一条事实上，旧事实没人引用就该收掉（放在 updateById 之后：
            // 此刻本行已经指向新事实，引用计数才不会把自己算进去）
            releaseCoreSchedule(existing.getStaffScheduleId());
        }
        return ok;
    }

    /**
     * 排班的诊室变了 → 把<b>今天该班次还没结束</b>的队列行与挂号快照一起搬到新诊室。
     *
     * <p>换诊室是「这个医生这个班次搬到另一个房间」，不是「某个患者换房间」，
     * 所以必须整批搬：已在候诊/就诊中的患者跟着走，已就诊/过号/退号的保持历史快照。
     *
     * <p><b>队列号前缀不会跟着改</b>：{@code queueNo} 是发号那一刻按诊室编号生成的，
     * 号已经叫出去/印给患者了。换诊室影响的是「去哪个房间」，以及<b>之后</b>新签到的号前缀。
     */
    private void syncRoomToTodayWorklist(BizSchedule schedule) {
        LocalDate today = LocalDate.now();
        if (schedule.getScheduleDate() != null && !schedule.getScheduleDate().equals(today)) {
            // 只动今天的班次：历史/未来日期的队列不该被追改
            return;
        }
        Long roomId = schedule.getRoomId();
        String roomName = schedule.getRoomName();
        if (roomId != null && !StringUtils.hasText(roomName)) {
            var room = clinicRoomService.getById(roomId);
            if (room != null) {
                roomName = room.getName();
                schedule.setRoomName(roomName);
                scheduleMapper.updateById(schedule);
            }
        }
        queueMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
        appointInfoMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long id) {
        BizSchedule existing = scheduleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (existing.getUsedSource() > 0) {
            throw new BusinessException("该排班已有挂号记录，无法删除");
        }
        assertNotPast(existing.getScheduleDate(), "删除");
        // 物理删（段与主表）：uk_schedule_window 与段的唯一键都不含 del_flag（铁律），BaseEntity 的 @TableLogic
        // 逻辑删会留下继续占用唯一键的行，「删了重排同一窗口」必然撞重复键
        slotService.physicalDeleteByScheduleId(id);
        boolean ok = scheduleMapper.physicalDeleteById(id) > 0;
        if (ok) {
            releaseCoreSchedule(existing.getStaffScheduleId());
        }
        return ok;
    }

    /**
     * 校验：启用态的排班必须排诊室。
     *
     * <p>为什么这条必须放在服务端：诊室不是「排班记录上的一个备注字段」，它是**下游三条链路的入参**——
     * <ol>
     *   <li>分诊台按「诊室编号」生成排队号前缀，没诊室 = 发不出号；</li>
     *   <li>挂号小票/大屏导诊靠诊室把人送到房间，没诊室 = 患者拿着号在楼里转；</li>
     *   <li>换诊室时要整批搬队列与挂号快照（见 {@link #syncRoomToTodayWorklist}），
     *       初始为空的诊室没有"旧值"，这条同步逻辑从一开始就是空转。</li>
     * </ol>
     *
     * <p>只有<b>启用态</b>才要求：停诊（status=0）的班次本来就不放号，允许先建后补诊室。
     *
     * <p><b>只有医生岗（staff_type=1）才要求</b>（sql/195）：护士、技师、收费员这些出勤岗
     * 没有"诊室"概念——他们排的是「人在不在」，不是「在哪个房间接诊」。
     * 出勤岗的诊室由 {@link #applyStaffType} 直接清空，所以这里不会误伤。
     *
     * <p>注意 {@code ScheduleUpsertDTO} 上 roomId/roomName 已经不挂 {@code @NotNull}（出勤岗可以不填），
     * 业务语义（启用态 + 医生岗才必填）无法用声明式注解表达，所以在服务端收口。
     */
    private void checkRoomRequired(BizSchedule schedule) {
        boolean enabled = !ScheduleStatusEnum.stopped(schedule.getStatus());
        // B类（条件必填）：必填性取决于同一请求里的状态与岗位类别，声明式注解做不到一刀切
        if (enabled && StaffTypeEnum.hasSource(schedule.getStaffType())
                && schedule.getRoomId() == null && !StringUtils.hasText(schedule.getRoomName())) {
            throw new BusinessException("启用状态的医生排班必须指定诊室：分诊台按诊室编号发号，缺诊室患者找不到房间");
        }
    }

    /**
     * 岗位类别收口（sql/195）：写库前按岗位裁剪字段，前端传什么都不算。
     *
     * <p>为什么必须由服务端覆盖而不是"前端传什么存什么"：
     * 岗位类别决定了这条排班走哪条规则链（号源/诊室/时间片段/加号/停诊退号），
     * 一旦让「非医生岗 + 号源 20」这种组合落库，挂号下拉就会拉出一条没有医生在场的号源
     * （虽然 selectList 已按 staff_type=1 过滤，但号源面板、号源统计、报盘都会读到它）。
     *
     * <p><b>医生岗</b>：号源必须 ≥1（没有号源的出诊班挂不出号，不如不排），预约池不得大于号源总数。
     * <br><b>其余岗位</b>：纯出勤——号源、预约池、专家标志、挂号/诊查/专家费、诊室一律清零，
     * 「今天谁在岗」之外的一切属性都不属于这条排班。
     */
    private void applyStaffType(BizSchedule schedule) {
        StaffTypeEnum.assertValid(schedule.getStaffType());
        if (StaffTypeEnum.hasSource(schedule.getStaffType())) {
            int total = schedule.getTotalSource() == null ? 0 : schedule.getTotalSource();
            if (total < 1) {
                throw new BusinessException("医生排班必须配置号源数量（至少 1）：没有号源的出诊班挂不出号");
            }
            int appt = schedule.getAppointmentSource() == null ? 0 : schedule.getAppointmentSource();
            if (appt > total) {
                throw new BusinessException("预约号源数不能大于号源总数");
            }
            schedule.setTotalSource(total);
            return;
        }
        schedule.setTotalSource(0);
        schedule.setAppointmentSource(0);
        schedule.setUsedAppointmentSource(0);
        schedule.setIsAppointment(0);
        schedule.setIsExpert(0);
        schedule.setExpertFee(BigDecimal.ZERO);
        schedule.setRegistFee(BigDecimal.ZERO);
        schedule.setDiagnosisFee(BigDecimal.ZERO);
        schedule.setRoomId(null);
        schedule.setRoomName(null);
    }

    /**
     * 班次是时间段与班别的<b>唯一</b>来源：前端传什么都不认，一律按班次字典覆盖
     * （原来「标准班次」和「班次类型」两个并列字段可以互相矛盾，就是这么来的）。
     */
    private void applyShift(BizSchedule schedule) {
        BizShift shift = shiftService.resolveForScheduling(schedule.getShiftId(), schedule.getDeptId());
        schedule.setStartTime(shift.getStartTime());
        schedule.setEndTime(shift.getEndTime());
        schedule.setShiftId(shift.getId());
    }

    /**
     * 展示侧补班次信息：排班信息上不再存班别，班别与班次名都是班次的属性，
     * 按 shift_id 一次批量查字典回填（挂号台/急诊/排班面板的分组与配色继续按班别走，零改动）。
     *
     * <p>班次被删过（逻辑删）时字典里查不到 → 班别留空，前端渲染成「未分类」，
     * <b>不</b>按起止时间猜一个班别填上去（猜错比留空更难发现）。
     */
    @Override
    public void fillShiftDisplay(List<ScheduleDetailVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Map<Long, BizShift> shiftMap = shiftService.mapByIds(voList.stream()
                .map(ScheduleDetailVO::getShiftId).filter(Objects::nonNull).toList());
        for (ScheduleDetailVO vo : voList) {
            BizShift shift = vo.getShiftId() == null ? null : shiftMap.get(vo.getShiftId());
            if (shift != null) {
                vo.setShiftName(shift.getShiftName());
                vo.setScheduleType(shift.getScheduleType());
            }
        }
    }

    @Override
    public List<ScheduleDetailVO> listDetail(ScheduleQueryDTO queryDTO) {
        return toVOList(listPage(queryDTO));
    }

    @Override
    public List<ScheduleSelectListVO> selectListVO(ScheduleSelectQueryDTO scheduleQueryDTO) {
        return toVOList(scheduleSelectList(scheduleQueryDTO)).stream().map(detail -> {
            ScheduleSelectListVO vo = new ScheduleSelectListVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<ScheduleDetailVO> todayScheduleOfCurrentDept() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getDeptId() == null) {
            return List.of();
        }
        return toVOList(getTodaySchedule(user.getDeptId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleUpsert(ScheduleUpsertDTO upsertDTO) {
        BizSchedule schedule = convertToScheduleEntity(upsertDTO);
        try {
            boolean success = upsertDTO.getId() == null
                    ? addSchedule(schedule)
                    : updateSchedule(schedule);
            if (!success) {
                throw new BusinessException(upsertDTO.getId() == null ? "新增失败" : "修改失败");
            }
        } catch (DuplicateKeyException e) {
            // 并发兜底：checkScheduleOverlap 是先查后插，两个并发请求互相看不见对方，双双落库时
            // 由 uk_schedule_window(dept_id,doctor_id,schedule_date,start_time,end_time)
            // 或事实层 uk_emp_date_shift 兜住。把底层 500「系统内部错误」转成与业务校验同文案的 400。
            throw new BusinessException("该医生在此时间段已有排班，时间冲突");
        }
    }

    private List<ScheduleDetailVO> toVOList(List<BizSchedule> list) {
        List<ScheduleDetailVO> voList = list.stream().map(this::convertToScheduleVO).toList();
        fillShiftDisplay(voList);
        return voList;
    }

    private BizSchedule convertToScheduleEntity(ScheduleUpsertDTO upsertDTO) {
        BizSchedule entity = new BizSchedule();
        entity.setId(upsertDTO.getId());
        entity.setDeptId(upsertDTO.getDeptId());
        entity.setDeptName(upsertDTO.getDeptName());
        entity.setDoctorId(upsertDTO.getDoctorId());
        entity.setDoctorName(upsertDTO.getDoctorName());
        entity.setStaffType(upsertDTO.getStaffType());
        entity.setScheduleDate(upsertDTO.getScheduleDate());
        entity.setStartTime(upsertDTO.getStartTime());
        entity.setEndTime(upsertDTO.getEndTime());
        entity.setShiftId(upsertDTO.getShiftId());
        entity.setTotalSource(upsertDTO.getTotalSource());
        entity.setRegistFee(upsertDTO.getRegistFee());
        entity.setDiagnosisFee(upsertDTO.getDiagnosisFee());
        entity.setRoomId(upsertDTO.getRoomId());
        entity.setRoomName(upsertDTO.getRoomName());
        entity.setIsExpert(upsertDTO.getIsExpert());
        entity.setExpertFee(upsertDTO.getExpertFee());
        entity.setIsAppointment(upsertDTO.getIsAppointment());
        entity.setAppointmentSource(upsertDTO.getAppointmentSource());
        entity.setStatus(upsertDTO.getStatus());
        return entity;
    }

    private ScheduleDetailVO convertToScheduleVO(BizSchedule entity) {
        if (entity == null) {
            return null;
        }
        ScheduleDetailVO vo = new ScheduleDetailVO();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(entity.getDoctorName());
        vo.setStaffType(entity.getStaffType());
        // 岗位类别名不落排班表（类别码已在表上），按码从枚举带出——枚举是本地常量，零查库
        vo.setStaffTypeName(StaffTypeEnum.labelOf(entity.getStaffType()));
        vo.setScheduleDate(entity.getScheduleDate());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setShiftId(entity.getShiftId());
        vo.setTotalSource(entity.getTotalSource());
        vo.setUsedSource(entity.getUsedSource());
        vo.setAvailableSource(entity.getAvailableSource());
        vo.setRegistFee(entity.getRegistFee());
        vo.setDiagnosisFee(entity.getDiagnosisFee());
        vo.setIsExpert(entity.getIsExpert());
        vo.setExpertFee(entity.getExpertFee());
        vo.setIsAppointment(entity.getIsAppointment());
        vo.setAppointmentSource(entity.getAppointmentSource());
        vo.setUsedAppointmentSource(entity.getUsedAppointmentSource());
        vo.setStatus(entity.getStatus());
        vo.setConsultStatus(entity.getConsultStatus());
        vo.setRoomId(entity.getRoomId() != null ? String.valueOf(entity.getRoomId()) : null);
        vo.setRoomName(entity.getRoomName());
        return vo;
    }

    /**
     * 排班日期不得早于今天：已过日期的班次一律只读。
     *
     * <p>过去的日期上患者已就诊完，改号源/换诊室/停诊都不会再影响任何真实就诊，
     * 只会让「排班表」和「门诊日志」两个口径分家（历史以日志为准）；新增过去的日期
     * 更等于往号源池里开一个「能挂历史号」的口子。所以新增、修改、停诊、删除走同一条线。
     *
     * <p>粒度只到<b>日期</b>，不到时刻：今天 08:00~12:00 这种"时段已过"的班次当天仍可停诊
     * （医生临时走人必须当天停），所以放行今天。
     */
    private void assertNotPast(LocalDate date, String action) {
        if (date == null || date.compareTo(LocalDate.now()) >= 0) {
            return;
        }
        throw new BusinessException("排班日期 " + date + " 已过，不允许" + action + "（历史排班以门诊日志为准）");
    }

    /**
     * 校验同一医生同一天的时间段是否冲突
     * 时间重叠条件：startTime1 < endTime2 且 startTime2 < endTime1
     */
    private void checkScheduleOverlap(BizSchedule schedule, Long excludeId) {
        if (schedule.getDoctorId() == null || schedule.getScheduleDate() == null
                || !StringUtils.hasText(schedule.getStartTime()) || !StringUtils.hasText(schedule.getEndTime())) {
            return;
        }
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDoctorId, schedule.getDoctorId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .ne(excludeId != null, BizSchedule::getId, excludeId);
        List<BizSchedule> existing = scheduleMapper.selectList(wrapper);
        for (BizSchedule s : existing) {
            if (s.getStartTime().compareTo(schedule.getEndTime()) < 0
                    && schedule.getStartTime().compareTo(s.getEndTime()) < 0) {
                throw new BusinessException("该医生在此时间段已有排班，时间冲突");
            }
        }
    }

    @Override
    public List<BizSchedule> getTodaySchedule(Long deptId) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        // 只返回医生出诊排班：这是「今天谁在接诊」的名单（医生站/叫号用），
        // 护士、收费员这些出勤岗不接诊，混进来会让医生站多出一堆叫不了号的人（sql/195）。
        wrapper.eq(BizSchedule::getDeptId, deptId)
                .eq(BizSchedule::getScheduleDate, LocalDate.now())
                .eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizSchedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    @Override
    public boolean updateConsultStatus(Long scheduleId, Integer consultStatus) {
        // 值域在服务端收口：码值只允许 0/1/2，越界值（如 99）不能落库
        if (ConsultStatusEnum.fromCode(consultStatus) == null) {
            throw new BusinessException("就诊状态只允许 " + ConsultStatusEnum.whitelistText());
        }
        BizSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        schedule.setConsultStatus(consultStatus);
        return scheduleMapper.updateById(schedule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long scheduleId, Integer status) {
        BizSchedule old = scheduleMapper.selectById(scheduleId);
        if (old == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(old.getScheduleDate(), ScheduleStatusEnum.stopped(status) ? "停诊" : "启用");
        // 只更新状态列：MyBatis-Plus updateById 跳过 null 字段，不能走 updateSchedule 的号源 delta 逻辑。
        // 停诊也**不动出勤事实**：号池停了不代表人不上班（见 ScheduleStatusEnum）
        BizSchedule schedule = new BizSchedule();
        schedule.setId(scheduleId);
        schedule.setStatus(status);
        boolean ok = scheduleMapper.updateById(schedule) > 0;
        if (ok) {
            // 段状态随主表联动：停诊 → 段全停（扣减 SQL 带 status=1，停用段不可再挂）；启用 → 段恢复
            slotService.syncStatusToSlots(scheduleId, status);
        }
        return ok;
    }

    @Override
    public List<StopImpactItemVO> stopImpact(Long scheduleId) {
        if (scheduleMapper.selectById(scheduleId) == null) {
            throw new BusinessException("排班记录不存在");
        }
        List<BizAppointInfo> regs = appointInfoMapper.selectList(
                new LambdaQueryWrapper<BizAppointInfo>()
                        .eq(BizAppointInfo::getScheduleId, scheduleId)
                        .notIn(BizAppointInfo::getRegistStatus, 5, 6) // 5-已退号 6-已爽约
                        .orderByAsc(BizAppointInfo::getRegistStatus)
                        .orderByAsc(BizAppointInfo::getSlotTime));
        return regs.stream().map(r -> {
            StopImpactItemVO vo = new StopImpactItemVO();
            vo.setRegistId(r.getId());
            vo.setPatientId(r.getPatientId());
            vo.setPatientName(r.getPatientName());
            vo.setPatientNo(r.getPatientNo());
            vo.setPhone(r.getPhone());
            vo.setRegistSource(r.getRegistSource());
            vo.setRegistStatus(r.getRegistStatus());
            vo.setSlotTime(r.getSlotTime());
            return vo;
        }).toList();
    }

    @Override
    public String batchCancel(List<Long> registIds, String reason) {
        String cancelReason = StringUtils.hasText(reason) ? reason : "班次停诊，批量退号";
        int ok = 0;
        for (Long id : registIds) {
            try {
                if (appointService.cancelRegist(id, cancelReason)) {
                    ok++;
                }
            } catch (Exception ignored) {
                // 单条失败（如已支付需先退费）不中断整批，结论里如实计数
            }
        }
        return "批量退号完成：成功 " + ok + " 条 / 共 " + registIds.size() + " 条"
                + (ok < registIds.size()
                ? "（失败多为：已缴费需先到收费处退费；或已接诊/已就诊——诊疗已发生，号源锁死）"
                : "");
    }

    // 今日在岗（排班的下游出口）

    @Override
    public List<OnDutyStaffVO> onDuty(OnDutyQueryDTO queryDTO) {
        OnDutyQueryDTO cond = queryDTO == null ? new OnDutyQueryDTO() : queryDTO;
        Long deptId = cond.getDeptId() != null ? cond.getDeptId() : currentDeptId();
        if (deptId == null) {
            return List.of();
        }
        LocalDate date = cond.getDate() != null ? cond.getDate() : LocalDate.now();
        LocalTime moment = cond.getMoment() == null
                ? LocalTime.now()
                : ShiftCoverUtil.parseShiftTime(cond.getMoment());
        boolean onlyOnDuty = !Boolean.FALSE.equals(cond.getOnDutyOnly());

        List<BizSchedule> rows = loadDaySchedule(deptId, date, cond.getStaffType());
        List<OnDutyStaffVO> all = rows.stream().map(s -> toOnDutyVO(s, moment)).toList();
        if (!onlyOnDuty) {
            return all;
        }
        return all.stream().filter(vo -> Boolean.TRUE.equals(vo.getOnDutyNow())).toList();
    }

    @Override
    public OnDutyStaffVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId) {
        if (deptId == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<BizSchedule> rows = loadDaySchedule(deptId, today, staffType);
        if (rows.isEmpty()) {
            return null;
        }
        List<BizSchedule> onDuty = rows.stream()
                .filter(s -> ShiftCoverUtil.covers(now, s.getStartTime(), s.getEndTime()))
                .toList();
        if (!onDuty.isEmpty()) {
            // 同科室多人同时在职：优先本人（当班护士自己操作，理应记她），不是随便抓一个
            for (BizSchedule s : onDuty) {
                if (preferEmpId != null && Objects.equals(s.getDoctorId(), preferEmpId)) {
                    return toOnDutyVO(s, now);
                }
            }
            // 交接班时段两班重叠 → 取最近开班的那一班（接班人），
            // 否则凌晨分诊会被记到已经下班的上一班头上
            BizSchedule latest = onDuty.stream()
                    .max(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime()),
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(null);
            return latest == null ? null : toOnDutyVO(latest, now);
        }
        // 交接班空档：此刻谁都不在班里，退而取当日最早一班（当天在岗过的人）。
        // 仍然不是「登录人兜底」——身份凭证 ≠ 当班责任
        BizSchedule earliest = rows.stream()
                .min(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime()),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return earliest == null ? null : toOnDutyVO(earliest, now);
    }

    /**
     * 当日该科室全部有效排班（默认全岗位）。
     * 与 getTodaySchedule 不是一回事：那个是「今日医生出诊名单」（硬编码 staff_type=1），
     * 这里是「今日全部/某岗位的出勤表」，两者不能混用。
     */
    private List<BizSchedule> loadDaySchedule(Long deptId, LocalDate date, Integer staffType) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDeptId, deptId)
                .eq(BizSchedule::getScheduleDate, date)
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .isNotNull(BizSchedule::getDoctorId);
        if (staffType != null) {
            wrapper.eq(BizSchedule::getStaffType, staffType);
        }
        wrapper.orderByAsc(BizSchedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    private OnDutyStaffVO toOnDutyVO(BizSchedule s, LocalTime moment) {
        OnDutyStaffVO vo = new OnDutyStaffVO();
        vo.setScheduleId(s.getId());
        vo.setStaffId(s.getDoctorId());
        vo.setStaffName(s.getDoctorName());
        vo.setStaffType(s.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.labelOf(s.getStaffType()));
        vo.setDeptId(s.getDeptId());
        vo.setDeptName(s.getDeptName());
        vo.setShiftId(s.getShiftId());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        vo.setRoomId(s.getRoomId());
        vo.setRoomName(s.getRoomName());
        vo.setStatus(s.getStatus());
        vo.setOnDutyNow(ShiftCoverUtil.covers(moment, s.getStartTime(), s.getEndTime()));
        if (s.getShiftId() != null && shiftService != null) {
            BizShift shift = shiftService.getById(s.getShiftId());
            vo.setShiftName(shift == null ? null : shift.getShiftName());
        }
        return vo;
    }

    /**
     * 当前登录人所属科室；取不到返回 null（由调用方决定空结果还是报错）
     */
    private Long currentDeptId() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        return Long.valueOf(user.getDeptId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSource(Long scheduleId, Integer addNum, String reason) {
        BizSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过期的班次不允许加号");
        }
        if (ScheduleStatusEnum.stopped(schedule.getStatus())) {
            throw new BusinessException("停诊中的班次不允许加号，请先启用");
        }
        // 加号是给医生出诊班加号源：出勤岗压根没有号源池，加了也挂不出去（sql/195）
        if (!StaffTypeEnum.hasSource(schedule.getStaffType())) {
            throw new BusinessException("只有医生出诊排班能加号："
                    + StaffTypeEnum.labelOf(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
        }
        int added = schedule.getAddedSource() == null ? 0 : schedule.getAddedSource();
        String stamp = LocalDate.now() + " 加号" + addNum + "（" + reason + "）";
        String newRemark = StringUtils.hasText(schedule.getRemark()) ? schedule.getRemark() + "；" + stamp : stamp;
        BizSchedule update = new BizSchedule();
        update.setId(scheduleId);
        update.setTotalSource((schedule.getTotalSource() == null ? 0 : schedule.getTotalSource()) + addNum);
        update.setAvailableSource((schedule.getAvailableSource() == null ? 0 : schedule.getAvailableSource()) + addNum);
        update.setAddedSource(added + addNum);
        update.setRemark(newRemark);
        boolean ok = scheduleMapper.updateById(update) > 0;
        if (ok) {
            // 加号摊到段（余数给前面的段），Σ段写回主表——号源事实在段上，主表只是汇总
            slotService.spreadAddSource(scheduleId, addNum);
        }
        return ok;
    }
}
