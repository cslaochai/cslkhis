package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.ScheduleTemplateQueryPageDTO;
import com.his.appoint.dto.ScheduleTemplateUpsertDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.entity.BizScheduleSlotTemplate;
import com.his.appoint.entity.BizScheduleTemplate;
import com.his.system.entity.BizShift;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.mapper.BizScheduleSlotTemplateMapper;
import com.his.appoint.mapper.BizScheduleTemplateMapper;
import com.his.appoint.service.ScheduleService;
import com.his.appoint.service.ScheduleSlotService;
import com.his.appoint.service.ScheduleTemplateService;
import com.his.system.service.ShiftService;
import com.his.appoint.vo.ScheduleTemplatePreviewVO;
import com.his.appoint.vo.ScheduleTemplateVO;
import com.his.common.base.PageResult;
import com.his.common.enums.ScheduleStatusEnum;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 排班模板服务实现
 * 铁律：模板是长期资产，排班是按周生成的产物——临时调整排班不影响模板。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleTemplateServiceImpl extends ServiceImpl<BizScheduleTemplateMapper, BizScheduleTemplate>
        implements ScheduleTemplateService {

    private final BizScheduleTemplateMapper templateMapper;
    private final BizScheduleMapper scheduleMapper;
    private final BizScheduleSlotTemplateMapper slotTemplateMapper;
    private final ScheduleSlotService slotService;
    /**
     * 出诊计划的写口径（出勤事实派生、岗位类别以人事为准）与手工排班共用同一条，模板不另算一遍
     */
    private final ScheduleService scheduleService;
    /**
     * 班次字典：模板的时间段与班别全部由 shift_id 带出，模板表不再自己存一份
     */
    private final ShiftService shiftService;

    @Override
    public List<BizScheduleTemplate> listTemplates(Long deptId, Integer staffType, Integer weekDay, Integer status) {
        LambdaQueryWrapper<BizScheduleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(deptId != null, BizScheduleTemplate::getDeptId, deptId)
                .eq(staffType != null, BizScheduleTemplate::getStaffType, staffType)
                .eq(weekDay != null, BizScheduleTemplate::getWeekDay, weekDay)
                .eq(status != null, BizScheduleTemplate::getStatus, status)
                .orderByAsc(BizScheduleTemplate::getDeptId)
                .orderByAsc(BizScheduleTemplate::getDoctorId)
                .orderByAsc(BizScheduleTemplate::getWeekDay)
                .orderByAsc(BizScheduleTemplate::getStartTime);
        return templateMapper.selectList(wrapper);
    }

    @Override
    public void fillShiftDisplay(List<ScheduleTemplateVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Map<Long, BizShift> shiftMap = shiftService.mapByIds(voList.stream()
                .map(ScheduleTemplateVO::getShiftId).filter(Objects::nonNull).toList());
        for (ScheduleTemplateVO vo : voList) {
            BizShift shift = vo.getShiftId() == null ? null : shiftMap.get(vo.getShiftId());
            if (shift != null) {
                vo.setShiftName(shift.getShiftName());
                vo.setScheduleType(shift.getScheduleType());
            }
        }
    }

    @Override
    public List<ScheduleTemplateVO> listVO(Long deptId, Integer staffType, Integer weekDay, Integer status) {
        List<ScheduleTemplateVO> voList = listTemplates(deptId, staffType, weekDay, status).stream()
                .map(this::convertToVO).toList();
        fillShiftDisplay(voList);
        return voList;
    }

    @Override
    public PageResult<ScheduleTemplateVO> pageVO(ScheduleTemplateQueryPageDTO dto) {
        LambdaQueryWrapper<BizScheduleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getDeptId() != null, BizScheduleTemplate::getDeptId, dto.getDeptId())
                .eq(dto.getStaffType() != null, BizScheduleTemplate::getStaffType, dto.getStaffType())
                .eq(dto.getWeekDay() != null, BizScheduleTemplate::getWeekDay, dto.getWeekDay())
                .eq(dto.getStatus() != null, BizScheduleTemplate::getStatus, dto.getStatus())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(BizScheduleTemplate::getDeptName, dto.getKeyword())
                        .or().like(BizScheduleTemplate::getDoctorName, dto.getKeyword())
                        .or().like(BizScheduleTemplate::getRoomName, dto.getKeyword())
                        .or().like(BizScheduleTemplate::getRemark, dto.getKeyword()))
                // 二级键 id：同星期同班次的行顺序不稳定（分页铁律，防翻页重复+丢行）
                .orderByAsc(BizScheduleTemplate::getWeekDay)
                .orderByAsc(BizScheduleTemplate::getStartTime)
                .orderByAsc(BizScheduleTemplate::getId);
        Page<BizScheduleTemplate> page = this.page(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<ScheduleTemplateVO> voList = page.getRecords().stream().map(this::convertToVO).toList();
        fillShiftDisplay(voList);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public void upsertTemplate(ScheduleTemplateUpsertDTO dto) {
        boolean success = saveTemplate(convertToEntity(dto));
        if (!success) {
            throw new BusinessException(dto.getId() == null ? "新增失败" : "修改失败");
        }
    }

    private BizScheduleTemplate convertToEntity(ScheduleTemplateUpsertDTO dto) {
        BizScheduleTemplate t = new BizScheduleTemplate();
        t.setId(dto.getId());
        t.setDeptId(dto.getDeptId());
        t.setDeptName(dto.getDeptName());
        t.setDoctorId(dto.getDoctorId());
        t.setDoctorName(dto.getDoctorName());
        t.setStaffType(dto.getStaffType());
        t.setWeekDay(dto.getWeekDay());
        t.setWeekParity(dto.getWeekParity());
        t.setValidFrom(parseDate(dto.getValidFrom()));
        t.setValidUntil(parseDate(dto.getValidUntil()));
        t.setStartTime(dto.getStartTime());
        t.setEndTime(dto.getEndTime());
        t.setShiftId(dto.getShiftId());
        t.setTotalSource(dto.getTotalSource());
        t.setRoomId(dto.getRoomId());
        t.setRoomName(dto.getRoomName());
        t.setRegistFee(dto.getRegistFee());
        t.setDiagnosisFee(dto.getDiagnosisFee());
        t.setIsExpert(dto.getIsExpert());
        t.setExpertFee(dto.getExpertFee());
        t.setIsAppointment(dto.getIsAppointment());
        t.setAppointmentSource(dto.getAppointmentSource());
        t.setStatus(dto.getStatus());
        t.setRemark(dto.getRemark());
        return t;
    }

    private ScheduleTemplateVO convertToVO(BizScheduleTemplate t) {
        if (t == null) {
            return null;
        }
        ScheduleTemplateVO vo = new ScheduleTemplateVO();
        vo.setId(t.getId());
        vo.setDeptId(t.getDeptId());
        vo.setDeptName(t.getDeptName());
        vo.setDoctorId(t.getDoctorId());
        vo.setDoctorName(t.getDoctorName());
        vo.setStaffType(t.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.labelOf(t.getStaffType()));
        vo.setWeekDay(t.getWeekDay());
        vo.setWeekParity(t.getWeekParity());
        vo.setValidFrom(t.getValidFrom() != null ? t.getValidFrom().toString() : null);
        vo.setValidUntil(t.getValidUntil() != null ? t.getValidUntil().toString() : null);
        vo.setStartTime(t.getStartTime());
        vo.setEndTime(t.getEndTime());
        vo.setShiftId(t.getShiftId());
        vo.setTotalSource(t.getTotalSource());
        vo.setRoomId(t.getRoomId() != null ? String.valueOf(t.getRoomId()) : null);
        vo.setRoomName(t.getRoomName());
        vo.setRegistFee(t.getRegistFee());
        vo.setDiagnosisFee(t.getDiagnosisFee());
        vo.setIsExpert(t.getIsExpert());
        vo.setExpertFee(t.getExpertFee());
        vo.setIsAppointment(t.getIsAppointment());
        vo.setAppointmentSource(t.getAppointmentSource());
        vo.setStatus(t.getStatus());
        vo.setRemark(t.getRemark());
        vo.setCreateTime(t.getCreateTime());
        return vo;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date);
    }

    @Override
    public boolean saveTemplate(BizScheduleTemplate template) {
        // 岗位类别（sql/195）：决定这条模板走哪条规则链——医生岗配号源/诊室/挂号费，其余岗位是纯出勤
        applyStaffType(template);
        // 班次是模板唯一的时间段/班别来源：不存在、停用、跨科室、没配班别都在这里拦下
        BizShift shift = shiftService.resolveForScheduling(template.getShiftId(), template.getDeptId());
        template.setShiftId(shift.getId());
        template.setStartTime(shift.getStartTime());
        template.setEndTime(shift.getEndTime());
        // 划池校验：预约号源数不得超过总号源（appointment_source=0 视为未划池）
        int apptSource = template.getAppointmentSource() == null ? 0 : template.getAppointmentSource();
        if (apptSource > template.getTotalSource()) {
            throw new BusinessException("预约号源数不能大于号源总数");
        }
        if (apptSource > 0) {
            template.setIsAppointment(1);
        }
        // 时间窗必须有序：生成侧切半小时段依赖 end > start，乱填会在整周生成时才炸
        if (template.getEndTime().compareTo(template.getStartTime()) <= 0) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        // 同医生同星期几同班次拒重（逻辑删不做 DB 唯一键，应用层保证）
        checkDuplicate(template);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        if (template.getId() == null) {
            return templateMapper.insert(template) > 0;
        }
        if (templateMapper.selectById(template.getId()) == null) {
            throw new BusinessException("模板不存在");
        }
        return templateMapper.updateById(template) > 0;
    }

    /**
     * 岗位类别收口（sql/195）：与排班表同一口径，模板生成排班时原样带给排班信息。
     *
     * <p><b>医生岗</b>：号源至少 1（模板上没有号源的医生班，生成出来的排班挂不出号）。
     * <br><b>其余岗位</b>：纯出勤模板——号源、预约池、专家标志、费用、诊室一律清零，
     * 它描述的是「每周一上午谁在岗」，不是「放几个号」。
     */
    private void applyStaffType(BizScheduleTemplate template) {
        StaffTypeEnum.assertValid(template.getStaffType());
        if (StaffTypeEnum.hasSource(template.getStaffType())) {
            if (template.getTotalSource() == null || template.getTotalSource() < 1) {
                throw new BusinessException("医生模板的号源数量至少为1");
            }
            return;
        }
        template.setTotalSource(0);
        template.setAppointmentSource(0);
        template.setIsAppointment(0);
        template.setIsExpert(0);
        template.setExpertFee(BigDecimal.ZERO);
        template.setRegistFee(BigDecimal.ZERO);
        template.setDiagnosisFee(BigDecimal.ZERO);
        template.setRoomId(null);
        template.setRoomName(null);
    }

    @Override
    public boolean deleteTemplate(Long id) {
        if (templateMapper.selectById(id) == null) {
            throw new BusinessException("模板不存在");
        }
        return templateMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        BizScheduleTemplate tpl = new BizScheduleTemplate();
        tpl.setId(id);
        tpl.setStatus(status);
        return templateMapper.updateById(tpl) > 0;
    }

    @Override
    public String generateForWeek(Integer weekOffset, Long deptId, Integer staffType) {
        int offset = weekOffset == null ? 1 : weekOffset;
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(offset);
        LocalDate sunday = monday.plusDays(6);
        LocalDate today = LocalDate.now();
        // 整周已过 → 直接拒绝（不是"生成 0 条"）。往历史周造班次会让号源池凭空多出能挂历史号的口子
        if (sunday.isBefore(today)) {
            throw new BusinessException("目标周 " + monday + " ~ " + sunday + " 已全部过去，不能为已结束的周次生成排班");
        }

        List<BizScheduleTemplate> templates = loadEnabledTemplates(deptId, staffType);
        if (templates.isEmpty()) {
            return staffType == null
                    ? "没有启用的排班模板，请先在「排班模板」中配置"
                    : "没有启用的「" + StaffTypeEnum.labelOf(staffType) + "」岗位排班模板";
        }
        List<BizSchedule> existing = loadWeekSchedules(monday, sunday);
        Map<Long, BizShift> shiftMap = loadShifts(templates);

        int created = 0;
        int skippedExist = 0;
        int skippedOverlap = 0;
        int skippedParity = 0;
        int skippedExpired = 0;
        int skippedPast = 0;
        int skippedInvalid = 0;
        for (BizScheduleTemplate tpl : templates) {
            LocalDate date = monday.plusDays(tpl.getWeekDay() - 1);
            // 本周部分日期已过（周三生成本周）：逐条跳过，周四五六日照常生成
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
            BizShift shift = shiftOf(shiftMap, tpl);
            if (shift == null) {
                skippedInvalid++;
                log.warn("模板 {}（{} {}）未选班次或班次已被删除，无法带出时间段，跳过", tpl.getId(), tpl.getDoctorName(), date);
                continue;
            }
            if (existsSame(existing, tpl.getDoctorId(), date, tpl.getShiftId())) {
                skippedExist++;
                continue;
            }
            if (hasTimeOverlap(existing, tpl.getDoctorId(), date, shift.getStartTime(), shift.getEndTime())) {
                skippedOverlap++;
                continue;
            }
            BizSchedule s = buildSchedule(tpl, shift, date);
            try {
                // 出勤事实与手工排班走同一条派生口径；本方法不带事务，一条模板配错只跳过这一条
                scheduleService.bindCoreSchedule(s, StaffScheduleSourceEnum.TEMPLATE);
            } catch (Exception e) {
                skippedInvalid++;
                log.warn("模板 {}（{} {}）挂不上岗位排班事实，跳过：{}", tpl.getId(), tpl.getDoctorName(), date, e.getMessage());
                continue;
            }
            scheduleMapper.insert(s);
            try {
                // 段生成：模板配置了片段 → 按配置；否则按半小时自动切分均分（与手工排班同一规则）。
                // 只有医生岗切片（sql/195）：出勤岗号源恒 0，切出来是一堆永远挂不上的空段。
                // 生成批不走单条事务：失败在这里就地清理残缺排班（不能留一张没有段的排班）
                if (StaffTypeEnum.hasSource(s.getStaffType())) {
                    generateSlotsForSchedule(s, tpl.getId());
                }
            } catch (Exception e) {
                skippedInvalid++;
                slotService.physicalDeleteByScheduleId(s.getId());
                scheduleMapper.physicalDeleteById(s.getId());
                log.warn("模板生成排班失败（{} {} {}）：{}", tpl.getDoctorName(), date,
                        shift.getStartTime() + "-" + shift.getEndTime(), e.getMessage());
                continue;
            }
            // 加入内存列表：同批次后续模板与它互查（同班次/时间重叠）
            existing.add(s);
            created++;
        }

        int skipped = skippedExist + skippedOverlap + skippedParity + skippedExpired + skippedPast + skippedInvalid;
        StringBuilder msg = new StringBuilder("按模板生成 ")
                .append(monday).append(" ~ ").append(sunday)
                .append(" 排班完成：新增 ").append(created).append(" 条");
        if (skipped > 0) {
            msg.append("，跳过 ").append(skipped).append(" 条（")
                    .append(skippedExist).append(" 条已存在、").append(skippedOverlap).append(" 条时间冲突");
            if (skippedParity > 0) {
                msg.append("、").append(skippedParity).append(" 条单双周不匹配");
            }
            if (skippedExpired > 0) {
                msg.append("、").append(skippedExpired).append(" 条不在生效日期");
            }
            if (skippedPast > 0) {
                msg.append("、").append(skippedPast).append(" 条日期已过");
            }
            if (skippedInvalid > 0) {
                msg.append("、").append(skippedInvalid).append(" 条时间/号源配置非法");
            }
            msg.append("）");
        }
        return msg.toString();
    }

    /**
     * 给一条新排班生成时间片段：模板配置了片段（排班模板时段）→ 按配置；
     * 否则按半小时自动切分均分（余数给前面的段）。Σ段写回主表由 slotService 收口。
     */
    private void generateSlotsForSchedule(BizSchedule schedule, Long templateId) {
        List<BizScheduleSlotTemplate> tplSlots = slotTemplateMapper.selectList(
                new LambdaQueryWrapper<BizScheduleSlotTemplate>()
                        .eq(BizScheduleSlotTemplate::getTemplateId, templateId)
                        .orderByAsc(BizScheduleSlotTemplate::getSeq));
        if (tplSlots.isEmpty()) {
            slotService.generateSlots(schedule.getId(), schedule.getStartTime(), schedule.getEndTime(),
                    schedule.getTotalSource(), schedule.getAppointmentSource());
        } else {
            slotService.generateFromTemplate(schedule.getId(), tplSlots);
        }
    }

    @Override
    public ScheduleTemplatePreviewVO previewForWeek(Integer weekOffset, Long deptId, Integer staffType) {
        int offset = weekOffset == null ? 1 : weekOffset;
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(offset);
        LocalDate sunday = monday.plusDays(6);
        LocalDate today = LocalDate.now();

        ScheduleTemplatePreviewVO vo = new ScheduleTemplatePreviewVO();
        vo.setWeekStart(monday.toString());
        vo.setWeekEnd(sunday.toString());

        List<BizScheduleTemplate> templates = loadEnabledTemplates(deptId, staffType);
        if (templates.isEmpty()) {
            return vo;
        }
        List<BizSchedule> existing = loadWeekSchedules(monday, sunday);
        Map<Long, BizShift> shiftMap = loadShifts(templates);

        for (BizScheduleTemplate tpl : templates) {
            LocalDate date = monday.plusDays(tpl.getWeekDay() - 1);
            BizShift shift = shiftOf(shiftMap, tpl);
            String desc = weekDayName(tpl.getWeekDay()) + " " + (shift == null ? "未配班次" : shift.getShiftName()) + " "
                    + "[" + StaffTypeEnum.labelOf(tpl.getStaffType()) + "] " + tpl.getDoctorName() + " "
                    + (shift == null ? tpl.getStartTime() + "-" + tpl.getEndTime() : shift.getStartTime() + "-" + shift.getEndTime())
                    + " " + tpl.getTotalSource() + " 号"
                    + (tpl.getRoomName() != null ? " " + tpl.getRoomName() : "");
            if (date.isBefore(today)) {
                vo.getSkipPast().add(desc);
            } else if (!inParity(tpl, date)) {
                vo.getSkipParity().add(desc);
            } else if (!inValidRange(tpl, date)) {
                vo.getSkipExpired().add(desc);
            } else if (shift == null) {
                vo.getSkipInvalid().add(desc);
            } else if (existsSame(existing, tpl.getDoctorId(), date, tpl.getShiftId())) {
                vo.getSkipExist().add(desc);
            } else if (hasTimeOverlap(existing, tpl.getDoctorId(), date, shift.getStartTime(), shift.getEndTime())) {
                vo.getSkipOverlap().add(desc);
            } else {
                vo.getWillCreate().add(desc);
            }
            if (!vo.getDoctorNames().contains(tpl.getDoctorName())) {
                vo.getDoctorNames().add(tpl.getDoctorName());
            }
        }
        return vo;
    }

    private List<BizScheduleTemplate> loadEnabledTemplates(Long deptId, Integer staffType) {
        LambdaQueryWrapper<BizScheduleTemplate> tw = new LambdaQueryWrapper<>();
        tw.eq(BizScheduleTemplate::getStatus, 1);
        tw.eq(deptId != null, BizScheduleTemplate::getDeptId, deptId);
        tw.eq(staffType != null, BizScheduleTemplate::getStaffType, staffType);
        return templateMapper.selectList(tw);
    }

    private List<BizSchedule> loadWeekSchedules(LocalDate monday, LocalDate sunday) {
        return scheduleMapper.selectList(new LambdaQueryWrapper<BizSchedule>()
                .ge(BizSchedule::getScheduleDate, monday)
                .le(BizSchedule::getScheduleDate, sunday));
    }

    /**
     * 单双周匹配：0/null=每周；1=单周（ISO 周号奇数）2=双周（偶数）
     */
    private boolean inParity(BizScheduleTemplate tpl, LocalDate date) {
        Integer parity = tpl.getWeekParity();
        if (parity == null || parity == 0) {
            return true;
        }
        int weekNo = date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return parity == 1 ? weekNo % 2 == 1 : weekNo % 2 == 0;
    }

    /**
     * 生效日期范围：valid_from/valid_until 任一为空视为不限
     */
    private boolean inValidRange(BizScheduleTemplate tpl, LocalDate date) {
        if (tpl.getValidFrom() != null && date.isBefore(tpl.getValidFrom())) {
            return false;
        }
        return tpl.getValidUntil() == null || !date.isAfter(tpl.getValidUntil());
    }

    private String weekDayName(Integer weekDay) {
        String[] names = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weekDay != null && weekDay >= 1 && weekDay <= 7 ? names[weekDay - 1] : "周" + weekDay;
    }

    /**
     * 一批模板对应的班次，一次查库（生成/预览都在循环里要用，不能逐条查）
     */
    private Map<Long, BizShift> loadShifts(List<BizScheduleTemplate> templates) {
        return shiftService.mapByIds(templates.stream()
                .map(BizScheduleTemplate::getShiftId).filter(Objects::nonNull).toList());
    }

    /**
     * 模板的班次：没选、或班次已被删 → 返回 null，调用方按「配置非法」跳过而不是猜时间
     */
    private BizShift shiftOf(Map<Long, BizShift> shiftMap, BizScheduleTemplate tpl) {
        return tpl.getShiftId() == null ? null : shiftMap.get(tpl.getShiftId());
    }

    /**
     * 模板 → 排班：时间段与班次以字典为准（模板改过、字典也改过时，字典赢）；
     * 星期与岗位类别不在这里定，由 {@code ScheduleService#bindCoreSchedule} 从出勤事实带出。
     */
    private BizSchedule buildSchedule(BizScheduleTemplate tpl, BizShift shift, LocalDate date) {
        BizSchedule s = new BizSchedule();
        s.setDeptId(tpl.getDeptId());
        s.setDeptName(tpl.getDeptName());
        s.setDoctorId(tpl.getDoctorId());
        s.setDoctorName(tpl.getDoctorName());
        s.setStaffType(tpl.getStaffType());
        s.setScheduleDate(date);
        s.setStartTime(shift.getStartTime());
        s.setEndTime(shift.getEndTime());
        s.setShiftId(shift.getId());
        s.setTotalSource(tpl.getTotalSource());
        s.setUsedSource(0);
        s.setAvailableSource(tpl.getTotalSource());
        s.setRoomId(tpl.getRoomId());
        s.setRoomName(tpl.getRoomName());
        s.setRegistFee(tpl.getRegistFee());
        s.setDiagnosisFee(tpl.getDiagnosisFee());
        s.setIsExpert(tpl.getIsExpert());
        s.setExpertFee(tpl.getExpertFee());
        s.setIsAppointment(tpl.getIsAppointment());
        s.setAppointmentSource(tpl.getAppointmentSource());
        s.setStatus(ScheduleStatusEnum.NORMAL.getCode());
        return s;
    }

    /**
     * 同医生同日同班次算「已存在」：口径与模板/排班一致，都看 shift_id
     */
    private boolean existsSame(List<BizSchedule> list, Long doctorId, LocalDate date, Long shiftId) {
        return list.stream().anyMatch(s ->
                Objects.equals(s.getDoctorId(), doctorId)
                        && date.equals(s.getScheduleDate())
                        && Objects.equals(s.getShiftId(), shiftId));
    }

    /**
     * 时间重叠条件：startTime1 < endTime2 且 startTime2 < endTime1
     */
    private boolean hasTimeOverlap(List<BizSchedule> list, Long doctorId, LocalDate date, String start, String end) {
        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
            return false;
        }
        return list.stream().anyMatch(s ->
                Objects.equals(s.getDoctorId(), doctorId)
                        && date.equals(s.getScheduleDate())
                        && StringUtils.hasText(s.getStartTime()) && StringUtils.hasText(s.getEndTime())
                        && s.getStartTime().compareTo(end) < 0
                        && start.compareTo(s.getEndTime()) < 0);
    }

    private void checkDuplicate(BizScheduleTemplate template) {
        LambdaQueryWrapper<BizScheduleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizScheduleTemplate::getDoctorId, template.getDoctorId())
                .eq(BizScheduleTemplate::getWeekDay, template.getWeekDay())
                .eq(BizScheduleTemplate::getShiftId, template.getShiftId())
                .ne(template.getId() != null, BizScheduleTemplate::getId, template.getId());
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该" + StaffTypeEnum.labelOf(template.getStaffType())
                    + "在此星期已排过同一班次");
        }
    }
}
