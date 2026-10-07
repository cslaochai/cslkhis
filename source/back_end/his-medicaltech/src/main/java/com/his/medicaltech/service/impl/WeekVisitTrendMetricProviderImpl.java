package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.WorkbenchMetricMapper;
import com.his.medicaltech.service.WeekVisitTrendMetricProvider;
import com.his.medicaltech.vo.WorkbenchWeekTrendRowVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * 与旧首页一致：含今天在内 7 天
     */
    private static final int DAYS = 7;

    private final WorkbenchMetricMapper workbenchMetricMapper;

    @Override
    public String widgetCode() {
        return "weekVisitTrend";
    }

    /**
     * 外层这层 {@code items} 包装是 SPI 边界上的一次性适配（父接口签名固定为
     * {@code Map<String, Object>}）；点本身是有类型的 {@link WorkbenchWeekTrendRowVO}，
     * 字段名与前端 {@code WeekTrendWidget.vue} 取的 {@code item.date} / {@code item.cnt} 一致。
     */
    @Override
    public Map<String, Object> summary(CurrentUser user) {
        // 按日期分组的中间容器：真字典（补零要用），不是数据契约
        Map<String, Long> cntByDate = new HashMap<>();
        for (WorkbenchWeekTrendRowVO row : workbenchMetricMapper.weekRegistTrend()) {
            if (row.getDate() != null) {
                cntByDate.put(row.getDate(), row.getCnt());
            }
        }
        LocalDate today = LocalDate.now();
        List<WorkbenchWeekTrendRowVO> items = new ArrayList<>(DAYS);
        for (int i = DAYS - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            WorkbenchWeekTrendRowVO point = new WorkbenchWeekTrendRowVO();
            point.setDate(date);
            point.setCnt(cntByDate.getOrDefault(date, 0L));
            items.add(point);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        return data;
    }
}