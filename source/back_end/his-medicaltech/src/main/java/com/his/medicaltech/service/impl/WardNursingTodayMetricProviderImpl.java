package com.his.medicaltech.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.WardNursingTodayMetricProvider;
import com.his.medicaltech.vo.WorkbenchNurseStatsRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 卡片 wardNursingToday：病区今日概况（迁移自旧首页的 nurse 分支）。
 */
@Service
@RequiredArgsConstructor
public class WardNursingTodayMetricProviderImpl implements WorkbenchMetricProvider, WardNursingTodayMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "wardNursingToday";
    }

    /**
     * VO 转 Map 是 SPI 边界上的一次性适配（父接口签名固定为 {@code Map<String, Object>}），
     * 键名与 VO 字段名一致，与前端 {@code METRIC_SPECS.wardNursingToday} 逐项对齐。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        WorkbenchNurseStatsRowVO row = workbenchMetricMapper.nurseStats(user.getDeptId());
        return BeanUtil.beanToMap(row);
    }
}