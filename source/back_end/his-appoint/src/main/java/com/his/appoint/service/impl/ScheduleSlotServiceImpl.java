package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.ScheduleSlotItemUpsertDTO;
import com.his.appoint.dto.ScheduleSlotUpsertDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.entity.BizScheduleSlot;
import com.his.appoint.entity.BizScheduleSlotTemplate;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.mapper.BizScheduleSlotMapper;
import com.his.appoint.service.ScheduleSlotService;
import com.his.appoint.vo.ScheduleSlotVO;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.ScheduleStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 排班时间片段服务实现。
 *
 * <p>分配规则（设计定稿 2026-09-21）：均分，余数给前面的段
 * （23 个号切 8 段 → 前 7 段各 3、末段 2）；预约池同理，段内预留不得超过段号源。
 */
@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl extends ServiceImpl<BizScheduleSlotMapper, BizScheduleSlot>
        implements ScheduleSlotService {

    private final BizScheduleSlotMapper slotMapper;
    private final BizScheduleMapper scheduleMapper;

    @Override
    public List<BizScheduleSlot> generateSlots(Long scheduleId, String startTime, String endTime,
                                               Integer totalSource, Integer appointmentSource) {
        List<String[]> segs = splitHalfHour(startTime, endTime);
        int n = segs.size();
        int total = totalSource == null ? 0 : totalSource;
        int appt = appointmentSource == null ? 0 : appointmentSource;
        if (appt > total) {
            throw new BusinessException("预约号源数不能大于号源总数");
        }
        int[] totals = splitEven(total, n);
        int[] appts = clampSpread(splitEven(appt, n), totals);

        List<BizScheduleSlot> slots = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            BizScheduleSlot slot = new BizScheduleSlot();
            slot.setScheduleId(scheduleId);
            slot.setSeq(i + 1);
            slot.setStartTime(segs.get(i)[0]);
            slot.setEndTime(segs.get(i)[1]);
            slot.setTotalSource(totals[i]);
            slot.setUsedSource(0);
            slot.setAvailableSource(totals[i]);
            slot.setAddedSource(0);
            slot.setAppointmentSource(appts[i]);
            slot.setUsedAppointmentSource(0);
            slot.setStatus(1);
            this.save(slot);
            slots.add(slot);
        }
        syncSumToSchedule(scheduleId);
        return slots;
    }

    @Override
    public List<BizScheduleSlot> generateFromTemplate(Long scheduleId, List<BizScheduleSlotTemplate> tplSlots) {
        List<BizScheduleSlot> slots = new ArrayList<>(tplSlots.size());
        int seq = 1;
        for (BizScheduleSlotTemplate tpl : tplSlots) {
            BizScheduleSlot slot = new BizScheduleSlot();
            slot.setScheduleId(scheduleId);
            slot.setSeq(seq++);
            slot.setStartTime(tpl.getStartTime());
            slot.setEndTime(tpl.getEndTime());
            slot.setTotalSource(nz(tpl.getTotalSource()));
            slot.setUsedSource(0);
            slot.setAvailableSource(nz(tpl.getTotalSource()));
            slot.setAddedSource(0);
            slot.setAppointmentSource(nz(tpl.getAppointmentSource()));
            slot.setUsedAppointmentSource(0);
            slot.setStatus(1);
            this.save(slot);
            slots.add(slot);
        }
        syncSumToSchedule(scheduleId);
        return slots;
    }

    @Override
    public void regenerateForSchedule(BizSchedule schedule, Integer newTotal, Integer newAppointment) {
        Long scheduleId = schedule.getId();
        List<BizScheduleSlot> old = listByScheduleId(scheduleId);
        int usedSum = old.stream().mapToInt(s -> nz(s.getUsedSource())).sum();

        List<String[]> newSegs = splitHalfHour(schedule.getStartTime(), schedule.getEndTime());
        boolean sameWindow = old.size() == newSegs.size();
        if (sameWindow) {
            for (int i = 0; i < old.size(); i++) {
                if (!old.get(i).getStartTime().equals(newSegs.get(i)[0])
                        || !old.get(i).getEndTime().equals(newSegs.get(i)[1])) {
                    sameWindow = false;
                    break;
                }
            }
        }

        if (!sameWindow) {
            // 时间窗变化：段边界是挂号快照（slot_start/slot_end）的语义来源，
            // 有挂号时改窗等于篡改已挂号的时段事实，必须先退号。
            if (usedSum > 0) {
                throw new BusinessException("该班次已有挂号记录，不能修改就诊时段；请先退号或另建排班");
            }
            physicalDeleteByScheduleId(scheduleId);
            generateSlots(scheduleId, schedule.getStartTime(), schedule.getEndTime(), newTotal, newAppointment);
            return;
        }

        // 窗口没变：重摊号源。used 是事实（挂号的段分布），不能动——
        // 先按均分+余数给前面分配，再把 used 超过分配值的段压到 used、差额向后面有空余的段顺延。
        // Σ约束：主表已校验 newTotal ≥ Σused，顺延必然有解；解完后 Σ段可能大于入参 newTotal
        // （used 保底挤压所致），Σ段写回主表为准——主表 total 以段的事实收口。
        int n = old.size();
        int total = newTotal == null ? 0 : newTotal;
        int appt = newAppointment == null ? 0 : newAppointment;
        if (appt > total) {
            throw new BusinessException("预约号源数不能大于号源总数");
        }
        int[] totals = splitEven(total, n);
        int[] usedArr = new int[n];
        for (int i = 0; i < n; i++) {
            usedArr[i] = nz(old.get(i).getUsedSource());
        }
        int deficit = 0;
        for (int i = 0; i < n; i++) {
            if (totals[i] < usedArr[i]) {
                deficit += usedArr[i] - totals[i];
                totals[i] = usedArr[i];
            }
        }
        for (int j = n - 1; j >= 0 && deficit > 0; j--) {
            int room = totals[j] - usedArr[j];
            int move = Math.min(room, deficit);
            totals[j] -= move;
            deficit -= move;
        }
        if (deficit > 0) {
            throw new BusinessException("号源数量不能小于已使用数量");
        }
        int[] appts = clampSpread(splitEven(appt, n), totals);

        for (int i = 0; i < n; i++) {
            BizScheduleSlot up = new BizScheduleSlot();
            up.setId(old.get(i).getId());
            up.setTotalSource(totals[i]);
            up.setAvailableSource(totals[i] - usedArr[i]);
            up.setAppointmentSource(appts[i]);
            slotMapper.updateById(up);
        }
        syncSumToSchedule(scheduleId);
    }

    @Override
    public void spreadAddSource(Long scheduleId, int addNum) {
        if (addNum <= 0) {
            return;
        }
        List<BizScheduleSlot> slots = listByScheduleId(scheduleId);
        if (slots.isEmpty()) {
            // 无段的旧排班（历史数据）：加号只走主表，调用方已更新主表，此处无事可做
            return;
        }
        int n = slots.size();
        int[] add = splitEven(addNum, n);
        for (int i = 0; i < n; i++) {
            if (add[i] == 0) {
                continue;
            }
            BizScheduleSlot s = slots.get(i);
            BizScheduleSlot up = new BizScheduleSlot();
            up.setId(s.getId());
            up.setTotalSource(nz(s.getTotalSource()) + add[i]);
            up.setAvailableSource(nz(s.getAvailableSource()) + add[i]);
            up.setAddedSource(nz(s.getAddedSource()) + add[i]);
            slotMapper.updateById(up);
        }
        syncSumToSchedule(scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlotSources(ScheduleSlotUpsertDTO dto) {
        BizSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过期的班次不允许调整号源");
        }
        if (ScheduleStatusEnum.stopped(schedule.getStatus())) {
            throw new BusinessException("停诊中的班次不允许调整号源，请先启用");
        }
        // 段号源只对医生出诊班有意义：出勤岗没有号源池（sql/195），与加号同一道闸
        if (!StaffTypeEnum.hasSource(schedule.getStaffType())) {
            throw new BusinessException("只有医生出诊排班能调整号源："
                    + StaffTypeEnum.getText(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
        }

        Map<Long, BizScheduleSlot> byId = new HashMap<>();
        for (BizScheduleSlot s : listByScheduleId(dto.getScheduleId())) {
            byId.put(s.getId(), s);
        }
        // 整批校验整批生效：部分成功会打破「Σ段=主表」的总量约束，中途状态比全拒绝更糟
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizScheduleSlot slot = byId.get(item.getId());
            if (slot == null) {
                throw new BusinessException("时间段不存在或不属于该排班");
            }
            if (EnableStatusEnum.fromCode(item.getStatus()) == null) {
                throw new BusinessException("段状态只允许 0-停用 / 1-正常");
            }
            int used = nz(slot.getUsedSource());
            if (item.getTotalSource() < used) {
                throw new BusinessException(slot.getStartTime() + " 段号源不能小于已挂号数（" + used + "）");
            }
            int appt = item.getAppointmentSource() == null ? 0 : item.getAppointmentSource();
            if (appt > item.getTotalSource()) {
                throw new BusinessException(slot.getStartTime() + " 段预约预留不能大于段号源");
            }
            int usedAppt = nz(slot.getUsedAppointmentSource());
            if (appt < usedAppt) {
                throw new BusinessException(slot.getStartTime() + " 段预约预留不能小于预约已用（" + usedAppt + "）");
            }
        }
        // 逐段落库：available 同步重算（used 是已发生的事实，不动）
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizScheduleSlot slot = byId.get(item.getId());
            BizScheduleSlot up = new BizScheduleSlot();
            up.setId(slot.getId());
            up.setTotalSource(item.getTotalSource());
            up.setAvailableSource(item.getTotalSource() - nz(slot.getUsedSource()));
            up.setAppointmentSource(item.getAppointmentSource() == null ? 0 : item.getAppointmentSource());
            up.setStatus(item.getStatus());
            slotMapper.updateById(up);
        }
        syncSumToSchedule(dto.getScheduleId());

        // 留痕与加号同口径：往排班备注追加摘要。只记有变化的段，摘要截断防备注列撑爆
        List<String> changes = new ArrayList<>();
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizScheduleSlot slot = byId.get(item.getId());
            if (!Objects.equals(nz(slot.getTotalSource()), item.getTotalSource())) {
                changes.add(slot.getStartTime() + " 号源" + slot.getTotalSource() + "→" + item.getTotalSource());
            }
            int oldAppt = nz(slot.getAppointmentSource());
            int newAppt = item.getAppointmentSource() == null ? 0 : item.getAppointmentSource();
            if (oldAppt != newAppt) {
                changes.add(slot.getStartTime() + " 预约池" + oldAppt + "→" + newAppt);
            }
            if (!Objects.equals(slot.getStatus(), item.getStatus())) {
                changes.add(slot.getStartTime() + (item.getStatus() == 0 ? " 停用" : " 恢复"));
            }
        }
        if (!changes.isEmpty()) {
            String summary = String.join("、", changes);
            if (summary.length() > 120) {
                summary = summary.substring(0, 120) + "…";
            }
            String stamp = LocalDate.now() + " 段级号源调整（" + summary + "）";
            String newRemark = StringUtils.hasText(schedule.getRemark())
                    ? schedule.getRemark() + "；" + stamp : stamp;
            BizSchedule up = new BizSchedule();
            up.setId(schedule.getId());
            up.setRemark(newRemark);
            scheduleMapper.updateById(up);
        }
    }

    @Override
    public void syncSumToSchedule(Long scheduleId) {
        List<BizScheduleSlot> slots = listByScheduleId(scheduleId);
        int total = 0;
        int used = 0;
        int added = 0;
        int appt = 0;
        int usedAppt = 0;
        for (BizScheduleSlot s : slots) {
            total += nz(s.getTotalSource());
            used += nz(s.getUsedSource());
            added += nz(s.getAddedSource());
            appt += nz(s.getAppointmentSource());
            usedAppt += nz(s.getUsedAppointmentSource());
        }
        BizSchedule up = new BizSchedule();
        up.setId(scheduleId);
        up.setTotalSource(total);
        up.setUsedSource(used);
        up.setAvailableSource(total - used);
        up.setAddedSource(added);
        up.setAppointmentSource(appt);
        up.setUsedAppointmentSource(usedAppt);
        scheduleMapper.updateById(up);
    }

    @Override
    public void syncStatusToSlots(Long scheduleId, Integer status) {
        if (status == null) {
            return;
        }
        this.update(new LambdaUpdateWrapper<BizScheduleSlot>()
                .eq(BizScheduleSlot::getScheduleId, scheduleId)
                .set(BizScheduleSlot::getStatus, status));
    }

    @Override
    public void physicalDeleteByScheduleId(Long scheduleId) {
        slotMapper.physicalDeleteByScheduleId(scheduleId);
    }

    @Override
    public List<BizScheduleSlot> listByScheduleId(Long scheduleId) {
        return slotMapper.selectList(new LambdaQueryWrapper<BizScheduleSlot>()
                .eq(BizScheduleSlot::getScheduleId, scheduleId)
                .orderByAsc(BizScheduleSlot::getSeq)
                .orderByAsc(BizScheduleSlot::getId));
    }

    @Override
    public List<BizScheduleSlot> listByScheduleIds(Collection<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 去重 + 去空：调用方传的是页面当前排班ID，重复不会报错但会白查一遍
        List<Long> ids = scheduleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return slotMapper.selectList(new LambdaQueryWrapper<BizScheduleSlot>()
                .in(BizScheduleSlot::getScheduleId, ids)
                .orderByAsc(BizScheduleSlot::getScheduleId)
                .orderByAsc(BizScheduleSlot::getSeq)
                .orderByAsc(BizScheduleSlot::getId));
    }

    @Override
    public List<ScheduleSlotVO> listVOByScheduleId(Long scheduleId) {
        return listByScheduleId(scheduleId).stream().map(this::convertToSlotVO).toList();
    }

    @Override
    public List<ScheduleSlotVO> listVOByScheduleIds(Collection<Long> scheduleIds) {
        return listByScheduleIds(scheduleIds).stream().map(this::convertToSlotVO).toList();
    }

    private ScheduleSlotVO convertToSlotVO(BizScheduleSlot slot) {
        if (slot == null) {
            return null;
        }
        ScheduleSlotVO vo = new ScheduleSlotVO();
        vo.setId(slot.getId());
        vo.setScheduleId(slot.getScheduleId());
        vo.setSeq(slot.getSeq());
        vo.setStartTime(slot.getStartTime());
        vo.setEndTime(slot.getEndTime());
        vo.setTotalSource(slot.getTotalSource());
        vo.setUsedSource(slot.getUsedSource());
        vo.setAvailableSource(slot.getAvailableSource());
        vo.setAddedSource(slot.getAddedSource());
        vo.setAppointmentSource(slot.getAppointmentSource());
        vo.setUsedAppointmentSource(slot.getUsedAppointmentSource());
        vo.setStatus(slot.getStatus());
        return vo;
    }

    /**
     * [startTime, endTime) 按 30 分钟切分，尾段不足半小时取班次结束时刻。
     * "HH:mm" 定宽字符串可直接按字典序比较，这里转分钟算术避免跨小时进位错误。
     */
    private List<String[]> splitHalfHour(String startTime, String endTime) {
        // C类（非 web 入口入参）：起止时间由调用方从排班实体带进来，不过 Bean Validation 这一层
        if (startTime == null || endTime == null || startTime.length() < 4 || endTime.length() < 4) {
            throw new BusinessException("排班开始/结束时间不能为空");
        }
        int start = toMinute(startTime);
        int end = toMinute(endTime);
        if (end <= start) {
            throw new BusinessException("排班结束时间必须晚于开始时间");
        }
        List<String[]> segs = new ArrayList<>();
        int cursor = start;
        while (cursor < end) {
            int next = Math.min(cursor + 30, end);
            segs.add(new String[]{toHHmm(cursor), toHHmm(next)});
            cursor = next;
        }
        return segs;
    }

    /**
     * 均分：base 给所有段，余数按序补给前面的段
     */
    private int[] splitEven(int total, int n) {
        int[] arr = new int[n];
        int base = total / n;
        int rem = total % n;
        for (int i = 0; i < n; i++) {
            arr[i] = base + (i < rem ? 1 : 0);
        }
        return arr;
    }

    /**
     * 预约池 clamp：段内预约预留不得超过段号源，超出部分向后面有空余的段顺延。
     * 均分+余数给前面时逐段通常天然不超（前缀 ceil 差单调），此处兜底保证不变量成立。
     */
    private int[] clampSpread(int[] appts, int[] totals) {
        int n = appts.length;
        for (int i = 0; i < n; i++) {
            if (appts[i] > totals[i]) {
                int overflow = appts[i] - totals[i];
                appts[i] = totals[i];
                for (int j = n - 1; j >= 0 && overflow > 0; j--) {
                    int room = totals[j] - appts[j];
                    int move = Math.min(room, overflow);
                    appts[j] += move;
                    overflow -= move;
                }
                if (overflow > 0) {
                    throw new BusinessException("预约号源无法在时间段内分配，请调整预约号源数");
                }
            }
        }
        return appts;
    }

    private int toMinute(String hhmm) {
        return Integer.parseInt(hhmm.substring(0, 2)) * 60 + Integer.parseInt(hhmm.substring(3, 5));
    }

    private String toHHmm(int minute) {
        return String.format("%02d:%02d", minute / 60, minute % 60);
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
