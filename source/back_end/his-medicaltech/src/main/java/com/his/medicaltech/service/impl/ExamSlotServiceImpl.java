package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictTypeConst;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamAppointment;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.entity.BizExamSlot;
import com.his.medicaltech.mapper.BizExamAppointmentMapper;
import com.his.medicaltech.mapper.BizExamDeviceMapper;
import com.his.medicaltech.mapper.BizExamSlotMapper;
import com.his.medicaltech.service.ExamSlotService;
import com.his.medicaltech.support.ExamGrid;
import com.his.medicaltech.vo.ExamApptVO;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 检查号源服务：按设备开放时段生成分时段格子、看板、锁号、对账，以及占号/退号的原子动作。
 */
@Service
@RequiredArgsConstructor
public class ExamSlotServiceImpl extends ServiceImpl<BizExamSlotMapper, BizExamSlot> implements ExamSlotService {

    private static final int SLOT_LOCKED = 0;
    private static final int SLOT_OPEN = 1;
    private static final int RECALC_DETAIL_LIMIT = 50;

    private final BizExamDeviceMapper bizExamDeviceMapper;
    private final BizExamSlotMapper bizExamSlotMapper;
    private final BizExamAppointmentMapper bizExamAppointmentMapper;
    private final DictCacheService dictCacheService;
    private final DeptScopeService deptScopeService;

    // 生成 / 看板

    private static boolean overlaps(BizExamSlot cell, BizExamAppointment appt) {
        if (appt.getStartTime() == null || appt.getEndTime() == null) {
            return false;
        }
        int cs = ExamGrid.toMin(cell.getStartTime());
        int ce = ExamGrid.toMin(cell.getEndTime());
        int as = ExamGrid.toMin(appt.getStartTime());
        int ae = ExamGrid.toMin(appt.getEndTime());
        return as < ce && ae > cs;
    }

    private static boolean isPast(BizExamSlot cell, LocalDate date, LocalDateTime now) {
        if (date.isBefore(now.toLocalDate())) {
            return true;
        }
        if (!date.isEqual(now.toLocalDate())) {
            return false;
        }
        return LocalTime.parse(cell.getEndTime()).isBefore(now.toLocalTime());
    }

    /**
     * 生成（补齐）号源：可连生成多天，幂等
     */
    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.SlotEnsureVO ensureSlots(ExamApptDTO.SlotEnsure dto) {
        BizExamDevice device = bizExamDeviceMapper.selectForUpdate(dto.getDeviceId());
        if (device == null) {
            throw new BusinessException("预约设备不存在：" + dto.getDeviceId());
        }
        // B类收口：只能为本岗位授权科室的设备生成号源（全院角色不受限）
        if (device.getDeptId() != null) {
            deptScopeService.resolveDeptId(device.getDeptId());
        }
        int days = dto.getDays() == null ? 1 : dto.getDays();
        if (days < 1 || days > 31) {
            throw new BusinessException("一次生成的天数应在 1~31 之间，当前：" + days);
        }
        LocalDate start = dto.getStartDate();
        LocalDate today = LocalDate.now();
        if (start.isBefore(today)) {
            throw new BusinessException("不能为已过去的日期生成号源：" + start);
        }
        if (start.isAfter(today.plusDays(device.getAheadDays()))) {
            throw new BusinessException("设备「" + device.getDeviceName() + "」可提前预约 " + device.getAheadDays()
                    + " 天，" + start + " 超出可排范围");
        }
        int created = 0;
        int existing = 0;
        int total = 0;
        for (int i = 0; i < days; i++) {
            int[] r = ensureDayLocked(device, start.plusDays(i));
            created += r[0];
            existing += r[1];
            total += r[2];
        }
        ExamApptVO.SlotEnsureVO vo = new ExamApptVO.SlotEnsureVO();
        vo.setCreated(created);
        vo.setExisting(existing);
        vo.setTotalSlots(total);
        vo.setMessage("设备「" + device.getDeviceName() + "」" + start + " 起 " + days + " 天：新增 "
                + created + " 格，已有 " + existing + " 格保持不变");
        return vo;
    }

    /**
     * 号源看板：格子计数 + 实际占号者（谁占了这一格要能指认出来）
     */
    public ExamApptVO.SlotBoardVO board(ExamApptDTO.SlotQuery dto) {
        BizExamDevice device = bizExamDeviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new BusinessException("预约设备不存在：" + dto.getDeviceId());
        }
        if (device.getDeptId() != null) {
            deptScopeService.resolveDeptId(device.getDeptId());
        }
        List<BizExamSlot> cells = currentGridCells(device, dto.getSlotDate());
        List<BizExamAppointment> occupants = bizExamAppointmentMapper.selectOccupants(device.getId(), dto.getSlotDate());
        LocalDateTime now = LocalDateTime.now();

        ExamApptVO.SlotBoardVO vo = new ExamApptVO.SlotBoardVO();
        vo.setDeviceId(device.getId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeptName(device.getDeptName());
        vo.setRoomName(device.getRoomName());
        vo.setDeviceStatus(device.getStatus());
        vo.setDeviceStatusText(dictCacheService.getDicDataLabel(DictTypeConst.EXAM_DEVICE_STATUS, device.getStatus()));
        vo.setSlotDate(dto.getSlotDate());
        vo.setSlotMinutes(device.getSlotMinutes());
        vo.setParallelCount(device.getParallelCount());

        List<ExamApptVO.SlotCellVO> out = new ArrayList<>();
        int free = 0;
        int usedFull = 0;
        int locked = 0;
        for (BizExamSlot cell : cells) {
            ExamApptVO.SlotCellVO c = new ExamApptVO.SlotCellVO();
            c.setSlotId(cell.getId());
            c.setSeq(cell.getSeq());
            c.setStartTime(cell.getStartTime());
            c.setEndTime(cell.getEndTime());
            c.setTotalSource(cell.getTotalSource());
            c.setUsedSource(cell.getUsedSource());
            c.setAvailableSource(cell.getAvailableSource());
            c.setStatus(cell.getStatus());
            List<String> nos = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (BizExamAppointment a : occupants) {
                if (overlaps(cell, a)) {
                    nos.add(a.getApptNo() + "(" + dictCacheService.getDicDataLabel(DictTypeConst.EXAM_APPOINT_STATUS, a.getStatus()) + ")");
                    names.add(a.getPatientName());
                }
            }
            c.setOccupyApptNos(nos);
            c.setOccupyPatientNames(names);
            c.setPast(isPast(cell, dto.getSlotDate(), now));
            if (cell.getStatus() != null && cell.getStatus() == SLOT_LOCKED) {
                locked++;
            } else if (cell.getAvailableSource() != null && cell.getAvailableSource() > 0 && !c.getPast()) {
                free++;
            } else {
                usedFull++;
            }
            out.add(c);
        }
        vo.setTotalSlots(out.size());
        vo.setFreeSlots(free);
        vo.setUsedSlots(usedFull);
        vo.setLockedSlots(locked);
        vo.setSlots(out);
        return vo;
    }

    // 占号 / 退号（供预约服务在同一事务内调用）

    /**
     * 锁号 / 放号：格子里还有人占着就不许锁
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggle(ExamApptDTO.SlotToggle dto) {
        if (dto.getStatus() == null || (dto.getStatus() != SLOT_LOCKED && dto.getStatus() != SLOT_OPEN)) {
            throw new BusinessException("号源状态只能是 0-锁号 或 1-正常");
        }
        BizExamSlot cell = bizExamSlotMapper.selectById(dto.getSlotId());
        if (cell == null) {
            throw new BusinessException("号源格子不存在：" + dto.getSlotId());
        }
        BizExamDevice device = bizExamDeviceMapper.selectForUpdate(cell.getDeviceId());
        if (device == null) {
            throw new BusinessException("预约设备不存在：" + cell.getDeviceId());
        }
        if (device.getDeptId() != null) {
            deptScopeService.resolveDeptId(device.getDeptId());
        }
        ensureDayLocked(device, cell.getSlotDate());
        List<BizExamSlot> cells = bizExamSlotMapper.selectDayForUpdate(device.getId(), cell.getSlotDate());
        BizExamSlot target = cells.stream().filter(x -> x.getId().equals(dto.getSlotId())).findFirst()
                .orElseThrow(() -> new BusinessException("号源格子不属于该设备该日，请刷新后重试"));
        if (dto.getStatus() == SLOT_LOCKED && target.getUsedSource() != null && target.getUsedSource() > 0) {
            throw new BusinessException("该时段已有 " + target.getUsedSource() + " 个占号，请先改约或取消再锁号");
        }
        int total = device.getParallelCount();
        int used = target.getUsedSource() == null ? 0 : target.getUsedSource();
        BizExamSlot update = new BizExamSlot();
        update.setId(target.getId());
        update.setStatus(dto.getStatus());
        update.setAvailableSource(dto.getStatus() == SLOT_LOCKED ? 0 : Math.max(0, total - used));
        bizExamSlotMapper.updateById(update);
    }

    /**
     * 号源对账：以预约单为事实独立复算 used_source，报漂移并修正
     */
    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.SlotRecalcVO recalc(ExamApptDTO.SlotRecalc dto) {
        if (dto.getDateTo().isBefore(dto.getDateFrom())) {
            throw new BusinessException("对账截止日期不得早于起始日期");
        }
        if (dto.getDateFrom().plusDays(92).isBefore(dto.getDateTo())) {
            throw new BusinessException("一次对账的日期跨度不得超过 92 天");
        }
        List<BizExamDevice> devices;
        if (dto.getDeviceId() == null) {
            // 只对账本岗位授权科室的设备（设备未绑科室的不拦）
            devices = bizExamDeviceMapper.selectList(new LambdaQueryWrapper<BizExamDevice>()
                            .orderByAsc(BizExamDevice::getDeviceCode)).stream()
                    .filter(d -> d.getDeptId() == null || deptScopeService.canAccessDept(d.getDeptId()))
                    .toList();
        } else {
            BizExamDevice one = requireDevice(dto.getDeviceId());
            if (one.getDeptId() != null) {
                deptScopeService.resolveDeptId(one.getDeptId());
            }
            devices = List.of(one);
        }

        int checked = 0;
        List<ExamApptVO.SlotDriftVO> drifts = new ArrayList<>();
        for (BizExamDevice device : devices) {
            // 与占号同一把设备行锁 + 同日格子锁：对账要改计数，不锁就等于和预约互相踩
            BizExamDevice locked = bizExamDeviceMapper.selectForUpdate(device.getId());
            if (locked == null) {
                continue;
            }
            List<BizExamAppointment> appts = bizExamAppointmentMapper
                    .selectRangeOccupants(locked.getId(), dto.getDateFrom(), dto.getDateTo());
            Map<LocalDate, List<BizExamAppointment>> byDate = new HashMap<>();
            for (BizExamAppointment a : appts) {
                byDate.computeIfAbsent(a.getExamDate(), k -> new ArrayList<>()).add(a);
            }
            for (LocalDate date = dto.getDateFrom(); !date.isAfter(dto.getDateTo()); date = date.plusDays(1)) {
                List<BizExamSlot> cells = lockedGridCells(locked, date);
                if (cells.isEmpty()) {
                    continue;
                }
                List<BizExamAppointment> occupants = byDate.getOrDefault(date, List.of());
                for (BizExamSlot cell : cells) {
                    checked++;
                    int actual = (int) occupants.stream().filter(a -> overlaps(cell, a)).count();
                    int before = cell.getUsedSource() == null ? 0 : cell.getUsedSource();
                    if (actual == before) {
                        continue;
                    }
                    int total = cell.getTotalSource() == null ? actual : Math.max(cell.getTotalSource(), actual);
                    int available = cell.getStatus() != null && cell.getStatus() == SLOT_LOCKED
                            ? 0 : Math.max(0, total - actual);
                    BizExamSlot fix = new BizExamSlot();
                    fix.setId(cell.getId());
                    fix.setUsedSource(actual);
                    fix.setAvailableSource(available);
                    if (total != (cell.getTotalSource() == null ? 0 : cell.getTotalSource())) {
                        fix.setTotalSource(total);
                    }
                    bizExamSlotMapper.updateById(fix);
                    if (drifts.size() < RECALC_DETAIL_LIMIT) {
                        ExamApptVO.SlotDriftVO d = new ExamApptVO.SlotDriftVO();
                        d.setSlotId(cell.getId());
                        d.setDeviceId(device.getId());
                        d.setDeviceName(device.getDeviceName());
                        d.setSlotDate(date);
                        d.setStartTime(cell.getStartTime());
                        d.setUsedBefore(before);
                        d.setUsedActual(actual);
                        drifts.add(d);
                    }
                }
            }
        }
        ExamApptVO.SlotRecalcVO vo = new ExamApptVO.SlotRecalcVO();
        vo.setChecked(checked);
        vo.setDrifted(drifts.size());
        vo.setDetails(drifts);
        vo.setMessage("复核 " + checked + " 个格子，" + (drifts.isEmpty()
                ? "计数与预约单完全一致"
                : "发现并修正 " + drifts.size() + " 个漂移格子"
                + (drifts.size() >= RECALC_DETAIL_LIMIT ? "（明细仅展示前 " + RECALC_DETAIL_LIMIT + " 条）" : "")));
        return vo;
    }

    /**
     * 锁设备 → 幂等补齐格子 → 返回按 start_time 升序的当日格子（仅当前开放窗口内的）
     */
    public List<BizExamSlot> ensureLockedDay(BizExamDevice device, LocalDate date) {
        ensureDayLocked(device, date);
        List<BizExamSlot> rows = bizExamSlotMapper.selectDayForUpdate(device.getId(), date);
        Map<String, BizExamSlot> byStart = new LinkedHashMap<>();
        for (BizExamSlot row : rows) {
            byStart.put(row.getStartTime(), row);
        }
        List<BizExamSlot> out = new ArrayList<>();
        for (int[] g : ExamGrid.daySlots(device)) {
            BizExamSlot cell = byStart.get(ExamGrid.toHHmm(g[0]));
            if (cell != null) {
                out.add(cell);
            }
        }
        return out;
    }

    /**
     * 逐格 +1 占号；expectUsed 兜底，锁没生效时宁可抛错也不静默写歪
     */
    public void claim(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span) {
        write(device, date, cells, span, 1);
    }

    /**
     * 逐格 -1 退号（取消/爽约）
     */
    public void release(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span) {
        write(device, date, cells, span, -1);
    }

    // 内部

    /**
     * 占用区间覆盖的格子集合（拒绝跨过午休断档与开放窗口之外的请求）
     */
    public int[] spanOf(List<BizExamSlot> cells, int startMin, int endMin) {
        List<int[]> grid = new ArrayList<>();
        for (BizExamSlot c : cells) {
            grid.add(new int[]{ExamGrid.toMin(c.getStartTime()), ExamGrid.toMin(c.getEndTime())});
        }
        int[] span = ExamGrid.spanOf(grid, startMin, endMin);
        if (span == null) {
            throw new BusinessException("所选时段与号源网格不匹配：开始时刻必须正好落在一个格子的起点，"
                    + "且整个检查区间不得跨过午休或超出该设备开放窗口");
        }
        return span;
    }

    public BizExamDevice requireDevice(Long deviceId) {
        BizExamDevice device = deviceId == null ? null : bizExamDeviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("预约设备不存在：" + deviceId);
        }
        return device;
    }

    private void write(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span, int delta) {
        for (int i = span[0]; i <= span[1]; i++) {
            BizExamSlot cell = cells.get(i);
            int used = cell.getUsedSource() == null ? 0 : cell.getUsedSource();
            int total = cell.getTotalSource() == null ? device.getParallelCount() : cell.getTotalSource();
            int next = Math.max(0, used + delta);
            if (delta > 0 && next > total) {
                throw new BusinessException("号源不足：" + date + " " + cell.getStartTime() + "-"
                        + cell.getEndTime() + " 已约满（" + used + "/" + total + "）");
            }
            int available = cell.getStatus() != null && cell.getStatus() == SLOT_LOCKED
                    ? 0 : Math.max(0, total - next);
            if (bizExamSlotMapper.updateUsed(cell.getId(), used, next, available) == 0) {
                throw new BusinessException("号源已被并发修改，请重试：" + date + " " + cell.getStartTime());
            }
            cell.setUsedSource(next);
            cell.setAvailableSource(available);
        }
    }

    /**
     * 返回 [本次新增, 原有, 当日应有格子数]
     */
    private int[] ensureDayLocked(BizExamDevice device, LocalDate date) {
        List<int[]> grid = ExamGrid.daySlots(device);
        List<BizExamSlot> rows = bizExamSlotMapper.selectDayForUpdate(device.getId(), date);
        Map<String, BizExamSlot> byStart = new HashMap<>();
        for (BizExamSlot row : rows) {
            byStart.put(row.getStartTime(), row);
        }
        int created = 0;
        int existing = 0;
        int seq = 1;
        for (int[] g : grid) {
            String start = ExamGrid.toHHmm(g[0]);
            String end = ExamGrid.toHHmm(g[1]);
            BizExamSlot cell = byStart.get(start);
            if (cell == null) {
                BizExamSlot insert = new BizExamSlot();
                insert.setDeviceId(device.getId());
                insert.setSlotDate(date);
                insert.setSeq(seq);
                insert.setStartTime(start);
                insert.setEndTime(end);
                insert.setTotalSource(device.getParallelCount());
                insert.setUsedSource(0);
                insert.setAvailableSource(device.getParallelCount());
                insert.setStatus(SLOT_OPEN);
                bizExamSlotMapper.insert(insert);
                created++;
            } else {
                existing++;
                alignCapacity(device, cell, end);
            }
            seq++;
        }
        return new int[]{created, existing, grid.size()};
    }

    /**
     * 设备改了并行数或格子尾点时，把已有格子对齐到新口径：
     * 只放宽不收紧 —— 容量调小时不删已约的号（宁可超卖也不静默把患者挪走），
     * 收紧的活儿由人工在号源台上锁号完成。
     */
    private void alignCapacity(BizExamDevice device, BizExamSlot cell, String newEnd) {
        int used = cell.getUsedSource() == null ? 0 : cell.getUsedSource();
        int target = device.getParallelCount();
        boolean endChanged = !newEnd.equals(cell.getEndTime());
        boolean capacityChanged = cell.getTotalSource() == null || cell.getTotalSource() < target;
        if (!endChanged && !capacityChanged) {
            return;
        }
        BizExamSlot update = new BizExamSlot();
        update.setId(cell.getId());
        if (endChanged) {
            update.setEndTime(newEnd);
            cell.setEndTime(newEnd);
        }
        if (capacityChanged) {
            update.setTotalSource(target);
            update.setAvailableSource(cell.getStatus() != null && cell.getStatus() == SLOT_LOCKED
                    ? 0 : Math.max(0, target - used));
            cell.setTotalSource(target);
        }
        bizExamSlotMapper.updateById(update);
    }

    private List<BizExamSlot> currentGridCells(BizExamDevice device, LocalDate date) {
        return filterGrid(device, bizExamSlotMapper.selectList(new LambdaQueryWrapper<BizExamSlot>()
                .eq(BizExamSlot::getDeviceId, device.getId())
                .eq(BizExamSlot::getSlotDate, date)
                .orderByAsc(BizExamSlot::getStartTime)));
    }

    private List<BizExamSlot> lockedGridCells(BizExamDevice device, LocalDate date) {
        return filterGrid(device, bizExamSlotMapper.selectDayForUpdate(device.getId(), date));
    }

    /**
     * 只保留仍落在当前开放窗口内的格子（改过开放时间后，窗口外的历史格子不再出现在看板上）
     */
    private List<BizExamSlot> filterGrid(BizExamDevice device, List<BizExamSlot> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        java.util.Set<String> starts = new java.util.HashSet<>();
        for (int[] g : ExamGrid.daySlots(device)) {
            starts.add(ExamGrid.toHHmm(g[0]));
        }
        return rows.stream().filter(r -> starts.contains(r.getStartTime())).toList();
    }
}
