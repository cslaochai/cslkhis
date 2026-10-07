package com.his.medicaltech.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.HospitalTodayMetricProvider;
import com.his.medicaltech.vo.WorkbenchHospitalAlertRowVO;
import com.his.medicaltech.vo.WorkbenchHospitalCoreRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /**
     * 概况与告警<b>拍平到同一层</b>（而不是嵌套两个段）：前端
     * {@code METRIC_SPECS.hospitalToday} 把两组指标平铺在同一个 items 数组里，
     * 嵌套会多一层取值路径。
     *
     * <p>两段合到一张 Map 是 SPI 边界上的一次性适配（父接口签名固定为
     * {@code Map<String, Object>}）；VO 转 Map 用字段名做键，与前端 key 逐项对齐。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        WorkbenchHospitalCoreRowVO core = workbenchMetricMapper.hospitalCoreStats();
        WorkbenchHospitalAlertRowVO alert = workbenchMetricMapper.hospitalAlertStats();
        Map<String, Object> data = BeanUtil.beanToMap(core);
        data.putAll(BeanUtil.beanToMap(alert));
        return data;
    }
}