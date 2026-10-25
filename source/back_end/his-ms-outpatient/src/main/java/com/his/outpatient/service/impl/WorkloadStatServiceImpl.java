package com.his.outpatient.service.impl;

import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.entity.BizShift;
import com.his.outpatient.mapper.BizScheduleMapper;
import com.his.outpatient.mapper.BizShiftMapper;
import com.his.outpatient.service.WorkloadStatService;
import com.his.outpatient.vo.WorkloadStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全岗位工时统计实现：从 biz_schedule 已发布行按人员聚合。
 */
@Service
@RequiredArgsConstructor
public class WorkloadStatServiceImpl implements WorkloadStatService {

    private final BizScheduleMapper bizScheduleMapper;
    private final BizShiftMapper bizShiftMapper;

    @Override
    public List<WorkloadStatVO> monthlyStats(LocalDate monthStart) {
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        // 查当月所有已发布的排班行
        List<BizSchedule> rows = bizScheduleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizSchedule>()
                        .eq(BizSchedule::getDelFlag, 0)
                        .between(BizSchedule::getScheduleDate, monthStart, monthEnd)
                        .in(BizSchedule::getPublishStatus, 2, 4)  // 已发布/已变更
        );

        // 批量查班次：收集所有用到的shiftId，一次性查出来判断是否夜班
        Set<Long> shiftIds = rows.stream()
                .map(BizSchedule::getShiftId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, BizShift> shiftMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            List<BizShift> shifts = bizShiftMapper.selectBatchIds(shiftIds);
            shiftMap = shifts.stream().collect(Collectors.toMap(BizShift::getId, s -> s));
        }

        // 按人员聚合
        Map<Long, WorkloadStatVO> map = new HashMap<>();
        for (BizSchedule row : rows) {
            Long empId = row.getEmployeeId();
            WorkloadStatVO vo = map.computeIfAbsent(empId, k -> {
                WorkloadStatVO v = new WorkloadStatVO();
                v.setEmployeeId(empId);
                v.setEmpCode(row.getEmpCode());
                v.setEmployeeName(row.getEmployeeName());
                v.setStaffType(row.getStaffType());
                v.setStaffTypeName(StaffTypeEnum.getLabel(row.getStaffType()));
                v.setDeptId(row.getDeptId());
                v.setDeptName(row.getDeptName());
                v.setWorkDays(0);
                v.setNightDays(0);
                v.setRestDays(0);
                v.setTotalHours(java.math.BigDecimal.ZERO);
                return v;
            });

            // 统计出勤/休息
            if (StaffDutyStatusEnum.WORK.getCode() == row.getDutyStatus()) {
                vo.setWorkDays(vo.getWorkDays() + 1);
                if (row.getWorkMinutes() != null) {
                    vo.setTotalHours(vo.getTotalHours().add(
                            java.math.BigDecimal.valueOf(row.getWorkMinutes())
                                    .divide(java.math.BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP)
                    ));
                }
                // 夜班统计：班次is_night=1
                BizShift shift = shiftMap.get(row.getShiftId());
                if (shift != null && shift.getIsNight() != null && shift.getIsNight() == 1) {
                    vo.setNightDays(vo.getNightDays() + 1);
                }
            } else if (StaffDutyStatusEnum.REST.getCode() == row.getDutyStatus()) {
                vo.setRestDays(vo.getRestDays() + 1);
            }
        }

        return new ArrayList<>(map.values());
    }
}
