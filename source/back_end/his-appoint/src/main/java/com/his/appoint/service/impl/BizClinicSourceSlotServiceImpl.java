package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.ScheduleSlotItemUpsertDTO;
import com.his.appoint.dto.ScheduleSlotUpsertDTO;
import com.his.appoint.entity.BizClinicSource;
import com.his.appoint.entity.BizClinicSourceSlot;
import com.his.appoint.entity.BizClinicSourceSlotTemplate;
import com.his.appoint.mapper.BizClinicSourceMapper;
import com.his.appoint.mapper.BizClinicSourceSlotMapper;
import com.his.appoint.service.BizClinicSourceSlotService;
import com.his.appoint.vo.ScheduleSlotVO;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.ScheduleStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.provider.DeptScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 排班时间片段服务实现。
 */
@Service
@RequiredArgsConstructor
public class BizClinicSourceSlotServiceImpl extends ServiceImpl<BizClinicSourceSlotMapper, BizClinicSourceSlot> implements BizClinicSourceSlotService {

    private final BizClinicSourceSlotMapper bizScheduleSlotMapper;

    private final BizClinicSourceMapper bizScheduleMapper;

    private final DeptScopeService deptScopeService;

    @Override
    public List<BizClinicSourceSlot> generateSlots(Long scheduleId, String startTime, String endTime,
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

        List<BizClinicSourceSlot> slots = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            BizClinicSourceSlot slot = new BizClinicSourceSlot();
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
    public List<BizClinicSourceSlot> generateFromTemplate(Long scheduleId, List<BizClinicSourceSlotTemplate> tplSlots) {
        List<BizClinicSourceSlot> slots = new ArrayList<>(tplSlots.size());
        int seq = 1;
        for (BizClinicSourceSlotTemplate tpl : tplSlots) {
            BizClinicSourceSlot slot = new BizClinicSourceSlot();
            slot.setScheduleId(scheduleId);
            slot.setSeq(seq++);
            slot.setStartTime(tpl.getStartTime());
            slot.setEndTime(tpl.getEndTime());
            slot.setTotalSource(NumUtil.orZero(tpl.getTotalSource()));
            slot.setUsedSource(0);
            slot.setAvailableSource(NumUtil.orZero(tpl.getTotalSource()));
            slot.setAddedSource(0);
            slot.setAppointmentSource(NumUtil.orZero(tpl.getAppointmentSource()));
            slot.setUsedAppointmentSource(0);
            slot.setStatus(1);
            this.save(slot);
            slots.add(slot);
        }
        syncSumToSchedule(scheduleId);
        return slots;
    }

    @Override
    public void regenerateForSchedule(BizClinicSource schedule, Integer newTotal, Integer newAppointment) {
        Long scheduleId = schedule.getId();
        List<BizClinicSourceSlot> old = listByScheduleId(scheduleId);
        int usedSum = old.stream().mapToInt(s -> NumUtil.orZero(s.getUsedSource())).sum();

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
            if (usedSum > 0) {
                throw new BusinessException("该班次已有挂号记录，不能修改就诊时段；请先退号或另建排班");
            }
            physicalDeleteByScheduleId(scheduleId);
            generateSlots(scheduleId, schedule.getStartTime(), schedule.getEndTime(), newTotal, newAppointment);
            return;
        }

        int n = old.size();
        int total = newTotal == null ? 0 : newTotal;
        int appt = newAppointment == null ? 0 : newAppointment;
        if (appt > total) {
            throw new BusinessException("预约号源数不能大于号源总数");
        }
        int[] totals = splitEven(total, n);
        int[] usedArr = new int[n];
        for (int i = 0; i < n; i++) {
            usedArr[i] = NumUtil.orZero(old.get(i).getUsedSource());
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
            BizClinicSourceSlot up = new BizClinicSourceSlot();
            up.setId(old.get(i).getId());
            up.setTotalSource(totals[i]);
            up.setAvailableSource(totals[i] - usedArr[i]);
            up.setAppointmentSource(appts[i]);
            bizScheduleSlotMapper.updateById(up);
        }
        syncSumToSchedule(scheduleId);
    }

    @Override
    public void spreadAddSource(Long scheduleId, int addNum) {
        if (addNum <= 0) {
            return;
        }
        List<BizClinicSourceSlot> slots = listByScheduleId(scheduleId);
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
            BizClinicSourceSlot s = slots.get(i);
            BizClinicSourceSlot up = new BizClinicSourceSlot();
            up.setId(s.getId());
            up.setTotalSource(NumUtil.orZero(s.getTotalSource()) + add[i]);
            up.setAvailableSource(NumUtil.orZero(s.getAvailableSource()) + add[i]);
            up.setAddedSource(NumUtil.orZero(s.getAddedSource()) + add[i]);
            bizScheduleSlotMapper.updateById(up);
        }
        syncSumToSchedule(scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlotSources(ScheduleSlotUpsertDTO dto) {
        BizClinicSource schedule = bizScheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        deptScopeService.assertDeptAccessible(schedule.getDeptId());
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过期的班次不允许调整号源");
        }
        if (ScheduleStatusEnum.stopped(schedule.getStatus())) {
            throw new BusinessException("停诊中的班次不允许调整号源，请先启用");
        }
        // 段号源只对医生出诊班有意义：出勤岗没有号源池，与加号同一道闸
        if (!StaffTypeEnum.hasSource(schedule.getStaffType())) {
            throw new BusinessException("只有医生出诊排班能调整号源："
                    + StaffTypeEnum.getText(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
        }

        Map<Long, BizClinicSourceSlot> byId = new HashMap<>();
        for (BizClinicSourceSlot s : listByScheduleId(dto.getScheduleId())) {
            byId.put(s.getId(), s);
        }
        // 整批校验整批生效：部分成功会打破「Σ段=主表」的总量约束，中途状态比全拒绝更糟
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizClinicSourceSlot slot = byId.get(item.getId());
            if (slot == null) {
                throw new BusinessException("时间段不存在或不属于该排班");
            }
            if (EnableStatusEnum.fromCode(item.getStatus()) == null) {
                throw new BusinessException("段状态只允许 0-停用 / 1-正常");
            }
            int used = NumUtil.orZero(slot.getUsedSource());
            if (item.getTotalSource() < used) {
                throw new BusinessException(slot.getStartTime() + " 段号源不能小于已挂号数（" + used + "）");
            }
            int appt = item.getAppointmentSource() == null ? 0 : item.getAppointmentSource();
            if (appt > item.getTotalSource()) {
                throw new BusinessException(slot.getStartTime() + " 段预约预留不能大于段号源");
            }
            int usedAppt = NumUtil.orZero(slot.getUsedAppointmentSource());
            if (appt < usedAppt) {
                throw new BusinessException(slot.getStartTime() + " 段预约预留不能小于预约已用（" + usedAppt + "）");
            }
        }
        // 逐段落库：available 同步重算（used 是已发生的事实，不动）
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizClinicSourceSlot slot = byId.get(item.getId());
            BizClinicSourceSlot up = new BizClinicSourceSlot();
            up.setId(slot.getId());
            up.setTotalSource(item.getTotalSource());
            up.setAvailableSource(item.getTotalSource() - NumUtil.orZero(slot.getUsedSource()));
            up.setAppointmentSource(item.getAppointmentSource() == null ? 0 : item.getAppointmentSource());
            up.setStatus(item.getStatus());
            bizScheduleSlotMapper.updateById(up);
        }
        syncSumToSchedule(dto.getScheduleId());

        // 留痕与加号同口径：往排班备注追加摘要。只记有变化的段，摘要截断防备注列撑爆
        List<String> changes = new ArrayList<>();
        for (ScheduleSlotItemUpsertDTO item : dto.getSlots()) {
            BizClinicSourceSlot slot = byId.get(item.getId());
            if (!Objects.equals(NumUtil.orZero(slot.getTotalSource()), item.getTotalSource())) {
                changes.add(slot.getStartTime() + " 号源" + slot.getTotalSource() + "→" + item.getTotalSource());
            }
            int oldAppt = NumUtil.orZero(slot.getAppointmentSource());
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
            String newRemark = TextUtil.hasText(schedule.getRemark())
                    ? schedule.getRemark() + "；" + stamp : stamp;
            BizClinicSource up = new BizClinicSource();
            up.setId(schedule.getId());
            up.setRemark(newRemark);
            bizScheduleMapper.updateById(up);
        }
    }

    @Override
    public void syncSumToSchedule(Long scheduleId) {
        List<BizClinicSourceSlot> slots = listByScheduleId(scheduleId);
        int total = 0;
        int used = 0;
        int added = 0;
        int appt = 0;
        int usedAppt = 0;
        for (BizClinicSourceSlot s : slots) {
            total += NumUtil.orZero(s.getTotalSource());
            used += NumUtil.orZero(s.getUsedSource());
            added += NumUtil.orZero(s.getAddedSource());
            appt += NumUtil.orZero(s.getAppointmentSource());
            usedAppt += NumUtil.orZero(s.getUsedAppointmentSource());
        }
        BizClinicSource up = new BizClinicSource();
        up.setId(scheduleId);
        up.setTotalSource(total);
        up.setUsedSource(used);
        up.setAvailableSource(total - used);
        up.setAddedSource(added);
        up.setAppointmentSource(appt);
        up.setUsedAppointmentSource(usedAppt);
        bizScheduleMapper.updateById(up);
    }

    @Override
    public void syncStatusToSlots(Long scheduleId, Integer status) {
        if (status == null) {
            return;
        }
        this.update(new LambdaUpdateWrapper<BizClinicSourceSlot>()
                .eq(BizClinicSourceSlot::getScheduleId, scheduleId)
                .set(BizClinicSourceSlot::getStatus, status));
    }

    @Override
    public void physicalDeleteByScheduleId(Long scheduleId) {
        bizScheduleSlotMapper.physicalDeleteByScheduleId(scheduleId);
    }

    @Override
    public List<BizClinicSourceSlot> listByScheduleId(Long scheduleId) {
        return bizScheduleSlotMapper.selectList(new LambdaQueryWrapper<BizClinicSourceSlot>()
                .eq(BizClinicSourceSlot::getScheduleId, scheduleId)
                .orderByAsc(BizClinicSourceSlot::getSeq)
                .orderByAsc(BizClinicSourceSlot::getId));
    }

    @Override
    public List<BizClinicSourceSlot> listByScheduleIds(Collection<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 去重 + 去空：调用方传的是页面当前排班ID，重复不会报错但会白查一遍
        List<Long> ids = scheduleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return bizScheduleSlotMapper.selectList(new LambdaQueryWrapper<BizClinicSourceSlot>()
                .in(BizClinicSourceSlot::getScheduleId, ids)
                .orderByAsc(BizClinicSourceSlot::getScheduleId)
                .orderByAsc(BizClinicSourceSlot::getSeq)
                .orderByAsc(BizClinicSourceSlot::getId));
    }

    @Override
    public List<ScheduleSlotVO> listVOByScheduleId(Long scheduleId) {
        return listByScheduleId(scheduleId).stream().map(this::convertToSlotVO).toList();
    }

    @Override
    public List<ScheduleSlotVO> listVOByScheduleIds(Collection<Long> scheduleIds) {
        return listByScheduleIds(scheduleIds).stream().map(this::convertToSlotVO).toList();
    }

    private ScheduleSlotVO convertToSlotVO(BizClinicSourceSlot slot) {
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
        // C-非 web 入参：起止时间由调用方从排班实体带进来，不过 Bean Validation 这一层，保留
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

}
