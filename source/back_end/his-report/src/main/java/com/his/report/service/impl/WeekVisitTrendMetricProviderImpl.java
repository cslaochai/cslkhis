package com.his.report.service.impl;

import com.his.report.service.WeekVisitTrendMetricProvider;
import com.his.report.mapper.WorkbenchMetricMapper;
import com.his.security.entity.CurrentUser;
import com.his.security.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡片 {@code weekVisitTrend}：近 7 天挂号趋势（全院口径）。
 *
 * <p>必须补零：SQL 只对"有数据的日期"分组，周末没人挂号就少一行，
 * 折线图会把它画成"漏了一天"而不是"0 人"。
 */
@Service
@RequiredArgsConstructor
public class WeekVisitTrendMetricProviderImpl implements WorkbenchMetricProvider, WeekVisitTrendMetricProvider {

    /** 与旧首页一致：含今天在内 7 天 */
    private static final int DAYS = 7;

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "weekVisitTrend";
    }

    @Override
    public Map<String, Object> summary(CurrentUser user) {
        Map<String, Object> cntByDate = new LinkedHashMap<>();
        List<Map<String, Object>> rows = workbenchMetricMapper.weekRegistTrend();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object date = row.get("date");
                if (date != null) {
                    cntByDate.put(date.toString(), row.get("cnt"));
                }
            }
        }
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> items = new ArrayList<>(DAYS);
        for (int i = DAYS - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date);
            item.put("cnt", cntByDate.getOrDefault(date, 0L));
            items.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        return data;
    }
}
