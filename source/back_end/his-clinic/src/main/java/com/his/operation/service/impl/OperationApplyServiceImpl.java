package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.*;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.*;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.entity.BizOperationCount;
import com.his.operation.entity.BizOperationSafetyCheck;
import com.his.operation.entity.SysOperationRoom;
import com.his.common.enums.OperationAnesthesiaMethodEnum;
import com.his.common.enums.OperationApplyStatusEnum;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.mapper.BizOperationCountMapper;
import com.his.operation.mapper.BizOperationSafetyCheckMapper;
import com.his.operation.mapper.SysOperationRoomMapper;
import com.his.operation.service.OperationApplyService;
import com.his.operation.support.AnesthesiaCalcs;
import com.his.operation.support.OperationCheckItems;
import com.his.operation.support.SafetyCheckItems;
import com.his.operation.vo.OperationApplyVO;
import com.his.operation.vo.OperationScheduleMatrixVO;
import com.his.operation.vo.AiSuggestResultVO;
import com.his.operation.vo.AiSuggestionVO;
import com.his.operation.vo.RoomUtilizationStatVO;
import com.his.operation.vo.RoomUtilizationVO;
import com.his.common.enums.InpatientRecordTypeEnum;
import com.his.patient.dto.ClosedLoopRecordDTO;
import com.his.operation.dto.SurgeryOperationDTO;
import com.his.patient.service.PatientFacadeService;
import com.his.patient.vo.AdmissionVO;
import com.his.patient.vo.ClosedLoopRecordVO;
import com.his.patient.vo.PatientMasterVO;
import com.his.operation.vo.SurgeryOperationVO;
import com.his.patient.vo.WardVO;
import com.his.system.dto.TechAuthGateDTO;
import com.his.common.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.utils.UserUtils;
import com.his.system.service.DeptScopeService;
import com.his.system.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 住院手术闭环服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationApplyServiceImpl extends ServiceImpl<BizOperationApplyMapper, BizOperationApply> implements OperationApplyService {

    private final RedisSequenceService redisSequenceService;
    /**
     * 术前核对完成后多久没结束算"卡住"（查询时算，不落状态列）
     */
    private static final long STALLED_HOURS = 24;

    private final BizOperationApplyMapper bizOperationApplyMapper;

    private final PatientFacadeService patientFacadeService;

    private final EmployeeTechAuthService employeeTechAuthService;

    private final BizOperationCountMapper bizOperationCountMapper;

    private final SysOperationRoomMapper sysOperationRoomMapper;

    private final BizOperationSafetyCheckMapper bizOperationSafetyCheckMapper;

    private final DictCacheService dictCacheService;

    private final DeptScopeService deptScopeService;

    /**
     * 手术间单日运营窗口分钟数（08:00~18:00 = 600 分钟；利用率口径常量，如需按院方上下班配置改为读字典）。
     */
    private static final long ROOM_DAILY_CAPACITY_MINUTES = 600L;

    // 查询
    private static String textOr(String value, String fallback) {
        return TextUtil.hasText(value) ? value : fallback;
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    /**
     * 下界宽松解析：{@code yyyy-MM-dd} → 当天 00:00:00；带时分秒则原样使用（含）。
     */
    private static String normalizeFrom(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.from;
    }

    /**
     * 上界宽松解析：{@code yyyy-MM-dd} → <b>次日</b> 00:00:00（不含，这样能覆盖当天最后一秒）；
     * 带时分秒则原样使用（不含）。
     */
    private static String normalizeTo(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.to;
    }

    private static Parsed parse(String raw) {
        if (!TextUtil.hasText(raw)) {
            return null;
        }
        String s = raw.trim();
        try {
            if (s.length() == 10) {
                LocalDate d = LocalDate.parse(s);
                return new Parsed(TimeUtil.dayStart(d).format(DateFormats.DATETIME),
                        TimeUtil.dayStart(d.plusDays(1)).format(DateFormats.DATETIME));
            }
            LocalDateTime t = LocalDateTime.parse(s, DateFormats.DATETIME);
            return new Parsed(t.format(DateFormats.DATETIME), t.format(DateFormats.DATETIME));
        } catch (DateTimeParseException e) {
            // 明确报格式问题，不静默忽略、也不让它变成 500
            throw new BusinessException("时间格式不正确：" + raw + "（应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）");
        }
    }

    @Override
    public IPage<OperationApplyVO> listPage(OperationApplyQueryPageDTO query) {
        query.setPlannedDateFrom(normalizeFrom(query.getPlannedDateFrom()));
        query.setPlannedDateTo(normalizeTo(query.getPlannedDateTo()));
        IPage<OperationApplyVO> page = bizOperationApplyMapper.selectApplyPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query, deptScopeService.scopedDeptIds(query.getApplyDeptId()));
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public OperationApplyVO getDetailById(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        OperationApplyVO vo = bizOperationApplyMapper.selectApplyById(applyId);
        if (vo == null) {
            throw new BusinessException("手术申请单不存在");
        }
        deptScopeService.assertDeptAccessible(vo.getApplyDeptId());
        decorate(vo);
        return vo;
    }

    // 一、申请（待排期）

    @Override
    public List<OperationApplyVO> listByAdmission(Long admissionId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        AdmissionVO admission = patientFacadeService.getAdmission(admissionId);
        if (admission != null) {
            deptScopeService.assertDeptAccessible(admission.getDeptId());
        }
        List<OperationApplyVO> list = bizOperationApplyMapper.selectByAdmission(admissionId);
        list.forEach(this::decorate);
        return list;
    }

    // 二、排台（待排期 → 已排期；已排期可改期）

    @Override
    public long countUnfinished(Long admissionId) {
        return bizOperationApplyMapper.countUnfinished(admissionId, deptScopeService.scopedDeptIds(null));
    }

    // 三、术前核对（已排期 → 术前核对完成）

    @Override
    public List<String> roomList() {
        // 手术间主数据（启用）优先，再并入历史 distinct —— 老申请单用过的自由文本
        // 不能丢（否则改排一台历史手术时下拉里找不到它原来的手术间）。
        List<String> rooms = new ArrayList<>();
        sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                        .eq(SysOperationRoom::getStatus, 1)
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode))
                .forEach(r -> rooms.add(r.getRoomName()));
        for (String legacy : bizOperationApplyMapper.selectRoomList()) {
            if (!rooms.contains(legacy)) {
                rooms.add(legacy);
            }
        }
        return rooms;
    }

    /**
     * 手术间利用率：区间内各启用手术间已排台数/急诊数/占用分钟/可用分钟/利用率，末行是全院汇总。
     *
     * <p>口径（务实，注释声明）：可用分钟 = 单间每日运营窗口(08:00~18:00 常量) × 区间天数；
     * 占用分钟按排台计划起止计算，跨天台不截断；利用率 = 占用/可用保留 1 位百分比。
     * 未排台/无排台的启用手术间也出列（利用率 0），排到未登记手术间的手工名进汇总差额。
     */
    @Override
    public List<RoomUtilizationVO> roomUtilization(String from, String to) {
        LocalDate startDay = parseDay(from, "开始日期");
        LocalDate endDay = parseDay(to, "结束日期");
        if (endDay.isBefore(startDay)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        long days = startDay.toEpochDay() == endDay.toEpochDay() ? 1
                : endDay.toEpochDay() - startDay.toEpochDay() + 1;
        long capacityMinutes = ROOM_DAILY_CAPACITY_MINUTES * days;

        LocalDateTime fromLdt = startDay.atStartOfDay();
        LocalDateTime toLdt = endDay.plusDays(1).atStartOfDay();
        List<RoomUtilizationStatVO> stats = bizOperationApplyMapper.selectRoomUtilization(fromLdt, toLdt);
        Map<String, RoomUtilizationStatVO> byRoom = new HashMap<>();
        for (RoomUtilizationStatVO s : stats) {
            byRoom.put(textOr(s.getRoomName(), "未指定手术间"), s);
        }

        List<RoomUtilizationVO> rows = new ArrayList<>();
        List<SysOperationRoom> rooms = sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                .eq(SysOperationRoom::getStatus, 1)
                .orderByAsc(SysOperationRoom::getSortOrder)
                .orderByAsc(SysOperationRoom::getRoomCode));
        for (SysOperationRoom room : rooms) {
            RoomUtilizationStatVO s = byRoom.remove(room.getRoomName());
            RoomUtilizationVO vo = new RoomUtilizationVO();
            vo.setRoomId(room.getId());
            vo.setRoomCode(room.getRoomCode());
            vo.setRoomName(room.getRoomName());
            vo.setLocation(room.getLocation());
            if (s != null) {
                vo.setScheduledCount(s.getScheduledCount());
                vo.setEmergencyCount(s.getEmergencyCount());
                vo.setOccupiedMinutes(s.getOccupiedMinutes());
            } else {
                vo.setScheduledCount(0);
                vo.setEmergencyCount(0);
                vo.setOccupiedMinutes(0L);
            }
            vo.setCapacityMinutes(capacityMinutes);
            vo.setUtilizationRate(rateOf(vo.getOccupiedMinutes(), capacityMinutes));
            rows.add(vo);
        }
        // 未登记手术间（手工输入名）的台也计入全院汇总差额，不丢
        long orphanMinutes = byRoom.values().stream().mapToLong(RoomUtilizationStatVO::getOccupiedMinutes).sum();
        long orphanCount = byRoom.values().stream().mapToLong(s -> s.getScheduledCount() == null ? 0 : s.getScheduledCount()).sum();
        if (orphanMinutes > 0 || orphanCount > 0) {
            RoomUtilizationVO orphan = new RoomUtilizationVO();
            orphan.setRoomName("未登记手术间（手工名）");
            orphan.setScheduledCount((int) orphanCount);
            orphan.setEmergencyCount(0);
            orphan.setOccupiedMinutes(orphanMinutes);
            orphan.setCapacityMinutes(0L);
            orphan.setUtilizationRate(BigDecimal.ZERO);
            rows.add(orphan);
        }
        // 全院汇总行
        long totalOccupied = rows.stream().mapToLong(v -> v.getOccupiedMinutes() == null ? 0 : v.getOccupiedMinutes()).sum();
        long totalCapacity = rows.stream().mapToLong(v -> v.getCapacityMinutes() == null ? 0 : v.getCapacityMinutes()).sum();
        RoomUtilizationVO total = new RoomUtilizationVO();
        total.setRoomName("全院合计");
        total.setScheduledCount(rows.stream().mapToInt(v -> v.getScheduledCount() == null ? 0 : v.getScheduledCount()).sum());
        total.setEmergencyCount(rows.stream().mapToInt(v -> v.getEmergencyCount() == null ? 0 : v.getEmergencyCount()).sum());
        total.setOccupiedMinutes(totalOccupied);
        total.setCapacityMinutes(totalCapacity);
        total.setUtilizationRate(rateOf(totalOccupied, totalCapacity));
        rows.add(total);
        return rows;
    }

    private LocalDate parseDay(String text, String label) {
        try {
            return TextUtil.hasText(text) ? LocalDate.parse(text.trim()) : LocalDate.now();
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + "格式不正确：" + text + "（应为 yyyy-MM-dd）");
        }
    }

    private BigDecimal rateOf(Long occupied, Long capacity) {
        if (occupied == null || capacity == null || capacity <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(occupied * 100.0 / capacity).setScale(1, RoundingMode.HALF_UP);
    }

    // ==================== AI 排台建议（贪心 + 约束，只出建议不落库） ====================

    /** AI 预估时长：按手术级别（1级60 / 2级90 / 3级120 / 4级180 分钟），未定级别 90 */
    private static int estimateDurationMinutes(Integer operationLevel) {
        if (operationLevel == null) {
            return 90;
        }
        return switch (operationLevel) {
            case 1 -> 60;
            case 3 -> 120;
            case 4 -> 180;
            default -> 90;
        };
    }

    @Override
    public AiSuggestResultVO aiSuggest(AiSuggestQueryDTO dto) {
        LocalDate day = parseDay(dto == null ? null : dto.getDate(), "排台日期");
        List<Long> roomIds = dto == null ? null : dto.getRoomIds();
        List<Long> deptIds = deptScopeService.scopedDeptIds(null);
        LocalDateTime dayFrom = day.atStartOfDay();
        LocalDateTime dayTo = day.plusDays(1).atStartOfDay();

        // 手术间：roomIds 空=全部启用，否则在启用里过滤
        List<SysOperationRoom> rooms = sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                .eq(SysOperationRoom::getStatus, 1)
                .orderByAsc(SysOperationRoom::getSortOrder)
                .orderByAsc(SysOperationRoom::getRoomCode));
        List<SysOperationRoom> candidates = new ArrayList<>();
        for (SysOperationRoom room : rooms) {
            if (roomIds == null || roomIds.isEmpty() || roomIds.contains(room.getId())) {
                candidates.add(room);
            }
        }
        if (candidates.isEmpty()) {
            throw new BusinessException("未选择可用的手术间（无启用手术间或所选 id 不在启用列表）");
        }

        // 当日已排台（同间时段 + 人力占用）
        List<OperationApplyVO> scheduled = bizOperationApplyMapper.selectScheduledBetween(dayFrom, dayTo, deptIds);
        Map<String, List<LocalDateTime[]>> byRoom = new HashMap<>();
        Map<Long, List<LocalDateTime[]>> byPerson = new HashMap<>();
        for (OperationApplyVO s : scheduled) {
            String room = textOr(s.getOperationRoom(), "未登记手术间");
            byRoom.computeIfAbsent(room, k -> new ArrayList<>())
                    .add(new LocalDateTime[]{s.getPlannedStartTime(), s.getPlannedEndTime()});
            addPersonSpan(byPerson, s.getSurgeonId(), s.getPlannedStartTime(), s.getPlannedEndTime());
            addPersonSpan(byPerson, s.getAnesthetistId(), s.getPlannedStartTime(), s.getPlannedEndTime());
            addPersonSpan(byPerson, s.getCirculatingNurseId(), s.getPlannedStartTime(), s.getPlannedEndTime());
            addPersonSpan(byPerson, s.getScrubNurseId(), s.getPlannedStartTime(), s.getPlannedEndTime());
        }
        byRoom.values().forEach(list -> list.sort(Comparator.comparing(a -> a[0])));

        LocalTime open = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(18, 0);
        AiSuggestResultVO result = new AiSuggestResultVO();
        result.setDate(day.toString());
        result.setCapacityMinutesPerRoom(ROOM_DAILY_CAPACITY_MINUTES);

        List<OperationApplyVO> pending = bizOperationApplyMapper.selectUnscheduled(deptIds);
        for (OperationApplyVO op : pending) {
            int duration = estimateDurationMinutes(op.getOperationLevel());
            AiSuggestionVO best = null;
            for (SysOperationRoom room : candidates) {
                List<LocalDateTime[]> spans = byRoom.getOrDefault(room.getRoomName(), List.of());
                // 该房间当天全部可用空档逐个试人力：最早空档撞主刀/麻醉/护士时段分身时，
                // 继续试该房间后续空档（贪心取全局最早可行档，而不是一撞就弃房）
                for (LocalDateTime slotStart : availableSlots(day, open, close, spans, duration)) {
                    LocalDateTime slotEnd = slotStart.plusMinutes(duration);
                    if (!personFree(byPerson, op, slotStart, slotEnd)) {
                        continue;
                    }
                    if (best == null || slotStart.isBefore(best.getSuggestedStart())) {
                        best = toSuggestion(op, duration, room.getRoomName(), slotStart, slotEnd, byRoom.get(room.getRoomName()));
                    }
                    break; // 该房间取第一个可行档即可，其余档由全局最早约束裁掉
                }
            }
            if (best != null) {
                result.getSuggestions().add(best);
                // 建议占用：同间 + 人力都推进（供后续单继续贪心）
                byRoom.computeIfAbsent(best.getSuggestedRoomName(), k -> new ArrayList<>())
                        .add(new LocalDateTime[]{best.getSuggestedStart(), best.getSuggestedEnd()});
                byRoom.get(best.getSuggestedRoomName()).sort(Comparator.comparing(a -> a[0]));
                addPersonSpan(byPerson, op.getSurgeonId(), best.getSuggestedStart(), best.getSuggestedEnd());
                addPersonSpan(byPerson, op.getAnesthetistId(), best.getSuggestedStart(), best.getSuggestedEnd());
                addPersonSpan(byPerson, op.getCirculatingNurseId(), best.getSuggestedStart(), best.getSuggestedEnd());
                addPersonSpan(byPerson, op.getScrubNurseId(), best.getSuggestedStart(), best.getSuggestedEnd());
            } else {
                AiSuggestionVO blocked = toSuggestion(op, duration, null, null, null, null);
                blocked.setReason("当日全部手术间在营业时段（08:00~18:00）内均无 ≥ " + duration + " 分钟的连续空档（或已定主刀/麻醉/护士时段分身）");
                result.getBlocked().add(blocked);
            }
        }
        return result;
    }

    private static void addPersonSpan(Map<Long, List<LocalDateTime[]>> byPerson, Long employeeId,
                                      LocalDateTime start, LocalDateTime end) {
        if (employeeId != null && start != null && end != null) {
            byPerson.computeIfAbsent(employeeId, k -> new ArrayList<>())
                    .add(new LocalDateTime[]{start, end});
        }
    }

    /** 同手术间当天全部可用空档（营业窗口内，与已排区间不重叠，每段至少 duration 分钟） */
    private static List<LocalDateTime> availableSlots(LocalDate day, LocalTime open, LocalTime close,
                                                      List<LocalDateTime[]> spans, int duration) {
        List<LocalDateTime> out = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.of(day, open);
        LocalDateTime dayEnd = LocalDateTime.of(day, close);
        for (LocalDateTime[] span : spans) {
            LocalDateTime s = span[0] == null ? cursor : span[0];
            LocalDateTime e = span[1] == null ? cursor : span[1];
            if (s.isBefore(cursor)) {
                // 与当前游标重叠/跨段：游标推到该段之后（若该段已开始）
                if (e.isAfter(cursor)) {
                    cursor = e;
                }
                continue;
            }
            if (e.isAfter(cursor)) {
                if (e.isBefore(dayEnd)) {
                    if (Duration.between(cursor, s).toMinutes() >= duration) {
                        out.add(cursor);
                    }
                    cursor = e;
                } else {
                    // 区间占满到营业结束
                    if (Duration.between(cursor, s).toMinutes() >= duration) {
                        out.add(cursor);
                    }
                    cursor = dayEnd;
                    break;
                }
            }
        }
        if (!cursor.plusMinutes(duration).isAfter(dayEnd)) {
            out.add(cursor);
        }
        return out;
    }

    /** 已定主刀/麻醉/护士是否与建议时段分身（未定人不受限） */
    private static boolean personFree(Map<Long, List<LocalDateTime[]>> byPerson, OperationApplyVO op,
                                      LocalDateTime start, LocalDateTime end) {
        return spanFree(byPerson, op.getSurgeonId(), start, end)
                && spanFree(byPerson, op.getAnesthetistId(), start, end)
                && spanFree(byPerson, op.getCirculatingNurseId(), start, end)
                && spanFree(byPerson, op.getScrubNurseId(), start, end);
    }

    private static boolean spanFree(Map<Long, List<LocalDateTime[]>> byPerson, Long employeeId,
                                    LocalDateTime start, LocalDateTime end) {
        if (employeeId == null) {
            return true;
        }
        for (LocalDateTime[] span : byPerson.getOrDefault(employeeId, List.of())) {
            if (span[0] != null && span[1] != null
                    && start.isBefore(span[1]) && span[0].isBefore(end)) {
                return false;
            }
        }
        return true;
    }

    private static AiSuggestionVO toSuggestion(OperationApplyVO op, int duration, String roomName,
                                               LocalDateTime start, LocalDateTime end,
                                               List<LocalDateTime[]> roomSpans) {
        AiSuggestionVO vo = new AiSuggestionVO();
        vo.setApplyId(op.getId());
        vo.setApplyNo(op.getApplyNo());
        vo.setPatientName(op.getPatientName());
        vo.setPlannedOperationName(op.getPlannedOperationName());
        vo.setOperationLevel(op.getOperationLevel());
        vo.setIsEmergency(op.getIsEmergency());
        vo.setApplyDeptName(op.getApplyDeptName());
        vo.setDurationMinutes(duration);
        vo.setSurgeonId(op.getSurgeonId());
        vo.setSurgeonName(op.getSurgeonName());
        vo.setAnesthetistId(op.getAnesthetistId());
        vo.setAnesthetistName(op.getAnesthetistName());
        vo.setCirculatingNurseId(op.getCirculatingNurseId());
        vo.setScrubNurseId(op.getScrubNurseId());
        vo.setSuggestedRoomName(roomName);
        vo.setSuggestedStart(start);
        vo.setSuggestedEnd(end);
        if (start != null) {
            boolean first = start.toLocalTime().equals(LocalTime.of(8, 0));
            String prev = "当日首台空档";
            if (!first && roomSpans != null) {
                for (LocalDateTime[] span : roomSpans) {
                    if (span[1] != null && !span[1].isAfter(start)) {
                        prev = "前一班 " + span[1].toLocalTime() + " 结束的接台空档";
                    }
                }
            }
            vo.setReason("AI 建议：" + prev + "（急诊优先 / 按申请先后 / 避开同间时段与人力分身）");
        }
        return vo;
    }


    // 四、完成（术前核对完成 → 已完成；回写首页明细 + 手术记录病历）

    @Override
    public OperationScheduleMatrixVO scheduleMatrix(String date) {
        LocalDate day;
        try {
            day = TextUtil.hasText(date) ? LocalDate.parse(date.trim()) : LocalDate.now();
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确：" + date + "（应为 yyyy-MM-dd）");
        }

        OperationScheduleMatrixVO matrix = new OperationScheduleMatrixVO();
        matrix.setDate(day.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // 列 = 启用中的手术间主数据（顺序即总表列序）
        List<SysOperationRoom> enabledRooms = sysOperationRoomMapper.selectList(
                new LambdaQueryWrapper<SysOperationRoom>()
                        .eq(SysOperationRoom::getStatus, 1)
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode));

        LocalDateTime from = TimeUtil.dayStart(day);
        LocalDateTime to = TimeUtil.dayStart(day.plusDays(1));
        List<Long> deptIds = deptScopeService.scopedDeptIds(null);
        List<OperationApplyVO> scheduled = bizOperationApplyMapper.selectScheduledBetween(from, to, deptIds);
        List<OperationApplyVO> pending = bizOperationApplyMapper.selectUnscheduled(deptIds);
        scheduled.forEach(this::decorate);
        pending.forEach(this::decorate);

        // 三方核查轮数：只在这张表里批量回填一次（列表接口不查，避免 N+1）
        Map<Long, Long> phaseCounts = phaseCountsOf(scheduled, pending);
        scheduled.forEach(vo -> vo.setSafetyCheckPhases(
                phaseCounts.getOrDefault(vo.getId(), 0L).intValue()));
        pending.forEach(vo -> vo.setSafetyCheckPhases(0));

        // 落桶：手术间名与主数据对上的进对应列，对不上的进 others —— 一台都不能丢
        Map<String, List<OperationApplyVO>> byRoom = new LinkedHashMap<>();
        for (OperationApplyVO vo : scheduled) {
            byRoom.computeIfAbsent(textOr(vo.getOperationRoom(), "未指定手术间"),
                    k -> new ArrayList<>()).add(vo);
        }
        List<OperationScheduleMatrixVO.RoomColumn> columns = new ArrayList<>();
        for (SysOperationRoom room : enabledRooms) {
            OperationScheduleMatrixVO.RoomColumn col = new OperationScheduleMatrixVO.RoomColumn();
            col.setRoomId(room.getId());
            col.setRoomCode(room.getRoomCode());
            col.setRoomName(room.getRoomName());
            col.setLocation(room.getLocation());
            List<OperationApplyVO> ops = byRoom.remove(room.getRoomName());
            col.setOps(ops == null ? new ArrayList<>() : ops);
            columns.add(col);
        }
        List<OperationScheduleMatrixVO.RoomColumn> others = new ArrayList<>();
        byRoom.forEach((name, ops) -> {
            OperationScheduleMatrixVO.RoomColumn col = new OperationScheduleMatrixVO.RoomColumn();
            col.setRoomName(name);
            col.setOps(ops);
            others.add(col);
        });
        others.sort(Comparator.comparing(OperationScheduleMatrixVO.RoomColumn::getRoomName));

        matrix.setRooms(columns);
        matrix.setOthers(others);
        matrix.setUnscheduled(pending);
        matrix.setScheduledCount(scheduled.size());
        matrix.setEmergencyCount((int) scheduled.stream()
                .filter(vo -> Objects.equals(1, vo.getIsEmergency())).count());
        return matrix;
    }

    // 五、取消（仅待排期 / 已排期 → 已取消）

    /**
     * 批量取多张申请单的已签核查轮数（一次 IN 查询；唯一键 uk_check_apply_phase 保证一申请一时段一行，行数即轮数）
     */
    private Map<Long, Long> phaseCountsOf(List<OperationApplyVO>... groups) {
        List<Long> ids = new ArrayList<>();
        for (List<OperationApplyVO> group : groups) {
            group.forEach(vo -> {
                if (vo.getId() != null) {
                    ids.add(vo.getId());
                }
            });
        }
        Map<Long, Long> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        List<BizOperationSafetyCheck> rows = bizOperationSafetyCheckMapper.selectList(
                new LambdaQueryWrapper<BizOperationSafetyCheck>()
                        .select(BizOperationSafetyCheck::getApplyId)
                        .in(BizOperationSafetyCheck::getApplyId, ids));
        for (BizOperationSafetyCheck row : rows) {
            result.merge(row.getApplyId(), 1L, Long::sum);
        }
        return result;
    }

    // 回写：手术记录病历

    @Override
    public List<OperationApplyVO.CheckItem> checkItems() {
        List<OperationApplyVO.CheckItem> list = new ArrayList<>();
        OperationCheckItems.all().forEach((code, label) -> {
            OperationApplyVO.CheckItem item = new OperationApplyVO.CheckItem();
            item.setCode(code);
            item.setLabel(label);
            item.setRequired(OperationCheckItems.REQUIRED.contains(code));
            list.add(item);
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(OperationApplyUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        AdmissionVO admission = patientFacadeService.getAdmission(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者当前不是「在院」状态，不能申请手术（已出院的住院不能开手术单）");
        }
        PatientMasterVO patient = patientFacadeService.getPatient(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        int isMain = dto.getIsMain();
        boolean create = dto.getId() == null;

        BizOperationApply entity;
        if (create) {
            deptScopeService.assertDeptAccessible(admission.getDeptId());
            entity = new BizOperationApply();
            entity.setAdmissionId(dto.getAdmissionId());
            entity.setAdmissionNo(admission.getAdmissionNo());
            entity.setPatientId(admission.getPatientId());
            entity.setPatientName(patient.getPatientName());
            entity.setGender(patient.getGender());
            entity.setAge(patient.getAge());
            entity.setApplyDeptId(admission.getDeptId());
            entity.setApplyDeptName(deptNameOf(admission.getDeptId()));
            entity.setApplyWardName(wardNameOf(admission.getWardId()));
            entity.setApplyBedNo(bedNoOf(admission.getBedId()));
            entity.setApplyDoctorId(operatorUser.getEmployeeId());
            entity.setApplyDoctorName(operatorUser.getRealName());
            entity.setApplyTime(TimeUtil.nowSeconds());
            entity.setApplyNo(nextApplyNo());
            entity.setOperationStatus(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode());
        } else {
            entity = mustGet(dto.getId());
            deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
            if (!Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), entity.getOperationStatus())) {
                throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                        + OperationApplyStatusEnum.getLabelOrUnknown(entity.getOperationStatus())
                        + "」，只有「待排期」可以修改申请内容（排台后术式已对外承诺，改请先取消或走停手术）");
            }
            if (!Objects.equals(entity.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("不允许把手术单改挂到另一次住院上");
            }
        }

        // 重复申请：同一住院 + 同一拟施术式，不允许并存两条未完成申请（四核对的"重复"）
        if (bizOperationApplyMapper.countUnfinishedSameName(dto.getAdmissionId(), dto.getPlannedOperationName()) > 0) {
            throw new BusinessException("该住院已有一条未完成的「" + dto.getPlannedOperationName()
                    + "」手术申请，请先完成或取消后再发起（同一台手术申请两次属于重复）");
        }
        // 主要手术唯一：同一次住院只能有一条主要手术申请
        if (isMain == 1
                && bizOperationApplyMapper.countMainOperation(dto.getAdmissionId(), entity.getId()) > 0) {
            throw new BusinessException("该住院已有一条「主要手术」申请，同一次住院只允许一条"
                    + "（首页主要手术只能有 1 条，与本条并存会导致首页出现两条主手术）");
        }

        entity.setPlannedOperationCode(dto.getPlannedOperationCode());
        entity.setPlannedOperationName(dto.getPlannedOperationName());
        entity.setOperationLevel(dto.getOperationLevel());
        entity.setIncisionLevel(dto.getIncisionLevel());
        entity.setAnesthesiaType(dto.getAnesthesiaType());
        entity.setPreopDiagnosis(dto.getPreopDiagnosis());
        entity.setOperationReason(dto.getOperationReason());
        entity.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
        entity.setIsMain(isMain);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }

        if (create) {
            bizOperationApplyMapper.insert(entity);
        } else {
            int rows = bizOperationApplyMapper.update(entity, new LambdaUpdateWrapper<BizOperationApply>()
                    .eq(BizOperationApply::getId, entity.getId())
                    .eq(BizOperationApply::getOperationStatus, OperationApplyStatusEnum.PENDING_SCHEDULE.getCode()));
            if (rows == 0) {
                throw new BusinessException("手术单 " + entity.getApplyNo()
                        + " 当前状态已变化，只有「待排期」可以修改申请内容"
                        + "（排台后术式已对外承诺，改请先取消或走停手术）");
            }
        }
        gateTechAuth(entity.getApplyDoctorId(), TechAuthCategoryEnum.SURGERY.getCode(), 1,
                entity, "手术申请人", null);
        log.info("{}手术申请 applyNo={} admissionId={} 术式={} 急诊={} 主要={} 申请人={}",
                create ? "发起" : "修改", entity.getApplyNo(), entity.getAdmissionId(),
                entity.getPlannedOperationName(), entity.getIsEmergency(), entity.getIsMain(), operatorUser.getRealName());
        return entity.getApplyNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedule(OperationScheduleDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        LocalDateTime start = TimeUtil.toSeconds(dto.getPlannedStartTime());
        LocalDateTime end = TimeUtil.toSeconds(dto.getPlannedEndTime());
        if (!end.isAfter(start)) {
            throw new BusinessException("计划结束时间必须晚于开始时间");
        }

        BizOperationApply entity = mustGet(dto.getApplyId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已完成，不能改排台信息");
        }
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能排台");
        }
        if (Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成术前核对，改时段需要重新核对，请先与手术室确认后再停台重排");
        }

        // 排台冲突：同手术间、时间区间重叠、在途/已完成的手术
        List<BizOperationApply> conflicts = bizOperationApplyMapper.selectRoomConflicts(
                dto.getOperationRoom(), start, end, entity.getId());
        if (!conflicts.isEmpty()) {
            BizOperationApply c = conflicts.get(0);
            throw new BusinessException("「" + dto.getOperationRoom() + "」在 "
                    + c.getPlannedStartTime().format(DateFormats.TIME_MINUTE) + "~" + c.getPlannedEndTime().format(DateFormats.TIME_MINUTE)
                    + " 已被 " + c.getApplyNo() + "（" + c.getPatientName() + " "
                    + c.getPlannedOperationName() + "）占用，请换手术间或换时段");
        }

        // 跨手术间人力冲突：主刀/麻醉/巡回/洗手护士同一时段不能分身两台（不同间各自不撞，却占用了同一个人）
        String surgeonName = employeeNameOf(dto.getSurgeonId());
        assertNoPersonConflict(dto.getSurgeonId(), "主刀医师", surgeonName, start, end, entity.getId());
        if (dto.getAnesthetistId() != null) {
            assertNoPersonConflict(dto.getAnesthetistId(), "麻醉医师", employeeNameOf(dto.getAnesthetistId()),
                    start, end, entity.getId());
        }
        if (dto.getCirculatingNurseId() != null) {
            assertNoPersonConflict(dto.getCirculatingNurseId(), "巡回护士", employeeNameOf(dto.getCirculatingNurseId()),
                    start, end, entity.getId());
        }
        if (dto.getScrubNurseId() != null) {
            assertNoPersonConflict(dto.getScrubNurseId(), "洗手护士", employeeNameOf(dto.getScrubNurseId()),
                    start, end, entity.getId());
        }
        entity.setOperationRoom(dto.getOperationRoom());
        entity.setPlannedStartTime(start);
        entity.setPlannedEndTime(end);
        entity.setSurgeonId(dto.getSurgeonId());
        entity.setSurgeonName(surgeonName);
        entity.setAssistantName(dto.getAssistantName());
        entity.setAnesthetistId(dto.getAnesthetistId());
        entity.setAnesthetistName(employeeNameOf(dto.getAnesthetistId()));
        entity.setCirculatingNurseId(dto.getCirculatingNurseId());
        entity.setCirculatingNurseName(dto.getCirculatingNurseId() == null ? null : employeeNameOf(dto.getCirculatingNurseId()));
        entity.setScrubNurseId(dto.getScrubNurseId());
        entity.setScrubNurseName(dto.getScrubNurseId() == null ? null : employeeNameOf(dto.getScrubNurseId()));
        entity.setScheduleDoctorId(operatorUser.getEmployeeId());
        entity.setScheduleDoctorName(operatorUser.getRealName());
        entity.setScheduleTime(TimeUtil.nowSeconds());
        entity.setScheduleRemark(dto.getScheduleRemark());
        entity.setOperationStatus(OperationApplyStatusEnum.SCHEDULED.getCode());
        int rows = bizOperationApplyMapper.update(entity, new LambdaUpdateWrapper<BizOperationApply>()
                .eq(BizOperationApply::getId, entity.getId())
                .notIn(BizOperationApply::getOperationStatus,
                        OperationApplyStatusEnum.FINISHED.getCode(),
                        OperationApplyStatusEnum.CANCELLED.getCode(),
                        OperationApplyStatusEnum.PREOP_CHECKED.getCode()));
        if (rows == 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 当前状态已变为已完成/已取消/已术前核对，不能排台，请刷新后重试");
        }
        gateTechAuth(dto.getSurgeonId(), TechAuthCategoryEnum.SURGERY.getCode(),
                entity.getOperationLevel(), entity, "主刀医师", surgeonName);
        if (dto.getAnesthetistId() != null) {
            gateTechAuth(dto.getAnesthetistId(), TechAuthCategoryEnum.ANESTHESIA.getCode(),
                    entity.getOperationLevel(), entity, "麻醉医师", entity.getAnesthetistName());
        }
        log.info("排台 applyNo={} 手术间={} {}~{} 主刀={} 排台人={}",
                entity.getApplyNo(), dto.getOperationRoom(),
                start.format(DateFormats.DATETIME), end.format(DateFormats.DATETIME), surgeonName, operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void preopCheck(OperationPreopCheckDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 尚未排台，不能做术前核对（手术间/时段/主刀都还没定，核对的是一个不存在的手术）");
        }
        if (!Objects.equals(OperationApplyStatusEnum.SCHEDULED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                    + OperationApplyStatusEnum.getLabelOrUnknown(entity.getOperationStatus())
                    + "」，只有「已排期」可以做术前核对");
        }

        Set<Integer> codes;
        try {
            codes = OperationCheckItems.parse(dto.getCheckItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        List<String> missing = OperationCheckItems.missingRequired(codes);
        if (!missing.isEmpty()) {
            throw new BusinessException("术前核对必核项未完成：" + String.join("；", missing)
                    + "。这 4 项少核任何一项，术前核对就不成立");
        }

        entity.setPreopCheckItems(OperationCheckItems.serialize(codes));
        entity.setPreopNote(dto.getPreopNote());
        entity.setPreopCheckDoctorId(operatorUser.getEmployeeId());
        entity.setPreopCheckDoctorName(operatorUser.getRealName());
        entity.setPreopCheckTime(TimeUtil.nowSeconds());
        entity.setOperationStatus(OperationApplyStatusEnum.PREOP_CHECKED.getCode());
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        // 守卫下沉到 WHERE：并发下状态被改离「已排期」时更新命中 0 行，兜底抛错（与上方 Java 校验同义）
        int rows = bizOperationApplyMapper.update(entity, new LambdaUpdateWrapper<BizOperationApply>()
                .eq(BizOperationApply::getId, entity.getId())
                .eq(BizOperationApply::getOperationStatus, OperationApplyStatusEnum.SCHEDULED.getCode()));
        if (rows == 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 当前状态已变化，只有「已排期」可以做术前核对");
        }
        log.info("术前核对完成 applyNo={} 核对项={} 异常说明={} 核对人={}",
                entity.getApplyNo(), entity.getPreopCheckItems(),
                TextUtil.hasText(dto.getPreopNote()) ? dto.getPreopNote() : "无", operatorUser.getRealName());
    }

    // 展示态

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(OperationFinishDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能完成");
        }
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已完成，不能重复回写"
                    + "（重复执行会产生第二份手术记录与第二条首页手术明细）");
        }
        if (!Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                    + OperationApplyStatusEnum.getLabelOrUnknown(entity.getOperationStatus())
                    + "」，未完成术前核对不能登记完成（术后补一条核对记录属于伪造，必须先把核对做完）");
        }

        LocalDateTime start = TimeUtil.toSeconds(dto.getOperationStartTime());
        LocalDateTime end = TimeUtil.toSeconds(dto.getOperationEndTime());
        if (!end.isAfter(start)) {
            throw new BusinessException("实际结束时间必须晚于开始时间");
        }

        AdmissionVO admission = patientFacadeService.getAdmission(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法回写（admissionId=" + entity.getAdmissionId() + "）");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能再登记手术完成（手术是住院期间发生的事件；"
                    + "出院后发现漏登记，请走病案质控缺陷流程补记，而不是直接改已结算的住院）");
        }

        // 归档闸门：已归档的病案首页不能再加手术明细（同"归档后病历不可修改"）
        if (patientFacadeService.isSummaryArchived(entity.getAdmissionId())) {
            throw new BusinessException("该住院的病案首页已归档，不能再回写手术明细（归档数据不可改）");
        }

        // 器械清点闸门：只要这台手术建过清点单，就必须三轮走完且对得上，
        guardCount(entity);

        // 三方安全核查闸门
        guardSafetyCheck(entity);

        LocalDateTime now = TimeUtil.nowSeconds();
        String actualName = dto.getActualOperationName().trim();
        String basis = buildOperationBasis(dto);

        SurgeryOperationDTO op = new SurgeryOperationDTO();
        op.setAdmissionId(entity.getAdmissionId());
        op.setApplyId(entity.getId());
        op.setIsMain(entity.getIsMain());
        op.setOperationCode(TextUtil.hasText(dto.getActualOperationCode())
                ? dto.getActualOperationCode() : entity.getPlannedOperationCode());
        op.setOperationName(actualName);
        op.setOperationDate(start);
        op.setOperationLevel(entity.getOperationLevel());
        op.setIncisionLevel(entity.getIncisionLevel());
        op.setAnesthesiaType(entity.getAnesthesiaType());
        op.setSurgeonId(entity.getSurgeonId());
        op.setSurgeonName(entity.getSurgeonName());
        op.setAssistantName(entity.getAssistantName());
        op.setOperationBasis(basis);
        op.setRemark("系统回写：手术申请单号 " + entity.getApplyNo()
                + "，主刀 " + textOr(entity.getSurgeonName(), "未指定")
                + "，麻醉方式 " + OperationAnesthesiaMethodEnum.getLabel(entity.getAnesthesiaType()));
        // 首页明细的重复闸、序号重排、is_surgery 置位都在首页写入方里做（它才是这张表的所有者）
        SurgeryOperationVO opVo = patientFacadeService.appendSurgeryOperation(op);

        // ② 回写手术记录病历（record_type=5）
        ClosedLoopRecordVO record = writeBackRecord(entity, admission, dto, start, end, basis, now);

        entity.setActualOperationCode(dto.getActualOperationCode());
        entity.setActualOperationName(actualName);
        entity.setOperationStartTime(start);
        entity.setOperationEndTime(end);
        entity.setBloodLoss(dto.getBloodLoss());
        entity.setIntraopFindings(dto.getIntraopFindings());
        entity.setIntraopProcedure(dto.getIntraopProcedure());
        entity.setPostopNote(dto.getPostopNote());
        entity.setSpecimenSent(dto.getSpecimenSent());
        entity.setFinishDoctorId(operatorUser.getEmployeeId());
        entity.setFinishDoctorName(operatorUser.getRealName());
        entity.setFinishTime(now);
        entity.setOperationId(opVo.getId());
        entity.setRecordId(record.getId());
        entity.setOperationStatus(OperationApplyStatusEnum.FINISHED.getCode());
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        // 状态守卫下沉到 WHERE：读-判-写之间并发回写完成/取消会使 Java 侧判断失效，
        // 重复执行会产生第二份手术记录与第二条首页手术明细
        int rows = bizOperationApplyMapper.update(entity, new LambdaUpdateWrapper<BizOperationApply>()
                .eq(BizOperationApply::getId, entity.getId())
                .eq(BizOperationApply::getOperationStatus, OperationApplyStatusEnum.PREOP_CHECKED.getCode()));
        if (rows == 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 状态已变更（可能已被并发登记完成或取消），"
                    + "未完成术前核对不能登记完成，请刷新后重试");
        }

        log.info("手术完成 applyNo={} 术式={} {}~{} 首页明细ID={} 病历号={} 录入人={}",
                entity.getApplyNo(), actualName, start.format(DateFormats.DATETIME), end.format(DateFormats.DATETIME),
                opVo.getId(), record.getRecordNo(), operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(OperationCancelDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能重复取消");
        }
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成，不能取消；已完成的手术已经写进首页和病历，取消它就是销毁证据");
        }
        if (Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成术前核对，不能取消（患者已进入手术区流程）；要停台请由手术室登记停手术并写明原因");
        }
        entity.setOperationStatus(OperationApplyStatusEnum.CANCELLED.getCode());
        entity.setCancelReason(dto.getCancelReason());
        entity.setCancelDoctorId(operatorUser.getEmployeeId());
        entity.setCancelDoctorName(operatorUser.getRealName());
        entity.setCancelTime(TimeUtil.nowSeconds());
        // 守卫下沉到 WHERE：并发下已被取消/完成/术前核对时更新命中 0 行，兜底抛错（与上方 Java 校验同义）
        int rows = bizOperationApplyMapper.update(entity, new LambdaUpdateWrapper<BizOperationApply>()
                .eq(BizOperationApply::getId, entity.getId())
                .notIn(BizOperationApply::getOperationStatus,
                        OperationApplyStatusEnum.CANCELLED.getCode(),
                        OperationApplyStatusEnum.FINISHED.getCode(),
                        OperationApplyStatusEnum.PREOP_CHECKED.getCode()));
        if (rows == 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 当前状态已变为已取消/已完成/已术前核对，不能取消，请刷新后重试");
        }
        log.info("取消手术 applyNo={} 原因={} 操作人={}",
                entity.getApplyNo(), dto.getCancelReason(), operatorUser.getRealName());
    }


    /**
     * 把这次手术回写成一份住院病历（record_type=5 手术记录，状态直接「已提交」）。
     */
    private ClosedLoopRecordVO writeBackRecord(BizOperationApply entity, AdmissionVO admission,
                                               OperationFinishDTO dto,
                                               LocalDateTime start, LocalDateTime end,
                                               String basis, LocalDateTime now) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        PatientMasterVO patient = patientFacadeService.getPatient(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在，无法回写手术记录");
        }

        String deptName = deptNameOf(admission.getDeptId());
        String wardName = wardNameOf(admission.getWardId());
        String bedNo = bedNoOf(admission.getBedId());

        StringBuilder course = new StringBuilder();
        course.append("手术名称：").append(dto.getActualOperationName()).append('\n');
        course.append("手术时间：").append(start.format(DateFormats.DATETIME)).append(" ~ ").append(end.format(DateFormats.DATETIME))
                .append("（").append(AnesthesiaCalcs.durationText(
                        Duration.between(start, end).toMinutes())).append("）\n");
        course.append("术中所见：").append(dto.getIntraopFindings()).append('\n');
        course.append("手术经过：").append(dto.getIntraopProcedure()).append('\n');
        course.append("术后处理：").append(dto.getPostopNote());
        if (dto.getBloodLoss() != null) {
            course.append('\n').append("术中出血量：").append(dto.getBloodLoss()).append(" ml");
        }
        if (TextUtil.hasText(dto.getSpecimenSent())) {
            course.append('\n').append("标本送检：").append(dto.getSpecimenSent());
        }
        if (TextUtil.hasText(entity.getPreopCheckItems())) {
            course.append('\n').append("术前核对：")
                    .append(OperationCheckItems.summaryText(entity.getPreopCheckItems()));
        }

        ClosedLoopRecordDTO record = new ClosedLoopRecordDTO();
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        record.setAge(patient.getAge());
        record.setAgeUnit(1);
        record.setDeptId(admission.getDeptId());
        record.setDeptName(deptName);
        record.setWardId(admission.getWardId());
        record.setWardName(wardName);
        record.setBedNo(bedNo);
        record.setRecordType(InpatientRecordTypeEnum.OPERATION_RECORD.getCode());
        record.setRecordTitle("手术记录");
        record.setRecordTime(end);
        record.setDiagnosisName(entity.getPreopDiagnosis());
        record.setCourseNote(course.toString());
        record.setRemark("系统回写：手术申请单号 " + entity.getApplyNo()
                + "，主刀 " + textOr(entity.getSurgeonName(), "未指定")
                + "，麻醉方式 " + OperationAnesthesiaMethodEnum.getLabel(entity.getAnesthesiaType())
                + "，手术级别 " + dictCacheService.getDicDataLabel(DictTypeConst.OPERATION_LEVEL, entity.getOperationLevel())
                + "，切口等级 " + dictCacheService.getDicDataLabel(DictTypeConst.INCISION_LEVEL, entity.getIncisionLevel()));
        record.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
        record.setDoctorId(entity.getSurgeonId() != null ? entity.getSurgeonId() : operatorUser.getEmployeeId());
        record.setDoctorName(TextUtil.hasText(entity.getSurgeonName())
                ? entity.getSurgeonName() : operatorUser.getRealName());
        record.setSubmitTime(now);
        return patientFacadeService.appendClosedLoopRecord(record);
    }

    /**
     * 器械清点闸门。
     */
    private void guardCount(BizOperationApply entity) {
        long sheets = bizOperationCountMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizOperationCount>()
                        .eq(BizOperationCount::getApplyId, entity.getId()));
        if (sheets == 0) {
            return;
        }
        if (bizOperationCountMapper.countDiscrepancy(entity.getId()) > 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的器械清点存在未处理的差异，不能登记手术完成 —— "
                    + "先把差异查清并在清点单上写明处理结果（这一条是异物遗留唯一能在关腔前发现的机制）");
        }
        if (bizOperationCountMapper.countUnfinished(entity.getId()) > 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的器械清点尚未走完三轮（术前 → 关体前 → 关体后），不能登记手术完成");
        }
    }

    /**
     * 三方安全核查闸门（P134.2）。
     *
     * <p>与 {@code guardCount} 同一条理由："手术完成"是这台手术对外生效的那一刻；
     * 核查签了两轮就登记完成，意味着麻醉实施前/术后离室那一次核对根本没做，
     * 而单子上却留着看起来走过流程的签名 —— 这比没签更糟。
     */
    private void guardSafetyCheck(BizOperationApply entity) {
        long phases = bizOperationSafetyCheckMapper.countPhases(entity.getId());
        if (phases > 0 && phases < SafetyCheckItems.ALL_PHASES.size()) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的安全核查只签了 " + phases + " 轮（共 3 轮：麻醉实施前 → 手术开始前 → 患者离开手术室前），"
                    + "签过就要签满，否则不能登记手术完成");
        }
    }

    /**
     * 首页手术明细的"手术依据"：四核对里"编码有没有病历支持"的那一段。
     */
    private String buildOperationBasis(OperationFinishDTO dto) {
        String basis = "手术记录：术中所见 "
                + dto.getIntraopFindings() + "；手术经过 " + dto.getIntraopProcedure();
        return basis.length() > 490 ? basis.substring(0, 490) + "…" : basis;
    }

    private void decorate(OperationApplyVO vo) {
        vo.setOperationStatusText(OperationApplyStatusEnum.getLabel(vo.getOperationStatus()));
        vo.setOperationLevelText(dictCacheService.getDicDataLabel(DictTypeConst.OPERATION_LEVEL, vo.getOperationLevel()));
        vo.setIncisionLevelText(dictCacheService.getDicDataLabel(DictTypeConst.INCISION_LEVEL, vo.getIncisionLevel()));
        vo.setAnesthesiaTypeText(OperationAnesthesiaMethodEnum.getLabel(vo.getAnesthesiaType()));
        vo.setIsEmergencyText(YesOrNoEnum.getLabel(vo.getIsEmergency()));
        vo.setIsMainText(vo.getIsMain() == null ? "—" : (vo.getIsMain() == 1 ? "主要手术" : "次要手术"));
        vo.setGenderText(SysGenderEnum.getLabel(vo.getGender()));
        vo.setAdmitStatusText(AdmitStatusEnum.getLabel(vo.getAdmitStatus()));
        vo.setPreopCheckItemsText(OperationCheckItems.summaryText(vo.getPreopCheckItems()));
        vo.setCheckItemOptions(checkItems());

        boolean pending = Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), vo.getOperationStatus());
        boolean scheduled = Objects.equals(OperationApplyStatusEnum.SCHEDULED.getCode(), vo.getOperationStatus());
        boolean checked = Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), vo.getOperationStatus());
        boolean finished = Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), vo.getOperationStatus());
        boolean cancelled = Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), vo.getOperationStatus());

        vo.setCanEdit(pending);
        vo.setCanSchedule(pending || scheduled);
        vo.setCanPreopCheck(scheduled);
        vo.setCanFinish(checked);
        vo.setCanCancel(pending || scheduled);

        if (vo.getPlannedStartTime() != null && vo.getPlannedEndTime() != null) {
            vo.setPlannedTimeText(vo.getPlannedStartTime().format(DateFormats.DATETIME) + " ~ "
                    + vo.getPlannedEndTime().format(DateFormats.TIME_MINUTE));
        }
        if (vo.getOperationStartTime() != null && vo.getOperationEndTime() != null) {
            long minutes = Math.max(0, Duration.between(
                    vo.getOperationStartTime(), vo.getOperationEndTime()).toMinutes());
            vo.setDurationMinutes(minutes);
            vo.setDurationText(AnesthesiaCalcs.durationText(minutes));
        }
        vo.setWaitText(waitText(vo, pending, scheduled, checked));

        LocalDateTime now = TimeUtil.nowSeconds();
        boolean stalled = checked && vo.getPreopCheckTime() != null
                && now.isAfter(vo.getPreopCheckTime().plusHours(STALLED_HOURS));
        vo.setStalled(stalled);
        if (stalled) {
            vo.setStalledText("术前核对完成已超过 " + STALLED_HOURS + " 小时仍未登记手术完成");
        }
        if (finished && (vo.getOperationId() == null || vo.getRecordId() == null)) {
            // 链断了的假数据：状态"已完成"却没有首页明细/病历锚点。必须能看见，不允许静默。
            vo.setStalled(true);
            vo.setStalledText("已完成但回写链不完整（首页明细ID/病历ID 缺失），请核查");
        }
        if (cancelled) {
            vo.setWaitText(null);
        }
    }

    private String waitText(OperationApplyVO vo, boolean pending, boolean scheduled, boolean checked) {
        LocalDateTime now = TimeUtil.nowSeconds();
        if (pending && vo.getApplyTime() != null) {
            long m = Math.max(0, Duration.between(vo.getApplyTime(), now).toMinutes());
            return "申请后已等待 " + AnesthesiaCalcs.durationText(m) + " 未排台";
        }
        if (scheduled && vo.getPlannedStartTime() != null) {
            if (now.isBefore(vo.getPlannedStartTime())) {
                return "距计划开始 " + AnesthesiaCalcs.durationText(
                        Duration.between(now, vo.getPlannedStartTime()).toMinutes());
            }
            return "已过计划开始时间 " + AnesthesiaCalcs.durationText(
                    Duration.between(vo.getPlannedStartTime(), now).toMinutes());
        }
        if (checked && vo.getPreopCheckTime() != null) {
            return "核对完成已 " + AnesthesiaCalcs.durationText(
                    Duration.between(vo.getPreopCheckTime(), now).toMinutes()) + "，尚未结束";
        }
        return null;
    }

    private BizOperationApply mustGet(Long applyId) {
        BizOperationApply entity = bizOperationApplyMapper.selectById(applyId);
        if (entity == null) {
            throw new BusinessException("手术申请单不存在");
        }
        return entity;
    }

    /**
     * 科室名（取不到就返回原文案"未知科室(ID=x)"，绝不编一个科室名）
     */
    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectDeptName(deptId);
        return TextUtil.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String wardNameOf(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = patientFacadeService.getWard(wardId);
        return ward == null ? "未知病区(ID=" + wardId + ")" : ward.getWardName();
    }

    private String bedNoOf(Long bedId) {
        return patientFacadeService.getBedNo(bedId);
    }

    /**
     * 员工姓名（服务端查名，不信任前端传来的姓名 —— 姓名是可以随便伪造的字符串）
     */
    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectEmployeeName(empId);
        return TextUtil.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    /**
     * 跨手术间人力冲突校验：同一人（主刀/麻醉/巡回/洗手护士）目标时段已排在其他手术间则拒绝。
     *
     * <p>同手术间冲突拦得住"资源"，拦不住"人"——两台在不同手术间各自合法，
     * 但同一人同时跟两台是事故级错误（医生开两台、护士跟两台都不成立）。
     * 端点相接不算冲突（连台是常态）。命中台以本人实际担任的角色出文案。
     */
    private void assertNoPersonConflict(Long employeeId, String roleLabel, String empName,
                                        LocalDateTime start, LocalDateTime end, Long excludeId) {
        List<BizOperationApply> hits = bizOperationApplyMapper.selectPersonConflicts(employeeId, start, end, excludeId);
        if (hits.isEmpty()) {
            return;
        }
        BizOperationApply c = hits.get(0);
        String roleInHit = personRoleLabel(c, employeeId);
        throw new BusinessException(roleLabel + " " + empName + " 在「" + c.getOperationRoom() + "」"
                + c.getPlannedStartTime().format(DateFormats.TIME_MINUTE) + "~" + c.getPlannedEndTime().format(DateFormats.TIME_MINUTE)
                + " 已排 " + c.getApplyNo() + "（" + c.getPatientName() + " " + c.getPlannedOperationName()
                + "，" + roleInHit + "），同一时段不能分身两台，请先处理该台或改时段");
    }

    /**
     * 命中冲突台里，employeeId 是以什么身份被占用的（按四列逐一比对，主刀优先于麻醉优先于巡回优先于洗手）。
     */
    private String personRoleLabel(BizOperationApply row, Long employeeId) {
        if (row.getSurgeonId() != null && row.getSurgeonId().equals(employeeId)) {
            return "主刀";
        }
        if (row.getAnesthetistId() != null && row.getAnesthetistId().equals(employeeId)) {
            return "麻醉";
        }
        if (row.getCirculatingNurseId() != null && row.getCirculatingNurseId().equals(employeeId)) {
            return "巡回护士";
        }
        if (row.getScrubNurseId() != null && row.getScrubNurseId().equals(employeeId)) {
            return "洗手护士";
        }
        return "跟台人员";
    }

    private String nextApplyNo() {
        return redisSequenceService.generateOperationApplyNo();
    }

    /**
     * 技术授权准入闸：择期拒单，急诊由授权服务写越权登记后放行（同一事务，越权登记与手术单同生共死）。
     *
     * <p>拒单信息一定带上「谁是这个角色、要求几级」，否则收费台/手术室只能反查是谁被谁拦了。
     */
    private void gateTechAuth(Long employeeId, int authCategory, Integer requiredLevel,
                              BizOperationApply entity, String roleName, String personName) {
        TechAuthGateDTO gate = new TechAuthGateDTO();
        gate.setEmployeeId(employeeId);
        gate.setAuthCategory(authCategory);
        gate.setRequiredLevel(requiredLevel);
        gate.setItemCode(entity.getPlannedOperationCode());
        gate.setEmergency(Objects.equals(1, entity.getIsEmergency()));
        gate.setSourceType(TechOverrideSourceEnum.OPERATION_APPLY.getCode());
        gate.setSourceId(entity.getId());
        gate.setSourceNo(entity.getApplyNo());
        gate.setReason(roleName + "「" + textOr(personName, "未指名") + "」在急诊手术 " + entity.getApplyNo()
                + "（" + entity.getPlannedOperationName() + "）上越权，该手术要求 " + requiredLevel + " 级授权");
        try {
            employeeTechAuthService.gate(gate);
        } catch (BusinessException e) {
            throw new BusinessException(roleName + "（" + textOr(personName, "未指名") + "）" + e.getMessage());
        }
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
    private record Parsed(String from, String to) {
    }
}