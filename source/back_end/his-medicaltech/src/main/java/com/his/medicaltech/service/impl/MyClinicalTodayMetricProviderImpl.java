package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.MyClinicalTodayMetricProvider;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 卡片 {@code myClinicalToday}：我（医生）的今日诊疗（迁移自旧首页的 doctor 分支）。
 *
 * <p>收口在 SQL 里按 {@code employeeId} 过滤，不接受前端传参 —— 否则"工作台看全院、
 * 点进去只有本科室"。{@code todoConsultationCount} 里的 {@code to_dept_id} 是唯一
 * 按科室收敛的一项（会诊单可以发给科室而非个人），所以 deptId 也必须带上。
 */
@Service
@RequiredArgsConstructor
public class MyClinicalTodayMetricProviderImpl implements WorkbenchMetricProvider, MyClinicalTodayMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "myClinicalToday";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Long doctorId = user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        return workbenchMetricMapper.doctorStats(doctorId, user.getDeptId());
    }
}
