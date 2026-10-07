package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.ScheduleChangeTypeEnum;
import com.his.system.entity.BizScheduleChangeLog;
import com.his.system.entity.BizShift;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.BizScheduleChangeLogMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.ScheduleChangeLogService;
import com.his.system.service.ShiftService;
import com.his.system.vo.ScheduleChangeLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班变更留痕服务实现。
 */
@Service
@RequiredArgsConstructor
public class ScheduleChangeLogServiceImpl extends ServiceImpl<BizScheduleChangeLogMapper, BizScheduleChangeLog>
        implements ScheduleChangeLogService {

    /**
     * 留痕原因列宽：超长直接截断，避免「原始报错塞进原因列」把整笔业务顶成 500
     */
    private static final int W_REASON = 200;

    private final ShiftService shiftService;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public Long record(Long staffScheduleId, ScheduleChangeTypeEnum actionType,
                       Long fromEmployeeId, Long toEmployeeId,
                       Long fromShiftId, Long toShiftId, Integer amount, String reason) {
        BizScheduleChangeLog log = new BizScheduleChangeLog();
        log.setStaffScheduleId(staffScheduleId);
        log.setActionType(actionType.getCode());
        log.setFromEmployeeId(fromEmployeeId);
        log.setToEmployeeId(toEmployeeId);
        log.setFromShiftId(fromShiftId);
        log.setToShiftId(toShiftId);
        log.setAmount(amount);
        log.setOccurTime(LocalDateTime.now());
        if (reason != null) {
            log.setReason(reason.length() > W_REASON ? reason.substring(0, W_REASON) : reason);
        }
        save(log);
        return log.getId();
    }

    @Override
    public List<ScheduleChangeLogVO> listBySchedule(Long staffScheduleId) {
        List<BizScheduleChangeLog> rows = list(new LambdaQueryWrapper<BizScheduleChangeLog>()
                .eq(BizScheduleChangeLog::getStaffScheduleId, staffScheduleId)
                // 二级键 id：同一秒内连续两条变更的排序不稳定（翻页/追溯都会乱行）
                .orderByDesc(BizScheduleChangeLog::getOccurTime).orderByDesc(BizScheduleChangeLog::getId));
        if (rows.isEmpty()) {
            return List.of();
        }
        Set<Long> empIds = new HashSet<>();
        Set<Long> shiftIds = new HashSet<>();
        for (BizScheduleChangeLog row : rows) {
            collect(empIds, row.getFromEmployeeId(), row.getToEmployeeId());
            collect(shiftIds, row.getFromShiftId(), row.getToShiftId());
        }
        Map<Long, SysEmployee> employees = employeesOf(empIds);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIds);
        List<ScheduleChangeLogVO> vos = new ArrayList<>(rows.size());
        for (BizScheduleChangeLog row : rows) {
            ScheduleChangeLogVO vo = new ScheduleChangeLogVO();
            vo.setId(row.getId());
            vo.setStaffScheduleId(row.getStaffScheduleId());
            vo.setActionType(row.getActionType());
            vo.setActionTypeText(ScheduleChangeTypeEnum.getText(row.getActionType()));
            vo.setFromEmployeeId(row.getFromEmployeeId());
            vo.setToEmployeeId(row.getToEmployeeId());
            SysEmployee from = employees.get(row.getFromEmployeeId());
            SysEmployee to = employees.get(row.getToEmployeeId());
            vo.setFromEmployeeName(from == null ? null : from.getEmpName());
            vo.setToEmployeeName(to == null ? null : to.getEmpName());
            vo.setFromShiftId(row.getFromShiftId());
            vo.setToShiftId(row.getToShiftId());
            BizShift fromShift = shifts.get(row.getFromShiftId());
            BizShift toShift = shifts.get(row.getToShiftId());
            vo.setFromShiftName(fromShift == null ? null : fromShift.getShiftName());
            vo.setToShiftName(toShift == null ? null : toShift.getShiftName());
            vo.setAmount(row.getAmount());
            vo.setReason(row.getReason());
            vo.setOccurTime(row.getOccurTime());
            vo.setCreateBy(row.getCreateBy());
            vos.add(vo);
        }
        return vos;
    }

    private Map<Long, SysEmployee> employeesOf(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysEmployeeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysEmployee::getId, e -> e));
    }

    private void collect(Set<Long> target, Long... ids) {
        for (Long id : ids) {
            if (id != null) {
                target.add(id);
            }
        }
    }
}
