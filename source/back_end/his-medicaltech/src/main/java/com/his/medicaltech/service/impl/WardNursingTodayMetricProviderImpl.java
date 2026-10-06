package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.WardNursingTodayMetricProvider;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 卡片 {@code wardNursingToday}：病区今日概况（迁移自旧首页的 nurse 分支）。
 *
 * <p>护士的"病区"由当前科室推导（病区的科室ID = 护士主科室）；护士未切科室时
 * deptId 为空，SQL 的 {@code IN (SELECT ... WHERE dept_id = NULL)} 匹配 0 行 → 全为 0，
 * 而不是抛异常。
 */
@Service
@RequiredArgsConstructor
public class WardNursingTodayMetricProviderImpl implements WorkbenchMetricProvider, WardNursingTodayMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "wardNursingToday";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        return workbenchMetricMapper.nurseStats(user.getDeptId());
    }
}
