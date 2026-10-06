package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.HospitalTodayMetricProvider;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 卡片 {@code hospitalToday}：全院今日概况 + 异常告警（迁移自旧首页的 admin 分支）。
 *
 * <p>不做角色判断 —— 谁能看这张卡由工作台卡片注册表的权限码
 * （{@code report:stats:list}）在工作台侧决定，本类只负责"全院数字"。
 */
@Service
@RequiredArgsConstructor
public class HospitalTodayMetricProviderImpl implements WorkbenchMetricProvider, HospitalTodayMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "hospitalToday";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Map<String, Object> data = new LinkedHashMap<>(workbenchMetricMapper.hospitalCoreStats());
        data.putAll(workbenchMetricMapper.hospitalAlertStats());
        return data;
    }
}
