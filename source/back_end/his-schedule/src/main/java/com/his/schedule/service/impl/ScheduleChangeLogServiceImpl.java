package com.his.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.ScheduleChangeTypeEnum;
import com.his.common.util.TextUtil;
import com.his.schedule.dto.StaffScheduleChangePageQueryDTO;
import com.his.schedule.entity.BizClinicSourceChangeLog;
import com.his.schedule.entity.BizSchedule;
import com.his.schedule.entity.BizShift;
import com.his.system.entity.SysEmployee;
import com.his.schedule.mapper.BizClinicSourceChangeLogMapper;
import com.his.schedule.mapper.BizScheduleMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.schedule.service.ScheduleChangeLogService;
import com.his.schedule.service.ShiftService;
import com.his.schedule.vo.ScheduleChangeLogVO;
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
public class ScheduleChangeLogServiceImpl extends ServiceImpl<BizClinicSourceChangeLogMapper, BizClinicSourceChangeLog>
        implements ScheduleChangeLogService {

    /**
     * 留痕原因列宽：超长直接截断，避免「原始报错塞进原因列」把整笔业务顶成 500
     */
    private static final int W_REASON = 200;

    private final ShiftService shiftService;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final BizScheduleMapper bizScheduleMapper;

    @Override
    public Long record(Long staffScheduleId, ScheduleChangeTypeEnum actionType,
                       Long fromEmployeeId, Long toEmployeeId,
                       Long fromShiftId, Long toShiftId, Integer amount, String reason) {
        BizClinicSourceChangeLog log = new BizClinicSourceChangeLog();
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
        List<BizClinicSourceChangeLog> rows = list(new LambdaQueryWrapper<BizClinicSourceChangeLog>()
                .eq(BizClinicSourceChangeLog::getStaffScheduleId, staffScheduleId)
                // 二级键 id：同一秒内连续两条变更的排序不稳定（翻页/追溯都会乱行）
                .orderByDesc(BizClinicSourceChangeLog::getOccurTime).orderByDesc(BizClinicSourceChangeLog::getId));
        if (rows.isEmpty()) {
            return List.of();
        }
        return toVOs(rows);
    }

    @Override
    public PageResult<ScheduleChangeLogVO> pageQuery(StaffScheduleChangePageQueryDTO dto) {
        LambdaQueryWrapper<BizClinicSourceChangeLog> wrapper = new LambdaQueryWrapper<BizClinicSourceChangeLog>()
                .ge(dto.getOccurStart() != null, BizClinicSourceChangeLog::getOccurTime, dto.getOccurStart())
                .le(dto.getOccurEnd() != null, BizClinicSourceChangeLog::getOccurTime, dto.getOccurEnd())
                .eq(dto.getActionType() != null, BizClinicSourceChangeLog::getActionType, dto.getActionType())
                .orderByDesc(BizClinicSourceChangeLog::getOccurTime).orderByDesc(BizClinicSourceChangeLog::getId);
        if (TextUtil.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            Set<Long> empIds = employeeIdsOf(kw);
            if (empIds.isEmpty()) {
                return PageResult.of(0, dto.getPageNum(), dto.getPageSize(), 0, List.of());
            }
            wrapper.and(w -> w.in(BizClinicSourceChangeLog::getFromEmployeeId, empIds)
                    .or().in(BizClinicSourceChangeLog::getToEmployeeId, empIds));
        }
        Page<BizClinicSourceChangeLog> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<ScheduleChangeLogVO> vos = page.getRecords().isEmpty() ? List.of() : toVOs(page.getRecords());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    /**
     * 关键词 → 员工ID集合（姓名或工号模糊匹配，两侧都查）
     */
    private Set<Long> employeeIdsOf(String keyword) {
        List<SysEmployee> employees = sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>()
                        .and(w -> w.like(SysEmployee::getEmpName, keyword)
                                .or().like(SysEmployee::getEmpCode, keyword))
                        .select(SysEmployee::getId));
        Set<Long> ids = new HashSet<>();
        for (SysEmployee e : employees) {
            if (e.getId() != null) {
                ids.add(e.getId());
            }
        }
        return ids;
    }

    private List<ScheduleChangeLogVO> toVOs(List<BizClinicSourceChangeLog> rows) {
        Set<Long> empIds = new HashSet<>();
        Set<Long> shiftIds = new HashSet<>();
        Set<Long> scheduleIds = new HashSet<>();
        for (BizClinicSourceChangeLog row : rows) {
            collect(empIds, row.getFromEmployeeId(), row.getToEmployeeId());
            collect(shiftIds, row.getFromShiftId(), row.getToShiftId());
            if (row.getStaffScheduleId() != null) {
                scheduleIds.add(row.getStaffScheduleId());
            }
        }
        Map<Long, SysEmployee> employees = employeesOf(empIds);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIds);
        Map<Long, BizSchedule> schedules = scheduleIds.isEmpty()
                // 整周级留痕没有单行（staff_schedule_id 为空）→ 空表 + 空键，
                // Map.of().get(null) 会抛 NPE，必须用 Collections.emptyMap()
                ? Collections.emptyMap()
                : bizScheduleMapper.selectBatchIds(scheduleIds).stream()
                        .collect(Collectors.toMap(BizSchedule::getId, s -> s, (a, b) -> a));
        List<ScheduleChangeLogVO> vos = new ArrayList<>(rows.size());
        for (BizClinicSourceChangeLog row : rows) {
            ScheduleChangeLogVO vo = new ScheduleChangeLogVO();
            vo.setId(row.getId());
            vo.setStaffScheduleId(row.getStaffScheduleId());
            vo.setActionType(row.getActionType());
            vo.setActionTypeText(ScheduleChangeTypeEnum.getLabel(row.getActionType()));
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
            BizSchedule schedule = row.getStaffScheduleId() == null ? null : schedules.get(row.getStaffScheduleId());
            if (schedule != null) {
                vo.setScheduleDate(schedule.getScheduleDate());
                vo.setOrgType(schedule.getOrgType());
                vo.setOrgName(schedule.getOrgName());
            }
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
            // 必须用 Collections.emptyMap()：Map.of() 的 get(null) 直接抛 NPE，
            // 而调用方拿「行上的原/新值班人（审批类留痕为空）」来取，空表 + 空键就整页 500。
            // 审批留痕（提交审核/审核发布/驳回/整周动作）本来就没有原/新值班人，必然踩这条。
            return Collections.emptyMap();
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
