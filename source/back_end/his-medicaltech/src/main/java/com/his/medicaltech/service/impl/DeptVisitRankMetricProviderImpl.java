package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.DeptVisitRankMetricProvider;
import com.his.medicaltech.vo.WorkbenchDeptVisitRankRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡片 {@code deptVisitRank}：今日科室就诊排行（top 6，全院口径）。
 *
 * <p>它挂在 {@code report:stats:list} 下，是给统计岗/院领导看的全院数字，
 * 不是"我本科室"—— 需要按科室收敛的卡请另开一张，别往这里加参数。
 */
@Service
@RequiredArgsConstructor
public class DeptVisitRankMetricProviderImpl implements WorkbenchMetricProvider, DeptVisitRankMetricProvider {

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "deptVisitRank";
    }

    /**
     * 行本身是有类型的 {@link WorkbenchDeptVisitRankRowVO}（前端 {@code DeptRankWidget.vue}
     * 按 {@code item.deptName} / {@code item.cnt} 取值，与 VO 字段名一致，故直接交给
     * Jackson 序列化即可）；外层这层 {@code items} 包装是 SPI 边界上的一次性适配
     * （父接口签名固定为 {@code Map<String, Object>}）。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        List<WorkbenchDeptVisitRankRowVO> rows = workbenchMetricMapper.deptVisitRank();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", rows == null ? Collections.emptyList() : rows);
        return data;
    }
}