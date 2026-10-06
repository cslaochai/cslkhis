package com.his.report.service.impl;

import com.his.report.service.DeptVisitRankMetricProvider;
import com.his.report.mapper.WorkbenchMetricMapper;
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

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        List<Map<String, Object>> rows = workbenchMetricMapper.deptVisitRank();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", rows == null ? Collections.emptyList() : rows);
        return data;
    }
}
