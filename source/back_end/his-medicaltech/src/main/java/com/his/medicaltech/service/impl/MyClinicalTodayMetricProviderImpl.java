package com.his.medicaltech.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.MyClinicalTodayMetricProvider;
import com.his.medicaltech.vo.WorkbenchDoctorStatsRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 卡片 myClinicalToday：我（医生）的今日诊疗（迁移自旧首页的 doctor 分支）。
 */
@Service
@RequiredArgsConstructor
public class MyClinicalTodayMetricProviderImpl implements WorkbenchMetricProvider, MyClinicalTodayMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "myClinicalToday";
    }

    /**
     * VO 转 Map 是 SPI 边界上的一次性适配（父接口签名固定为 {@code Map<String, Object>}），
     * 键名与 VO 字段名一致，与前端 {@code METRIC_SPECS.myClinicalToday} 逐项对齐。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Long doctorId = user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        WorkbenchDoctorStatsRowVO row = workbenchMetricMapper.doctorStats(doctorId, user.getDeptId());
        return BeanUtil.beanToMap(row);
    }
}